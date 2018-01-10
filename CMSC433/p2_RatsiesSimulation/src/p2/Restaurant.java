package cmsc433.p2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import cmsc433.p2.FoodType;
import cmsc433.p2.Machine;
import cmsc433.p2.Machine.MachineType;

public class Restaurant {
	
	/////////////// Variables used for joinRestaurant in Customer /////////////////////
	public Object joinLock = new Object();
	private final int numTables;	
	private int currentCapacity = 0;
	public int currentCapacity() {
		synchronized (joinLock) {
			return this.currentCapacity;
		}
	}
	////////////////////////////////////////////////////////////////////////////////////
	
	/////////////// Variables used for placeOrder/completedOrder ///////////////
	public Object orderLock = new Object();
	
	private List<Integer> finishedOrders = new ArrayList<Integer>();
	private HashMap<Order, Customer> customerOrders = new HashMap<Order, Customer>();
	private Queue<Order> orderQueue = new LinkedList<Order>();
	//////////////////////////////////////////////////////////////////////////
	
	/////////////// Variables used for processOrder ///////////////
	private Object cookOrderLock = new Object();
	
	private HashMap<String, Food> foodNames = new HashMap<String, Food>();
	private HashMap<Food, Machine> foodMachines = new HashMap<Food, Machine>();
	
	///////////////////////////////////////////////////////////////
	
	/////////// Misc. variables ////////////
	private int machineCapacity;
	private Machine fountain;
	private Machine fryer;
	private Machine grillPress;
	private Machine oven;
	////////////////////////////////////////
	
	
	/* *****************************************************************************/
	/* Restaurant construction & other setup **********************************************************/
	
	public static Restaurant instance = null;
	
	// Private constructor
	private Restaurant(int tables, int cooks, int machCapacity) {
		this.numTables = tables;
		this.machineCapacity = machCapacity;
		this.currentCapacity = tables;
	}
	
	// Factory method to create a new instance of the Restaurant
	public static Restaurant newInstance(int tables, int cooks, int machCapacity) {
		if( instance == null ) 
			instance = new Restaurant(tables, cooks, machCapacity);
		return instance;
	}
	
	// get instance of restaurant
	public static Restaurant getInstance() {
		return instance;
	}

	/** Set up / start all machines */
	public void setMachines() {
		this.fountain = new Machine(MachineType.fountain, FoodType.soda, machineCapacity);
		Simulation.logEvent(SimulationEvent.machineStarting(fountain, FoodType.soda, machineCapacity));
		this.fryer = new Machine(MachineType.fryer, FoodType.wings, machineCapacity);
		Simulation.logEvent(SimulationEvent.machineStarting(fryer, FoodType.wings, machineCapacity));
		this.grillPress = new Machine(MachineType.grillPress, FoodType.sub, machineCapacity);
		Simulation.logEvent(SimulationEvent.machineStarting(grillPress, FoodType.sub, machineCapacity));
		this.oven = new Machine(MachineType.oven, FoodType.pizza, machineCapacity);
		Simulation.logEvent(SimulationEvent.machineStarting(oven, FoodType.pizza, machineCapacity));
		foodMachines.put(FoodType.soda, fountain);
		foodMachines.put(FoodType.wings, fryer);
		foodMachines.put(FoodType.sub, grillPress);
		foodMachines.put(FoodType.pizza, oven);
	}
	
	public void endMachines() {
		Simulation.logEvent(SimulationEvent.machineEnding(fountain));
		Simulation.logEvent(SimulationEvent.machineEnding(fryer));
		Simulation.logEvent(SimulationEvent.machineEnding(grillPress));
		Simulation.logEvent(SimulationEvent.machineEnding(oven));
	}
	
	
	/* *****************************************************************************/
	/* Functional methods **********************************************************/
	
	/** Method to add a customer to the Restaurant. Updates current Restaurant capacity based off tables & customers */
	public void addCustomer() {
		synchronized(joinLock) {
			currentCapacity = currentCapacity() - 1;
			//joinLock.notifyAll();
		}
	}
	
