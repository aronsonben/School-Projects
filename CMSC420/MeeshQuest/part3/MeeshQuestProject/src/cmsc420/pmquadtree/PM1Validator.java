package cmsc420.pmquadtree;

import cmsc420.geom.Inclusive2DIntersectionVerifier;
import cmsc420.geometry.City;
import cmsc420.geometry.Road;
import cmsc420.pmquadtree.PMQuadtree.Black;

public class PM1Validator implements Validator {

	@Override
	public boolean valid(Black node) {
		City c = node.getCity();
		
		// No city, one road in BlackNode = valid PM1 node
		if(c == null) {
			return node.getRoads().size() >= 1 ? true : false;
		} 
		else {	// One city, all roads intersect at city = valid PM1 node
			if(node.getRoads() != null) {
				for(Road r : node.getRoads()) {
					if( !Inclusive2DIntersectionVerifier.intersects(c.toPoint2D(), r.toLine2D()) )
						return false;
				}
			}
			return true;
		}
	}

}
