package cmsc420.meeshquest.part1;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;
import cmsc420.geom.Circle2D;

public class PRQuadtree {
	
	public final int spatialWidth;
	public final int spatialHeight;
	private WhiteNode whiteNode;
	private WhiteNode whiteInstance;
	
	private QuadtreeNode root;
	CanvasPlus canvas = null;
	Document results = null;
	
	public PRQuadtree(int width, int height, CanvasPlus canvas) {
		this.spatialWidth = width;
		this.spatialHeight = height;
		this.root = null;
		this.canvas = canvas;
		
		// FIX(?) - trying to create a singleton WhiteNode object
		whiteNode = new WhiteNode();
		whiteInstance = whiteNode.getInstance();
	}
	
	/** Function to insert a named city into the spatial map */
	public QuadtreeNode insert(City c) {
		return insert(c, root, 0, 0, spatialWidth, spatialHeight);
	}
	
	private QuadtreeNode insert(City c, QuadtreeNode node, int minX, int minY, int curWidth, int curHeight) {
		QuadtreeNode newNode = null;
		if(node == null) {
			// Tree is empty, make root a BlackNode with this City
			root = new BlackNode(c);
			canvas.addPoint(c.name, c.getX(), c.getY(), Color.BLACK);
		} 
		else if(node instanceof WhiteNode) {
			WhiteNode wn = (WhiteNode)node;
			newNode = wn.add(c);
		} 
		else if(node instanceof BlackNode) {
			BlackNode bn = (BlackNode)node;
			newNode = bn.add(c, minX, minY, curWidth, curHeight);
			if(node.equals(root)) {
				root = newNode;
			}
			canvas.addPoint(c.name, c.getX(), c.getY(), Color.BLACK);
		} 
		else {
			GrayNode gn = (GrayNode)node;
			newNode = gn.add(c, c.getX(), c.getY());
			canvas.addPoint(c.name, c.getX(), c.getY(), Color.BLACK);
		}
		return newNode;
	}
	
	public QuadtreeNode delete(City c) {
		return delete(c, root);
	}
	
	private QuadtreeNode delete(City c, QuadtreeNode node) {
		QuadtreeNode newNode = null;
		if(node == null) {
			// Empty tree, can't do anything -- ERROR?
		} else if(node instanceof BlackNode) {
			BlackNode bn = (BlackNode)node;
			if(c.equals(bn.city)) {
				newNode = bn.remove(c);
				root = newNode;
			}
		} else if(node instanceof GrayNode) {
			GrayNode gn = (GrayNode)node;
			newNode = gn.remove(c);
			
			if(newNode instanceof GrayNode) {
				GrayNode newGray = (GrayNode)newNode;
				QuadtreeNode bNode = newGray.checkCollapse(newGray.getChildren());
				newNode = newGray.collapse(bNode);
			}
			
			root = newNode;
			canvas.removePoint(c.name, c.getX(), c.getY(), Color.BLACK);
		} else {
			// WhiteNode... what do you even do? ... nothing?
		}
		if(root instanceof WhiteNode) {
			root = null;
		}
		return newNode;
	}
	
	public TreeMap<String, City> range(int x, int y, int radius) {
		canvas.addCircle(x, y, radius, Color.BLACK, false);
		TreeMap<String, City> withinRange = new TreeMap<String, City>(new CityNameComparator());
		if(root == null) {
			// can't do anything 
		} else {
			Point2D pt = new Point(x, y);
			range(withinRange, root, pt, radius);
		}
		return withinRange;
	}
	
	private void range(TreeMap<String, City> withinRange, QuadtreeNode node, Point2D pt, int radius) {
		if(node instanceof BlackNode) {
			BlackNode bn = (BlackNode)node;
			City c = bn.getCity();
			if(pt.distance(c) <= radius) {
				withinRange.put(c.name, c);
			}
		} else if(node instanceof GrayNode) {
			GrayNode gn = (GrayNode)node;
			QuadtreeNode[] gnChildren = gn.getChildren();
			for(QuadtreeNode n : gnChildren) {
				if(n instanceof BlackNode) {
					BlackNode bn = (BlackNode)n;
					City c = bn.getCity();
					if(pt.distance(c) <= radius) {
						withinRange.put(c.name, c);
					}
				} else if(n instanceof GrayNode) {
					range(withinRange, n, pt, radius);
				} else {
					// WhiteNode, do nothing
				}
			}
		} else {
			// WhiteNode, do nothing
		}
	}
	
