package cmsc433.p2;

import java.util.List;

public class Order {
	
	public List<Food> orderList;
	public final int orderNumber;
	
	public Order(List<Food> order, int orderNum) {
		this.orderList = order;
		this.orderNumber = orderNum;
	}
	
}
