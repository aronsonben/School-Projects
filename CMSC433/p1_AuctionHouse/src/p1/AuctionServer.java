package cmsc433.p1;

/**
 *  @author Ben Aronson
 *  Last Updated 2-19-2017
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;



public class AuctionServer
{
	/**
	 * Singleton: the following code makes the server a Singleton. You should
	 * not edit the code in the following noted section.
	 * 
	 * For test purposes, we made the constructor protected. 
	 */

	/* Singleton: Begin code that you SHOULD NOT CHANGE! */
	protected AuctionServer()
	{
	}

	public static AuctionServer instance = new AuctionServer();

	public static AuctionServer getInstance()
	{
		return instance;
	}

	/* Singleton: End code that you SHOULD NOT CHANGE! */





	/* Statistic variables and server constants: Begin code you should likely leave alone. */


	/**
	 * Server statistic variables and access methods:
	 */
	private int soldItemsCount = 0;
	private int revenue = 0;
	private int uncollectedRevenue = 0;

	public int soldItemsCount()
	{
		synchronized (instanceLock) {
			return this.soldItemsCount;
		}
	}

	public int revenue()
	{
		synchronized (instanceLock) {
			return this.revenue;
		}
	}
	
	public int uncollectedRevenue () {
		synchronized (instanceLock) {
			return this.uncollectedRevenue;
		}
	}



	/**
	 * Server restriction constants:
	 */
	public static final int maxBidCount = 10; // The maximum number of bids at any given time for a buyer.
	public static final int maxSellerItems = 20; // The maximum number of items that a seller can submit at any given time.
	public static final int serverCapacity = 80; // The maximum number of active items at a given time.


	/* Statistic variables and server constants: End code you should likely leave alone. */



	/**
	 * Some variables we think will be of potential use as you implement the server...
	 */
	// TODO: variable list
	// List of items currently up for bidding (will eventually remove things that have expired).
	private List<Item> itemsUpForBidding = new ArrayList<Item>();


	// The last value used as a listing ID.  We'll assume the first thing added gets a listing ID of 0.
	private int lastListingID = -1; 

	// List of item IDs and actual items.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Item> itemsAndIDs = new HashMap<Integer, Item>();

	// List of itemIDs and the highest bid for each item.  This is a running list with everything ever bid upon.
	private HashMap<Integer, Integer> highestBids = new HashMap<Integer, Integer>();

	// List of itemIDs and the person who made the highest bid for each item.   This is a running list with everything ever bid upon.
	private HashMap<Integer, String> highestBidders = new HashMap<Integer, String>(); 
	
	// List of Bidders who have been permanently banned because they failed to pay the amount they promised for an item. 
	private HashSet<String> blacklist = new HashSet<String>();
	
	// List of sellers and how many items they have currently up for bidding.
	private HashMap<String, Integer> itemsPerSeller = new HashMap<String, Integer>();

	// List of buyers and how many items on which they are currently bidding.
	private HashMap<String, Integer> itemsPerBuyer = new HashMap<String, Integer>();

	// List of itemIDs that have been paid for. This is a running list including everything ever paid for.
	private HashSet<Integer> itemsSold = new HashSet<Integer> ();

	// Object used for instance synchronization if you need to do it at some point 
	// since as a good practice we don't use synchronized (this) if we are doing internal
	// synchronization.
	private Object instanceLock = new Object(); 









	/*
	 *  The code from this point forward can and should be changed to correctly and safely 
	 *  implement the methods as needed to create a working multi-threaded server for the 
	 *  system.  If you need to add Object instances here to use for locking, place a comment
	 *  with them saying what they represent.  Note that if they just represent one structure
	 *  then you should probably be using that structure's intrinsic lock.
	 */


	/**
	 * Attempt to submit an <code>Item</code> to the auction
	 * @param sellerName Name of the <code>Seller</code>
	 * @param itemName Name of the <code>Item</code>
	 * @param lowestBiddingPrice Opening price
	 * @param biddingDurationMs Bidding duration in milliseconds
	 * @return A positive, unique listing ID if the <code>Item</code> listed successfully, otherwise -1
	 */
	public synchronized int submitItem(String sellerName, String itemName, int lowestBiddingPrice, int biddingDurationMs)
	{
		// TODO: submitItem
		
		int serverSize = itemsUpForBidding.size();
		
		if( serverSize >= serverCapacity) {
			return -1;
		} else {
			
			//System.out.println(newID);
			
			if( !itemsPerSeller.containsKey(sellerName) ) {
				// Seller has no listed items, add new seller to list selling only 1 item
				//System.out.println("Didn't exist");
				int newID = ++lastListingID;
				Item newItem = new Item(sellerName, itemName, newID, lowestBiddingPrice, biddingDurationMs);
				
				itemsPerSeller.put(sellerName, 1);
				itemsUpForBidding.add(newItem);
				itemsAndIDs.put(newID, newItem);
				highestBids.put(newID, lowestBiddingPrice);
				
				
				return newID;
			} else {
			
				// checking seller items
				int sellerItems = itemsPerSeller.get(sellerName);
				//System.out.println("sellerName:" + sellerName + "," + sellerItems);
				if( sellerItems >= maxSellerItems ) { return -1; }
				
				int newID = ++lastListingID;
				Item newItem = new Item(sellerName, itemName, newID, lowestBiddingPrice, biddingDurationMs);
				sellerItems++;
				//System.out.println(sellerItems);
				
				itemsUpForBidding.add(newItem);
				itemsAndIDs.put(newID, newItem);
				highestBids.put(newID, lowestBiddingPrice);
				itemsPerSeller.put(sellerName, sellerItems);
				//System.out.println("seller items2: " + sellerItems);
				
				return newID;
			}
		}
		// Some reminders:
		//   *Make sure there's room in the auction site.
		//   *If the seller is a new one, add them to the list of sellers.
		//   *If the seller has too many items up for bidding, don't let them add this one.
		//   *Don't forget to increment the number of things the seller has currently listed.
	}

	
	/**
	 * Get all <code>Items</code> active in the auction
	 * @return A copy of the <code>List</code> of <code>Items</code>
	 */
	public synchronized List<Item> getItems()
	{
		// TODO: getItems()
		List<Item> copy = new ArrayList<Item>();
		for(int i=0; i < itemsUpForBidding.size(); i++) {
			copy.add(itemsUpForBidding.get(i));
		}
		return copy;
		
		// Some reminders:
		//    Don't forget that whatever you return is now outside of your control.
	}
	// 2/20: Finished. Note: "Reminders" means that this is published AKA available to other parts of program (other threads)


	/**
	 * Attempt to submit a bid for an <code>Item</code>
	 * @param bidderName Name of the <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @param biddingAmount Total amount to bid
	 * @return True if successfully bid, false otherwise
	 */
	public synchronized boolean submitBid(String bidderName, int listingID, int biddingAmount)
	{
		// TODO: submitBid()
		if( !itemsAndIDs.containsKey(listingID) || (biddingAmount < 0) ) {
			// item does not exist
			return false;
		} 
		Item current = itemsAndIDs.get(listingID);
		
		if( !itemsUpForBidding.contains(current) || current.biddingOpen()==false) {
			System.out.println("failed test " + current.name());
			return false;
		} 
		
		if( blacklist.contains(bidderName) ) {
			return false;
		}
		
		int highestBid = highestBids.get(listingID);


		// If the highestBid is the opening bid price, new bidAmount can be >= openingBid
		if( biddingAmount < highestBid ) {
			//ystem.out.println("less than lowest bidding price on first try");
			return false;
		}
		
//		// If highestBid is not opening price, then bidding amount MUST be > highestBid
//		if( (highestBid > current.lowestBiddingPrice()) && (biddingAmount <= highestBid) ) {	
//			return false;
//		}
		
		// Highest bidder is not the same person whos placing a bid
		if(!highestBidders.containsKey(listingID) || (highestBidders.containsKey(listingID) && 
				!highestBidders.get(listingID).equals(bidderName))) {
			
			if (highestBidders.containsKey(listingID)) {
				// First time buyer
				if( !itemsPerBuyer.containsKey(bidderName)) {
					
					// Decrement previous buyer
					String highestBidder = highestBidders.get(listingID);
					int formerBidderBids = itemsPerBuyer.get(highestBidder);
					itemsPerBuyer.put(highestBidder, formerBidderBids-1);
					
					// first time current bidder is bidding - initialize new buyer
					System.out.println("First time");
					itemsPerBuyer.put(bidderName, 1);
					highestBidders.put(listingID, bidderName);
					highestBids.put(listingID, biddingAmount);
					return true;
					
				// Existing Buyer
				} else {
					// Check bid count
					if((itemsPerBuyer.get(bidderName) >= maxBidCount)) {
						return false;
					} 
					
					String highestBidder = highestBidders.get(listingID);
					int formerBidderBids = itemsPerBuyer.get(highestBidder);
					itemsPerBuyer.put(highestBidder, formerBidderBids-1);
					
					int curBids = itemsPerBuyer.get(bidderName);
					curBids++;
					itemsPerBuyer.put(bidderName, curBids);
					
					highestBids.put(listingID, biddingAmount);
					highestBidders.put(listingID, bidderName);
					return true;
				}
			} else {
				// First time buyer
				if( !itemsPerBuyer.containsKey(bidderName)) {
					
					// first time current bidder is bidding - initialize new buyer
					//System.out.println("First time");
					itemsPerBuyer.put(bidderName, 1);
					highestBidders.put(listingID, bidderName);
					highestBids.put(listingID, biddingAmount);
					return true;
					
				// Existing Buyer
				} else {
					// Check bid count
					if((itemsPerBuyer.get(bidderName) >= maxBidCount)) {
						return false;
					} 
					
					int curBids = itemsPerBuyer.get(bidderName);
					curBids++;
					itemsPerBuyer.put(bidderName, curBids);
					
					highestBids.put(listingID, biddingAmount);
					highestBidders.put(listingID, bidderName);
					return true;
				}
			}
			
		} 
		return false; 
		
		// Some reminders:
		//   *See if the item exists.
		//   *See if it can be bid upon.
		//   *See if this bidder has too many items in their bidding list.
		//   *Make sure the bidder has not been blacklisted
		//   *Get current bidding info.
		//   *See if they already hold the highest bid.
		//   *See if the new bid isn't better than the existing/opening bid floor.
		//   Decrement the former winning bidder's count
		//   Put your bid in place
	}
	
	
	

	/**
	 * Check the status of a <code>Bidder</code>'s bid on an <code>Item</code>
	 * @param bidderName Name of <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return 1 (success) if bid is over and this <code>Bidder</code> has won<br>
	 * 2 (open) if this <code>Item</code> is still up for auction<br>
	 * 3 (failed) If this <code>Bidder</code> did not win or the <code>Item</code> does not exist
	 */
	public synchronized int checkBidStatus(String bidderName, int listingID)
	{
		// TODO: checkBidStatus()
		final int SUCCESS = 1, OPEN = 2, FAILURE = 3;
		
		if( !itemsAndIDs.containsKey(listingID) ) {
			// listingID does not match any item ever listed by a seller
			return FAILURE;
		} 
		
		Item current = itemsAndIDs.get(listingID);
		
		if( current.biddingOpen() == true ) {
			return OPEN;
		} else if( !itemsUpForBidding.contains(current) ) {
			//System.out.println("else if");
			
			if(!highestBidders.containsKey(listingID)) {
				// wasn't even a bid on it
				return FAILURE;
			} else {
				if( !highestBidders.get(listingID).equals(bidderName) ) {
					//System.out.println("Failure");
					return FAILURE;
				} else {
					//System.out.println("Success");
					return SUCCESS;
				}
			}
			
		} else {
			System.out.println("else");
			//remove item from items currently being bid on
			itemsUpForBidding.remove(current);
			
			// Decrease open bids for current seller
			String seller = current.seller();
			int sellerItems = itemsPerSeller.get(seller) - 1;
			itemsPerSeller.put(seller, sellerItems);
			
			if( !highestBidders.containsKey(listingID) ) {
				System.out.println("Not bid on");
				return FAILURE;
			}
			
			// remove one bid item from winner (that is not current bidder)
			String winningBidder = highestBidders.get(listingID);
			if( itemsPerBuyer.get(winningBidder) > 0 ) {
				int lessItem = itemsPerBuyer.get(winningBidder) - 1;
				itemsPerBuyer.put(winningBidder, lessItem);
			}
			
			int highBid = highestBids.get(listingID);
			
			uncollectedRevenue = uncollectedRevenue() + highBid;
			//System.out.println("uncollectedRevenue + " + highBid + " = " + uncollectedRevenue);
			
			if( !winningBidder.equals(bidderName) ) {
				return FAILURE;
			} else {
				return SUCCESS;
			}
		}
	
		// Some reminders:
		//   If the bidding is closed, clean up for that item.
		//     *Remove item from the list of things up for bidding.
		//     *Decrease the count of items being bid on by the winning bidder if there was any...
		//     *Update the number of open bids for this seller
		//     *If the item was sold to someone, update the uncollectedRevenue field appropriately
	}
	/* 2/20:
	 * 	if: bidding is still open = OPEN
	 *  else if: item is not being bid on currently = FAILED {might not need}
	 *  		 -do not have to update anything because the item never existed 
	 *  else: bidding is closed = SUCCESS || FAILED
	 *    if: the name of the highest bidder does NOT match current bidder name = FAILED
	 *    else: name of highest bidder does equal current bidder name = SUCCESS
	 * 2/23: put lock around whole thing
	 */

	/**
	 * Check the current bid for an <code>Item</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return The highest bid so far or the opening price if there is no bid on the <code>Item</code>,
	 * or -1 if no <code>Item</code> with the given listingID exists
	 */
	public synchronized int itemPrice(int listingID)
	{
		// TODO: itemPrice()
		if( !(highestBids.containsKey(listingID)) ) {
			return -1;
		} else { 
			return highestBids.get(listingID);
		}
		
		
		// Remember: once an item has been purchased, this method should continue to return the
		// highest bid, even if the buyer paid more than necessary for the item or if the buyer
		// is subsequently blacklisted
	}
	// 2/20: Finished(?)
	

	/**
	 * Check whether an <code>Item</code> has a bid on it
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return True if there is no bid or the <code>Item</code> does not exist, false otherwise
	 */
	public synchronized boolean itemUnbid(int listingID) {
		// TODO: itemUnbid()
		synchronized (instanceLock) {
			Item it = itemsAndIDs.get(listingID);
			if( !(itemsUpForBidding.contains(it)) || highestBids.get(listingID)==it.lowestBiddingPrice() ) {
				// If item does not exist in list of current items, it is unbid = true;
				// If current highest bid is still the starting bid price, it is unbid = true;
				// -- This is true even if it was bid on by a blacklisted bidder and reset!
				return true;
			} else {
				return false;
			}
		}
	}
	/* 2/20: Does this include items that have already been sold? I guess that would be false since there was a bid on it.
	 * --Using "highestBids" since the listingID would only exist there if there absolutely is a bid on it. Otherwise it wouldn't appear there.
	 * 
	 * 2/21: See Piazza post @34
	 * 	-- Got rid of "if( !(highestBids.containsKey(listingID)) ) {"
	 * 
	*/
	
	
	/**
	 * Pay for an <code>Item</code> that has already been won.
	 * @param bidderName Name of <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @param amount The amount the <code>Bidder</code> is paying for the item 
	 * @return The name of the <code>Item</code> won, or null if the <code>Item</code> was not won by the <code>Bidder</code> or if the <code>Item</code> did not exist
	 * @throws InsufficientFundsException If the <code>Bidder</code> did not pay at least the final selling price for the <code>Item</code>
	 */
	public synchronized String payForItem (String bidderName, int listingID, int amount) throws InsufficientFundsException {
		// TODO: payForItem()
		
		if( !itemsAndIDs.containsKey(listingID) ) {
			// item does not exist
			//System.out.println("BAD-pfi1");
			return null;
		}
		Item current = itemsAndIDs.get(listingID);		
		
		if( current.biddingOpen() == true || itemsSold.contains(listingID) ) {
			return null;
		} else if( !highestBidders.get(listingID).equals(bidderName) ) {
			return null;
		} else {
			
			int highestBid = highestBids.get(listingID);
			System.out.println(highestBid);
			System.out.println(amount);
			System.out.println("uncollectedRevenue - " + highestBid + " = " + uncollectedRevenue);
			
			// buyer is correct individual and can or cannot afford item
			if( amount >= highestBid ) {
				// current bidder is winner and paid >= current highest bid
				
				//System.out.println("uncollectedRevenue - " + highestBid + " = " + uncollectedRevenue);
				
				itemsSold.add(listingID);
				soldItemsCount++;
				revenue = revenue() + amount;
				uncollectedRevenue = uncollectedRevenue() - highestBid;
				
				System.out.println("uncollectedRevenue2 - " + highestBid + " = " + uncollectedRevenue);
				
				//highestBids.put(listingID, amount);
				return current.name();
			} else {
				// current bidder is winner but did not pay enough = blacklist bidder
				blacklist.add(bidderName);
				
				int buyerItems = itemsPerBuyer.get(bidderName);
				itemsPerBuyer.put(bidderName, 0);
				
				
				for(int i=0; i < itemsUpForBidding.size()-1; i++) {
					//if(buyerItems==0) { break; }
					
					Item tmp = itemsUpForBidding.get(i);
					int tmpID = tmp.listingID();
					
					if(highestBidders.containsKey(tmpID)) {
						// If does not contain key, it obviously was not bid on by current bidder
						
						if(highestBidders.get(tmpID).equals(bidderName)) {
							// Do not need to check if bidding is still open since if the item appears here it
							//   must be still active b/c it is in 'getItems()' list (itemsUpForBidding)
							
							//update uncollectedRevenue based on this item
							//uncollectedRevenue = uncollectedRevenue() - highestBids.get(tmpID);
							
							highestBidders.remove(tmpID, bidderName);
							highestBids.put(tmpID, tmp.lowestBiddingPrice());
							// now has no highest bidders b/c no one has bid on it && reset highestBid to the item's lowest bidding price
							
							// Keeping track of this so I can stop if/when it reaches 0 (might not be needed)
							//buyerItems--; 
						}
					}
				}
				System.out.println("uncollectedRevenue3 - " + highestBid + " = " + uncollectedRevenue);
				
				throw new InsufficientFundsException();
			}				
			
		}
		
		
		
		
		

		// Remember:
		// - Check to make sure the buyer is the correct individual and can afford the item
		// - If the purchase is valid, update soldItemsCount, revenue, and uncollectedRevenue
		// - If the amount tendered is insufficient, cancel all active bids held by the buyer, 
		//   add the buyer to the blacklist, and throw an InsufficientFundsException 
	}
	/*
		int status = checkBidStatus(bidderName, listingID);
		
		Item current = itemsAndIDs.get(listingID);
		
		if( status==3 || status==2 ) {
			// Failed
			return null;
		} else {
			
			if( amount >= itemPrice(listingID) ) {
				// current bidder is winner and paid >= current highest bid
				
				// update highestBidder
				highestBids.put(current.listingID(), amount);
				
				soldItemsCount++;
				revenue += amount;
				uncollectedRevenue -= amount;
				
				// for testing
				System.out.println("Just paid and collected: " + revenue);
				return current.name();
			} else {
				// current bidder is winner but did not pay enough = blacklist bidder
				
				blacklist.add(bidderName);
				
				int buyerItems = itemsPerBuyer.get(bidderName);
				itemsPerBuyer.put(bidderName, 0);
				
				
				for(int i=0; i < itemsUpForBidding.size()-1; i++) {
					if(buyerItems==0) { break; }
					
					System.out.println("BAD");
					
					Item tmp = itemsUpForBidding.get(i);
					int tmpID = tmp.listingID();
					
					if(highestBidders.get(tmpID).equals(bidderName)) {
						// Do not need to check if bidding is still open since if the item appears here it
						//   must be still active b/c it is in 'getItems()' list (itemsUpForBidding)
							
						highestBidders.remove(tmpID, bidderName);
						highestBids.put(tmpID, tmp.lowestBiddingPrice());
						// now has no highest bidders b/c no one has bid on it && reset highestBid to the item's lowest bidding price

						// Keeping track of this so I can stop if/when it reaches 0 (might not be needed)
						buyerItems--; 
					}
				}
				
				throw new InsufficientFundsException();
			}			
		}
	 */

}
