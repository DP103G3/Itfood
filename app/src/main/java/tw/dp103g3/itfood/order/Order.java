package tw.dp103g3.itfood.order;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import tw.dp103g3.itfood.address.Address;
import tw.dp103g3.itfood.shop.Shop;


public class Order implements Serializable {
	private int order_id;
	private int del_id;
	private Shop shop;
	private int mem_id;
	private int pay_id;
	private int order_state;
	private int sp_id;
	private Date order_time;
	private Date order_ideal;
	private Date order_delivery;
//	private int adrs_id;
	private String order_name;
	private String order_phone;
	private int order_ttprice;
	private int order_area;
	private int order_type;
	private List<OrderDetail> orderDetails;
	private Address address;
	private boolean isExpanded;
	
	public Order(Shop shop, int mem_id, int del_id, int pay_id, int sp_id, Date order_ideal, Date order_delivery, 
			Address address, String order_name, String order_phone, int order_ttprice, int order_type, 
			List<OrderDetail> orderDetails) {
		super();
		this.del_id = del_id;
		this.shop = shop;
		this.mem_id = mem_id;
		this.pay_id = pay_id;
		this.sp_id = sp_id;
		this.order_ideal = order_ideal;
		this.order_delivery = order_delivery;
		this.address = address;
		this.order_name = order_name;
		this.order_phone = order_phone;
		this.order_ttprice = order_ttprice;
		this.order_type = order_type;
		this.orderDetails = orderDetails;
	}
	
	
//	public Order(int order_id, Shop shop, int mem_id,int del_id, int pay_id, int sp_id, Date order_ideal,
//			Date order_time, Date order_delivery, int adrs_id, String order_name, String order_phone, int order_ttprice, 
//			int order_area, int order_state, int order_type, List<OrderDetail> orderDetails) {
//		super();
//		this.order_id = order_id;
//		this.del_id = del_id;
//		this.shop = shop;
//		this.mem_id = mem_id;
//		this.pay_id = pay_id;
//		this.sp_id = sp_id;
//		this.order_ideal = order_ideal;
//		this.order_time = order_time;
//		this.order_delivery = order_delivery;
//		this.adrs_id = adrs_id;
//		this.order_name = order_name;
//		this.order_phone = order_phone;
//		this.order_ttprice = order_ttprice;
//		this.order_area = order_area;
//		this.order_state = order_state;
//		this.order_type = order_type;
//		this.orderDetails = orderDetails;
//	}
	
	public Order(int order_id, Shop shop, int mem_id,int del_id, int pay_id, int sp_id, Date order_ideal,
			Date order_time, Date order_delivery, Address address, String order_name, String order_phone, int order_ttprice, 
			int order_area, int order_state, int order_type, List<OrderDetail> orderDetails) {
		super();
		this.order_id = order_id;
		this.del_id = del_id;
		this.shop = shop;
		this.mem_id = mem_id;
		this.pay_id = pay_id;
		this.sp_id = sp_id;
		this.order_ideal = order_ideal;
		this.order_time = order_time;
		this.order_delivery = order_delivery;
		this.address = address;
		this.order_name = order_name;
		this.order_phone = order_phone;
		this.order_ttprice = order_ttprice;
		this.order_area = order_area;
		this.order_state = order_state;
		this.order_type = order_type;
		this.orderDetails = orderDetails;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + order_id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		if (order_id != other.order_id)
			return false;
		return true;
	}

	public int getOrder_id() {
		return order_id;
	}

	public void setOrder_id(int order_id) {
		this.order_id = order_id;
	}

	public int getDel_id() {
		return del_id;
	}

	public void setDel_id(int del_id) {
		this.del_id = del_id;
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

	public int getMem_id() {
		return mem_id;
	}

	public void setMem_id(int mem_id) {
		this.mem_id = mem_id;
	}

	public int getPay_id() {
		return pay_id;
	}

	public void setPay_id(int pay_id) {
		this.pay_id = pay_id;
	}

	public int getOrder_state() {
		return order_state;
	}

	public void setOrder_state(int order_statu) {
		this.order_state = order_statu;
	}

	public int getSp_id() {
		return sp_id;
	}

	public void setSp_id(int sp_id) {
		this.sp_id = sp_id;
	}

	public Date getOrder_time() {
		return order_time;
	}

	public void setOrder_time(Date order_time) {
		this.order_time = order_time;
	}

	public Date getOrder_ideal() {
		return order_ideal;
	}

	public void setOrder_ideal(Date order_ideal) {
		this.order_ideal = order_ideal;
	}

	public Date getOrder_delivery() {
		return order_delivery;
	}

	public void setOrder_delivery(Date order_delivery) {
		this.order_delivery = order_delivery;
	}

//	public int getAdrs_id() {
//		return adrs_id;
//	}
//
//	public void setAdrs_id(int adrs_id) {
//		this.adrs_id = adrs_id;
//	}

	public String getOrder_name() {
		return order_name;
	}

	public void setOrder_name(String order_name) {
		this.order_name = order_name;
	}

	public String getOrder_phone() {
		return order_phone;
	}

	public void setOrder_phone(String order_phone) {
		this.order_phone = order_phone;
	}

	public int getOrder_ttprice() {
		return order_ttprice;
	}

	public void setOrder_ttprice(int order_ttprice) {
		this.order_ttprice = order_ttprice;
	}

	public int getOrder_area() {
		return order_area;
	}

	public void setOrder_area(int order_area) {
		this.order_area = order_area;
	}

	public int getOrder_type() {
		return order_type;
	}

	public void setOrder_type(int order_type) {
		this.order_type = order_type;
	}

	public List<OrderDetail> getOrderDetails() {
		return orderDetails;
	}

	public void setOrderDetails(List<OrderDetail> orderDetails) {
		this.orderDetails = orderDetails;
	}


	public Address getAddress() {
		return address;
	}


	public void setAddress(Address address) {
		this.address = address;
	}

	public boolean isExpanded() {
		return isExpanded;
	}

	public void setExpanded(boolean expanded) {
		this.isExpanded = expanded;
	}

	@Override
	public String toString() {
		return getOrder_id() + " + " + getOrder_state();
	}
}