	/** Remove Customer from Restaurant */
	/*public void removeCustomer() {
		System.out.println("Customer " + Thread.currentThread() + " trying to leave");
		synchronized(joinLock) {
			System.out.println("Customer " + Thread.currentThread() + " about to leave");
			finishedCustomers.poll();
			currentCapacity = currentCapacity() + 1;
			joinLock.notifyAll();
		}
	}
	*/
	
	
	/** Summary: Takes order from Customer and notifies/gives to Cook
	 * -Put Customer thread's params into appropriate variables
	 * -Notify the Cooks (orderLock.notifyAll)
	 */
	public void placeOrder(Customer cust, Order curOrder) {
		synchronized(orderLock) {
			customerOrders.put(curOrder, cust);
			orderQueue.add(curOrder);
			orderLock.notifyAll();
		}
	}
	
	
	/** Summary: Customer calls this to wait for their order to be ready 
	 * -Customer enters method, obtains orderLock (WHY?)
	 * -Waits for its Order to appear in finishedOrders List (thus releases orderLock)
	 * -Once Order is finished, it will try to reacquire orderLock so it can update currentCapacity and leave
	 * -Send SimulationEvent that customer has received order since it will only be at that part once it has done so
	 * -Increase capacity and notify any waiting Customers on orderLock
	 * 	 (which would be locked in Customer.joinRestaurant)
	*/
	public void customerWaitLeave(Customer cust, Order custOrder) {
		synchronized(orderLock) {
			try {
				// Customer waits for Order to be ready
				while(!finishedOrders.contains(custOrder.orderNumber)) {
					orderLock.wait();
				}
				
				// If Customer has woken up from wait(), its Order has finished
				Simulation.logEvent(SimulationEvent.customerReceivedOrder(cust, custOrder.orderList, custOrder.orderNumber));
				
				
				// Customer has obtained Order and now tries to leave
				currentCapacity = currentCapacity() + 1;
				Simulation.logEvent(SimulationEvent.customerLeavingRatsies(cust));
				
				// TODO: why using orderLock here? check up on this
				orderLock.notifyAll();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/////////////// Cook methods ///////////////////
	
	/** Summary: Cook calls this to wait for an order to go up for grabs 
	 * -Cook obtains orderLock (WHY?)
	 * -Cook waits for any Order to appear in the Queue of Orders
	 */
	public Order cookWait(Cook curCook) {
		synchronized(orderLock) {
			try {
				while(orderQueue.isEmpty()) {
					//System.out.println("Cook about to wait and release orderLock");
					orderLock.wait();
				}
				
				// An Order has been added to the Queue, so Cook grabs it
				Order curOrder = orderQueue.remove();
				
				// TODO: Do not do SimulationEvent "cookReceivedOrder" here b/c that should only fire when
				// 	a Cook is "starting" an Order, which is not happening here since it may actually get
				//  interrupted.
				
				
				return curOrder;
			} catch (InterruptedException e) {
				// Since the Cook will always enter this method after completing an Order, it will return this here
				Simulation.logEvent(SimulationEvent.cookEnding(curCook));
				return null;
			}
		}
	}

	
	/** Summary: Cook calls this to break down the order and send it to Machines 
	 *  -obtain cookOrderLock - TODO: do I even need this? this is basically just synchronizing the whole method
	 *  								so only one cook can process an Order at a time? 
	 *  -
	 */
	public void processOrder(Cook curCook, Order curOrder) {
		synchronized(cookOrderLock) {
			
			// Cook is officially starting to deal with an Order here
			Simulation.logEvent(SimulationEvent.cookReceivedOrder(curCook, curOrder.orderList, curOrder.orderNumber));
			
			//// Init some needed variables for later ////
			List<Food> foodList = curOrder.orderList;
			HashMap<Thread, Food> threadFoods = new HashMap<Thread, Food>();
			Thread[] threadList = new Thread[foodList.size()];
			int index = 0;
			
			// Deep copy foodList into a new Queue - to prevent unintended publishing
			Queue<Food> foodQueue = new LinkedList<Food>(foodList);
			
			// Keep attempting to add to Machines until all Food items have been processed
			while(!foodQueue.isEmpty()) {
				Food item = foodQueue.poll();
				Machine tempMachine = foodMachines.get(item);
				
				// If current machine is at full capacity, put item back in Queue and move on
				if(tempMachine.currentCapacity() == 0) {
					foodQueue.add(item);
				} else {
					
					// Machine has open slots - send item to Machine = start cooking this food
					Simulation.logEvent(SimulationEvent.cookStartedFood(curCook, item, curOrder.orderNumber));
					
					// Machine returns a Thread for that specific Food item - store it - incr. local index
					threadList[index] = tempMachine.makeFood(item);
					threadFoods.put(threadList[index], item);
					index++;
				}
			}
			
			try {
				for(int i = 0; i < foodList.size(); i++) {
					// When a Thread joins that means it has finished cooking
					threadList[i].join();
					Simulation.logEvent(SimulationEvent.cookFinishedFood(curCook, threadFoods.get(threadList[i]) , curOrder.orderNumber));
				}
				
			}  catch (InterruptedException e) {
				System.out.println("Error in processFood with Thread " + Thread.currentThread());
				e.printStackTrace();
			}
		}
			
	}
	
	
	
	
	/** Summary: Called by Cook - will notify Customer
	 * -Cook calls this once Order is completed, obtains orderLock (WHY?)
	 * -Generate SimulationEvent saying it has finished
	 * -Put Order in List of finished Orders and notify all Customers waiting on their Orders
	 */
	public void completedOrder(Cook curCook, Order curOrder) {
		synchronized(orderLock) {
			Simulation.logEvent(SimulationEvent.cookCompletedOrder(curCook, curOrder.orderNumber));
			finishedOrders.add(curOrder.orderNumber);
			orderLock.notifyAll();
		}
	}
	
}
