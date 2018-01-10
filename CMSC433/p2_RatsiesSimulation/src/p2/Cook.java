package cmsc433.p2;


/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Eaters and process them.
 */
public class Cook implements Runnable {
	private final String name;
	private Restaurant ratsies;
	
	/**
	 * You can feel free to modify this constructor.  It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful. 
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name) {
		this.name = name;
		this.ratsies = Restaurant.getInstance();
	}

	public String toString() {
		return name;
	}

	/**
	 * This method executes as follows.  The cook tries to retrieve
	 * orders placed by Customers.  For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine, by calling makeFood().  Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified.  The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 * -----
	 * 1. Wait for an order to be placed then retrieve
	 * 2. Process order ...
	 * 2.1. Send all Food in Order to machines
	 * 2.2. Wait for all Food to be done
	 * 2.3. Notify Customer
	 * ----- Simulation Events -----
	 * -Startup: 			cookStarting		***
	 * -Starting order: 	cookReceivedOrder	*** (in Restaurant.cookWait)
	 * -Submitting food: 	cookStartedFood		 ** (in Restaurant.processOrder)
	 * -Finished food: 		cookFinishedFood	 ** (in Restaurant.processOrder)
	 * -Finished order: 	cookCompletedOrder	 ** (in Restaurant.completedOrder)
	 * -Terminate:			cookEnding			*?* (in Restaurant.
	 */
	public void run() {

		Simulation.logEvent(SimulationEvent.cookStarting(this));
		//try {
			while(true) {
				
				// Cook waits for an Order to be placed
				Order curOrder = ratsies.cookWait(this);
				
				// If curOrder is null it means that it was interrupted, which should only happen at end
				//  so just break and terminate this Cook thread.
				if(curOrder == null) {
					break;
				}
				
				
				// processOrder() sends Food to Machines = "cooks" the food
				//	calls: SimulationEvent.cookReceivedOrder() 
				ratsies.processOrder(this, curOrder);
				
				
				// at end
				ratsies.completedOrder(this, curOrder);
				//SimulationEvent.cookCompletedOrder(this, curOrder.orderNumber);
			}
		// TODO: WHAT IF SIMULATION CALLS "interrupt()" WHEN COOK IS NOT IN "cookWait()" ????
		/*}
		catch(InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
		*/
	}
}