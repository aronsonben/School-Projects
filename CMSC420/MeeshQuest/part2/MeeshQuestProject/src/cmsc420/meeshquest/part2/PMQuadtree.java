package cmsc420.meeshquest.part2;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import cmsc420.geom.Circle2D;
import cmsc420.geom.Geometry2D;
import cmsc420.geom.Inclusive2DIntersectionVerifier;
import cmsc420.meeshquest.p1canonical.Canvas;
import cmsc420.meeshquest.p1canonical.City;
import cmsc420.meeshquest.p1canonical.CityAlreadyMappedException;
import cmsc420.meeshquest.p1canonical.CityOutOfBoundsException;
import cmsc420.meeshquest.p1canonical.prquadtree.EmptyNode;
import cmsc420.meeshquest.p1canonical.prquadtree.Node;

public class PMQuadtree {
	
	/* ***** Begin global variables ****** */
	
	/** Root Node for the PMQuadtree */
	private Node root;
	
	// Not sure if these are needed
	public int spatialWidth;
	public int spatialHeight;
	
	/** bounds of the spatial map */
	protected Point2D.Float spatialOrigin;
	
	/** Validates nodes based on which PM# implementation is being used */
	private final Validator validator;
	
	/** Object to check for intersections between Geometery2D objects */
	public static final Inclusive2DIntersectionVerifier verifier = new Inclusive2DIntersectionVerifier();
	
	/** A single instance of a WhiteNode, represents an empty leaf node */
	private final WhiteNode whiteSingleton = new WhiteNode();
	
	/** Keeps track of ALL (iso & non-iso) cityNames inside the PMQuadtree */
	private HashSet<String> cityNames = new HashSet<String>();
	
	/** Keep track of all <i>isolated</i> cities in PMQuadtree */
	private HashSet<String> isolatedCities = new HashSet<String>();
	
	/** Keep track of all Road objects that exist in PMQuadtree */
	private ArrayList<Road> allRoads = new ArrayList<Road>();
	
	/** Keep track of all Road's hashcodes, used for checking if roads with
	 * 		with same but swapped start and end points are equal. */
	 private TreeSet<Integer> allRoadsHash = new TreeSet<Integer>();
	
	/** Keeps track of all roads created and mapped in PMQuadtree */
	private HashSet<String> roadNames = new HashSet<String>();
	
	/** Adjacency list to map Cities to Set of Roads */
	private TreeMap<String, TreeSet<String>> adjList = new TreeMap<String, TreeSet<String>>();
	
	/* ***************************************************** */
	/* End global variable initialization */
	/* ***************************************************** */
	
	public PMQuadtree() {
		validator = null;
		root = whiteSingleton;
		
		spatialOrigin = new Point2D.Float(0, 0);
	}
	
	/** 
	 * Creates a new PM#Quadtree.
	 * Should only be used by PM1 or PM3 Quadtree subclasses that will pass in
	 * their respective Validators.
	 */
	public PMQuadtree(Validator validator) {
		this.validator = validator;
		root = whiteSingleton;
		
		spatialOrigin = new Point2D.Float(0, 0);
		
	}
	
	/**
	 * Sets the width and height of the spatial map.
	 * 
	 * @param spatialWidth
	 *            width of the spatial map
	 * @param spatialHeight
	 *            height of the spatial map
	 */
	public void setRange(int spatialWidth, int spatialHeight) {
		this.spatialWidth = spatialWidth;
		this.spatialHeight = spatialHeight;
	}

	public float getSpatialHeight() {
		return spatialHeight;
	}

	public float getSpatialWidth() {
		return spatialWidth;
	}

	public Node getRoot() {
		return root;
	}
	
	public boolean isEmpty() {
		return (root == whiteSingleton || root == null);
	}

	public boolean containsCity(String name) {
		return cityNames.contains(name);
	}
	
	/**
	 * Only called by a "mapCity" command. Adds an <tt>isolated city</tt> to the 
	 * PMQuadtree. Places its name in: cityNames, isolatedCities.
	 */
	public void addCity(City city) throws CityAlreadyMappedException, 
									CityOutOfBoundsException {
		if (cityNames.contains(city.getName())) {
			/* city already mapped */
			throw new CityAlreadyMappedException();
		}
		
		/* check bounds */
		int x = (int) city.getX();
		int y = (int) city.getY();
		if (x < spatialOrigin.x || x > spatialWidth || y < spatialOrigin.y
				|| y > spatialHeight) {
			/* city out of bounds */
			throw new CityOutOfBoundsException();
		}
		
		/* insert city into PMQuadTree */
		cityNames.add(city.getName());
		isolatedCities.add(city.getName());
		root = root.add(city, spatialOrigin, spatialWidth, spatialHeight);	
	}
	
