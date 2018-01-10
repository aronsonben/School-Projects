package cmsc433.p3;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import cmsc433.p3.STMazeSolverBFS.SolutionNode;

import java.util.concurrent.RecursiveAction;




/**
 * This file needs to hold your solver to be tested. 
 * You can alter the class to extend any class that extends MazeSolver.
 * It must have a constructor that takes in a Maze.
 * It must have a solve() method that returns the datatype List<Direction>
 *   which will either be a reference to a list of steps to take or will
 *   be null if the maze cannot be solved.
 */
public class StudentMTMazeSolver extends SkippingMazeSolver
{
			//private BasicCountingLatchP3 endLatch;
		
		private ForkJoinPool fjPool;
		
		
		private BasicCountingLatchP3 endLatch;   // Tracks number of tasks created but not completed.
		//private int THRESHOLD;
		
		private List<Direction> finalResults = new LinkedList<Direction>();
		
		
		public StudentMTMazeSolver(Maze maze)
		{
		super(maze);
		}
		
		public class SolutionNode
		{
			public SolutionNode parent;
			public Choice choice;
			
			public SolutionNode(SolutionNode parent, Choice choice)
			{
			    this.parent = parent;
			    this.choice = choice;
			}
		}
		
		// Spawn FJDFSTasks
		private class FJTask extends RecursiveAction {
		private Choice ch;
		private Move mv;
		private SolutionNode node;
		
		public FJTask(Choice ch, Move mv, SolutionNode node) {
			this.ch = ch;
			this.mv = mv;
			this.node = node;
		}
		
		
		protected void compute() {
			Direction moveDir = null;
			try {
				if(!ch.isDeadend()) {
					while(ch.choices.size() > 0) { 
						
						moveDir = ch.choices.pop();
						Choice newCh = follow(ch.at, moveDir);
						Move newMove = new Move(ch.at, moveDir, mv);
						SolutionNode newNode = new SolutionNode(node, newCh);
						FJDFSTask task = new FJDFSTask(newCh, newMove, newNode);
						//TODO:
						//System.out.println(Thread.currentThread().getName() + " launching task at: " + ch.at);
						// Only need to fork() b/c all tasks are independent
						task.fork();
					}
				}
				
				FJTask.helpQuiesce();
				//endLatch.countDown();
			} catch (SolutionFound e) {
				LinkedList<Direction> path = new LinkedList<Direction>();
				Move current = new Move(ch.at, moveDir, new Move(null, null, null));
				while(current.previous != null) {
					path.push(current.to);
					current = current.previous;
				}
				finalResults = pathToFullPath(path);
			}
		}
		
		}
		
		
		// Trying ForkJoin tasks
		private class FJDFSTask extends RecursiveAction {
		private Choice ch;
		private Move mv;
		private SolutionNode node;
		
		public FJDFSTask(Choice ch, Move mv, SolutionNode node) {
			this.ch = ch;
			this.mv = mv;
			this.node = node;
		}
		
		
		protected void compute() {
			taskSolver();
		}
		
