package net.petsinamerica.askavet.utils;

public interface Constants {
	
	public static final String APPTAG = "AskaVet";
	
	public static final boolean DEBUG = true;
	
	public static final int NO_ERROR = 0; 
	
	/*
	 * for internet connections
	 */
	public static final String PREFERENCES_NAME = "net_pets_in_america_askavet";
	
	public static final String URL_BASE = "http://petsinamerica.net/new/api";
	
	public static final String URL_BLOGCN = URL_BASE + "/" + "blogCN/";
	
	public static final String URL_ARTICLE_API = URL_BASE + "/" + "article/";
	
	public static final String URL_USERINFO = URL_BASE + "/" + "userinfo/";
	
	public static final String URL_LOGIN = URL_BASE + "/" + "login/";
	
	public static final String URL_ENQUIRY = URL_BASE + "/" + "publicQueryList/";
	
	public static final String URL_MYENQUIRY = URL_BASE + "/" + "userQueryList/";
	
	public static final String URL_NEWENQUIRY = URL_BASE + "/" + "newQuery/";
	
	public static final String URL_PRODUCTLIST = URL_BASE + "/" + "productlist/";
	
	public static final String URL_USERPETS = URL_BASE + "/" + "userpets/";
	
    public static final String TAG_USERNAME = "username";
    
  	public static final String TAG_PASSWORD = "password";
  	
  	public static final String TAG_USERID = "userid";
  	
  	public static final String TAG_USERTOKEN = "token";
  	
  	/*
  	 * for storage 
  	 */
  	public static final String PIA_ROOT_DIR = "AskaVet";
  	
  	public static final String TEMP_SUBDIR = "temp";

}
