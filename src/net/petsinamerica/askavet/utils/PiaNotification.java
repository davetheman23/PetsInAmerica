package net.petsinamerica.askavet.utils;

public class PiaNotification {
	private long id;
	private long type;
	private String subject;
	private String message;
	
	public long getId(){
		return id;
	}
	public void setId(long id){
		this.id = id;
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
	
	public String getMessage(){
		return message;
	}
	public void setMessage(String message){
		this.message = message;
	}
	
	@Override
	public String toString() {
		return ("subject:" + subject + "; message:" + message);
	}
}
