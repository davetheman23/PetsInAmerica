package net.petsinamerica.askavet.utils;


public class PiaNotification {
	public static final int STATUS_RECEIVED = 0;
	public static final int STATUS_VIEWED = 1;
	
	/** id of the notification */
	private long id;
	/** id of the user */
	private String uid;	
	/** type of the notification message */
	private long type;
	/** a subject line for the notification */
	private String subject;
	/** additional content attached to the notification */
	private String content;
	/** the time the device received the notification */
	private String created_at;
	/** the time the user viewed the notification*/
	private int status;
	
	public long getId(){
		return id;
	}
	public void setId(long id){
		this.id = id;
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public long getType(){
		return type;
	}
	public void setType(long type){
		this.type = type;
	} 
	
	public String getSubject(){
		return subject;
	}
	public void setSubject(String subject){
		this.subject = subject;
	}
	
	public String getContent(){
		return content;
	}
	public void setContent(String content){
		this.content = content;
	}
	
	public String getCreated_at() {
		return created_at;
	}
	
	public void setCreated_at(String date) {
		this.created_at = date;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return ("subject:" + subject + "; content:" + content+ "; created_at:" + created_at.toString());
	}
}
