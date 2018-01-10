package cmsc420.meeshquest.part2;

public class PM3Quadtree extends PMQuadtree {

	private final static PM3Validator validatorPM3 = new PM3Validator();
		
	public PM3Quadtree() {
		super(validatorPM3);
	}
	
	
	public static class PM3Validator implements Validator {
		
		/** 
		 * PM3 - only valid BlackNode if:
		 * <ul>
		 * <li> One city AND </li>
		 * <li> 0 or 1+ q-edges	</li>
		 * </ul>
		 */
		@Override
		public boolean valid(BlackNode node) {
			return node.getCity() != null ? false : true;
		}
		
	}
}
