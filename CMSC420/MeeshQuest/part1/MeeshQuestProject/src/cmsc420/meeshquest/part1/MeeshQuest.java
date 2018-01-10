package cmsc420.meeshquest.part1;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmsc420.drawing.CanvasPlus;
import cmsc420.xml.XmlUtility;

public class MeeshQuest {

    public static void main(String[] args) {
    	
    	Document results = null;
    	CanvasPlus canvas = null;
    	int spatialWidth = 0;
    	int spatialHeight = 0;
    	
        try {
        	Document doc = XmlUtility.validateNoNamespace(System.in); 						//Use for submission
        	//Document doc = XmlUtility.validateNoNamespace(new File("part1.delete2.input.xml"));	//Use for testing purposes
        	results = XmlUtility.getDocumentBuilder().newDocument();
        
        	Element commandNode = doc.getDocumentElement();
        	
        	// Gathering spatial data
        	spatialWidth = Integer.parseInt(commandNode.getAttribute("spatialWidth"));
        	spatialHeight = Integer.parseInt(commandNode.getAttribute("spatialHeight"));
        	
        	// Start XML output creation
        	Element resultsRoot = results.createElement("results");
        	results.appendChild(resultsRoot);
        	
        	///////// ***************************** /////////
        	///////// BEGIN: User created variables /////////
        	
        	// *** Data Dictionary variables *** //
        	//// Comparators ////
        	CityNameComparator nameComp 	= new CityNameComparator();
        	CityLocationComparator locComp  = new CityLocationComparator();
        	//// TreeMaps ////
        	TreeMap<String, City> nameMap 	  = new TreeMap<>(nameComp);
        	TreeMap<City, String> locationMap = new TreeMap<>(locComp);
        	//TreeMap<String, Boolean> isMapped = new TreeMap<>();
        	ArrayList<String> isMapped = new ArrayList<>();
        	//// Data Dictionary Object ////
        	DataDictionary dataDict = new DataDictionary(nameMap, locationMap, isMapped);
        	
        	// *** Spatial Dictionary variables *** //
        	//// CanvasPlus Object ////
        	canvas = new CanvasPlus("MeeshQuest", spatialWidth*2, spatialHeight*2);
        	canvas.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);
        	
        	//// PRQuadtree Object ////
        	PRQuadtree quadtree = new PRQuadtree(spatialWidth, spatialHeight, canvas);
        	
        	//// SpatialMap Object ////
        	SpatialMap spatialMap = new SpatialMap(quadtree, dataDict, spatialWidth, spatialHeight);
        	
        	///////// END: User created variables   /////////
        	///////// ***************************** /////////
        	
        	final NodeList nl = commandNode.getChildNodes();
        	for (int i = 0; i < nl.getLength(); i++) {
        		if (nl.item(i).getNodeType() == Document.ELEMENT_NODE) {
        			commandNode = (Element) nl.item(i);
                
        			/////////////// BEGIN commandNode processing ///////////////
        			
        			String tagName = commandNode.getTagName();
        			if(tagName.equals("createCity")) {
        				
        				// Pre-processing for "createCity"
        		    	String name = commandNode.getAttribute("name");
        				int x = Integer.parseInt(commandNode.getAttribute("x"));
        				int y = Integer.parseInt(commandNode.getAttribute("y"));
        				int radius = Integer.parseInt(commandNode.getAttribute("radius"));
        				String color = commandNode.getAttribute("color");
        				
        				// Process "createCity" in Data Dictionary structure
        				int resultVal = dataDict.createCity(name, x, y, radius, color);
        				if(resultVal == 0) {
        					Element succ = results.createElement("success");
        					resultsRoot.appendChild(succ);
        					
        					Element paramTag = resultSetUp(results, succ, "createCity");
        					
        					cityParamXML(results, paramTag, name, x, y, radius, color);
        					
        					Element output = results.createElement("output");
        					succ.appendChild(output);
        					
        				} else {
        					Element err = results.createElement("error");
        					String errorType = null;
        					if(resultVal == 1) { errorType = "duplicateCityCoordinates"; }
        					if(resultVal == 2) { errorType = "duplicateCityName"; }
        					
        					err.setAttribute("type", errorType);
        					resultsRoot.appendChild(err);
        					
        					Element paramTag = resultSetUp(results, err, "createCity");
        					cityParamXML(results, paramTag, name, x, y, radius, color);
        					
        				}
        				
        			} else if(tagName.equals("listCities")) {
        				String sortBy = commandNode.getAttribute("sortBy");
        				
        				if(dataDict.dictEmpty()) {
        					// ERROR: noCitiesToList - Data Dictionary is empty, output error
        					Element err = results.createElement("error");
        					err.setAttribute("type", "noCitiesToList");
        					resultsRoot.appendChild(err);
        					
        					Element paramTag = resultSetUp(results, err, "listCities");
        					listParamXML(results, paramTag, sortBy);
        					
        				} else {
        					Element succ = results.createElement("success");
        					resultsRoot.appendChild(succ);
        					
        					Element paramTag = resultSetUp(results, succ, "listCities");
        					listParamXML(results, paramTag, sortBy);
        					
        					Element output = results.createElement("output");
        					succ.appendChild(output);
        					
        					Element cityListTag = results.createElement("cityList");
        					output.appendChild(cityListTag);
        					
        					dataDict.listCities(results, cityListTag, sortBy);
        				}
        				
        			} else if(tagName.equals("clearAll")) {
        				Element succ = results.createElement("success");
    					resultsRoot.appendChild(succ);
    					
    					resultSetUp(results, succ, "clearAll");
    					
    					Element output = results.createElement("output");
    					succ.appendChild(output);
    					
    					canvas = new CanvasPlus("MeeshQuest", spatialWidth*2, spatialHeight*2);
    					canvas.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);
    					quadtree = new PRQuadtree(spatialWidth, spatialHeight, canvas);
    					
    					nameMap 	= new TreeMap<>(nameComp);
    		        	locationMap = new TreeMap<>(locComp);
    		        	isMapped = new ArrayList<>();
    		        	dataDict = new DataDictionary(nameMap, locationMap, isMapped);
    		        	//dataDict.clearAll();
    					
    					spatialMap = new SpatialMap(quadtree, dataDict, spatialWidth, spatialHeight);
        			} else if(tagName.equals("mapCity")) {
        				
        				String name = commandNode.getAttribute("name");
        				
        				int resultVal = spatialMap.map(name);
        				if(resultVal == 0) {
        					Element succ = results.createElement("success");
        					resultsRoot.appendChild(succ);
        					
        					Element paramTag = resultSetUp(results, succ, "mapCity");
        					mapParamXML(results, paramTag, name);
        					
        					Element output = results.createElement("output");
        					succ.appendChild(output);
        					
        				} else {
        					Element err = results.createElement("error");
        					String errorType = null;
        					if(resultVal == 1) { errorType = "nameNotInDictionary"; }
        					if(resultVal == 2) { errorType = "cityAlreadyMapped"; }
        					if(resultVal == 3) { errorType = "cityOutOfBounds"; }
        					
        					err.setAttribute("type", errorType);
        					resultsRoot.appendChild(err);
        					
        					Element paramTag = resultSetUp(results, err, "mapCity");
        					mapParamXML(results, paramTag, name);
        				}
        			} else if(tagName.equals("unmapCity")) {
        				String name = commandNode.getAttribute("name");
        				
        				int resultVal = spatialMap.unmap(name);
        				if(resultVal == 0) {
        					Element succ = results.createElement("success");
        					resultsRoot.appendChild(succ);
        					
        					Element paramTag = resultSetUp(results, succ, "unmapCity");
        					mapParamXML(results, paramTag, name);
        					
        					Element output = results.createElement("output");
        					succ.appendChild(output);
        					
        				} else {
        					Element err = results.createElement("error");
        					String errorType = null;
        					if(resultVal == 1) { errorType = "nameNotInDictionary"; }
        					if(resultVal == 2) { errorType = "cityNotMapped"; }
        					
        					err.setAttribute("type", errorType);
        					resultsRoot.appendChild(err);
        					
        					Element paramTag = resultSetUp(results, err, "unmapCity");
        					mapParamXML(results, paramTag, name);
        				}
        			}
        			else if(tagName.equals("deleteCity")) {
        				String name = commandNode.getAttribute("name");
        				boolean wasMapped = false;
        				
        				City c = dataDict.getCity(name);
	    				if(c == null) {
	    					// ERROR: cityDoesNotExist
	    					Element err = results.createElement("error");
	    					err.setAttribute("type", "cityDoesNotExist");
	    					resultsRoot.appendChild(err);
	    					Element paramTag = resultSetUp(results, err, "deleteCity");
	    					mapParamXML(results, paramTag, name);	// can use mapParamXML for this too
	    				} else {
	    					// TODO: deleteCity
		    				if(dataDict.isMapped.contains(name)) {
		    					wasMapped = true;
	        					spatialMap.unmap(name);
	        				}
		    				dataDict.delete(c);
		    				
		    				Element succ = results.createElement("success");
        					resultsRoot.appendChild(succ);
        					
        					Element paramTag = resultSetUp(results, succ, "deleteCity");
        					mapParamXML(results, paramTag, name);
        					
        					Element output = results.createElement("output");
        					succ.appendChild(output);
        					if(wasMapped) {
        						Element unmapped = results.createElement("cityUnmapped");
        						unmapped = cityOutputXML(unmapped, c);
        						
        						output.appendChild(unmapped);
        					}
	    				}
        			}
        			else if(tagName.equals("rangeCities")) {
        				int x = Integer.parseInt(commandNode.getAttribute("x"));
        				int y = Integer.parseInt(commandNode.getAttribute("y"));
        				int radius = Integer.parseInt(commandNode.getAttribute("radius"));
        				String saveMap = commandNode.getAttribute("saveMap");
        				
        				TreeMap<String, City> citiesInRange = spatialMap.range(x, y, radius);
        				if(citiesInRange == null) {
        					// ERROR: noCitiesExistInRange
        					Element err = results.createElement("error");
	    					err.setAttribute("type", "noCitiesExistInRange");
	    					resultsRoot.appendChild(err);
	    					Element paramTag = resultSetUp(results, err, "rangeCities");
	    					rangeParamXML(results, paramTag, x, y, radius, saveMap);
        					if(!saveMap.equals("")) {
    							canvas.addCircle(x, y, radius, Color.BLUE, false);
    							canvas.save(saveMap);
    							canvas.dispose();
        					} 
        				} else {
        					Element succ = results.createElement("success");
        					resultsRoot.appendChild(succ);
        					
        					Element paramTag = resultSetUp(results, succ, "rangeCities");
        					rangeParamXML(results, paramTag, x, y, radius, saveMap);
        					
        					Element output = results.createElement("output");
        					succ.appendChild(output);
        					
        					Element cityList = results.createElement("cityList");
        					output.appendChild(cityList);
        					
        					// Create cityList output
        					for(String cname : citiesInRange.keySet()) {
        						City c = citiesInRange.get(cname);
        						Element cityTag = results.createElement("city");
        						cityTag = cityOutputXML(cityTag, c);
        						cityList.appendChild(cityTag);
        					}
        					
        					if(!saveMap.equals("")) {
    							canvas.addCircle(x, y, radius, Color.BLUE, false);
    							canvas.save(saveMap);
    							canvas.dispose();
        					} 
        				}
        			}
        			else if(tagName.equals("printPRQuadtree")) {
        				if(quadtree.isEmpty()) {
        					// ERROR: mapIsEmpty
        					Element err = results.createElement("error");
	    					err.setAttribute("type", "mapIsEmpty");
	    					resultsRoot.appendChild(err);
	    					Element paramTag = resultSetUp(results, err, "printPRQuadtree");
	    					err.appendChild(paramTag);
        				} else {
		    				Element succ = results.createElement("success");
							resultsRoot.appendChild(succ);
							Element paramTag = resultSetUp(results, succ, "printPRQuadtree");
							succ.appendChild(paramTag);
							Element output = results.createElement("output");
							succ.appendChild(output);
							Element tree = results.createElement("quadtree");
							output.appendChild(tree);
							quadtree.printTree(results, tree);
							//tree.appendChild(printed);
        				}
        			}
        			else if(tagName.equals("saveMap")) {
        				String name = commandNode.getAttribute("name");
        				Element succ = results.createElement("success");
    					resultsRoot.appendChild(succ);
    					Element paramTag = resultSetUp(results, succ, "saveMap");
    					mapParamXML(results, paramTag, name);
    					succ.appendChild(paramTag);
    					Element output = results.createElement("output");
    					succ.appendChild(output);
    					canvas.save(name);
    					canvas.dispose();
        			}
        			else if(tagName.equals("nearestCity")) {
    					int x = Integer.parseInt(commandNode.getAttribute("x"));
        				int y = Integer.parseInt(commandNode.getAttribute("y"));
        				
        				if(quadtree.isEmpty()) {
        					// ERROR: mapIsEmpty
        					Element err = results.createElement("error");
	    					err.setAttribute("type", "mapIsEmpty");
	    					resultsRoot.appendChild(err);
	    					Element paramTag = resultSetUp(results, err, "nearestCity");
	    					nearParamXML(results, paramTag, x, y);
        				} else {
        					Element succ = results.createElement("success");
        					resultsRoot.appendChild(succ);
        					Element paramTag = resultSetUp(results, succ, "nearestCity");
        					nearParamXML(results, paramTag, x, y);
        					succ.appendChild(paramTag);
        					
        					City nearest = quadtree.nearestCity(x, y);
        					
        					Element output = results.createElement("output");
        					Element city = results.createElement("city");
        					city = cityOutputXML(city, nearest);
        					output.appendChild(city);
        					succ.appendChild(output);
        				}
        			}
        			else {
        				Element err = results.createElement("undefinedError");
        				resultsRoot.appendChild(err);
        			}
        			
        			/////////////// END commandNode processing ///////////////
        			
        		}
        	}
        } catch (SAXException | IOException | ParserConfigurationException e) {
        	
        	//////////// Fatal error processing ////////////
        	try {
				results = XmlUtility.getDocumentBuilder().newDocument();
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
			}
        	Element err = results.createElement("fatalError");
        	results.appendChild(err);
        	
		} finally {
			//drawCanvas(canvas);
			try {
				XmlUtility.print(results);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
        }
    }
    
    /* **** XML I/O helper methods **** */
    
    /** Sets up XML Output for a success with tags: success, command, parameters
     * @param results = results document -- @param root - is the resultsRoot element  
     * @param cmdRslt - will put 'success' or 'failure' depending on what is needed
     * @param cmdName - is name of command calling this
     * */
    private static Element resultSetUp(Document results, Element root, String cmdName) {
		Element cmd = results.createElement("command");
		cmd.setAttribute("name", cmdName);
		root.appendChild(cmd);
		Element param = results.createElement("parameters");
		root.appendChild(param);
		return param;
    }
    
    /** Helper for XML output of params for "createCity" */
    private static void cityParamXML(Document results, Element param, String name, int x, int y, int radius, String color) {
    	Element nameXML = results.createElement("name");
		nameXML.setAttribute("value", name);
		param.appendChild(nameXML);
		
		Element xXML = results.createElement("x");
		xXML.setAttribute("value", String.valueOf(x));
		param.appendChild(xXML);
		
		Element yXML = results.createElement("y");
		yXML.setAttribute("value", String.valueOf(y));
		param.appendChild(yXML);
		
		Element radiusXML = results.createElement("radius");
		radiusXML.setAttribute("value", String.valueOf(radius));
		param.appendChild(radiusXML);
		
		Element colorXML = results.createElement("color");
		colorXML.setAttribute("value", color);
		param.appendChild(colorXML);
    }
    
    /** Helper for XML output of params for "listCities" */
    private static void listParamXML(Document results, Element param, String sortBy) {
    	Element sortByXML = results.createElement("sortBy");
    	sortByXML.setAttribute("value", sortBy);
    	param.appendChild(sortByXML);
    }
    
    private static void mapParamXML(Document results, Element param, String name) {
    	Element nameTag = results.createElement("name");
		nameTag.setAttribute("value", name);
		param.appendChild(nameTag);
    }
    
    private static void rangeParamXML(Document results, Element param, int x, int y, int radius, String saveMap) {
    	Element xXML = results.createElement("x");
		xXML.setAttribute("value", String.valueOf(x));
		param.appendChild(xXML);
		
		Element yXML = results.createElement("y");
		yXML.setAttribute("value", String.valueOf(y));
		param.appendChild(yXML);
		
		Element radiusXML = results.createElement("radius");
		radiusXML.setAttribute("value", String.valueOf(radius));
		param.appendChild(radiusXML);
		
		if(!saveMap.equals("")) {
			Element saveXML = results.createElement("saveMap");
			saveXML.setAttribute("value", saveMap);
			param.appendChild(saveXML);
		}
    }
    
    private static void nearParamXML(Document results, Element param, int x, int y) {
    	Element xXML = results.createElement("x");
		xXML.setAttribute("value", String.valueOf(x));
		param.appendChild(xXML);
		
		Element yXML = results.createElement("y");
		yXML.setAttribute("value", String.valueOf(y));
		param.appendChild(yXML);
    }
    
    private static Element cityOutputXML(Element unmapped, City c) {
		int x = new Double(c.getX()).intValue();
		int y = new Double(c.getY()).intValue();
		int radius = new Double(c.radius).intValue();
		unmapped.setAttribute("name", c.name);
		unmapped.setAttribute("x", String.valueOf(x));
		unmapped.setAttribute("y", String.valueOf(y));
		unmapped.setAttribute("color", c.color);
		unmapped.setAttribute("radius", String.valueOf(radius));
		return unmapped;
    }
    
    /* ************************************ */
    private static void drawCanvas(CanvasPlus canvas) {
    	canvas.draw();
		/*try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		canvas.dispose();*/
    }
}
