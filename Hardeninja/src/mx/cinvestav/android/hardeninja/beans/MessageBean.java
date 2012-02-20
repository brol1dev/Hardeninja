package mx.cinvestav.android.hardeninja.beans;

public class MessageBean {

	private String srcNumber;
	private String msgBody;
	private String date;
	private String code;
	
	public MessageBean() {
		srcNumber = null;
		msgBody = null;
		date = null;
	}
	
	public MessageBean(String srcNumber, String msgBody, String date, 
			String code) {
		this.srcNumber = srcNumber;
		this.msgBody = msgBody;
		this.date = date;
		this.code = code;
	}
	
	public String getSrcNumber() {
		return srcNumber;
	}
	
	public void setSrcNumber(String srcNumber) {
		this.srcNumber = srcNumber;
	}
	
	public String getMsgBody() {
		return msgBody;
	}
	
	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
