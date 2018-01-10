package cmsc433.p2;

import java.util.List;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order.  When running, an
 * customer attempts to enter the Ratsie's (only successful if the
 * Ratsie's has a free table), place its order, and then leave the
 * Ratsie's when the order is complete.
 */
public class Customer implements Runnable {
	private final String name;
	private final List<Food> foodList;
	private final int orderNum;    
	private final Order order;
	
	private Restaurant ratsies;
	
	private static int runningCounter = 0;

	/**
	 * You can feel free modify this constructor.  It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> food) {
		this.name = name;
		this.foodList = food;
		this.orderNum = ++runningCounter;
		this.order = new Order(foodList, orderNum);
		
		ratsies = Restaurant.getInstance();
	}

	public String toString() {
		return name;
	}

	/** 
	 * This method defines what an Customer does: The customer attempts to
	 * enter the Ratsie's (only successful when the Ratsie's has a
	 * free table), place its order, and then leave the Ratsie's
	 * when the order is complete.
	 * -----
	 * 1. Try to enter Restaurant & wait if needed	*
	 * 2. Place order & wait for it to be ready
	 * 3. Leave Restaurant
	 * ----- Simulation Events -----
	 * -Before enter: customerStarting			***	
	 * -After enter : customerEnteredRatsies	***
	 * -Before order: customerPlacedOrder		***
	 * -After order : customerReceivedOrder		*** (in Restaurant.customerWaitLeave)
	 * -Before Leave: customerLeavingRatsies	*** (in Restaurant.customerWaitLeave)
	 */
	public void run() {
		
		Simulation.logEvent(SimulationEvent.customerStarting(this));
		
		// Enter Restaurant with wait if needed
		joinRestaurant();
		Simulation.logEvent(SimulationEvent.customerEnteredRatsies(this));
		
		
		// placeOrder() adds Customer's Order to Restaurant and notifies Cooks
		Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, order.orderList, order.orderNumber));
		ratsies.placeOrder(this, this.order);
		
		// customerWaitLeave() method calls Simulation Event "customerReceivedOrder" AND "customerLeavingRatsies"
		ratsies.customerWaitLeave(this, this.order);
		
		// Customer will have left the Restaurant at this point, so terminate thread
	}
	
	/** Try to join restaurant */
	private void joinRestaurant() {
		synchronized (ratsies.orderLock) {
			try {
				//System.out.println(this.name + " trying to join restaurant - " + Thread.currentThread());
				while(ratsies.currentCapacity() == 0) {
					//System.out.println(this.name + " waiting to join restaurant");
					ratsies.orderLock.wait();
				}
				
				ratsies.addCustomer();
				//System.out.println(this.name + " joined restaurant");
			} catch (InterruptedException e) {
				System.out.println(this.name + " failed in joinRestaurant");
				e.printStackTrace();
			}
			
		}
	}
	
	
	
}