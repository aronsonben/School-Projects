/**
 * @(#)Command.java        1.1 
 * 
 * 2014/09/09
 *
 * @author Ruofei Du, Ben Zoller (University of Maryland, College Park), 2014
 * 
 * All rights reserved. Permission is granted for use and modification in CMSC420 
 * at the University of Maryland.
 */
package cmsc420.meeshquest.p1canonical;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;
import cmsc420.geom.Circle2D;
import cmsc420.geom.Shape2DDistanceCalculator;
import cmsc420.meeshquest.p1canonical.prquadtree.*;
import cmsc420.meeshquest.part2.PM1Quadtree;
import cmsc420.meeshquest.part2.PM3Quadtree;
/*
import cmsc420.exception.CityAlreadyMappedException;
import cmsc420.exception.CityOutOfBoundsException;
import cmsc420.meeshquest.p1canonical.City;
import cmsc420.structure.CityLocationComparator;
import cmsc420.structure.CityNameComparator;
import cmsc420.structure.prquadtree.InternalNode;
import cmsc420.structure.prquadtree.LeafNode;
import cmsc420.structure.prquadtree.Node;
import cmsc420.structure.prquadtree.PRQuadtree;
import cmsc420.utils.Canvas;
*/
import cmsc420.meeshquest.part2.PMQuadtree;
import cmsc420.sortedmap.AvlGTree;
import cmsc420.xml.XmlUtility;
import cmsc420.meeshquest.part2.PMQuadtree.BlackNode;
import cmsc420.meeshquest.part2.PMQuadtree.GrayNode;
import cmsc420.meeshquest.part2.Road;
import cmsc420.meeshquest.part2.RoadAlreadyMappedException;
import cmsc420.meeshquest.part2.RoadComparator;
import cmsc420.meeshquest.part2.RoadOutOfBoundsException;




/**
 * Processes each command in the MeeshQuest program. Takes in an XML command
 * node, processes the node, and outputs the results.
 * 
 * @author Ben Zoller
 * @version 2.0, 23 Jan 2007
 */
public class Command {
	/** output DOM Document tree */
	protected Document results;

	/** root node of results document */
	protected Element resultsNode;

