package tw.dp103g3.itfood.shop;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Shop implements Serializable {
	private int id;
	private String email;
	private String password;
	private String name;
	private String phone;
	private String tax;
	private String address;
	private double latitude;
	private double longitude;
	private int area;
	private byte state;
	private String info;
	private Date jointime;
	private Date suspendtime;
	private int ttscore;
	private int ttrate;
	private List<String> types;

	public Shop(int id, String email, String password, String name, String phone, String tax, String address, double latitude,
			double longitude, int area, byte state, String info, Date jointime, Date suspendtime, int ttscore,
			int ttrate) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
		this.name = name;
		this.phone = phone;
		this.tax = tax;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.area = area;
		this.state = state;
		this.info = info;
		this.jointime = jointime;
		this.suspendtime = suspendtime;
		this.ttscore = ttscore;
		this.ttrate = ttrate;
	}

	public Shop(int id, String email,String password, String name,String phone, String address, double latitude, double longitude, int area, byte state,
			String info, Date jointime, int ttscore, int ttrate) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
		this.name = name;
		this.phone = phone;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.area = area;
		this.state = state;
		this.info = info;
		this.jointime = jointime;
		this.ttscore = ttscore;
		this.ttrate = ttrate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Shop)) {
			return false;
		}
		Shop shop = (Shop) obj;
		return this.getId() == shop.getId();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTax() {
		return tax;
	}

	public void setTax(String tax) {
		this.tax = tax;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public int getArea() {
		return area;
	}

	public void setArea(int area) {
		this.area = area;
	}

	public byte getState() {
		return state;
	}

	public void setState(byte state) {
		this.state = state;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public Date getJointime() {
		return jointime;
	}

	public void setJointime(Date jointime) {
		this.jointime = jointime;
	}

	public Date getSuspendtime() {
		return suspendtime;
	}

	public void setSuspendtime(Date suspendtime) {
		this.suspendtime = suspendtime;
	}

	public int getTtscore() {
		return ttscore;
	}

	public void setTtscore(int ttscore) {
		this.ttscore = ttscore;
	}

	public int getTtrate() {
		return ttrate;
	}

	public void setTtrate(int ttrate) {
		this.ttrate = ttrate;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

}
