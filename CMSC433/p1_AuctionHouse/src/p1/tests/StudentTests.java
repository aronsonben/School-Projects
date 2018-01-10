package cmsc433.p1.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import cmsc433.p1.AuctionServer;
import cmsc433.p1.ConservativeBidder;
import cmsc433.p1.InsufficientFundsException;
import cmsc433.p1.Item;
import cmsc433.p1.Seller;

public class StudentTests {
	//ConservativeBidder cb1 = new ConservativeBidder(AuctionServer.getInstance(), "Buyer"+1, 100, 20, 150, 2);

	private Random rand = new Random();
	
	// int to keep track of size of server (for items total items submitted)
	private int serverSize = 0;
	
	private int createSellers(AuctionServer server, int numSellers, int submitCase) {
		Seller[] sellers = new Seller[numSellers];
		for(int i=0; i < numSellers; i++) {
            sellers[i] = new Seller(
					/* server = */          server,
					/* sellerName = */      "Seller"+i,
					/* cycles = */          100,
					/* maxSleepTimeMs = */  2,
					/* randomSeed = */      i);
		}
		//Make some items
		List<String> items = new ArrayList<String>();
        for (int i = 0; i < 100; ++i)
        	items.add("Seller" + "#" + i);
        
		int index;
		String item;
		
		int id = 0;
		switch(submitCase)
		{
		case 1: 
			System.out.println("Case 1");
			
			for(int i=0; i < numSellers; i++) {
				index = this.rand.nextInt(items.size());
				item = items.get(index);			
				id = server.submitItem(sellers[0].name(), item, this.rand.nextInt(100), this.rand.nextInt(100) + 100);
				//System.out.println("Added: " + items.get(index));
				if (id != -1) { items.remove(index); }
			}
			
			break;
		case 2:
			System.out.println("Case 2");
			
			for(int i=0; i < numSellers; i++) {
				index = this.rand.nextInt(items.size());
				item = items.get(index);
				id = server.submitItem(sellers[i].name(), item, this.rand.nextInt(100), this.rand.nextInt(100) + 100);
				if (id != -1) { items.remove(index); }
			}
			
			break;
		}
		return id;
	}
	
	
	private void submitItemTest(int numSellers, int submitCase) {
		//Create AuctionServer and a seller
		AuctionServer server = AuctionServer.getInstance();
		int id = createSellers(server,numSellers,submitCase);

		if(id == -1) { 
			fail("Hit server capacity or max seller items");
    	} else {
    		for(int i=0; i < numSellers; i++) {
        		//System.out.println("Increase server size"); 
        		this.serverSize++; 
    		}
    		assertEquals(this.serverSize, numSellers);
    	}
	}
	
	private void getItemsTest(int numSellers) {
		//Create AuctionServer and a seller
		AuctionServer server = AuctionServer.getInstance();
		createSellers(server,numSellers,2);
		
		List<Item> itemList = server.getItems();
		for(int i=0; i < itemList.size(); i++) {
			System.out.println(itemList.get(i).listingID());
		}
		assertEquals(itemList.size(),numSellers);
		
		int size = itemList.size();
		for(int j=0; j < size-1; j++) {
			System.out.println(itemList.get(j));
			itemList.remove(j);
			//try { itemList.remove(j); }
			//catch(IndexOutOfBoundsException e) { System.out.println("Failed" + j); }
			//System.out.println("Passed" + i);
		}
		assertTrue(itemList.isEmpty());
		//assertEquals(server.getItems(),numSellers);
	}
	
	private ConservativeBidder[] createConservativeBidders(AuctionServer server, int numBidders) {
		ConservativeBidder[] csrvBuyers = new ConservativeBidder[numBidders];
        for (int i=0; i<numBidders; ++i)
        {
            csrvBuyers[i] = new ConservativeBidder(
					/* server = */          server,
					/* buyerName = */       "Buyer"+i,
					/* initialCash = */     1000,
					/* cycles = */          20,
					/* maxSleepTimeMs = */  2,
					/* randomSeed = */      i);
        }
        return csrvBuyers;
	}
	
