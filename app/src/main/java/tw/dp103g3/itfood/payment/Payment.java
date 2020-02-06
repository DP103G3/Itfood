package tw.dp103g3.itfood.payment;

import java.io.Serializable;

public class Payment implements Serializable {
		private int pay_id;
		private String pay_name;
		private int member_id;
		private String pay_cardnum;
		private String pay_due;
		private String pay_holdername;
		private String pay_securitycode;
		private String pay_phone;
		private int pay_state;
		
		public Payment(int pay_id, String pay_name, int member_id, String pay_cardnum, String pay_due,
				String pay_holdername, String pay_securitycode, String pay_phone, int pay_state) {
			super();
			this.pay_id = pay_id;
			this.pay_name = pay_name;
			this.member_id = member_id;
			this.pay_cardnum = pay_cardnum;
			this.pay_due = pay_due;
			this.pay_holdername = pay_holdername;
			this.pay_securitycode = pay_securitycode;
			this.pay_phone = pay_phone;
			this.pay_state = pay_state;
		}

		public int getPay_id() {
			return pay_id;
		}

		public void setPay_id(int pay_id) {
			this.pay_id = pay_id;
		}

		public String getPay_name() {
			return pay_name;
		}

		public void setPay_name(String pay_name) {
			this.pay_name = pay_name;
		}

		public int getMember_id() {
			return member_id;
		}

		public void setMember_id(int member_id) {
			this.member_id = member_id;
		}

		public String getPay_cardnum() {
			return pay_cardnum;
		}

		public void setPay_cardnum(String pay_cardnum) {
			this.pay_cardnum = pay_cardnum;
		}

		public String getPay_due() {
			return pay_due;
		}

		public void setPay_due(String pay_due) {
			this.pay_due = pay_due;
		}

		public String getPay_holdername() {
			return pay_holdername;
		}

		public void setPay_holdername(String pay_holdername) {
			this.pay_holdername = pay_holdername;
		}

		public String getPay_securitycode() {
			return pay_securitycode;
		}

		public void setPay_securitycode(String pay_securitycode) {
			this.pay_securitycode = pay_securitycode;
		}

		public String getPay_phone() {
			return pay_phone;
		}

		public void setPay_phone(String pay_phone) {
			this.pay_phone = pay_phone;
		}

		public int getPay_state() {
			return pay_state;
		}

		public void setPay_state(int pay_state) {
			this.pay_state = pay_state;
		}
		
}