	public Element printTree(Document results, Element tree) {
		this.results = results;
		return printTree(results, tree, root);
	}
	
	private Element printTree(Document results, Element tree, QuadtreeNode node) {
		if(node == null) {
			// can't do anything --> tree is empty
		} else if(node instanceof BlackNode) {
			BlackNode bn = (BlackNode)node;
			Element blackTag = results.createElement("black");
			blackTag = bn.print(blackTag);
			tree.appendChild(blackTag);
		} else if(node instanceof GrayNode) {
			GrayNode gn = (GrayNode)node;
			Element grayTag = results.createElement("gray");
			grayTag = gn.print(grayTag);
			tree.appendChild(grayTag);
		} else {
			WhiteNode wn = (WhiteNode)node;
			Element whiteTag = results.createElement("white");
			tree.appendChild(whiteTag);
		}
		return tree;
	}
	
	public City nearestCity(int x, int y) {
		// PriorityQueue<QuadtreeNode> pq = new PriorityQueue<QuadtreeNode>(); 	// maybe use later	
		Point2D pt = new Point(x, y);
		CityNameComparator nameComp = new CityNameComparator();
		City nearestCity = null;
		return nearestCity(pt, root, nearestCity, nameComp);
	}
	
	// not using Samet's algo for right now
	private City nearestCity(Point2D pt, QuadtreeNode node, City nearestCity, CityNameComparator comparator) {
		
		if(node instanceof BlackNode) {
			BlackNode bn = (BlackNode)node;
			City c = bn.getCity();
			if(nearestCity != null) {
				if(pt.distance(nearestCity) > pt.distance(c)) {
					nearestCity = c;
				} else if(pt.distance(nearestCity) == pt.distance(c)) {
					if(comparator.compare(nearestCity.name, c.name) > 0) 
						nearestCity = c;
				} else {
					// nothing
				}
			} else {
				nearestCity = c;
			}
		} else if(node instanceof GrayNode) {
			GrayNode gn = (GrayNode)node;
			QuadtreeNode[] children = gn.getChildren();
			City temp;
			for(QuadtreeNode n : children) {
				temp = nearestCity(pt, n, nearestCity, comparator);
				if(nearestCity != null) {
					if(pt.distance(nearestCity) > pt.distance(temp)) {
						nearestCity = temp;
					} else if(pt.distance(nearestCity) == pt.distance(temp)) {
						if(comparator.compare(nearestCity.name, temp.name) > 0) 
							nearestCity = temp;
					} else {
						// nothing
					}
				} else {
					nearestCity = temp;
				}
			}
		} else {
			// WhiteNode
		}
		//System.out.println("Current nearestCity="+nearestCity.toString());
		return nearestCity;
	}
	
	public boolean isEmpty() {
		if(root == null)  
			return true;
		else
			return false;
	}
	
	@Override
	public String toString() {
		return root.toString();
	}
	
	
	/* **************************************************** */
	/* ******** Node classes ****************************** */
	/* **************************************************** */
	
	// TODO: Quadtree Node abstract class
	private abstract class QuadtreeNode {
		public QuadtreeNode() {
		}
		
		abstract public QuadtreeNode add(City c);
		abstract public QuadtreeNode remove(City c);
		abstract public Element print(Element tag);
		
		/** Call the appropriate "add()" depending on type of current node 
		 * @return newly created QuadtreeNode node. */
		/*public QuadtreeNode add(City c, int width, int height) {}*/
	}
	
	// TODO: WhiteNode class
	/** Singleton class to represent a WhiteNode (a leaf) in a PRQuadtree. Has no children. */
	private class WhiteNode extends QuadtreeNode {
		private WhiteNode whiteNodeObj;
		private WhiteNode() { }
		
