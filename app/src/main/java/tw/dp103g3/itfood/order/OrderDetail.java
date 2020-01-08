package tw.dp103g3.itfood.order;

import java.io.Serializable;

public class OrderDetail implements Serializable {

	private int od_id;
	private int order_id;
	private int dish_id;
	private int od_count;
	private int od_price;
	private String od_message;
	
	public OrderDetail(int od_id, int order_id, int dish_id, int od_count, int od_price, String od_message) {
		super();
		this.od_id = od_id;
		this.order_id = order_id;
		this.dish_id = dish_id;
		this.od_count = od_count;
		this.od_price = od_price;
		this.od_message = od_message;
	}
	
	public OrderDetail(int dish_id, int od_count) {
		this.dish_id = dish_id;
		this.od_count = od_count;
	}

	public int getOd_id() {
		return od_id;
	}

	public void setOd_id(int od_id) {
		this.od_id = od_id;
	}

	public int getOrder_id() {
		return order_id;
	}

	public void setOrder_id(int order_id) {
		this.order_id = order_id;
	}

	public int getDish_id() {
		return dish_id;
	}

	public void setDish_id(int dish_id) {
		this.dish_id = dish_id;
	}

	public int getOd_count() {
		return od_count;
	}

	public void setOd_count(int od_count) {
		this.od_count = od_count;
	}

	public int getOd_price() {
		return od_price;
	}

	public void setOd_price(int od_price) {
		this.od_price = od_price;
	}

	public String getOd_message() {
		return od_message;
	}

	public void setOd_message(String od_message) {
		this.od_message = od_message;
	}
	
	
	
	

}