	public void addRoad(Road road) throws RoadAlreadyMappedException, 
								RoadOutOfBoundsException {
		if(allRoadsHash.contains(road.hashCode())) {
			// Road already mapped
			throw new RoadAlreadyMappedException();
		}
		
		// out of bounds check
		Rectangle wholeGrid = new Rectangle(0, 0, spatialWidth, spatialHeight);
		if( !boundsChecker(road, wholeGrid) ) {
			// At least some part of the road is out of bounds
			throw new RoadOutOfBoundsException();
		}
		
		
		
		/* check new road doesn't intersect any others */
		/* *** See p2_extra.txt for this code*** */
		
		City startCity = road.getStartCity();
		City endCity = road.getEndCity();
		
		/* insert Road and Cities into PMQuadTree */
		if(!cityNames.contains(startCity.getName()) && 
				Inclusive2DIntersectionVerifier.intersects(startCity.toPoint2D(), wholeGrid)) {
			cityNames.add(startCity.getName());
			root = root.add(startCity, spatialOrigin, spatialWidth, spatialHeight);
		} 
		
		if(!cityNames.contains(endCity.getName()) && 
				Inclusive2DIntersectionVerifier.intersects(endCity.toPoint2D(), wholeGrid)) {
			cityNames.add(endCity.getName());
			root = root.add(endCity, spatialOrigin, spatialWidth, spatialHeight);
		}
		
		allRoads.add(road);
		allRoadsHash.add(road.hashCode());
		roadNames.add(road.getName());
		root = root.add(road, spatialOrigin, spatialWidth, spatialHeight);
		
		//System.out.println(root.toString());
	}
	
	/**
	 * Check to see if given Road is already mapped in the tree. 
	 * Ex: Road AB and Road BA are the same, but names would be different so 
	 * 		doing this check. 
	 * @return true -- when road is already mapped
	 */
	private boolean roadCheck(Road road) {
		
		return false;
	}
	
