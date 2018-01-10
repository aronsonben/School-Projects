package cmsc433.p3;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import javax.swing.JFrame;

/**
 * Main entry point for the program. Provides command-line support for
 * generating random mazes and passing maze files to solvers.
 */
public class Main
{
    private Maze maze;
    private boolean solvable;
    private int recWins = 0;
    private int dfsWins = 0;
    private int bfsWins = 0;
    private int benWins = 0;

    /**
     * Method that calls the solvers. To add your solver to the list of solvers
     * that is run, uncomment it in the "solvers" array defined at the top of this
     * method.
     */
    public void solve(int rdnum)
    {
    	// Some extra stuff added to see which solution is the fastest (for testing)
    	int currentClass = 0;
    	int lowestClass = 0; // 1 = Rec; 2 = DFS; 3 = BFS;    	
    	float lowestTime = -1;
    	
        // Add your solvers to this array to test them.
        MazeSolver[] solvers =
        {
        		//new STMazeSolverRec(maze),
                new STMazeSolverDFS(maze),
                new StudentMTMazeSolver(maze),
                new STMazeSolverBFS(maze), 
                //new StudentMTMazeSolver(maze),  //uncomment this line when you are ready to test yours
        };
        
        System.out.println("Round " + rdnum + " -- Maze Dimensions: " + maze.height + " by " + maze.width);
        for (MazeSolver solver : solvers)
        {
        	//Testing - keep track of lowest class
        	currentClass++;
        	
            long startTime, endTime;
            float sec;

            System.out.println();
            System.out.println(className(solver.getClass()) + ":");

            startTime = System.currentTimeMillis();
            List<Direction> soln = solver.solve();
            endTime = System.currentTimeMillis();
            sec = (endTime - startTime) / 1000F;

            if (soln == null)
            {
                if (!solvable) System.out.println("Correctly found no solution in " + sec + " seconds.");
                else System.out.println("Incorrectly returned no solution when there is one.");
            }
            else
            {
                if (maze.checkSolution(soln)) System.out.println("Correct solution found in " + sec + " seconds.");
                else System.out.println("Incorrect solution found.");
            }
            
            // TESTING: Recording lowest time stuff
            if(sec < lowestTime || lowestTime == -1) {
            	lowestClass = currentClass;
            	lowestTime = sec;
            }
        }
        //Reset currentClass and count wins
        if(lowestClass==1) { recWins++; }
        else if(lowestClass==2) { dfsWins++; }
        else if(lowestClass==3) { bfsWins++; }
        else if(lowestClass==4) { benWins++; }
        else { System.out.println("Error in counting wins"); }
        //currentClass = 0; lowestClass = 0; lowestTime = -1;
        System.out.println("-------------------------");
    }

    public static void main(String[] args)
    {
        Main m = new Main();

        //Uncomment these lines to run via command prompt with a certain file
        /*
        if (args.length != 1)
        {
            System.out.println("Arguments:");
            System.out.println("  filename");
            System.out.println("    To solve the maze stored in filename.");
            System.exit(-1);
        }
        */
        
        //These lines are to run via Eclipse without a command prompt
        String mazeLocationNotInProjectFolder = "C:\\Users\\Ben\\OneDrive\\Repositories\\CMSC433-Workspace\\maze-dist\\"; //replace this with your maze directory
        String whichMazeToUse = "1000x1000.mz"; //which maze file to load
        String[] replaceArgs = {mazeLocationNotInProjectFolder+whichMazeToUse};
        args = replaceArgs;
        
        //You probably shouldn't change the lines below
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("File " + file.getAbsolutePath() + " does not exist.");
            System.exit(-2);
        }
        try {
            m.read(args[0]);
            //m.read2(args[0]);
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException while reading maze from: " + args[0]);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException while reading maze from: " + args[0]);
            e.printStackTrace();
        }
        
        // Uncomment to use maze display
       // m.initDisplay();
        
        int rdnum = 0;
        
        // Uncomment this to run multiple times
//        for(int i=0; i < 3; i++) {
//        	m.solve(rdnum);
//        	rdnum++;
//        }
        
        
        // Uncomment this to run solve only once
        m.solve(rdnum);
        
        //System.out.println("Rec Wins: " + m.recWins + " -- DFS Wins: " + m.dfsWins + " -- BFS Wins: " + m.bfsWins + "-- Ben Wins: " + m.benWins);
    }
    

    @SuppressWarnings("unchecked")
    private void read(String filename) throws IOException, ClassNotFoundException
    {
        MazeInputStream in =
                new MazeInputStream(new BufferedInputStream(new FileInputStream(filename)));
        maze = (Maze) in.readObject();
        solvable = in.readBoolean();
        in.close();
    }

    private void read2(String filename) throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);

        maze = new Maze();
        //maze.readObject(ois);
        solvable = true;
        ois.close();
    }
    
    private String className(Class<?> cl)
    {
        StringBuffer fullname = new StringBuffer(cl.getName());
        String name = fullname.substring(fullname.lastIndexOf(".") + 1);
        return name;
    }
    
    
    private void initDisplay()
    {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int maze_width = maze.getWidth();
        int maze_height = maze.getHeight() + 2;
        int cell_width = (dim.width / maze_width);
        int cell_height = (dim.height / maze_height);
        int cell_size = Math.min(cell_width, cell_height);

        if (cell_size >= 2)
        {
            JFrame frame = new JFrame("Maze Solver");
            MazeDisplay display = new MazeDisplay(maze);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            maze.display = display;
            frame.setSize(maze_width * cell_size, maze_height * cell_size);
            frame.setVisible(true);
            Insets insets = frame.getInsets();
            frame.setSize(maze_width * cell_size + insets.left + insets.right + 3,
                    maze_height * cell_size + insets.top + insets.bottom + 2);
            System.out.println(frame.getSize());
            frame.getContentPane().add(display);
        }
        else
        {
            System.out.println("Maze too large to display on-screen.");
        }
    }
}
