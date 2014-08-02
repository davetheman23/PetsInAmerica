package net.petsinamerica.askavet.utils;

import net.petsinamerica.askavet.R;

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
	
	public static final String URL_WEIBO_LOGIN = URL_API  + "weiboLogin/";
	
	public static final String URL_SIGN_UP = URL_API  + "signUp/";
	
	public static final String URL_ENQUIRY = URL_API  + "publicQueryList/";
	
	public static final String URL_MYENQUIRY = URL_API  + "userQueryList/";
	
	public static final String URL_NEWENQUIRY = URL_API  + "newQuery/";
	
	public static final String URL_PRODUCTLIST = URL_API  + "productlist/";
	
	public static final String URL_USERPETS = URL_API  + "userpets/";
	
	public static final String URL_PETINFO = URL_API  + "pet/";
	
	public static final String URL_ARTICLE_LIKES = URL_API  + "zan/";
	
	public static final String URL_UPLOAD_IMAGE = URL_API  + "uploadImg/";
	
    public static final String KEY_USERNAME = "username";
    
  	public static final String KEY_PASSWORD = "password";
  	
  	public static final String KEY_WEIBO_USERNAME = "weibo_id";
    
  	public static final String KEY_WEIBO_PASSWORD = "secretKey";
  	
  	public static final String KEY_USERID = "userid";
  	
  	public static final String KEY_USERTOKEN = "token";
  	
  	public static final String KEY_RESULT = App.appContext.getString(R.string.JSON_tag_result);
	
  	public static final String KEY_ERROR = App.appContext.getString(R.string.JSON_tag_error);
  	
  	public static final String KEY_ERROR_MESSAGE = App.appContext.getString(R.string.JSON_tag_errorMessage);
  	
  	/*
  	 * for storage 
  	 */
  	public static final String PIA_ROOT_DIR = "AskaVet";
  	
  	public static final String TEMP_SUBDIR = "temp";
  	
  	/*
  	 * for JSON parsing
  	 */
  	public static final String KEY_PET_NAME = App.appContext.getString(R.string.JSON_tag_petname);
  	public static final String KEY_PET_PIC = App.appContext.getString(R.string.JSON_tag_petavatar);
  	public static final String KEY_PET_SEX = App.appContext.getString(R.string.JSON_tag_petsex);
  	public static final String KEY_PET_SPECIES = App.appContext.getString(R.string.JSON_tag_petspecies);
  	public static final String KEY_PET_BREED = App.appContext.getString(R.string.JSON_tag_petbreed);
  	public static final String KEY_PET_NEUTERAGE = App.appContext.getString(R.string.JSON_tag_petneuterage);
  	public static final String KEY_PET_BDAY = App.appContext.getString(R.string.JSON_tag_petbday);
  	public static final String KEY_PET_INSURANCE = App.appContext.getString(R.string.JSON_tag_petinsurance);
  	public static final String KEY_PET_ID = App.appContext.getString(R.string.JSON_tag_petid);
  	public static final String KEY_PET_FLEA = App.appContext.getString(R.string.JSON_tag_petflea);
  	public static final String KEY_PET_DEWORM = App.appContext.getString(R.string.JSON_tag_petdeworm);
  	public static final String KEY_PET_VACCINATION = App.appContext.getString(R.string.JSON_tag_petvaccination);

}