		public WhiteNode getInstance() {
			if(whiteNodeObj == null) 
				whiteNodeObj = new WhiteNode();
			return whiteNodeObj;
		}
		
		public QuadtreeNode add(City c) {
			return new BlackNode(c);
		}
		
		public Element print(Element tag) {
			return null;
		}
		
		// Needed because "remove" is abstract method -- shouldn't do anything
		public QuadtreeNode remove(City c) {
			return null;
		}
		public String toString() {
			return "[WhiteNode]";
		}
	}
	
	// TODO: BlackNode class
	private class BlackNode extends QuadtreeNode {
		public City city; 
		public BlackNode(City c) {
			this.city = c;
		}
		
		public QuadtreeNode add(City newCity, int minX, int minY, int width, int height) {
			GrayNode g = new GrayNode(city.getX(), city.getY(), minX, minY, width, height);
			g.add(city, city.getX(), city.getY()); 			// Add BlackNode city currently at this spot in PRQuadtree
			g.add(newCity, newCity.getX(), newCity.getY());		// Add new city
			return g;
		}
		
		public QuadtreeNode remove(City c) {
			canvas.removePoint(c.name, c.getX(), c.getY(), Color.BLACK);
			return new WhiteNode();
		}
		
		public Element print(Element tag) {
			int x = new Double(city.getX()).intValue();
			int y = new Double(city.getY()).intValue();
			tag.setAttribute("name", city.name);
			tag.setAttribute("x", String.valueOf(x));
			tag.setAttribute("y", String.valueOf(y));
			return tag;
		}
		
		public QuadtreeNode add(City newCity) { return null; }
		public String toString() {
			return "[BlackNode: "+city.name+"]";
		}
		
		public City getCity() {
			return this.city;
		}
	}
	
	// TODO: GrayNode class
	private class GrayNode extends QuadtreeNode {
		double x, y;
		int minimumX, minimumY;
		int newWidth, newHeight;
		QuadtreeNode[] children;
		Rectangle[] childrenBounds;			// Order: NW, NE, SW, SE
		
		public GrayNode(double x, double y, int minX, int minY, int width, int height) {
			this.x = x;
			this.y = y;
			minimumX = minX;
			minimumY = minY;
			newWidth = width/2;
			newHeight = height/2;
			childrenBounds = partition(width, height, minX, minY, newWidth, newHeight);
			children = new QuadtreeNode[4];
			for(int i=0; i < 4; i++) 
				children[i] = whiteInstance;		// Needed? CHECK
		}
		
		//Use bounds to figure out which quadrant to go into by checking with Rectangles
		public QuadtreeNode add(City c, double x, double y) {
			for(int i=0; i < 4; i++) {
				if(childrenBounds[i].contains(x, y)) {
					if(children[i] instanceof WhiteNode) {
						children[i] = children[i].add(c);
						return this;
					} else { // BlackNode or GrayNode
						int rectMinX = new Double(childrenBounds[i].getMinX()).intValue();
						int rectMinY = new Double(childrenBounds[i].getMinY()).intValue();
						children[i] = insert(c, children[i], rectMinX, rectMinY, newWidth, newHeight);
						return this;			// Changed this from "children[i]" to "this"
					}
				}
			}
			return null;
		}
		
		
		public QuadtreeNode remove(City c) {
			QuadtreeNode deleteNode = null;
			for(int i=0; i < 4; i++) {
				if(childrenBounds[i].contains(c.getX(), c.getY())) {
					if(children[i] instanceof BlackNode) {
						BlackNode childNode = (BlackNode)children[i];
						if(childNode.city.equals(c)) {
							children[i] = childNode.remove(c);
							deleteNode = children[i];
							
							// Return the BlackNode at the index returned by checkCollapse
							QuadtreeNode bNode = checkCollapse(children);
							return collapse(bNode);
						}
					} else if(children[i] instanceof GrayNode) {
						GrayNode childNode = (GrayNode)children[i];
						children[i] = delete(c, childNode);
						deleteNode = children[i];
						return this;
					} else {
						// white node do nothing
					}
				}
			}
			return deleteNode;
		}
		