	//ConservativeBidder cb1 = new ConservativeBidder(AuctionServer.getInstance(), "Buyer"+1, 100, 20, 150, 2);
	private void submitBidTest(int numSellers, int numConservBidders, int choice) {
		//Create AuctionServer and a seller
		AuctionServer server = AuctionServer.getInstance();
		createSellers(server,numSellers,2);
		
		switch(choice)
		{
		case 1:
			// test with just one consevative bidder
			ConservativeBidder bidderC0 = new ConservativeBidder(server, "Buyer0", 1000, 20, 2, this.rand.nextInt());
			int price = server.itemPrice(this.rand.nextInt(numSellers));
			if(!server.submitBid(bidderC0.name(), 0, price+1)) {
				fail("failed");
			}
			int bidstat = server.checkBidStatus(bidderC0.name(), 0);
			
			//try to pay for item even though it is open
			if(bidstat == 2) {
				try {
					//server.payForItem(bidderC0.name(), 0, price+1);
					
					System.out.println("tried to pay but bidding still open");
					// tried to pay but bidding still open
					assertEquals(server.payForItem(bidderC0.name(), 0, price+1),null);
					
				} catch(InsufficientFundsException e) {
					System.out.println(bidderC0.name() + " was unable to pay up and is retiring in disgrace.");
					return;
				}
			}
			System.out.println(bidstat);
			break;
		case 2:
			// test with just one consevative bidder
			ConservativeBidder bidderC1 = new ConservativeBidder(server, "Buyer1", 1000, 20, 2, this.rand.nextInt());
			ConservativeBidder bidderC2 = new ConservativeBidder(server, "Buyer2", 1000, 20, 2, this.rand.nextInt());
			int price1 = server.itemPrice(this.rand.nextInt(numSellers));
			if(!server.submitBid(bidderC1.name(), 0, price1-1)) { fail("submitted too low");}
			server.submitBid(bidderC1.name(), 0, price1);
			server.submitBid(bidderC2.name(), 0, price1+1);

			int bidstat1 = server.checkBidStatus(bidderC1.name(), 0);
			while(bidstat1 != 1) {
				bidstat1 = server.checkBidStatus(bidderC1.name(), 0);
				server.submitBid(bidderC1.name(), 0, price1+1);
				server.submitBid(bidderC2.name(), 0, price1+1);
			}
			if(bidstat1 == 1) {
				System.out.println("HERE");
			}
			break;
		case 3:
			ConservativeBidder[] cbidders = createConservativeBidders(server,numConservBidders);
			for(int i=0; i < cbidders.length-1; i++) {
				//server.submitBid(cbidders[i].name(), int listingID, int biddingAmount)
			}
			break;
		default:
			fail("fail");
			break;
		}
	}
	
	
	private void getItemPriceTest(int numSellers, int choice) {
		//Create AuctionServer and a seller
		AuctionServer server = AuctionServer.getInstance();
		
		if(choice == 1) {
			Seller seller0 = new Seller(server,"Seller0",20,20,this.rand.nextInt());
			String item = "Seller0#1";
			server.submitItem(seller0.name(), item, 10, this.rand.nextInt(100) + 100);
			List<Item> items = server.getItems();
			int price = server.itemPrice(0);
			System.out.println(price);
		} else if(choice == 2) {
			// should fail b/c item was not added
			Seller seller0 = new Seller(server,"Seller0",20,20,this.rand.nextInt());
			String item = "Seller0#1";
			server.submitItem(seller0.name(), item, 10, this.rand.nextInt(100) + 100);
			List<Item> items = server.getItems();
			int price = server.itemPrice(1);
			if(price==-1) { fail("Failed"); }
		} else {
			// some crap if i want to test with many sellers
			createSellers(server,numSellers,2);
			List<Item> items = server.getItems();
			int price = -1;
			if(items.size() > 0) {
				int index = rand.nextInt(items.size());
	
				Item item = items.get(index);
				items.remove(index);
				
				price = server.itemPrice(item.listingID());
			}
			if( price == -1) {
				fail("Did not update price correctly");
			} 
		}
		

	}
	
	
	@Test
	public void testSubmitItem() {
		// sanity check
		submitItemTest(5,1);
		this.serverSize = 0;
	}
	
	@Test
	public void testSubmitItem2() {
		// Test for maxSellerItems
		submitItemTest(21,1);
		this.serverSize = 0;
	}
	
	@Test
	public void testSubmitItem3() {
		// Test for maxSellerItems
		submitItemTest(81,2);
		this.serverSize = 0;
	}
	
	@Test
	public void testGetItems() {
		getItemsTest(50);
	}
	
	@Test
	public void testCheckPrice() {
		getItemPriceTest(1, 1);
	}
	
	@Test
	public void testCheckPrice2() {
		//should fail
		getItemPriceTest(1, 2);
	}
	
	@Test
	public void testSubmitBid1() {
		submitBidTest(1, 1, 1);
	}
	
	@Test
	public void testSubmitBid2() {
		submitBidTest(1, 1, 2);
	}
	
	
}