	/**
	 * Checks if the given coordinate pair lies within the grid. 
	 * @return false if it is out of bounds
	 */
	private boolean boundsChecker(Road road, Rectangle wholeGrid) {
		if(!Inclusive2DIntersectionVerifier.intersects(road, wholeGrid)) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Returns if any part of a circle lies within a given rectangular bounds
	 * according to the rules of the PR Quadtree.
	 * 
	 * @param circle
	 *            circular region to be checked
	 * @param rect
	 *            rectangular bounds the point is being checked against
	 * @return true if the point lies within the rectangular bounds, false
	 *         otherwise
	 */
	public boolean intersects(Circle2D circle, Rectangle2D rect) {
		final double radiusSquared = circle.getRadius() * circle.getRadius();

		/* translate coordinates, placing circle at origin */
		final Rectangle2D.Double r = new Rectangle2D.Double(rect.getX()
				- circle.getCenterX(), rect.getY() - circle.getCenterY(), rect
				.getWidth(), rect.getHeight());

		if (r.getMaxX() < 0) {
			/* rectangle to left of circle center */
			if (r.getMaxY() < 0) {
				/* rectangle in lower left corner */
				return ((r.getMaxX() * r.getMaxX() + r.getMaxY() * r.getMaxY()) < radiusSquared);
			} else if (r.getMinY() > 0) {
				/* rectangle in upper left corner */
				return ((r.getMaxX() * r.getMaxX() + r.getMinY() * r.getMinY()) < radiusSquared);
			} else {
				/* rectangle due west of circle */
				return (Math.abs(r.getMaxX()) < circle.getRadius());
			}
		} else if (r.getMinX() > 0) {
			/* rectangle to right of circle center */
			if (r.getMaxY() < 0) {
				/* rectangle in lower right corner */
				return ((r.getMinX() * r.getMinX() + r.getMaxY() * r.getMaxY()) < radiusSquared);
			} else if (r.getMinY() > 0) {
				/* rectangle in upper right corner */
				return ((r.getMinX() * r.getMinX() + r.getMinY() * r.getMinY()) <= radiusSquared);
			} else {
				/* rectangle due east of circle */
				return (r.getMinX() <= circle.getRadius());
			}
		} else {
			/* rectangle on circle vertical centerline */
			if (r.getMaxY() < 0) {
				/* rectangle due south of circle */
				return (Math.abs(r.getMaxY()) < circle.getRadius());
			} else if (r.getMinY() > 0) {
				/* rectangle due north of circle */
				return (r.getMinY() <= circle.getRadius());
			} else {
				/* rectangle contains circle center point */
				return true;
			}
		}
	}
	
	
	/* ******************************************************************** */
	/* ***** Node classes ***** */
	/* ******************************************************************** */
	
	public abstract class Node {
		/* Type flags used to idenfity nodes */
		public static final int WHITE = 0;
		public static final int BLACK = 1;
		public static final int GRAY  = 2;
		
		/** type of PM Quadtree node (either white, black, or gray) */
		protected final int type;
		
		public Node(final int type) {
			this.type = type;
		}
		
		public abstract Node add(Geometry2D g, Point2D.Float origin, int width, int height);
		public abstract Node remove(Geometry2D g, Point2D.Float origin, int width, int height);

		public int getType() {
			return type;
		}
	}
	
	public class WhiteNode extends Node {
		public WhiteNode() { 
			super(Node.WHITE);
		}
		
		@Override
		public Node add(Geometry2D g, Point2D.Float origin, int width, int height) {
			Node blackNode = new BlackNode();
			return blackNode.add(g, origin, width, height);
		}

		@Override
		public Node remove(Geometry2D g, Point2D.Float origin, int width, int height) {
			/* should never get here, nothing to remove */
			throw new IllegalArgumentException();
		}
		
		public String toString() {
			return "[WhiteNode]";
		}
	}

	public class BlackNode extends Node {
		/** The singular City object that can lie in a BlackNode */
		private City city = null;
		
		/** Geometry List inspired by spec pseudocode, TODO: need to figure out how to use it. */
		private ArrayList<Geometry2D> geometry = new ArrayList<Geometry2D>();
		
		/** List to hold 0 or more Road objects that exist in this BlackNode */
		private ArrayList<Road> roads = new ArrayList<Road>();
		private TreeMap<Road, Integer> roads2 = new TreeMap<Road, Integer>(new RoadComparator());
		
		/** Shows names of all roads in this BlackNode */
		private ArrayList<String> qEdgeNames = new ArrayList<String>();
		
		public BlackNode() { 
			super(Node.BLACK);
		}
		
		//add g to geometry
		//if(this node is valid) return this;
		//else return partition(...);
		@Override
		public Node add(Geometry2D g, Point2D.Float origin, int width, int height) {
			
			geometry.add(g);
			if(g.getType() == 0) {
				//System.out.println("Adding City "+g.toString()+" to "+this.toString());
				
				if(validator.valid(this)) {
					
					if(this.city == null)
						this.city = (City)g;
					
					return this;
				} else {
					GrayNode gray = new GrayNode(origin, width, height);
					gray.add(city, origin, width, height);
					//for(Road r : roads) {
					for(Road r : roads2.keySet()) {
						gray.add(r, origin, width, height);
					}
					gray.add(g, origin, width, height);
					return gray;
				}
				
			} else if(g.getType() == 1) {
				//System.out.println("Adding Road "+g.toString()+" to "+this.toString());
				
				// Add Road to BlackNode -- will need to do some intersection checking?
				Road r = (Road)g;
				roads.add(r);
				roads2.put(r, r.hashCode());
				qEdgeNames.add(r.getName());
				
				return this;
			} else {
				throw new InvalidParameterException("Invalid parameter exception in BlackNode");
			}
		}

		@Override
		public Node remove(Geometry2D g, Point2D.Float origin, int width, int height) {
			return null;
		}
		
		public City getCity() {
			return city;
		}
		
		/*public ArrayList<Road> getRoads() {
			return roads;
		}*/
		public TreeMap<Road, Integer> getRoads() {
			return roads2;
		}
		
		public String toString() {
			if(city == null && roads2.isEmpty()) {
				return "WhiteNode";
			} else if(city == null && !roads2.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				sb.append("[BlackNode: ");
				for(int i=0; i < qEdgeNames.size(); i++) {
					sb.append(qEdgeNames.get(i)+" ");
				}
				sb.append("]");
				return sb.toString();
			} else if(city != null && roads2.isEmpty()) {
				return "[BlackNode: "+city.getName()+"]";
			} else if(city != null && !roads2.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				sb.append("[BlackNode: City="+city.getName()+" Roads=(");
				for(int i=0; i < qEdgeNames.size(); i++) {
					sb.append(qEdgeNames.get(i)+" ");
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append(")]");
				return sb.toString();
			} else {
				return "Error in BlackNode.toString()";
			}
		}
	}

	public class GrayNode extends Node {
		/** children nodes of this node */
		public Node[] children;

		/** rectangular quadrants of the children nodes */
		protected Rectangle2D.Float[] regions;

		/** origin of the rectangular bounds of this node */
		public Point2D.Float origin;

		/** origins of the rectangular bounds of each child node */
		protected Point2D.Float[] origins;

		/** width of the rectangular bounds of this node */
		public int width;

		/** height of the rectangular bounds of this node */
		public int height;

		/** half of the width of the rectangular bounds of this node */
		protected int halfWidth;

		/** half of the height of the rectangular bounds of this node */
		protected int halfHeight;
		
		public GrayNode(Point2D.Float origin, int width, int height) {	
			super(Node.GRAY);
			
			this.origin = origin;
			
			children = new Node[4];
			for (int i = 0; i < 4; i++) {
				children[i] = whiteSingleton;
			}
			
			this.width = width;
			this.height = height;

			// This is same as dividing by 2
			halfWidth = width >> 1;
			halfHeight = height >> 1;

			origins = new Point2D.Float[4];
			origins[0] = new Point2D.Float(origin.x, origin.y + halfHeight);
			origins[1] = new Point2D.Float(origin.x + halfWidth, origin.y
					+ halfHeight);
			origins[2] = new Point2D.Float(origin.x, origin.y);
			origins[3] = new Point2D.Float(origin.x + halfWidth, origin.y);

			regions = new Rectangle2D.Float[4];
			int i = 0;
			while (i < 4) {
				regions[i] = new Rectangle2D.Float(origins[i].x, origins[i].y,
						halfWidth, halfHeight);
				i++;
			}
			
			/* add a cross to the drawing panel */
			if (Canvas.instance != null) {
	            //canvas.addCross(getCenterX(), getCenterY(), halfWidth, Color.d);
				int cx = getCenterX();
				int cy = getCenterY();
	            Canvas.instance.addLine(cx - halfWidth, cy, cx + halfWidth, cy, Color.GRAY);
	            Canvas.instance.addLine(cx, cy - halfHeight, cx, cy + halfHeight, Color.GRAY	);
			}
		}
		
		@Override
		public Node add(Geometry2D g, Point2D.Float origin, int width, int height) {
			//System.out.println("Adding "+g.toString()+" to GrayNode: "+this.toString());
			
			if(g.getType() == 0) {
				
				City c = (City)g;
				for(int i=0; i < 4; i++) {
					if(Inclusive2DIntersectionVerifier.intersects(c.toPoint2D(), regions[i])) {
						children[i] = children[i].add(g, origins[i], halfWidth, halfHeight);
					}
				}	
			} else if(g.getType() == 1) {
				Road r = (Road)g;
				
				for(int i=0; i < 4; i++) {
					if(Inclusive2DIntersectionVerifier.intersects(r, regions[i])) {	
						children[i] = children[i].add(g, origins[i], halfWidth, halfHeight);
					}
				}
			} else {
				// In case incorrect g.getType()
				throw new InvalidParameterException();
			}
			
			return this;
		}

		@Override
		public Node remove(Geometry2D g, Point2D.Float origin, int width, int height) {
			return null;
		}
		
		/**
		 * Gets the center X coordinate of this node's rectangular bounds.
		 * 
		 * @return center X coordinate of this node's rectangular bounds
		 */
		public int getCenterX() {
			return (int) origin.x + halfWidth;
		}

		/**
		 * Gets the center Y coordinate of this node's rectangular bounds.
		 * 
		 * @return center Y coordinate of this node's rectangular bounds
		 */
		public int getCenterY() {
			return (int) origin.y + halfHeight;
		}
		
		/**
		 * Gets the child node of this node according to which quadrant it falls
		 * in
		 * 
		 * @param quadrant
		 *            quadrant number (top left is 0, top right is 1, bottom
		 *            left is 2, bottom right is 3)
		 * @return child node
		 */
		public Node getChild(int quadrant) {
			if (quadrant < 0 || quadrant > 3) {
				throw new IllegalArgumentException();
			} else {
				return children[quadrant];
			}
		}
		
		/**
		 * Gets the rectangular region for the specified child node of this
		 * internal node.
		 * 
		 * @param quadrant
		 *            quadrant that child lies within
		 * @return rectangular region for this child node
		 */
		public Rectangle2D.Float getChildRegion(int quadrant) {
			if (quadrant < 0 || quadrant > 3) {
				throw new IllegalArgumentException();
			} else {
				return regions[quadrant];
			}
		}

		/**
		 * Gets the rectangular region contained by this internal node.
		 * 
		 * @return rectangular region contained by this internal node
		 */
		public Rectangle2D.Float getRegion() {
			return new Rectangle2D.Float(origin.x, origin.y, width, height);
		}
		
		/**
		 * Gets half the width of this internal node.
		 * @return half the width of this internal node
		 */
		public int getHalfWidth() {
			return halfWidth;
		}

		/** 
		 * Gets half the height of this internal node.
		 * @return half the height of this internal node
		 */
		public int getHalfHeight() {
			return halfHeight;
		}
		
		public String toString() {
			return "{"+children[0].toString() + ", " + children[1].toString() + 
					", " + children[2].toString() + ", " + children[3].toString()+"}";
		}
		
	}
}