		public Element print(Element tag) {
			tag.setAttribute("x", String.valueOf(minimumX+newHeight));
			tag.setAttribute("y", String.valueOf(minimumY+newWidth));
			for(QuadtreeNode n : children) {
				printTree(results, tag, n);
			}
			return tag;
		}
				
		// Helpers //
		private Rectangle[] partition(int width, int height, int minX, int minY, int newWidth, int newHeight) {
			Rectangle[] bounds = new Rectangle[4];
			bounds[0] = new Rectangle(minX, minY+newHeight, newWidth, newHeight);				// NW
			bounds[1] = new Rectangle(minX+newHeight, minY+newWidth, newWidth, newHeight);		// NE
			bounds[2] = new Rectangle(minX, minY, newWidth, newHeight);							// SW
			bounds[3] = new Rectangle(minX+newHeight, minY, newWidth, newHeight);				// SE
			canvas.addLine(minX+newWidth, minY, minX+newWidth, minY+height, Color.BLACK); 		// Vertical
			canvas.addLine(minX, minY+newHeight, minX+width, minY+newHeight, Color.BLACK);		// Horizontal
			return bounds;
		}
		
		/** Helper to check if collapse from GrayNode to BlackNode is needed after child removal 
		 * @return the index of the single BlackNode IF there are 3 white nodes and 1 black node 
		*/
		private QuadtreeNode checkCollapse(QuadtreeNode[] children) {
			int whiteCt = 0, blackCt = 0, blackIndex = -1;
			for(int i=0; i < 4; i++) {
				if(children[i] instanceof WhiteNode)
					whiteCt++;
				if(children[i] instanceof BlackNode) {
					blackCt++;
					blackIndex = i;
				}
			}
			if( whiteCt == 3 && blackCt == 1) {
				return children[blackIndex];
			} else { return null; }
		}
		
		private boolean checkGrayCollapse(QuadtreeNode[] children) {
			int wnCt = 0;
			for(QuadtreeNode n : children) {
				if(n instanceof WhiteNode) {
					wnCt++;
				}
			}
			if(wnCt == 4) 
				return true;
			else return false;
		}
		
		private void collapse() {
			canvas.removeLine(minimumX+newWidth, minimumY, minimumX+newWidth, minimumY+(newHeight*2), Color.BLACK);
			canvas.removeLine(minimumX, minimumY+newHeight, minimumX+(newWidth*2), minimumY+newHeight, Color.BLACK);
		}
		
		private QuadtreeNode collapse(QuadtreeNode bNode) {
			if(bNode != null) {
				/*if(c.name.equals("Miami")) {
					System.out.println("About to collapse node after deleting Miami. Collapsing on: "+
							(minimumX+newWidth)+","+minimumY+" to "+(minimumX+newWidth)+", "+(minimumY+(newHeight*2)));
					System.out.println("Returning "+bNode.toString());
				}*/
				canvas.removeLine(minimumX+newWidth, minimumY, minimumX+newWidth, minimumY+(newHeight*2), Color.BLACK);
				canvas.removeLine(minimumX, minimumY+newHeight, minimumX+(newWidth*2), minimumY+newHeight, Color.BLACK);
				return bNode;
			} else {
				if(checkGrayCollapse(children)) {
					canvas.removeLine(minimumX+newWidth, minimumY, minimumX+newWidth, minimumY+(newHeight*2), Color.BLACK);
					canvas.removeLine(minimumX, minimumY+newHeight, minimumX+(newWidth*2), minimumY+newHeight, Color.BLACK);
					return whiteInstance;
				} else {
					return this;
				}
			}
		}
		
		public String toString() {
			return "{"+children[0].toString() + ", " + children[1].toString() + 
					", " + children[2].toString() + ", " + children[3].toString()+"}";
		}
		
		//// Getters ////
		public double getXCoord() {
			return this.x;
		}
		public double getYCoord() {
			return this.y;
		}
		public QuadtreeNode[] getChildren() {
			return this.children;
		}
		

		// Need this?
		public QuadtreeNode add(City newCity) { return null; }
	}
	
}