	/**
	 * stores created cities sorted by their names (used with listCities command)
	 */
	protected final TreeMap<String, City> citiesByName = new TreeMap<String, City>(new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o2.compareTo(o1);
		}

	});

	/**
	 * stores created cities sorted by their locations (used with listCities command)
	 */
	protected final TreeSet<City> citiesByLocation = new TreeSet<City>(
			new CityLocationComparator());
	
	/** AvlGTree that sorts cities by their name (String) -- used for data dictionary*/
	protected AvlGTree<String, City> avlCitiesByName;
	protected AvlGTree<City, String> avlCitiesByLocation;

	protected final TreeMap<City, Integer> allMappedCitiesByName = new TreeMap<City, Integer>(new Comparator<City>() {

		@Override
		public int compare(City o1, City o2) {
				return o2.getName().compareTo(o1.getName());
			}
	});
	
	/**
	 * Store all mapped Roads in the same way mapped Cities are stored. Sorted in descending asciibetical. 
	 */
	protected final TreeSet<Road> allMappedRoadsByName = new TreeSet<Road>();
	
	/** Probably not the best coding style but needed a quick descending asciibetical Road Comparator
	 * and this came to mind. Might come back to make this nicer later.
	 */
	protected final Comparator<Road> roadComparator = new Comparator<Road>() {
		@Override
		public int compare(Road o1, Road o2) {
			return o2.getName().compareTo(o1.getName());
		}
	};
	
	/* Compare city names in descending asciibetical order */
	protected final CityNameComparator cityNameComp = new CityNameComparator();
	
	/** Store all cities added by the "mapCity" command that are then isolated cities */
	protected final TreeSet<String> isolatedCities = new TreeSet<String>(new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o2.compareTo(o1);
		}

	});

	/** Adjacency list to map Cities to Set of Cities */
	protected TreeMap<String, TreeSet<City>> adjList = new TreeMap<String, TreeSet<City>>();
	
	/** Adjacency list to map Cities to Set of Roads */
	protected TreeMap<String, TreeSet<Road>> adjList2 = new TreeMap<String, TreeSet<Road>>();
	
	/** stores mapped cities in a spatial data structure */
	protected final PRQuadtree prQuadtree = new PRQuadtree();

	/** stores mapped cities in a spatial data structure for part 2 
	 * Will be either a PM1 or PM3 Quadtree depending  on user input
	 */
	protected PMQuadtree pmQuadtree;
	
	
	/** spatial width and height of the PR/PM Quadtree */
	protected int spatialWidth, spatialHeight, g, pmOrder;

	/**
	 * Set the DOM Document tree to send the of processed commands to.
	 * 
	 * Creates the root results node.
	 * 
	 * @param results
	 *            DOM Document tree
	 */
	public void setResults(Document results) {
		this.results = results;
		resultsNode = results.createElement("results");
		results.appendChild(resultsNode);
	}

	/**
	 * Creates a command result element. Initializes the command name.
	 * 
	 * @param node
	 *            the command node to be processed
	 * @return the results node for the command
	 */
	private Element getCommandNode(final Element node) {
		final Element commandNode = results.createElement("command");
		commandNode.setAttribute("name", node.getNodeName());
		
		String id = node.getAttribute("id");
		if(node.hasAttribute("id"))
			commandNode.setAttribute("id", id);
		
		return commandNode;
	}

	/**
	 * Processes an integer attribute for a command. Appends the parameter to
	 * the parameters node of the results. Should not throw a number format
	 * exception if the attribute has been defined to be an integer in the
	 * schema and the XML has been validated beforehand.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            integer attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return integer attribute value
	 */
	private int processIntegerAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add the parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the integer value */
		return Integer.parseInt(value);
	}

	/**
	 * Processes a string attribute for a command. Appends the parameter to the
	 * parameters node of the results.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            string attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return string attribute value
	 */
	private String processStringAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the string value */
		return value;
	}

	/**
	 * Reports that the requested command could not be performed because of an
	 * error. Appends information about the error to the results.
	 * 
	 * @param type
	 *            type of error that occurred
	 * @param command
	 *            command node being processed
	 * @param parameters
	 *            parameters of command
	 */
	private void addErrorNode(final String type, final Element command,
			final Element parameters) {
		final Element error = results.createElement("error");
		error.setAttribute("type", type);
		error.appendChild(command);
		error.appendChild(parameters);
		resultsNode.appendChild(error);
	}

	/**
	 * Reports that a command was successfully performed. Appends the report to
	 * the results.
	 * 
	 * @param command
	 *            command not being processed
	 * @param parameters
	 *            parameters used by the command
	 * @param output
	 *            any details to be reported about the command processed
	 */
	private void addSuccessNode(final Element command,
			final Element parameters, final Element output) {
		final Element success = results.createElement("success");
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		resultsNode.appendChild(success);
	}
	
	private Element addSuccessNodeReturn(final Element command,
			final Element parameters, final Element output) {
		final Element success = results.createElement("success");
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		resultsNode.appendChild(success);
		return success;
	}

	/**
	 * Processes the commands node (root of all commands). Gets the spatial
	 * width and height of the map and send the data to the appropriate data
	 * structures.
	 * 
	 * @param node
	 *            commands node to be processed
	 */
	public void processCommands(final Element node) {
		spatialWidth 	= Integer.parseInt(node.getAttribute("spatialWidth"));
		spatialHeight 	= Integer.parseInt(node.getAttribute("spatialHeight"));
		g 				= Integer.parseInt(node.getAttribute("g"));
		pmOrder			= Integer.parseInt(node.getAttribute("pmOrder"));
		
		/* initialize canvas */
		Canvas.instance.setFrameSize(spatialWidth, spatialHeight);
		/* add a rectangle to show where the bounds of the map are located */
		Canvas.instance.addRectangle(0, 0, (spatialWidth > spatialHeight) ? spatialWidth : spatialHeight, 
				(spatialWidth > spatialHeight) ? spatialWidth : spatialHeight, Color.WHITE, true);
		Canvas.instance.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK,
				false);

		/* set PR Quadtree range */
		prQuadtree.setRange(spatialWidth, spatialHeight);
		
		if(pmOrder == 1)
			pmQuadtree = new PM1Quadtree();
		else if(pmOrder == 3)
			pmQuadtree = new PM3Quadtree();
		else
			throw new RuntimeException("Incorrect pmOrder. PMQuadtree not created.");
		
		pmQuadtree.setRange(spatialWidth, spatialHeight);
		avlCitiesByName = new AvlGTree<String, City>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}

		}, g);
		avlCitiesByLocation = new AvlGTree<City, String>(new CityLocationComparator(), g);
		
	}

	/**
	 * Processes a createCity command. Creates a city in the dictionary (Note:
	 * does not map the city). An error occurs if a city with that name or
	 * location is already in the dictionary.
	 * 
	 * @param node
	 *            createCity node to be processed
	 */
	public void processCreateCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);
		final String color = processStringAttribute(node, "color",
				parametersNode);

		/* create the city */
		final City city = new City(name, x, y, radius, color);

		if (citiesByLocation.contains(city)) {
			addErrorNode("duplicateCityCoordinates", commandNode,
					parametersNode);
		} else if (citiesByName.containsKey(name)) {
			addErrorNode("duplicateCityName", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");

			/* add city to dictionary */
			citiesByName.put(name, city);
			citiesByLocation.add(city);
			avlCitiesByName.put(name, city);
			avlCitiesByLocation.put(city, name);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Processes a deleteCity command. Deletes a city from the dictionary. An
	 * error occurs if the city does not exist or is currently mapped.
	 * 
	 * @param node
	 *            deleteCity node being processed
	 */
	public void processDeleteCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String name = processStringAttribute(node, "name", parametersNode);

		if (!citiesByName.containsKey(name)) {
			/* city with name does not exist */
			addErrorNode("cityDoesNotExist", commandNode, parametersNode);
		} else {
			/* delete city */
			final Element outputNode = results.createElement("output");
			final City deletedCity = citiesByName.get(name);

			if (prQuadtree.contains(name)) {
				/* city is mapped */
				prQuadtree.remove(deletedCity);
				addCityNode(outputNode, "cityUnmapped", deletedCity);
			}

			citiesByName.remove(name);
			citiesByLocation.remove(deletedCity);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Clears all the data structures do there are not cities or roads in
	 * existence in the dictionary or on the map.
	 * 
	 * @param node
	 *            clearAll node to be processed
	 */
	public void processClearAll(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* clear data structures */
		citiesByName.clear();
		citiesByLocation.clear();
		allMappedCitiesByName.clear();
		prQuadtree.clear();
		
		if(pmOrder == 1)
			pmQuadtree = new PM1Quadtree();
		else if(pmOrder == 3)
			pmQuadtree = new PM3Quadtree();
		else
			throw new RuntimeException("Incorrect pmOrder. PMQuadtree not created.");
		
		pmQuadtree.setRange(spatialWidth, spatialHeight);
		
		avlCitiesByName = new AvlGTree<String, City>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}

		}, g);
		avlCitiesByLocation = new AvlGTree<City, String>(new CityLocationComparator(), g);

		/* clear canvas */
		Canvas.instance.clear();
		/* add a rectangle to show where the bounds of the map are located */
		Canvas.instance.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK,
				false);

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Lists all the cities, either by name or by location.
	 * 
	 * @param node
	 *            listCities node to be processed
	 */
	public void processListCities(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String sortBy = processStringAttribute(node, "sortBy",
				parametersNode);

		if (citiesByName.isEmpty()) {
			addErrorNode("noCitiesToList", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");
			final Element cityListNode = results.createElement("cityList");

			Collection<City> cityCollection = null;
			if (sortBy.equals("name")) {
				cityCollection = citiesByName.values();
			} else if (sortBy.equals("coordinate")) {
				cityCollection = citiesByLocation;
			} else {
				/* XML validator failed */
				System.exit(-1);
			}

			for (City c : cityCollection) {
				if(isolatedCities.contains(c.getName())) {
					addIsoCityNode(cityListNode, c);
				} else {
					addCityNode(cityListNode, c); 
				}
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param cityNodeName
	 *            name of city node
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final String cityNodeName,
			final City city) {
		final Element cityNode = results.createElement(cityNodeName);
		cityNode.setAttribute("name", city.getName());
		cityNode.setAttribute("x", Integer.toString((int) city.getX()));
		cityNode.setAttribute("y", Integer.toString((int) city.getY()));
		cityNode.setAttribute("radius", Integer
				.toString((int) city.getRadius()));
		cityNode.setAttribute("color", city.getColor());
		node.appendChild(cityNode);
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final City city) {
		addCityNode(node, "city", city);
	}
	
	private void addIsoCityNode(final Element node, final City city) {
		addCityNode(node, "isolatedCity", city);
	}

	/**
	 * Maps a city to the spatial map.
	 * 
	 * @param node
	 *            mapCity command node to be processed
	 */
	public void processMapCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(name)) {
			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
		} else if (prQuadtree.contains(name) || pmQuadtree.containsCity(name)) {
			addErrorNode("cityAlreadyMapped", commandNode, parametersNode);
		} else {
			City city = citiesByName.get(name);
			try {
				/* insert city into PM Quadtree */
				//prQuadtree.add(city);
				pmQuadtree.addCity(city);
				allMappedCitiesByName.put(city, city.getRadius());
				isolatedCities.add(city.getName());
				
				if(!adjList2.containsKey(city.getName())) {
					adjList2.put(city.getName(), null);
				}
				
				if(!adjList.containsKey(city.getName())) {
					adjList.put(city.getName(), null);
				}
				
				/* add city to canvas */
				Canvas.instance.addPoint(city.getName(), city.getX(), city.getY(),
						Color.BLACK);

				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			} catch (CityAlreadyMappedException e) {
				addErrorNode("cityAlreadyMapped", commandNode, parametersNode);
			} catch (CityOutOfBoundsException e) {
				addErrorNode("cityOutOfBounds", commandNode, parametersNode);
			}
		}
	}

	/**
	 * Removes a city from the spatial map.
	 * 
	 * @param node
	 *            unmapCity command node to be processed
	 */
	public void processUnmapCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(name)) {
			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
		} else if (!prQuadtree.contains(name)) {
			addErrorNode("cityNotMapped", commandNode, parametersNode);
		} else {
			City city = citiesByName.get(name);

			/* unmap the city in the PR Quadtree */
			prQuadtree.remove(city);

			/* remove city from canvas */
			Canvas.instance.removePoint(city.getName(), city.getX(), city.getY(),
					Color.BLACK);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Processes a saveMap command. Saves the graphical map to a given file.
	 * 
	 * @param node
	 *            saveMap command to be processed
	 * @throws IOException
	 *             problem accessing the image file
	 */
	public void processSaveMap(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		/* save canvas to '<name>.png' */
		Canvas.instance.save(name);

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Prints out the structure of the PR Quadtree in a human-readable format.
	 * 
	 * @param node
	 *            printPRQuadtree command to be processed
	 */
	public void processPrintPRQuadtree(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		if (prQuadtree.isEmpty()) {
			/* empty PR Quadtree */
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {
			/* print PR Quadtree */
			final Element quadtreeNode = results.createElement("quadtree");
			printPRQuadtreeHelper(prQuadtree.getRoot(), quadtreeNode);

			outputNode.appendChild(quadtreeNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Traverses each node of the PR Quadtree.
	 * 
	 * @param currentNode
	 *            PR Quadtree node being printed
	 * @param xmlNode
	 *            XML node representing the current PR Quadtree node
	 */
	private void printPRQuadtreeHelper(final Node currentNode,
			final Element xmlNode) {
		if (currentNode.getType() == Node.EMPTY) {
			Element white = results.createElement("white");
			xmlNode.appendChild(white);
		} else {
			if (currentNode.getType() == Node.LEAF) {
				/* leaf node */
				final LeafNode currentLeaf = (LeafNode) currentNode;
				final Element black = results.createElement("black");
				black.setAttribute("name", currentLeaf.getCity().getName());
				black.setAttribute("x", Integer.toString((int) currentLeaf
						.getCity().getX()));
				black.setAttribute("y", Integer.toString((int) currentLeaf
						.getCity().getY()));
				xmlNode.appendChild(black);
			} else {
				/* internal node */
				final InternalNode currentInternal = (InternalNode) currentNode;
				final Element gray = results.createElement("gray");
				gray.setAttribute("x", Integer.toString((int) currentInternal
						.getCenterX()));
				gray.setAttribute("y", Integer.toString((int) currentInternal
						.getCenterY()));
				for (int i = 0; i < 4; i++) {
					printPRQuadtreeHelper(currentInternal.getChild(i), gray);
				}
				xmlNode.appendChild(gray);
			}
		}
	}

	/**
	 * Finds the mapped cities within the range of a given point.
	 * 
	 * @param node
	 *            rangeCities command to be processed
	 * @throws IOException
	 */
	public void processRangeCities(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final TreeSet<City> citiesInRange = new TreeSet<City>(
				new CityNameComparator());

		/* extract values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}
		/* get cities within range */
		final Point2D.Double point = new Point2D.Double(x, y);
		rangeCitiesHelper(point, radius, pmQuadtree.getRoot(), citiesInRange);

		/* print out cities within range */
		if (citiesInRange.isEmpty()) {
			addErrorNode("noCitiesExistInRange", commandNode, parametersNode);
		} else {
			/* get city list */
			final Element cityListNode = results.createElement("cityList");
			for (City city : citiesInRange) {
				addCityNode(cityListNode, city);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);

			if (pathFile.compareTo("") != 0) {
				/* save canvas to file with range circle */
				if(radius != 0) {
					Canvas.instance.addCircle(x, y, radius, Color.BLUE, false);
				}
				Canvas.instance.save(pathFile);
				if(radius != 0) {
					Canvas.instance.removeCircle(x, y, radius, Color.BLUE, false);
				}
			}
		}
	}

	/**
	 * Determines if any cities within the PR Quadtree not are within the radius
	 * of a given point.
	 * 
	 * @param point
	 *            point from which the cities are measured
	 * @param radius
	 *            radius from which the given points are measured
	 * @param node
	 *            PR Quadtree node being examined
	 * @param citiesInRange
	 *            a list of cities found to be in range
	 */
	private void rangeCitiesHelper(final Point2D.Double point,
			final int radius, final PMQuadtree.Node node, final TreeSet<City> citiesInRange) {
		if (node.getType() == PMQuadtree.Node.BLACK) {
			final BlackNode leaf = (BlackNode) node;
			if(leaf.getCity() != null) {
				final double distance = point.distance(leaf.getCity().toPoint2D());
				if (distance <= radius) {
					/* city is in range */
					final City city = leaf.getCity();
					citiesInRange.add(city);
				}
			}
		} else if (node.getType() == PMQuadtree.Node.GRAY) {
			/* check each quadrant of internal node */
			final GrayNode internal = (GrayNode) node;

			final Circle2D.Double circle = new Circle2D.Double(point, radius);
			for (int i = 0; i < 4; i++) {
				/*if(Inclusive2DIntersectionVerifier.intersects(internal.getChildRegion(i), circle)) {
					rangeCitiesHelper(point, radius, internal.getChild(i),
							citiesInRange);
				}*/
				if(pmQuadtree.intersects(circle, internal.getChildRegion(i))) {
					rangeCitiesHelper(point, radius, internal.getChild(i),
							citiesInRange);
				}
			}
		}
	}

	/**
	 * Finds the nearest city to a given point.
	 * 
	 * @param node
	 *            nearestCity command being processed
	 */
	public void processNearestCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		final Point2D.Float point = new Point2D.Float(x, y);

		if (citiesByName.size() <= 0) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
			return;
		}

		//final PriorityQueue<NearestCity> nearCities = new PriorityQueue<NearestCity>(
		//		citiesByName.size());

		if (pmQuadtree.getRoot().getType() == PMQuadtree.Node.WHITE) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else {

			//
			//nearCities.add(new NearestCity(null, Double.POSITIVE_INFINITY));
			//

			//nearestCityHelper(prQuadtree.getRoot(), point, nearCities);
			//NearestCity nearestCity = nearCities.remove();
			City n = nearestCityHelper2(pmQuadtree.getRoot(), point);
			//addCityNode(outputNode, nearestCity.getCity());
			
			/* might return null if only isolatedCities exist */
			if(n == null) {
				addErrorNode("cityNotFound", commandNode, parametersNode);
			} else {
				addCityNode(outputNode, n);
			
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	}

	/**
	 * 2/25/2011
	 * @param root
	 * @param point
	 */
	private City nearestCityHelper2(PMQuadtree.Node root, Point2D.Float point) {
		PriorityQueue<QuadrantDistance> q = new PriorityQueue<QuadrantDistance>();
		PMQuadtree.Node currNode = root;
		City currCity = null;
		
		while (currNode.getType() != PMQuadtree.Node.BLACK) {
			GrayNode g = (GrayNode) currNode;
			
			for (int i = 0; i < 4; i++) {
				PMQuadtree.Node kid = g.children[i];
				
				if (kid.getType() == PMQuadtree.Node.BLACK) {
					currCity = ((BlackNode)kid).getCity();
					if( currCity != null && !isolatedCities.contains(currCity.getName()) ) {
						q.add(new QuadrantDistance(kid, point));
					}
				} else if(kid.getType() == PMQuadtree.Node.GRAY) {
					q.add(new QuadrantDistance(kid, point));
				} else {
					// WhiteNode, do nothing
				}
			}
			currNode = q.remove().pmQuadNode;
		}
		
		currCity = ((BlackNode) currNode).getCity();
		if(isolatedCities.contains(currCity.getName())) {
			return null;
		} else {
			return currCity;
		}
	}

	
	public void processPrintAvlTree(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		if (avlCitiesByName.isEmpty()) {
			/* empty AVL G Tree */
			addErrorNode("emptyTree", commandNode, parametersNode);
		} else {
			/* print AVL G Tree  */
			final Element treeNode = results.createElement("AvlGTree");
			treeNode.setAttribute("cardinality", String.valueOf(avlCitiesByName.size()));
			treeNode.setAttribute("height", String.valueOf(avlCitiesByName.getRoot().getHeight()));
			treeNode.setAttribute("maxImbalance", String.valueOf(g));
			
			printAvlTreeHelper(avlCitiesByName.getRoot(), treeNode);

			outputNode.appendChild(treeNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}
	
	private void printAvlTreeHelper(final cmsc420.sortedmap.Node<String, City> node, 
									final Element xmlNode) {
		if (node != null) {
	         //System.out.print(node.getKey() + " ");
	         Element nodeEle = results.createElement("node");
	         nodeEle.setAttribute("key", node.getKey());
	         nodeEle.setAttribute("value", node.getValue().getLocationString());
	         printAvlTreeHelper(node.getLeft(), nodeEle);
	         printAvlTreeHelper(node.getRight(), nodeEle);
	         xmlNode.appendChild(nodeEle);
	     } else {
	    	 Element empty = results.createElement("emptyChild");
	    	 xmlNode.appendChild(empty);
	     }
	}
	
	/** 
	 * Maps a Road to the spatial map
	 * 
	 */
	public void processMapRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);
		
		final Element outputNode = results.createElement("output");
		
		if (!citiesByName.containsKey(start)) {
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
		} else if(!citiesByName.containsKey(end)) {
			addErrorNode("endPointDoesNotExist", commandNode, parametersNode);
		} else if(start.equals(end)) {
			addErrorNode("startEqualsEnd", commandNode, parametersNode);
		} else if(isolatedCities.contains(start) || isolatedCities.contains(end)) {
			addErrorNode("startOrEndIsIsolated", commandNode, parametersNode);
		} else {
			City startCity = citiesByName.get(start);
			City endCity = citiesByName.get(end);
			Road road = null; 
			if(cityNameComp.compare(endCity, startCity) > 0) {
				road = new Road(endCity, startCity, true);
			} else {
				road = new Road(startCity, endCity);
			}
			
			
			try {
				
				pmQuadtree.addRoad(road);
				allMappedCitiesByName.put(startCity, startCity.getRadius());
				allMappedCitiesByName.put(endCity, endCity.getRadius());
				allMappedRoadsByName.add(road);
				
				/* adding to adjacency list */
				if(!adjList2.containsKey(startCity.getName())) {
					TreeSet<Road> tempSet = new TreeSet<Road>();
					// Note: this could open up some issues, using the Road name
					tempSet.add(road);
					adjList2.put(startCity.getName(), tempSet);
				} else {
					// add road to existing key in adjList
					adjList2.get(startCity.getName()).add(road);
				}
				
				if(!adjList2.containsKey(endCity.getName())) {
					TreeSet<Road> tempSet = new TreeSet<Road>();
					// Note: this could open up some issues, using the Road name
					tempSet.add(road);
					adjList2.put(endCity.getName(), tempSet);
				} else {
					// add road to existing key in adjList
					adjList2.get(endCity.getName()).add(road);
				}
				
				// doing it with new adjList (delete above code after)
				if(!adjList.containsKey(startCity.getName())) {
					TreeSet<City> tempSet = new TreeSet<City>(new CityNameComparator());
					// Note: this could open up some issues, using the Road name
					tempSet.add(endCity);
					adjList.put(startCity.getName(), tempSet);
				} else {
					// add road to existing key in adjList
					adjList.get(startCity.getName()).add(endCity);
				}
				
				if(!adjList.containsKey(endCity.getName())) {
					TreeSet<City> tempSet = new TreeSet<City>(new CityNameComparator());
					// Note: this could open up some issues, using the Road name
					tempSet.add(startCity);
					adjList.put(endCity.getName(), tempSet);
				} else {
					// add road to existing key in adjList
					adjList.get(endCity.getName()).add(startCity);
				}
				
				final Element roadCreatedNode = results.createElement("roadCreated");
				roadCreatedNode.setAttribute("start", startCity.getName());
				roadCreatedNode.setAttribute("end", endCity.getName());
				
				
				outputNode.appendChild(roadCreatedNode);
				
				
				/* add city to canvas */	
				Canvas.instance.addPoint(startCity.getName(), startCity.getX(), startCity.getY(),
						Color.BLACK);
				Canvas.instance.addPoint(endCity.getName(), endCity.getX(), endCity.getY(),
						Color.BLACK);
				Canvas.instance.addLine(road.getX1(), road.getY1(), road.getX2(), road.getY2(), 
							Color.BLACK);
				
				
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			} catch (RoadAlreadyMappedException e) {
				addErrorNode("roadAlreadyMapped", commandNode, parametersNode);
			} catch (RoadOutOfBoundsException e) {
				addErrorNode("roadOutOfBounds", commandNode, parametersNode);
			} catch (InvalidParameterException e) {
				// Don't output anything just catch this error
			}
		}
	}
	
	/**
	 * Finds the mapped roads within the range of a given point.
	 * 
	 * @param node
	 *            rangeRoads command to be processed
	 * @throws IOException
	 */
	public void processRangeRoads(Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final TreeSet<Road> roadsInRange = new TreeSet<Road>(new RoadComparator());

		/* extract values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}
		
		/* get roads within range */
		final Point2D.Double point = new Point2D.Double(x, y);
		rangeRoadsHelper(point, radius, pmQuadtree.getRoot(), roadsInRange);

		/* print out roads within range */
		if (roadsInRange.isEmpty()) {
			addErrorNode("noRoadsExistInRange", commandNode, parametersNode);
		} else {
			/* get city list */
			final Element cityListNode = results.createElement("roadList");
			for (Road road : roadsInRange) {
				addRoadNodes(cityListNode, road);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);

			if (pathFile.compareTo("") != 0) {
				/* save canvas to file with range circle */
				if(radius != 0) {
					Canvas.instance.addCircle(x, y, radius, Color.BLUE, false);
				}
				Canvas.instance.save(pathFile);
				if(radius != 0) {
					Canvas.instance.removeCircle(x, y, radius, Color.BLUE, false);
				}
			}
		}
	}
	
	/**
	 * Determines if any roads within the PM Quadtree not are within the radius
	 * of a given point.
	 * 
	 * @param point
	 *            point from which the cities are measured
	 * @param radius
	 *            radius from which the given points are measured
	 * @param node
	 *            PR Quadtree node being examined
	 * @param citiesInRange
	 *            a list of cities found to be in range
	 */
	private void rangeRoadsHelper(final Point2D.Double point,
			final int radius, final PMQuadtree.Node node, final TreeSet<Road> roadsInRange) {
		final Circle2D.Double circle = new Circle2D.Double(point, radius);
		if (node.getType() == Node.LEAF) {
			final BlackNode leaf = (BlackNode) node;
			
			/*final ArrayList<Road> nodeRoads = leaf.getRoads();
			for(Road road : nodeRoads) {
				if(road.ptSegDist(point) <= radius) {
					roadsInRange.add(road);
				}
			}*/
			final TreeMap<Road, Integer> nodeRoads = leaf.getRoads();
			for(Road road : nodeRoads.keySet()) {
				if(road.ptSegDist(point) <= radius) {
					roadsInRange.add(road);
				}
			}
		} else if (node.getType() == Node.INTERNAL) {
			/* check each quadrant of internal node */
			final GrayNode internal = (GrayNode) node;

			//final Circle2D.Double circle = new Circle2D.Double(point, radius);
			for (int i = 0; i < 4; i++) {
				/*if (Inclusive2DIntersectionVerifier.intersects(internal.getChildRegion(i), circle)) {
					rangeRoadsHelper(point, radius, internal.getChild(i),
							roadsInRange);
				}*/
				if(pmQuadtree.intersects(circle, internal.getChildRegion(i))) {
					rangeRoadsHelper(point, radius, internal.getChild(i),
							roadsInRange);
				}
			}
		}
	}
	
	/**
	 * Finds the nearest <b>isolated</b> city to a given point.
	 * 
	 * @param node
	 *            nearestIsolatedCity command being processed
	 */
	public void processNearestIsolatedCity(Element node) {

		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		final Point2D.Float point = new Point2D.Float(x, y);

		if (isolatedCities.size() <= 0) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
			return;
		}

		//final PriorityQueue<NearestCity> nearCities = new PriorityQueue<NearestCity>(
		//		citiesByName.size());

		if (pmQuadtree.getRoot().getType() == PMQuadtree.Node.WHITE) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {

			//
			//nearCities.add(new NearestCity(null, Double.POSITIVE_INFINITY));
			//

			//nearestCityHelper(prQuadtree.getRoot(), point, nearCities);
			//NearestCity nearestCity = nearCities.remove();
			City n = nearestIsoCityHelper(pmQuadtree.getRoot(), point);
			
			if(n == null) {
				addErrorNode("cityNotFound", commandNode, parametersNode);
			} else {
				//addCityNode(outputNode, nearestCity.getCity());
				if(isolatedCities.contains(n.getName())) {
					addIsoCityNode(outputNode, n);
				}else {
					addCityNode(outputNode, n);
				}
	
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	
	}
	
	private City nearestIsoCityHelper(PMQuadtree.Node root, Point2D.Float point) {
		PriorityQueue<QuadrantDistance> q = new PriorityQueue<QuadrantDistance>();
		PMQuadtree.Node currNode = root;
		while (currNode.getType() != PMQuadtree.Node.BLACK) {
			GrayNode g = (GrayNode) currNode;
			for (int i = 0; i < 4; i++) {
				PMQuadtree.Node kid = g.children[i];
				if (kid.getType() == PMQuadtree.Node.BLACK) {
					BlackNode blackNode = (BlackNode) kid;
					if(blackNode.getCity() != null && isolatedCities.contains(blackNode.getCity().getName())) {
						//System.out.println("Adding isolated city: "+blackNode.getCity());
						
						q.add(new QuadrantDistance(kid, point));
					}
				} else if(kid.getType() == PMQuadtree.Node.GRAY) {
					q.add(new QuadrantDistance(kid, point));
				} else {
					// nothing
				}
			}
			currNode = q.remove().pmQuadNode;
		}

		return ((BlackNode) currNode).getCity();
	}
	
	
	/**
	 * Finds the nearest road to a given point.
	 * 
	 * @param node
	 *            nearestIsolatedCity command being processed
	 */
	public void processNearestRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		final Point2D.Float point = new Point2D.Float(x, y);

		if (allMappedRoadsByName.size() <= 0) {
			addErrorNode("roadNotFound", commandNode, parametersNode);
			return;
		}

		if (pmQuadtree.getRoot().getType() == PMQuadtree.Node.WHITE) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {
			Road n = nearestRoadHelper(pmQuadtree.getRoot(), point);
			
			if(n == null) {
				addErrorNode("roadNotFound", commandNode, parametersNode);
			} else {
				addRoadNodes(outputNode, n);
	
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	}
	
	/** 
	 * Helper method for finding the nearest road to a given point
	 */
	private Road nearestRoadHelper(PMQuadtree.Node root, Point2D.Float point) {
		double distance = -1;
		double tempDist = -1;
		TreeSet<Road> sameDistRoads = new TreeSet<Road>(new RoadComparator());
		
		for(Road r : allMappedRoadsByName) {
			//System.out.println("Checking road: "+r.toString());
			tempDist = r.ptSegDist(point);
			
			/* handling the first case */
			if(distance < 0) {
				distance = tempDist;
			}
			
			if(tempDist < distance) {
				sameDistRoads.clear();
				sameDistRoads.add(r);
				distance = tempDist;
			} else if(tempDist == distance) {
				sameDistRoads.add(r);
			} else {
				// nothing
			}
		}
		
		return sameDistRoads.first();
	}
	
	
	public void processNearestCityToRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);

		if (allMappedRoadsByName.size() <= 0) {
			addErrorNode("roadIsNotMapped", commandNode, parametersNode);
			return;
		}
		
		if (pmQuadtree.getRoot().getType() == PMQuadtree.Node.WHITE) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {
			
			final Road newRoad = new Road(citiesByName.get(start), citiesByName.get(end));
			final Road newRoad2 = new Road(citiesByName.get(end), citiesByName.get(start), true);
			Road n = findRoadHelper(newRoad);
			
			// Check again in case start and end were switched
			if(n == null)
				n = findRoadHelper(newRoad2);
			
			if(n == null) {
				addErrorNode("roadIsNotMapped", commandNode, parametersNode);
			} else {
				
				City c = findNearestCityToRoadHelper(n);
				if(c == null) {
					addErrorNode("noOtherCitiesMapped", commandNode, parametersNode);
				} else {
					//System.out.println(c.toString());
					addCityNode(outputNode, c);
					
					// add success node to results 
					addSuccessNode(commandNode, parametersNode, outputNode);
				}
			}	
		}
	}
	
	/** 
	 * Helper method for finding the nearest road to a given point
	 */
	private City findNearestCityToRoadHelper(Road road) {
		City currCity = null;
		double distance = -1;
		double tempDist;
		TreeSet<City> sameDistCities = new TreeSet<City>(new CityNameComparator());
		
		for(City c : allMappedCitiesByName.keySet()) {
			if(!c.getName().equals(road.getStartCity().getName()) && 
					!c.getName().equals(road.getEndCity().getName())) {
				
				tempDist = road.ptSegDist(c.toPoint2D());
				//System.out.println("Checking: "+c.toString()+". Distance="+tempDist);
				if(distance < 0) {
					distance = tempDist;
					currCity = c;
					sameDistCities.add(c);
				} else {
					if(tempDist < distance) {
						distance = tempDist;
						currCity = c;
						sameDistCities.clear();
						sameDistCities.add(c);
					} else if(tempDist == distance) {
						sameDistCities.add(c);
					} else {
						// nothing
					}
				}
			} 
		}
		if(sameDistCities.isEmpty()) {
			return null;
		} else {
			return sameDistCities.first();
		}
	}
	
	/** 
	 * Find the given Road in allMappedRoadsByName
	 */
	private Road findRoadHelper(Road newRoad) {
		Road givenRoad = null;
		if(!allMappedRoadsByName.contains(newRoad)) {
		} else {
			for(Road r : allMappedRoadsByName) {
				if(r.equals(newRoad)) {
					givenRoad = r;
				}
			}
		}
		return givenRoad;
	}
	
	public void processShortestPath(Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		
		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);
		
		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}
		
		String pathFile2 = "";
		if (node.getAttribute("saveHTML").compareTo("") != 0) {
			pathFile2 = processStringAttribute(node, "saveHTML", parametersNode);
		}
		
		if(!citiesByName.containsKey(start) || !pmQuadtree.containsCity(start)) {
			addErrorNode("nonExistentStart", commandNode, parametersNode);
		} else if(!citiesByName.containsKey(end) || !pmQuadtree.containsCity(end)) {
			addErrorNode("nonExistentEnd", commandNode, parametersNode);
		} else {
			final Element pathEle = results.createElement("path");
			
			ArrayList<String> path = processShortestPathHelper(pathEle, start, end);
			
			if(path == null || !path.contains(end)) {
				addErrorNode("noPathExists", commandNode, parametersNode);
			} else {
				
				Collections.reverse(path);
				
				ArrayList<Road> roadPath = new ArrayList<Road>();
				for(int i=0; i < path.size()-1; i++) {
					String from = path.get(i);
					String to = path.get(i+1);
					Road r = new Road(citiesByName.get(from), citiesByName.get(to));
					roadPath.add(r);
					
				}
				
				addDirections(pathEle, roadPath);
				
				outputNode.appendChild(pathEle);
				
				Element successNode = addSuccessNodeReturn(commandNode, parametersNode, outputNode);
				
				/* save map and/or HTML */
				if (pathFile.compareTo("") != 0) {
					
					/* save canvas to file. Will need to make new CanvasPlus and draw path */
					CanvasPlus canvas = new CanvasPlus(pathFile);
					/* initialize canvas */
					canvas.setFrameSize(spatialWidth, spatialHeight);
					/* add a rectangle to show where the bounds of the map are located */
					canvas.addRectangle(0, 0, (spatialWidth > spatialHeight) ? spatialWidth : spatialHeight, 
							(spatialWidth > spatialHeight) ? spatialWidth : spatialHeight, Color.WHITE, true);
					canvas.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK,
							false);
					
					/* add end point */
					canvas.addPoint(end, citiesByName.get(end).getX(), citiesByName.get(end).getY(), Color.RED);
					
					for(int i=0; i < roadPath.size(); i++) {
						String from = roadPath.get(i).getStartCity().getName();
						String to = roadPath.get(i).getEndCity().getName();
						canvas.addPoint(from, citiesByName.get(from).getX(), 
								citiesByName.get(from).getY(), Color.BLUE);
						canvas.addLine(citiesByName.get(from).getX(), citiesByName.get(from).getY(), 
								citiesByName.get(to).getX(), citiesByName.get(to).getY(), Color.BLUE);
					}
					
					/* add start point */
					canvas.removePoint(start, citiesByName.get(start).getX(), 
									citiesByName.get(start).getY(), Color.BLUE);
					canvas.addPoint(start, citiesByName.get(start).getX(), 
									citiesByName.get(start).getY(), Color.GREEN);
					
					
					canvas.save(pathFile);
					canvas.dispose();
				}
				if (pathFile2.compareTo("") != 0) {
					/* save HTML. read specs for more */
					try {
						org.w3c.dom.Document shortestPathDoc = XmlUtility.getDocumentBuilder().newDocument();
						org.w3c.dom.Node spNode = shortestPathDoc.importNode(successNode, true);
						shortestPathDoc.appendChild(spNode);
						XmlUtility.transform(shortestPathDoc, new File("shortestPath.xsl"), new File(pathFile2 + ".html"));
					} catch(TransformerException e) {
						// nothing
					} catch (ParserConfigurationException e) {
						// nothing
					} 
				}
				
			}
		}
		
	}
	
	private ArrayList<String> processShortestPathHelper(final Element pathEle, 
							final String startCity, final String endCity) {
		TreeMap<String, Float> dists2 = new TreeMap<String, Float>();
		TreeSet<String> visited = new TreeSet<String>();
		PriorityQueue<DijkstraNode> pq = new PriorityQueue<DijkstraNode>(new DijkstraComparator());
		
		TreeMap<String, String> path2 = new TreeMap<String, String>();
		ArrayList<String> finalPath = new ArrayList<String>();
		float finalDist = 0;
		
		// initialize the distances
		for(String s : adjList.keySet()) {
			dists2.put(s, Float.MAX_VALUE);
		}
		dists2.put(startCity, new Float(0));
		
		// add source vertex to priority queue initially
		DijkstraNode src = new DijkstraNode(startCity, 0);
		pq.add(src);
		path2.put(startCity, null);
		
		// cycle until the queue is empty
		while(!pq.isEmpty()) {
			
			// pull vertext with the smallest distance from the queue
			DijkstraNode curr = pq.poll();
			//System.out.println("Current: "+curr.name);
			
			// add to visited vertices
			visited.add(curr.name);
			
			// go through current vertex neighbors (Roads to other cities in this case)
			//TreeSet<Road> adjRoads = adjList2.get(curr.name);
			TreeSet<City> adjCities = adjList.get(curr.name);
			if(adjCities != null) {
				for(City c : adjCities) {
					City currCity = citiesByName.get(curr.name);
					
					// calculate weight = distance(sourceCity, toCity)
					Double segmentWeight = currCity.toPoint2D().distance(c.toPoint2D());
					Float currDist = dists2.get(curr.name) + segmentWeight.floatValue();
					//int currDist = curr.distance + segmentWeight.intValue();
					
					//System.out.println("Adjacent City: "+c.getName()+", "+currDist);
					
					// if d[v] > d[u] + weight(u, v) 	(u=curr, v=toCity)
					if(dists2.get(c.getName()) > currDist && !visited.contains(c.getName())) {
						
						// d[v] = d[u] + weight(u,v)
						dists2.put(c.getName(), currDist);
						path2.put(c.getName(), curr.name);
						
						// put v into priority queue (can use 'currDist' since just put it in as v's dist)
						pq.add(new DijkstraNode(c.getName(), currDist));
						
					}
				}
			}
		}
		
		String ele = endCity;
		while(ele != null) {
			//System.out.println(path2.get(ele));
			
			// probably not the most efficient thing but basically break if the endCity is not connected
			if(ele.equals(endCity) && path2.get(ele) == null) {
				if(ele.equals(startCity))
					finalPath.add(ele);
				
				break;
			}
			finalPath.add(ele);
			ele = path2.get(ele);
		}
		finalDist = dists2.get(endCity);
		
		double ff = Math.round(finalDist * 10000.0)/10000.0;
		//System.out.println(ff);
		double findist3 = round(ff, 3);
		//String findist3 = String.format("%.3f", finalDist);
		//System.out.println(findist3);
		String findist4 = String.format("%.3f", findist3);
		//System.out.println(findist4);
		
		pathEle.setAttribute("length", findist4);
		pathEle.setAttribute("hops", String.valueOf(finalPath.size()-1));
		
		//System.out.println(finalDist);
		//System.out.println(finalPath.toString());
		//System.out.println(path2.toString());
		//System.out.println(dists2.toString());
		
		return finalPath;
	}
	
	/**
	 * All credit given to StackExchange user highlighted in readme. This 
	 *  is used to help round a float/double to a certain number of decimal places.
	 */
	private double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	private void addDirections(final Element pathEle, ArrayList<Road> roadList) {
		
		if(roadList.size() == 1) {
			Road curr = roadList.get(0);
			final Element roadNode = results.createElement("road");
			roadNode.setAttribute("start", curr.getStartCity().getName());
			roadNode.setAttribute("end", curr.getEndCity().getName());
			pathEle.appendChild(roadNode);
		}
		
		for(int i=0; i < roadList.size()-1; i++) {
			Road curr = roadList.get(i);
			Road next = roadList.get(i+1);
			Point2D points[] = new Point2D[3];
			double theta2 = 0, origin = 0;
			points = getPoints(curr, next);
			
			final Element roadNode = results.createElement("road");
			roadNode.setAttribute("start", curr.getStartCity().getName());
			roadNode.setAttribute("end", curr.getEndCity().getName());
			pathEle.appendChild(roadNode);
			
			/* adding direction */
			
			// line1 = points[0]->points[1] 	line2 = points[1]->points[2]
			// Math.atan2( line1.y1 - line1.y2 , line1.x1 - line1.x2 )
			// Math.atan2( line2.y1 - line2.y2 , line2.x1 - line2.x2 )
			double angle1 = Math.atan2(points[0].getY() - points[1].getY(), points[0].getX() - points[1].getX());
			double angle2 = Math.atan2(points[2].getY() - points[1].getY(), points[2].getX() - points[1].getX());
			
			theta2 = Math.toDegrees(angle1-angle2);
			
			if(theta2 < 0) {
				theta2 += 360;
			}
			
			origin = theta2 - 90;
			
			if(theta2 > 135 && theta2 <= 225) {
				final Element straight = results.createElement("straight");
				pathEle.appendChild(straight);
			} else if(theta2 <= 135 && theta2 > 0) {
				final Element left = results.createElement("left");
				pathEle.appendChild(left);
			} else if(theta2 > 225 && theta2 < 360) {
				final Element right = results.createElement("right");
				pathEle.appendChild(right);
			} else {
				//System.out.println("Whoa {ERROR} ("+theta2+")");
			}
			
			/* add last Road */
			if(i == roadList.size()-2) {
				final Element roadNode2 = results.createElement("road");
				roadNode2.setAttribute("start", next.getStartCity().getName());
				roadNode2.setAttribute("end", next.getEndCity().getName());
				pathEle.appendChild(roadNode2);
			}
		}
		
	}
	
	private Point2D[] getPoints(Road curr, Road next) {
		Point2D a, b, c;
		Point2D points[] = new Point2D[3];
		
		City c1, c2, c3, c4;
		if(curr.SWAPPED && next.SWAPPED) {
			//System.out.println("1");
			c1 = curr.getEndCity();
			c2 = curr.getStartCity();
			c3 = next.getEndCity();
			c4 = next.getStartCity();
		} else if(!curr.SWAPPED && next.SWAPPED) {
			//System.out.println("2");
			c1 = curr.getStartCity();
			c2 = curr.getEndCity();
			c3 = next.getEndCity();
			c4 = next.getStartCity();
		} else if(curr.SWAPPED && !next.SWAPPED) {
			//System.out.println("3");
			c1 = curr.getEndCity();
			c2 = curr.getStartCity();
			c3 = next.getStartCity();
			c4 = next.getEndCity();
		} else {
			//System.out.println("4");
			c1 = curr.getStartCity();
			c2 = curr.getEndCity();
			c3 = next.getStartCity();
			c4 = next.getEndCity();
		}
		
		//System.out.println(String.join(" ", c1.toString(), c2.toString(), c3.toString(), c4.toString()));
		
		a = c1.toPoint2D();
		if(!c2.name.equals(c3.name)) {
			System.out.println("Something went wrong. "+c2.toString()+" "+c3.toString());
			b = null;
		} else {
			b = c2.toPoint2D();
		}
		c = c4.toPoint2D();
		
		points[0] = a;
		points[1] = b;
		points[2] = c;
		//System.out.println(String.join(" ", a.toString(), b.toString(), c.toString()));
		return points;
	}
	
	public void processPrintPMQuadtree(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		if (pmQuadtree.isEmpty()) {
			/* empty PM Quadtree */
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {
			/* print PR Quadtree */
			final Element quadtreeNode = results.createElement("quadtree");
			quadtreeNode.setAttribute("order", String.valueOf(pmOrder));
			
			printPMQuadtreeHelper(pmQuadtree.getRoot(), quadtreeNode);

			outputNode.appendChild(quadtreeNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}
	
	private void printPMQuadtreeHelper(final PMQuadtree.Node currentNode,
										final Element xmlNode) {
		if (currentNode.getType() == PMQuadtree.Node.WHITE) {
			Element white = results.createElement("white");
			xmlNode.appendChild(white);
		} else {
			if (currentNode.getType() == PMQuadtree.Node.BLACK) {
				/* leaf node */
				final BlackNode currentLeaf = (BlackNode) currentNode;
				final Element black = results.createElement("black");
				
				/* setting cardinality = num. roads + city (if city exists) */
				int cardinality = -1;
				if(currentLeaf.getCity() == null) {
					cardinality = currentLeaf.getRoads().size();
					black.setAttribute("cardinality", String.valueOf(cardinality));
				} else {
					cardinality = currentLeaf.getRoads().size() + 1;
					black.setAttribute("cardinality", String.valueOf(cardinality));
					if(isolatedCities.contains(currentLeaf.getCity().getName())) {
						addIsoCityNode(black, currentLeaf.getCity());
					} else {
						addCityNode(black, currentLeaf.getCity());
					}
				}
				
				TreeMap<Road, Integer> currentLeafRoads = currentLeaf.getRoads();
				//currentLeafRoads.sort(roadComparator); // made Road implement Comparable
				for(Road r : currentLeafRoads.keySet()) {
					addRoadNodes(black, r);
				}
				
				
				xmlNode.appendChild(black);
			} else {
				/* internal node */
				final GrayNode currentInternal = (GrayNode) currentNode;
				final Element gray = results.createElement("gray");
				gray.setAttribute("x", Integer.toString((int) currentInternal
						.getCenterX()));
				gray.setAttribute("y", Integer.toString((int) currentInternal
						.getCenterY()));
				for (int i = 0; i < 4; i++) {
					printPMQuadtreeHelper(currentInternal.getChild(i), gray);
				}
				xmlNode.appendChild(gray);
			}
		}
	}
	
	private void addRoadNodes(final Element node, final Road road) {
		final Element roadNode = results.createElement("road");
		if(cityNameComp.compare(road.getEndCity(), road.getStartCity()) > 0) {
			roadNode.setAttribute("start", road.getEndCity().getName());
			roadNode.setAttribute("end", road.getStartCity().getName());
		} else {
			roadNode.setAttribute("start", road.getStartCity().getName());
			roadNode.setAttribute("end", road.getEndCity().getName());
		}
		node.appendChild(roadNode);
	}
	
	/* ********************************************************************* */
	
	/**
	 * Object for storing city names and their distances from some 
	 * 	source vertex (that is specified in the algorithm). 
	 */
	private class DijkstraNode {
		public final String name;
		public final float distance;
		
		public DijkstraNode(String name, float distance) {
			this.name = name;
			this.distance = distance;
		}
		
		public String toString() {
			return "["+name+", "+distance+"]";
		}
	}
	
	/**
	 * Object used to compare DijkstraNode's in the shortest path
	 * 	priority queue. Sorts them by distance to a given source vertex.
	 */
	private class DijkstraComparator implements Comparator<DijkstraNode> {

		@Override
		public int compare(DijkstraNode arg0, DijkstraNode arg1) {
			return Float.compare(arg0.distance, arg1.distance);
		}
		
		/* * extra - just for printing out if needed (delete later) *
		Integer c = Integer.compare(arg0.distance, arg1.distance);
		System.out.println(c);
		return c;*/
	}
	
	
	class QuadrantDistance implements Comparable<QuadrantDistance> {
		public Node quadtreeNode;
		public PMQuadtree.Node pmQuadNode;
		private double distance;

		public QuadrantDistance(Node node, Point2D.Float pt) {
			quadtreeNode = node;
			if (node.getType() == Node.INTERNAL) {
				InternalNode gray = (InternalNode) node;
				distance = Shape2DDistanceCalculator.distance(pt, 
						new Rectangle2D.Float(gray.origin.x, gray.origin.y, gray.width, gray.height));
			} else if (node.getType() == Node.LEAF) {
				LeafNode leaf = (LeafNode) node;
				distance = pt.distance(leaf.getCity().pt);
			} else {
				throw new IllegalArgumentException("Only leaf or internal node can be passed in");
			}
		}
		
		/**
		 * Overloading to use this with PMQuadtree instead of PRQuadtree
		 */
		public QuadrantDistance(PMQuadtree.Node node, Point2D.Float pt) {
			pmQuadNode = node;
			if (node.getType() == PMQuadtree.Node.GRAY) {
				GrayNode gray = (GrayNode) node;
				distance = Shape2DDistanceCalculator.distance(pt, 
						new Rectangle2D.Float(gray.origin.x, gray.origin.y, gray.width, gray.height));
			} else if (node.getType() == PMQuadtree.Node.BLACK) {
				BlackNode leaf = (BlackNode) node;
				distance = pt.distance(leaf.getCity().pt);
			} else {
				throw new IllegalArgumentException("Only leaf or internal node can be passed in");
			}
		}

		public int compareTo(QuadrantDistance qd) {
			if (distance < qd.distance) {
				return -1;
			} else if (distance > qd.distance) {
				return 1;
			} else {
				if (pmQuadNode.getType() != qd.pmQuadNode.getType()) {
					if (pmQuadNode.getType() == PMQuadtree.Node.GRAY) {
						return -1;
					} else {
						return 1;
					}
				} else if (pmQuadNode.getType() == PMQuadtree.Node.BLACK) {
					// both are leaves
					return ((BlackNode) qd.pmQuadNode).getCity().getName().compareTo(
							((BlackNode) pmQuadNode).getCity().getName());
				} else {
					// both are internals
					return 0;
				}
			}
		}
	}

	//	/**
	//	 * Examines the distance from each city in a PR Quadtree node from the given
	//	 * point.
	//	 * 
	//	 * @param node
	//	 *            PR Quadtree node being examined
	//	 * @param point
	//	 *            point
	//	 * @param nearCities
	//	 *            priority queue of cities organized by how close they are to
	//	 *            the point
	//	 */
	//	private void nearestCityHelper(Node node, Point2D.Float point,
	//			PriorityQueue<NearestCity> nearCities) {
	//		if (node.getType() == Node.LEAF) {
	//			LeafNode leaf = (LeafNode) node;
	//			NearestCity nearCity = new NearestCity(leaf.getCity(), point
	//					.distance(leaf.getCity().toPoint2D()));
	//			if (nearCity.compareTo(nearCities.peek()) < 0) {
	//				nearCities.add(nearCity);
	//			}
	//		} else if (node.getType() == Node.INTERNAL) {
	//			InternalNode internal = (InternalNode) node;
	//			TreeSet<NearestQuadrant> nearestQuadrants = new TreeSet<NearestQuadrant>();
	//			for (int i = 0; i < 4; i++) {
	//				nearestQuadrants.add(new NearestQuadrant(Shape2DDistanceCalculator.distance(point, internal
	//						.getChildRegion(i)), i));
	//			}
	//			
	//			for (NearestQuadrant nearQuadrant : nearestQuadrants) {
	//				final int i = nearQuadrant.getQuadrant(); 
	//				
	//				if (Shape2DDistanceCalculator.distance(point, internal
	//						.getChildRegion(i)) <= nearCities.peek().getDistance()) {
	//
	//					nearestCityHelper(internal.getChild(i), point, nearCities);
	//				}
	//			}
	//		}
	//	}
	//	
	//	private class NearestQuadrant implements Comparable<NearestQuadrant> {
	//
	//		private double distance;
	//		
	//		private int quadrant;
	//		
	//		public NearestQuadrant(double distance, int quadrant) {
	//			this.distance = distance;
	//			this.quadrant = quadrant;
	//		}
	//
	//		public int getQuadrant() {
	//			return quadrant;
	//		}
	//
	//		public int compareTo(NearestQuadrant o) {
	//			if (distance < o.distance) {
	//				return -1;
	//			} else if (distance > o.distance) {
	//				return 1;
	//			} else {
	//				if (quadrant < o.quadrant) {
	//					return -1;
	//				} else if (quadrant > o.quadrant) {
	//					return 1;
	//				} else {
	//					return 0;
	//				}
	//			}
	//		}
	//		
	//	}
	//
	//	/**
	//	 * Used with the nearestCity command. Each NearestCity contains a city and
	//	 * the city's distance from a give point. A NearestCity is less than another
	//	 * if it's distance is smaller than the other's.
	//	 * 
	//	 * @author Ben Zoller
	//	 * @version 1.0
	//	 */
	//	private class NearestCity implements Comparable<NearestCity> {
	//		/** city */
	//		private final City city;
	//
	//		/** city's distance to a point */
	//		private final double distance;
	//
	//		/**
	//		 * Constructs a city and it's distance from a point.
	//		 * 
	//		 * @param city
	//		 *            city
	//		 * @param distance
	//		 *            distance from a point
	//		 */
	//		private NearestCity(final City city, final double distance) {
	//			this.city = city;
	//			this.distance = distance;
	//		}
	//
	//		/**
	//		 * Gets the city
	//		 * 
	//		 * @return city
	//		 */
	//		private City getCity() {
	//			return city;
	//		}
	//
	//		/**
	//		 * Compares one city to another based on their distances.
	//		 * 
	//		 * @param otherNearCity
	//		 *            other city
	//		 * @return distance comparison results
	//		 */
	//		public int compareTo(final NearestCity otherNearCity) {
	//			if (distance < otherNearCity.distance) {
	//				return -1;
	//			} else if (distance > otherNearCity.distance) {
	//				return 1;
	//			} else {
	//				return city.getName().compareTo(otherNearCity.city.getName());
	//			}
	//		}
	//
	//		/**
	//		 * Gets the distance
	//		 * 
	//		 * @return distance
	//		 */
	//		public double getDistance() {
	//			return distance;
	//		}
	//	}
}