		protected List<Direction> taskSolver() {
			List<Direction> pathToReturn = new LinkedList<Direction>();
			pathToReturn = null;
			Direction moveDir = null;
			try {
				
				if(!ch.isDeadend()) {
					
					while(ch.choices.size() > 0) { 
						
						moveDir = ch.choices.pop();
						Choice newCh = follow(ch.at, moveDir);
						Move newMove = new Move(ch.at, moveDir, mv);
						SolutionNode newNode = new SolutionNode(node, newCh);
						FJDFSTask task = new FJDFSTask(newCh, newMove, newNode);
						
						
						//System.out.println("About to fork at: " + ch.at);
						// Only need to fork() b/c all tasks are independent
						task.fork();
					}
					
				} 
				
			} catch(SolutionFound e) {
				//System.out.println("Solution found from: " + ch.at);
				
				LinkedList<Direction> resultPath = new LinkedList<Direction>();
				Move current = new Move(ch.at, moveDir, mv);
				SolutionNode curNode = new SolutionNode(node, ch);
				
				while(current.previous != null) {
					resultPath.push(current.to);
					current = current.previous;
				}
				
				pathToReturn = pathToFullPath(resultPath);
				//finalResults = pathToFullPath(resultPath);
			}
			return pathToReturn;
		}
		
	}
		/*
		 * protected void compute() {
			//System.out.println("Computing: " + ch.at);
			Direction moveDir = null;
			try {
				
				if(!ch.isDeadend()) {
					
					while(ch.choices.size() > 0) { 
						
						moveDir = ch.choices.pop();
						Choice newCh = follow(ch.at, moveDir);
						Move newMove = new Move(ch.at, moveDir, mv);
						SolutionNode newNode = new SolutionNode(node, newCh);
						FJDFSTask task = new FJDFSTask(newCh, newMove, newNode);
						
						
						//System.out.println("About to fork at: " + ch.at);
						// Only need to fork() b/c all tasks are independent
						task.fork();
					}
					
				} 
				
			} catch(SolutionFound e) {
				//System.out.println("Solution found from: " + ch.at);
				
				LinkedList<Direction> resultPath = new LinkedList<Direction>();
				Move current = new Move(ch.at, moveDir, mv);
				SolutionNode curNode = new SolutionNode(node, ch);
				
//				try {
//				while(curNode.parent != null) {
//					//System.out.println(curNode.choice.from.reverse() + " " + current.to);
//					resultPath.push(curNode.choice.from.reverse());
//					curNode = curNode.parent;
//					
//					//current = current.previous;
//				}
//				}catch(NullPointerException e2) {
//					System.out.println("yo");
//				}
				
				while(current.previous != null) {
					resultPath.push(current.to);
					current = current.previous;
				}
				
				finalResults = pathToFullPath(resultPath);
				//endLatch.countDown();
			}
			
		}
		 */
		
		
		@SuppressWarnings("null")
		public List<Direction> solve() {
		Choice startChoice = null;
		
		if( maze.getHeight() <= 200 && maze.getWidth() <= 200 ) {
			// run sequentially
			STMazeSolverDFS dfsSolve = new STMazeSolverDFS(maze);
			return dfsSolve.solve();
		} else {
			
			try {
		    	
		    	
		    	// Keeping track of how many tasks/threads are working/done
		    	//endLatch = new BasicCountingLatchP3(1);
		    	
		    	//startChoice = firstChoice(maze.getStart());
		    	
		    	
		    	//////////// Trying with ForkJoinPool /////////////////
		        fjPool = new ForkJoinPool();
		    	
		        startChoice = firstChoice(maze.getStart());
		    	//FJTask task0 = new FJTask(startChoice, new Move(null, null, null), new SolutionNode(null, null));
		    	FJDFSTask task0 = new FJDFSTask(startChoice, new Move(null, null, null), new SolutionNode(null, null));
		        
		    	fjPool.execute(task0);
		    	
		    	//endLatch.await();
		    	task0.join();
		
		    	fjPool.shutdown();
		    	
		    	//////////////////////////////////////////////////////
				
				
				// This method needs to have an option where it is 'null' if there is no solution or 'solutionPath' if there is
				if(finalResults.isEmpty()) {
					return null;
				} else {
					return finalResults;
				}
				
				
			} catch(SolutionFound e) {
				LinkedList<Direction> path = new LinkedList<Direction>();
				path.push(startChoice.choices.pop());
				finalResults = pathToFullPath(path);
				return finalResults;
			}
		
		}
		
	}
}







