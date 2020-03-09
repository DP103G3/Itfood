package tw.dp103g3.itfood.order;

public class DeliveryMessage {
    private String action;
    private Order order;
    private int areaCode;
    private String sender;
    private String receiver;


    /**
     * @param action
     * @param order
     * @param areaCode
     * @param sender
     * @param receiver
     */
    public DeliveryMessage(String action, Order order, int areaCode, String sender, String receiver) {
        super();
        this.action = action;
        this.order = order;
        this.areaCode = areaCode;
        this.sender = sender;
        this.receiver = receiver;
    }


    /**
     * @param action
     * @param areaCode
     */
    public DeliveryMessage(String action, int areaCode, String sender) {
        super();
        this.action = action;
        this.areaCode = areaCode;
        this.sender = sender;
    }


    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(int areaCode) {
        this.areaCode = areaCode;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


}
