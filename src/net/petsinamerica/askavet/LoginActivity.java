package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.JsonHelper;

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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
	
	private static final String sURL = "http://petsinamerica.net/new/api/login";
	private static final int sLOGIN_FAIL = 0;
	private static final int sLOGIN_SUCCEED = 1;
	private static final String sTAG_USERNAME = "username";
	private static final String sTAG_PASSWORD = "password";
	private static String sTAG_LOGIN;
	private String mUsername;
	private String mPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		sTAG_LOGIN = getResources().getString(R.string.JSON_tag_login);
		
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
				new LoginTask().execute(sURL);
			}
		});
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
	
	
	/*
	 * A subclass of AsyncTask to login via username and passwords
	 */
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
			
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair(sTAG_USERNAME, mUsername));
				nameValuePairs.add(new BasicNameValuePair(sTAG_PASSWORD, mPassword));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				HttpResponse response = mClient.execute(post);
				
				// obtain response from login
				String loginResponse = new BasicResponseHandler().handleResponse(response);
				
				// parse response as JSON object
				JSONObject responseObject = (JSONObject) new JSONTokener(loginResponse).nextValue();
				
				JsonHelper jhelper = new JsonHelper();
				Map<String, Object> responseMap = jhelper.toMap(responseObject);
				
				String loginToken = responseMap.get(sTAG_LOGIN).toString();
				if (Integer.parseInt(loginToken) == 1){
					Log.i("Http_Post", "Login successfully");
					 
					// obtain the userid & access token returned from the server
					String userid = responseMap.get("userid").toString();
					String token = responseMap.get("password").toString();
					
					
					// save the token in a safe place
					//TODO: if expiration date is available from server, should
					//      use the standard constructor for AccessToken
					AccessTokenManager.SaveAccessToken(
							getApplicationContext(), 
							new AccessToken(userid, token));

					return sLOGIN_SUCCEED;
				}else{
					Log.i("Http_Post", "Login failed");
					return sLOGIN_FAIL;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return sLOGIN_FAIL;
		}

		@Override
		protected void onPostExecute(Integer loginresult) {
			if (null != mClient)
				mClient.close();
			switch (loginresult){
			case sLOGIN_SUCCEED:
				Intent intent = new Intent(getApplicationContext(),
						HomeActivity.class);
				startActivity(intent);
				break;
			case sLOGIN_FAIL:
				DialogFragment df = AlertDialogFragment.newInstance();
				df.show(getFragmentManager(), "Login Failed");
				break;
			default:
				// == do something 
			}
			
		}

		@Override
		protected void onCancelled() {
			// TODO develop handler for cancel event if necessary
			super.onCancelled();
		}
		
	}
	
	public static class AlertDialogFragment extends DialogFragment{
		
		public static AlertDialogFragment newInstance(){
			return new AlertDialogFragment();
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
			return new AlertDialog.Builder(getActivity())
					  .setMessage("Invalid login Credentials!")
					  .create();
			
		}
		
	}
	
	
}
