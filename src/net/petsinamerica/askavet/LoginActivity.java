package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinaermica.askavet.utils.JsonHelper;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity{
	
	private static final String URL = "http://petsinamerica.net/new/api/login";
	private static final int LOGIN_FAIL = 0;
	private static final int LOGIN_SUCCEED = 1;
	private static final String TAG_USERNAME = "username";
	private static final String TAG_PASSWORD = "password";
	
	private static String TAG_LOGIN;
	private static String mUsername;
	private static String mPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		TAG_LOGIN = getResources().getString(R.string.JSON_tag_login);
		
		// = set click listener for sign-up link
		TextView tv_signup = (TextView) findViewById(R.id.link_to_register);
		tv_signup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), 
										RegistrationActivity.class);
				startActivity(intent);
				
			}
		});
		
		// = set click listener for login button
		Button btn_login = (Button) findViewById(R.id.button_login);
		btn_login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// = collect user inputs
				// - collect username/email
				EditText et_username = (EditText) findViewById(R.id.login_username);
				mUsername = et_username.getText().toString();
				mUsername = mUsername.replace(" ", "");
				// - collect password
				EditText et_password = (EditText) findViewById(R.id.login_password);
				mPassword = et_password.getText().toString();
				new LoginTask().execute(URL);
			}
		});
		
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
	
	private class LoginTask extends AsyncTask<String, Void, Integer> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
		
		
		
		@Override
		protected void onPreExecute() {
			// TODO show a dialog box with a loading icon
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(String... params) {
			String url = params[0];			
			
			HttpPost post = new HttpPost(url);
			//HttpPost post2 = new HttpPost("http://petsinamerica.net/new/api/userQueryList/1");
			
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair(TAG_USERNAME, mUsername));
				nameValuePairs.add(new BasicNameValuePair(TAG_PASSWORD, mPassword));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				HttpResponse response = mClient.execute(post);
				
				// obtain response from login
				String loginResponse = new BasicResponseHandler().handleResponse(response);
				
				// parse response as JSON object
				JSONObject responseObject = (JSONObject) new JSONTokener(loginResponse).nextValue();
				
				JsonHelper jhelper = new JsonHelper();
				Map<String, Object> responseMap = jhelper.toMap(responseObject);
				
				String loginToken = responseMap.get(TAG_LOGIN).toString();
				if (Integer.parseInt(loginToken) == 1){
					Log.i("Http_Post", "Login successfully");
					
					// TODO save userid and token in the shared pref with security 
					//      that can be accessed by only this app 
					String userid = responseMap.get("userid").toString();
					String password = responseMap.get("password").toString();
					nameValuePairs.clear();
					nameValuePairs.add(new BasicNameValuePair("userid", userid));
					nameValuePairs.add(new BasicNameValuePair("token", password));
					
					/*// TODO rethink how to incoporate these code into every other authentication requests
					post2.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					response = mClient.execute(post2);
					String userQueryList = new BasicResponseHandler().handleResponse(response);
					Log.i("Http_Post", userQueryList);*/
					return LOGIN_SUCCEED;
				}else{
					Log.i("Http_Post", "Login failed");
					return LOGIN_FAIL;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return LOGIN_FAIL;
		}

		@Override
		protected void onPostExecute(Integer loginresult) {
			if (null != mClient)
				mClient.close();
			switch (loginresult){
			case LOGIN_SUCCEED:
				Intent intent = new Intent(getApplicationContext(),
						HomeActivity.class);
				startActivity(intent);
				break;
			case LOGIN_FAIL:
				// TODO show a dialog box to show that login failed
				
				break;
			default:
				// == do something 
			}
			
		}
	
	}
	
	
	
}
