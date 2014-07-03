package net.petsinamerica.askavet.utils;

public interface Constants {
	
	public static final String APPTAG = "AskaVet";
	
	public static final boolean DEBUG = true;
	
	public static final int NO_ERROR = 0; 
	
	/*
	 * for internet connections
	 */
	public static final String PREFERENCES_NAME = "net_pets_in_america_askavet";
	
	public static final String URL_BASE = "http://petsinamerica.net/new/";
	
	public static final String URL_FILE_STORAGE = "http://petsinamerica.net/new/../upload/";
	
	public static final String URL_API = URL_BASE + "api/";
	
	public static final String URL_BLOGCN = URL_API + "blogCN/";
	
	public static final String URL_ARTICLE_API = URL_API + "article/";
	
	public static final String URL_USERINFO = URL_API  + "userinfo/";
	
	public static final String URL_LOGIN = URL_API  + "login/";
	
	public static final String URL_ENQUIRY = URL_API  + "publicQueryList/";
	
	public static final String URL_MYENQUIRY = URL_API  + "userQueryList/";
	
	public static final String URL_NEWENQUIRY = URL_API  + "newQuery/";
	
	public static final String URL_PRODUCTLIST = URL_API  + "productlist/";
	
	public static final String URL_USERPETS = URL_API  + "userpets/";
	
	public static final String URL_PETINFO = URL_API  + "pet/";
	
	public static final String URL_ARTICLE_LIKES = URL_API  + "zan/";
	
	public static final String URL_UPLOAD_IMAGE = URL_API  + "uploadImg/";
	
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