///////////////////////// StudentMTMazeSolver - ForkJoin Idea - 4/13 ~04:08 PM //////////////////////////////
/* 



//private BasicCountingLatchP3 endLatch;

	private ForkJoinPool fjPool;
	
	
	private BasicCountingLatchP3 endLatch;   // Tracks number of tasks created but not completed.
	//private int THRESHOLD;
	
	private List<Direction> finalResults = new LinkedList<Direction>();
	
	
	public StudentMTMazeSolver(Maze maze)
  {
      super(maze);
  }
	
	// Spawn FJDFSTasks
	private class FJTask extends RecursiveAction {
		private Choice ch;
		private Move mv;
		
		public FJTask(Choice ch, Move mv) {
			this.ch = ch;
			this.mv = mv;
		}
		
		
		protected void compute() {
			Direction moveDir = null;
			try {
				if(!ch.isDeadend()) {
					while(ch.choices.size() > 0) { 
						
						moveDir = ch.choices.pop();
						Choice newCh = follow(ch.at, moveDir);
						Move newMove = new Move(ch.at, moveDir, mv);
						FJDFSTask task = new FJDFSTask(newCh, newMove);
						
						//System.out.println(Thread.currentThread().getName() + " launching task at: " + ch.at);
						// Only need to fork() b/c all tasks are independent
						task.fork();
					}
				}
				
				FJTask.helpQuiesce();
				endLatch.countDown();
			} catch (SolutionFound e) {
				LinkedList<Direction> path = new LinkedList<Direction>();
				Move current = new Move(ch.at, moveDir, new Move(null, null, null));
				while(current.previous != null) {
					path.push(current.to);
					current = current.previous;
				}
				finalResults = pathToFullPath(path);
			}
		}
		
	}
	
	
	// Trying ForkJoin tasks
	private class FJDFSTask extends RecursiveAction {
		private Choice ch;
		private Move mv;
		
		public FJDFSTask(Choice ch, Move mv) {
			this.ch = ch;
			this.mv = mv;
		}

		// like run() method
		protected void compute() {
			//System.out.println("Computing: " + ch.at);
			Direction moveDir = null;
			try {
				
				if(!ch.isDeadend()) {
					
					while(ch.choices.size() > 0) { 
						
						moveDir = ch.choices.pop();
						Choice newCh = follow(ch.at, moveDir);
						Move newMove = new Move(ch.at, moveDir, mv);
						FJDFSTask task = new FJDFSTask(newCh, newMove);
						
						
						//System.out.println("About to fork at: " + ch.at);
						// Only need to fork() b/c all tasks are independent
						task.fork();
					}
					
				} 
				
			} catch(SolutionFound e) {
				//System.out.println("Solution found from: " + ch.at);
				
				
				LinkedList<Direction> resultPath = new LinkedList<Direction>();
				Move current = new Move(ch.at, moveDir, mv);
				while(current.previous != null) {
					resultPath.push(current.to);
					current = current.previous;
				}
				
				finalResults = pathToFullPath(resultPath);
				endLatch.countDown();
			}
			
		}
		
	}
	

	@SuppressWarnings("null")
	public List<Direction> solve() {
		Choice startChoice = null;
		
		if(maze.getHeight() <= 200 && maze.getWidth() <= 200) {
			// run sequentially
			STMazeSolverDFS dfsSolve = new STMazeSolverDFS(maze);
			return dfsSolve.solve();
		} else {
			
			try {
				
				// Start up Executor
				//int NUMTHREADS = Runtime.getRuntime().availableProcessors();
		    	//exec = Executors.newFixedThreadPool(NUMTHREADS);
		    	
		    	
		    	// Keeping track of how many tasks/threads are working/done
		    	endLatch = new BasicCountingLatchP3(1);
		    	
		    	//startChoice = firstChoice(maze.getStart());
		    	
		    	
		    	//////////// Trying with ForkJoinPool /////////////////
		    	//System.out.println("About to start up at: " + startChoice.at + " with " + NUMTHREADS + " threads possible");
		        fjPool = new ForkJoinPool();
		    	
		    	FJTask task0 = new FJTask(firstChoice(maze.getStart()), new Move(null, null, null));
		    	
		    	fjPool.execute(task0);
		    	
		    	endLatch.await();
		    	//task0.join();

		    	fjPool.shutdown();
		    	
		    	//////////////////////////////////////////////////////
				
				
				// This method needs to have an option where it is 'null' if there is no solution or 'solutionPath' if there is
				if(finalResults.isEmpty()) {
					return null;
				} else {
					return finalResults;
				}
				
				
			} catch(SolutionFound e) {
				LinkedList<Direction> path = new LinkedList<Direction>();
				path.push(startChoice.choices.pop());
				finalResults = pathToFullPath(path);
				return finalResults;
			}
		
		}
		
	}
	////////// Solve misc. solutions //////////////
----#1-----
  fjPool = new ForkJoinPool();
	
	FJTask task0 = new FJTask(startChoice, new Move(null, null, null));
	
	fjPool.execute(task0);
	
	task0.join();

	fjPool.shutdown();
	
	
	
	///// #2 /////    	
	    	fjPool = new ForkJoinPool();
	    	
	    	FJDFSTask task0 = new FJDFSTask(startChoice, new Move(null, null, null));
	    	
	    	tasks.countUp();
	    	
	    	fjPool.execute(task0);
	    	
	    	fjPool.awaitQuiescence(30, TimeUnit.SECONDS);
	    	//tasks.await();

	    	fjPool.shutdown();
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Try #2 ///////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
 * 	
	//private BasicCountingLatchP3 endLatch;
	
	private ForkJoinPool fjPool;
	
	
	private BasicCountingLatchP3 endLatch;   // Tracks number of tasks created but not completed.
	//private int THRESHOLD;
	
	private List<Direction> finalResults = new LinkedList<Direction>();
	
	
	public StudentMTMazeSolver(Maze maze)
    {
        super(maze);
    }
	
    public class SolutionNode
    {
        public SolutionNode parent;
        public Choice choice;

        public SolutionNode(SolutionNode parent, Choice choice)
        {
            this.parent = parent;
            this.choice = choice;
        }
    }
	
	// Spawn FJDFSTasks
	private class FJTask extends RecursiveAction {
		private Choice ch;
		private Move mv;
		private SolutionNode node;
		
		public FJTask(Choice ch, Move mv, SolutionNode node) {
			this.ch = ch;
			this.mv = mv;
			this.node = node;
		}
		
		
		protected void compute() {
			// Incoming Choice is "first choice" -- want to have first choice be null then second one be "firstChoice()" basically
			SolutionNode firstNode = new SolutionNode(node, ch);
			SolutionNode start = null;
			Direction moveDir = null;
			try {
				if(!ch.isDeadend()) {
					while(ch.choices.size() > 0) { 
						
//						moveDir = ch.choices.pop();
//						Position newPos = ch.at.move(moveDir);
//						if(newPos.equals(maze.getEnd())) throw new SolutionFound(newPos, moveDir);
//						
//						LinkedList<Direction> openPos = maze.getMoves(newPos);
//						openPos.remove(moveDir.reverse());
//						
//						Choice newCh = new Choice(newPos, moveDir, openPos);
//						Move newMove = new Move(ch.at, moveDir, mv);
//						SolutionNode newNode = new SolutionNode(node, newCh);
				/////////////////////////////////////////////////////////////////////////////////////////////////			
						moveDir = ch.choices.pop();
						Choice newCh = follow(ch.at, moveDir);
						Move newMove = new Move(ch.at, moveDir, mv);
						
						start = new SolutionNode(firstNode, newCh);
					
						FJDFSTask task = new FJDFSTask(newCh, newMove, start);
						
						//System.out.println(Thread.currentThread().getName() + " launching task at: " + ch.at);
						// Only need to fork() b/c all tasks are independent
						task.fork();
					}
				}
				
				FJTask.helpQuiesce();
				//endLatch.countDown();
				
			} catch (SolutionFound e) {
				LinkedList<Direction> path = new LinkedList<Direction>();
				Move current = new Move(ch.at, moveDir, new Move(null, null, null));
				while(current.previous != null) {
					path.push(current.to);
					current = current.previous;
				}
				finalResults = pathToFullPath(path);
			}
		}
		
	}
	
	
	// Trying ForkJoin tasks
	private class FJDFSTask extends RecursiveAction {
		private Choice ch;
		private Move mv;
		private Direction prev;
		private SolutionNode prevNode;
		
		public FJDFSTask(Choice ch, Move mv, SolutionNode prevNode) {
			this.ch = ch;
			this.mv = mv;
			this.prev = prev;
			this.prevNode = prevNode;
		}

		// like run() method
		protected void compute() {
			//System.out.println("Computing: " + ch.at);
			Direction moveDir = null;
			try {
				
				if(!ch.isDeadend()) {
					
					while(ch.choices.size() > 0) { 
//						moveDir = ch.choices.pop();
//						Position newPos = ch.at.move(moveDir);
//						if(newPos.equals(maze.getEnd())) throw new SolutionFound(newPos, moveDir);
//						
//						LinkedList<Direction> openPos = maze.getMoves(newPos);
//						openPos.remove(moveDir.reverse());
//						
//						Choice newCh = new Choice(newPos, moveDir, openPos);
//						Move newMove = new Move(ch.at, moveDir, mv);
//						SolutionNode newNode = new SolutionNode(prevNode, newCh);
//						
//						//System.out.println("New node at: " + newNode.choice.at + " with parent: " + newNode.parent.choice.at);
//						
//						FJDFSTask task = new FJDFSTask(newCh, newMove, newNode);
		/////////////////////////////////////////////////////////////////////////////////////////////////
						moveDir = ch.choices.pop();
						Choice newCh = follow(ch.at, moveDir);
						Move newMove = new Move(ch.at, moveDir, mv);
						SolutionNode newNode = new SolutionNode(prevNode, newCh);
						
						//System.out.println("New node at: " + newNode.choice.at + " with parent: " + newNode.parent.choice.at);
						
						FJDFSTask task = new FJDFSTask(newCh, newMove, newNode);
						
						
						//System.out.println("About to fork at: " + ch.at);
						// Only need to fork() b/c all tasks are independent
						task.fork();
					}
					
				} 
				
			} catch(SolutionFound e) {
				//System.out.println("Solution found at: " + ch.at);
				
				
				LinkedList<Direction> resultPath = new LinkedList<Direction>();
				Move current = new Move(ch.at, moveDir, mv);
				
				Choice finCh = new Choice(e.pos, e.from, null);
				
				SolutionNode curNode = new SolutionNode(prevNode, finCh);
				//System.out.println("SolutionNode: " + prevNode.parent.choice.at);
				
				while(curNode != null) {
					System.out.println(curNode.choice.at + " " + curNode.parent.choice.at + " " + curNode.choice.from.reverse());
					resultPath.push(curNode.choice.from);
					curNode = curNode.parent;
				}
				
				resultPath.push(Direction.SOUTH);
				System.out.println("pushed");
//				while(curNode.parent != null) {
//					resultPath.push(curNode.choice.from.reverse());
//					curNode = curNode.parent;
//					
//					//System.out.println(curNode.choice.at + " " + curNode.parent.choice.at + " " + curNode.choice.from.reverse());
//				}
				
//				while(current.previous != null) {
//					resultPath.push(current.to);
//					current = current.previous;
//					
//					//System.out.println(current.from + " " + current.to + " " + current.previous.from);
//				}
				//System.out.println(curNode.parent.choice.at);
				
				finalResults = pathToFullPath(resultPath);
				endLatch.countDown();
			}
			
		}
		
	}
	

	@SuppressWarnings("null")
	public List<Direction> solve() {
		Choice startChoice = null;
		
		if( !(maze.getHeight() <= 200 && maze.getWidth() <= 200) ) {
			// run sequentially
			STMazeSolverDFS dfsSolve = new STMazeSolverDFS(maze);
			return dfsSolve.solve();
		} else {
			
			try {
				
				// Start up Executor
				//int NUMTHREADS = Runtime.getRuntime().availableProcessors();
		    	//exec = Executors.newFixedThreadPool(NUMTHREADS);
		    	
		    	
		    	// Keeping track of how many tasks/threads are working/done
		    	endLatch = new BasicCountingLatchP3(1);
		    	
		    	
		    	//////////// Trying with ForkJoinPool /////////////////
		    	//System.out.println("About to start up at: " + startChoice.at + " with " + NUMTHREADS + " threads possible");
		        fjPool = new ForkJoinPool();
		        
		        
		    	//FJTask task0 = new FJTask(firstChoice(maze.getStart()), new Move(null, null, null));
//		        Choice earlyCh = new Choice(null, null, null);
//		        Position startPos = maze.getStart();
//		        LinkedList<Direction> openPos = maze.getMoves(startPos);
//		        openPos.remove(Direction.NORTH);
//		        Choice startCh = new Choice(startPos, Direction.NORTH, openPos);
		        //if(startPos.equals(maze.getEnd())) throw new SolutionFound(startPos, Direction.NORTH);
		        
		        startChoice = firstChoice(maze.getStart());
		        
		        SolutionNode firstNode = new SolutionNode(null, null);
		        
		        FJTask task0 = new FJTask( startChoice , new Move(null, null, null), firstNode);
		    	
		    	fjPool.execute(task0);
		    	
		    	//endLatch.await();
		    	task0.join();

		    	fjPool.shutdown();
		    	
		    	//////////////////////////////////////////////////////
				
				
				// This method needs to have an option where it is 'null' if there is no solution or 'solutionPath' if there is
				if(finalResults.isEmpty()) {
					return null;
				} else {
					return finalResults;
				}
				
				
			} catch(SolutionFound e) {
				LinkedList<Direction> path = new LinkedList<Direction>();
				path.push(startChoice.choices.pop());
				finalResults = pathToFullPath(path);
				return finalResults;
			}
		
		}
		
	}
	////////// Solve misc. solutions //////////////
	 ----#1-----
    fjPool = new ForkJoinPool();
	
	FJTask task0 = new FJTask(startChoice, new Move(null, null, null));
	
	fjPool.execute(task0);
	
	task0.join();

	fjPool.shutdown();
	
	
	
	///// #2 /////    	
	    	fjPool = new ForkJoinPool();
	    	
	    	FJDFSTask task0 = new FJDFSTask(startChoice, new Move(null, null, null));
	    	
	    	tasks.countUp();
	    	
	    	fjPool.execute(task0);
	    	
	    	fjPool.awaitQuiescence(30, TimeUnit.SECONDS);
	    	//tasks.await();

	    	fjPool.shutdown();
	    	
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Try 3 -working with SolutionNode & Move//////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
	    		//private BasicCountingLatchP3 endLatch;

		private ForkJoinPool fjPool;
		
		
		private BasicCountingLatchP3 endLatch;   // Tracks number of tasks created but not completed.
		//private int THRESHOLD;
		
		private List<Direction> finalResults = new LinkedList<Direction>();
		
		
		public StudentMTMazeSolver(Maze maze)
		{
	      super(maze);
		}
		
	    public class SolutionNode
	    {
	        public SolutionNode parent;
	        public Choice choice;
	
	        public SolutionNode(SolutionNode parent, Choice choice)
	        {
	            this.parent = parent;
	            this.choice = choice;
	        }
	    }
	    
		// Spawn FJDFSTasks
		private class FJTask extends RecursiveAction {
			private Choice ch;
			private Move mv;
			private SolutionNode node;
			
			public FJTask(Choice ch, Move mv, SolutionNode node) {
				this.ch = ch;
				this.mv = mv;
				this.node = node;
			}
			
			
			protected void compute() {
				Direction moveDir = null;
				try {
					if(!ch.isDeadend()) {
						while(ch.choices.size() > 0) { 
							
							moveDir = ch.choices.pop();
							Choice newCh = follow(ch.at, moveDir);
							Move newMove = new Move(ch.at, moveDir, mv);
							SolutionNode newNode = new SolutionNode(node, newCh);
							FJDFSTask task = new FJDFSTask(newCh, newMove, newNode);
							//TODO:
							//System.out.println(Thread.currentThread().getName() + " launching task at: " + ch.at);
							// Only need to fork() b/c all tasks are independent
							task.fork();
						}
					}
					
					FJTask.helpQuiesce();
					endLatch.countDown();
				} catch (SolutionFound e) {
					LinkedList<Direction> path = new LinkedList<Direction>();
					Move current = new Move(ch.at, moveDir, new Move(null, null, null));
					while(current.previous != null) {
						path.push(current.to);
						current = current.previous;
					}
					finalResults = pathToFullPath(path);
				}
			}
			
		}
		
		
		// Trying ForkJoin tasks
		private class FJDFSTask extends RecursiveAction {
			private Choice ch;
			private Move mv;
			private SolutionNode node;
			
			public FJDFSTask(Choice ch, Move mv, SolutionNode node) {
				this.ch = ch;
				this.mv = mv;
				this.node = node;
			}

			// like run() method
			protected void compute() {
				//System.out.println("Computing: " + ch.at);
				Direction moveDir = null;
				try {
					
					if(!ch.isDeadend()) {
						
						while(ch.choices.size() > 0) { 
							
							moveDir = ch.choices.pop();
							Choice newCh = follow(ch.at, moveDir);
							Move newMove = new Move(ch.at, moveDir, mv);
							SolutionNode newNode = new SolutionNode(node, newCh);
							FJDFSTask task = new FJDFSTask(newCh, newMove, newNode);
							
							
							//System.out.println("About to fork at: " + ch.at);
							// Only need to fork() b/c all tasks are independent
							task.fork();
						}
						
					} 
					
				} catch(SolutionFound e) {
					//System.out.println("Solution found from: " + ch.at);
					
					LinkedList<Direction> resultPath = new LinkedList<Direction>();
					Move current = new Move(ch.at, moveDir, mv);
					SolutionNode curNode = new SolutionNode(node, ch);
					
					try {
					while(curNode.parent != null) {
						System.out.println(curNode.choice.from.reverse() + " " + current.to);
						resultPath.push(curNode.choice.from.reverse());
						curNode = curNode.parent;
						
						current = current.previous;
					}
					}catch(NullPointerException e2) {
						System.out.println("yo");
					}
					
//					while(current.previous != null) {
//						resultPath.push(current.to);
//						current = current.previous;
//					}
					
					finalResults = pathToFullPath(resultPath);
					endLatch.countDown();
				}
				
			}
			
		}
		

		@SuppressWarnings("null")
		public List<Direction> solve() {
			Choice startChoice = null;
			
			if( !(maze.getHeight() <= 200 && maze.getWidth() <= 200) ) {
				// run sequentially
				STMazeSolverDFS dfsSolve = new STMazeSolverDFS(maze);
				return dfsSolve.solve();
			} else {
				
				try {
			    	
			    	
			    	// Keeping track of how many tasks/threads are working/done
			    	endLatch = new BasicCountingLatchP3(1);
			    	
			    	//startChoice = firstChoice(maze.getStart());
			    	
			    	
			    	//////////// Trying with ForkJoinPool /////////////////
			        fjPool = new ForkJoinPool();
			    	
			        startChoice = firstChoice(maze.getStart());
			    	FJTask task0 = new FJTask(startChoice, new Move(null, null, null), new SolutionNode(null, null));
			    	
			    	fjPool.execute(task0);
			    	
			    	endLatch.await();
			    	//task0.join();

			    	fjPool.shutdown();
			    	
			    	//////////////////////////////////////////////////////
					
					
					// This method needs to have an option where it is 'null' if there is no solution or 'solutionPath' if there is
					if(finalResults.isEmpty()) {
						return null;
					} else {
						return finalResults;
					}
					
					
				} catch(SolutionFound e) {
					LinkedList<Direction> path = new LinkedList<Direction>();
					path.push(startChoice.choices.pop());
					finalResults = pathToFullPath(path);
					return finalResults;
				}
			
			}
			
		}

*/