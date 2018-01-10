package cmsc433.p2;

import cmsc433.p2.Food;
import cmsc433.p2.Machine.MachineType;

/**
 * A Machine is used to make a particular Food.  Each Machine makes
 * just one kind of Food.  Each machine has a capacity: it can make
 * that many food items in parallel; if the machine is asked to
 * produce a food item beyond its capacity, the requester blocks.
 * Each food item takes at least item.cookTimeS seconds to produce.
 */

public class Machine {
	
	// Types of machines used in Ratsie's.  Recall that enum types are
	// effectively "static" and "final", so each instance of Machine
	// will use the same MachineType.
	
	public enum MachineType { fountain, fryer, grillPress, oven };
	
	// Converts Machine instances into strings based on MachineType.
	
	public String toString() {
		switch (machineType) {
		case fountain: 		return "Fountain";
		case fryer:			return "Fryer";
		case grillPress:	return "Grill Presss";
		case oven:			return "Oven";
		default:			return "INVALID MACHINE";
		}
	}
	
	private Object machineLock = new Object();
	private Restaurant ratsies = Restaurant.getInstance();
	
	public final MachineType machineType;
	public final Food machineFoodType;
	
	/////// Capacity variables ////////
	public final int totCapacity;
	public int currentCapacity;
	public int currentCapacity() {
		synchronized(machineLock) {
			return this.currentCapacity;
		}
	}
	//////////////////////////////////


	/**
	 * The constructor takes at least the type of the machine,
	 * the Food item it makes, and its capacity.  You may extend
	 * it with other arguments, if you wish.  Notice that the
	 * constructor currently does nothing with the capacity; you
	 * must add code to make use of this field (and do whatever
	 * initialization etc. you need).
	 */
	public Machine(MachineType machineType, Food food, int capacityIn) {
		this.machineType = machineType;
		this.machineFoodType = food;
		this.totCapacity = capacityIn;
		this.currentCapacity = capacityIn;
	}

	/**
	 * This method is called by a Cook in order to make the Machine's
	 * food item.  You can extend this method however you like, e.g.,
	 * you can have it take extra parameters or return something other
	 * than Object.  It should block if the machine is currently at full
	 * capacity.  If not, the method should return, so the Cook making
	 * the call can proceed.  You will need to implement some means to
	 * notify the calling Cook when the food item is finished.
	 * ----- Simulation Events -----
	 * -At startup: 			machineStarting()		* *		
	 * -Starting to make item: 	machineCookingFood()	* *
	 * -Done making item:		machineDoneFood()		* *
	 * -At end of Simulation: 	machineEnding()			* *
	 * 
	 */
	public synchronized Thread makeFood(Food item) {
		//synchronized(machineLock) {
			Thread foodSlot = new Thread(new CookAnItem());
			currentCapacity = currentCapacity() - 1;
			foodSlot.start();
			
			//System.out.println("Running: " + foodSlot.currentThread());
			return foodSlot;
		//}
	}

	
	private class CookAnItem implements Runnable {
		public void run() {
			try {
				// Starting Machine with current Food item 
				Simulation.logEvent(SimulationEvent.machineCookingFood(Machine.this, machineFoodType));
				
				Thread.sleep(machineFoodType.cookTimeS);
				
				synchronized(machineLock) {
					currentCapacity = currentCapacity() + 1;
				}
				
				// Generate machineDoneFood event before thread terminates
				Simulation.logEvent(SimulationEvent.machineDoneFood(Machine.this, machineFoodType));
			} catch(InterruptedException e) { 
				e.printStackTrace();
			}
		}
	}
}