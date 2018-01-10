package cmsc420.meeshquest.part2;

public class PM1Quadtree extends PMQuadtree {

	private final static PM1Validator validatorPM1 = new PM1Validator();
	
	public PM1Quadtree() {
		super(validatorPM1);
	}
	
	public static class PM1Validator implements Validator {

		/** 
		 * PM1 - only valid BlackNode if:
		 * <ul>
		 * <li> One city && 1+ q-edges 	OR </li>
		 * <li> 1+ q-edges	</li>
		 * </ul>
		 */
		@Override
		public boolean valid(BlackNode node) {
			//System.out.println("PM1Validator in BlackNode");
			return false;
		}
		
	}
}
