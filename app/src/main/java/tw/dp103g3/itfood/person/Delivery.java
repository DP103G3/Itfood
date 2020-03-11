package tw.dp103g3.itfood.person;

import java.util.Date;

public class Delivery {
	private int del_id;
	private String del_email;
	private String del_password;
	private String del_name;
	private String del_identityid;
	private String del_phone;
	private int del_area;
	private Date del_jointime;
	private Date del_leavetime;
	private Date del_suspendtime;
	private int del_state;

	public Delivery(int del_id, String del_name, String del_password, String del_email, String del_identityid, String del_phone,
			 int del_area,Date del_jointime, Date del_leavetime, Date del_suspendtime, int del_state) {
		super();
		this.del_id = del_id;
		this.del_name = del_name;
		this.del_password = del_password;
		this.del_email = del_email;
		this.del_identityid = del_identityid;
		this.del_phone = del_phone;
		this.del_area = del_area;
		this.del_jointime = del_jointime;
		this.del_leavetime = del_leavetime;
		this.del_suspendtime = del_suspendtime;
		this.del_state = del_state;
	}
	
	public Delivery(String del_name, String del_password, String del_email, String del_phone,
					String del_identityid, int del_state, int del_area) {
		this.del_name = del_name;
		this.del_password = del_password;
		this.del_email = del_email;
		this.del_phone = del_phone;
		this.del_identityid = del_identityid;
		this.del_state = del_state;
		this.del_area = del_area;
	}
	
	public  Delivery(int del_id, int del_state) {
		super();
		this.del_id = del_id;
		this.del_state = del_state;
	}
	

	public int getDelId() {
		return del_id;
	}

	public void setDelId(int del_id) {
		this.del_id = del_id;
	}

	public String getDelName() {
		return del_name;
	}

	public void setDelName(String del_name) {
		this.del_name = del_name;
	}

	public String getDelPassword() {
		return del_password;
	}

	public void setDelPassword(String del_password) {
		this.del_password = del_password;
	}

	public String getDelEmail() {
		return del_email;
	}

	public void setDelEmail(String del_email) {
		this.del_email = del_email;
	}

	public String getDelIdentityid() {
		return del_identityid;
	}

	public void setDelIdentityid(String del_identityid) {
		this.del_identityid = del_identityid;
	}
	
	public String getDelPhone() {
		return del_phone;
	}

	public void setDelPhone(String del_phone) {
		this.del_phone = del_phone;
	}
	
	public int getDelArea() {
		return del_area;
	}

	public void setDelArea(int del_area) {
		this.del_area = del_area;
	}
	
	public Date getDelJoindate() {
		return del_jointime;
	}

	public void setDelJoindate(Date del_jointime) {
		this.del_jointime = del_jointime;
	}
	
	public Date getDelLeavetime() {
		return del_leavetime;
	}

	public void setDelLeavetime(Date del_leavetime) {
		this.del_leavetime = del_leavetime;
	}

	public Date getDelSuspendtime() {
		return del_suspendtime;
	}

	public void setDelSuspendtime(Date del_suspendtime) {
		this.del_suspendtime = del_suspendtime;
	}

	public int getDelState() {
		return del_state;
	}

	public void setDelState(int del_state) {
		this.del_state = del_state;
	}
		
}
