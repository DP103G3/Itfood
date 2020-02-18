package tw.dp103g3.itfood.favorite;

public class Favorite {
	private int memberId;
	private int shopId;
	
	public Favorite(int memberId, int shopId) {
		super();
		this.memberId = memberId;
		this.shopId = shopId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}
}
