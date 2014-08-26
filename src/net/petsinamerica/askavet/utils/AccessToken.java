package net.petsinamerica.askavet.utils;

import java.util.Calendar;

import android.util.Log;

/**
 * Define the behavior of the Access Token & related methods
 *  
 * @since 05/23/2014  
 */
public class AccessToken {
	
	private static String userId = "";
	private static String accessToken = "";
	private static Calendar expiration = null;
	
	private static String sTAG = "Access Token";
	
	/*App Key for website: 3684625824*/
	
	/**
	 * Starndard constructor for the access token  
	 */
	public AccessToken(String userId, String token, Calendar expiration){
		
		AccessToken.userId = userId; 
		AccessToken.accessToken = token;
		AccessToken.expiration = expiration;
		
	}
	
	/**
	 * Constructor for the access token, without specifying 
	 * expiration time, the token will be renewed in Constants.RENEW_LOCAL_TOKEN_HOURS
	 *   
	 */
	public AccessToken(String userId, String token){
		AccessToken.userId = userId; 
		AccessToken.accessToken = token;
		
		// get current time and set expiration to renewHours hrs later
		Calendar time = Calendar.getInstance();
		time.add(Calendar.HOUR, Constants.RENEW_LOCAL_TOKEN_HOURS);
		AccessToken.expiration = time;		
	}

	/**
	 *  Getter method for userID, if token not expired, return current
	 *  user Id, otherwise return null
	 */
	public String getUserId(){
		if (!isExpired()){
			return userId;
		}
		return null;
	}
	
	/**
	 *  Getter method for Access token, if token not expired, return current
	 *  access token, otherwise return null
	 */
	public String getToken(){
		if (!isExpired()){
			return accessToken;
		}
		return null;
	}
	
	/**
	 *  Getter method for expiration date, if token not expired, return current
	 *  expiration, otherwise return null
	 */
	public Calendar getExpiration(){
		return expiration;
	}
	
	/**
	 *  Test method for expiration date, if token expired, return true
	 *  otherwise return false
	 */
	public boolean isExpired(){
		// get current time and test if token expired 
		Calendar time = Calendar.getInstance();
		
		if (time.before(expiration)){
			return false;
		}
		Log.i(sTAG, "Access Token is Expired!");
		return true;
	}
	
}
