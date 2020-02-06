package tw.dp103g3.itfood.order;

public class OrderMessage {
	private Order order;
	private String receiver;
	
	public OrderMessage(Order order, String receiver) {
		super();
		this.order = order;
		this.receiver = receiver;
	}
	
	public Order getOrder() {
		return order;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
}
