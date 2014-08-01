package net.petsinamerica.askavet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends Activity {
	
	TextView et_emailLabel;
	TextView et_usernameLabel;
	
	EditText et_username;
	EditText et_email;
	EditText et_password;
	EditText et_passwordConfirm;
	
	ProgressDialog dlg;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Complete the registration process
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		
		dlg = new ProgressDialog(this);
		
		// set click listener for login link
		TextView tv_login = (TextView) findViewById(R.id.signup_activity_link_to_login);
		tv_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), 
										LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		
		et_username = (EditText) findViewById(R.id.signup_activity_reg_username);
		et_usernameLabel = (TextView) findViewById(R.id.signup_activity_reg_username_label);
		et_email = (EditText) findViewById(R.id.signup_activity_reg_email);
		et_emailLabel = (TextView) findViewById(R.id.signup_activity_reg_email_label);
		et_password = (EditText) findViewById(R.id.signup_activity_reg_password);
		et_passwordConfirm = (EditText) findViewById(R.id.signup_activity_reg_password_confirm);
		
		// set up a listener for sign-up click
		Button btn_signup = (Button) findViewById(R.id.signup_activity_button_register);
		btn_signup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (! isEntryValid()){
					AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
					builder.setTitle("请检查")
					.setMessage("输入有误...")
					.setCancelable(false)
					.setPositiveButton("好的", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					AlertDialog dialog = builder.create();
					dialog.show();
					return;
				}
				
				String username = et_username.getText().toString().trim();
				String email = et_email.getText().toString().trim();
				String password = et_password.getText().toString().trim();
				// add user signup information
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("username", username));
				nameValuePairs.add(new BasicNameValuePair("email", email));
				nameValuePairs.add(new BasicNameValuePair("password", password));
				
				// sign up 
				new SignUpTask().execute(Constants.URL_SIGN_UP, nameValuePairs);

			}
			
		});
		
	}
	
	private boolean isEntryValid() {
		String username = et_username.getText().toString().trim();
		String email = et_email.getText().toString().trim();
		String password = et_password.getText().toString().trim();
		String password_confirm = et_passwordConfirm.getText().toString().trim();
		
		if (username.equals("")){
			return false;
		}
		if (!email.contains("@")){
			return false;
		}
		if (!password.equals(password_confirm)){
			return false;
		}
		return true;
	}
	
	private class SignUpTask extends AsyncTask<Object, Void, Map<String, Object>>{
		
		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
		
		@Override
		protected void onPreExecute() {
			if (dlg != null){
				dlg.setMessage("请稍等，正在注册中 ...");
				dlg.show();
			}
		}

		@Override
		protected Map<String, Object> doInBackground(Object... params) {
			String url = params[0].toString();
			
			// construct the parameter list
			@SuppressWarnings("unchecked")
			List<NameValuePair> nameValuePairs = (ArrayList<NameValuePair>)params[1];
			
			try {
				HttpPost post = new HttpPost(url);
				
				HttpParams httpParams = mClient.getParams();
				//httpParams.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, Charset.forName("UTF-8"));
				HttpConnectionParams.setConnectionTimeout(httpParams, 10*1000); // time in 10 second
				
				// add the params into the post, make sure to include encoding UTF_8 as follows
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

				// execute post
				HttpResponse response = mClient.execute(post);
				
				return GeneralHelpers.handlePiaResponse(response);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}finally{
				if (mClient!=null){
					mClient.close();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Map<String, Object> result) {
			if (dlg!= null){
				if (dlg.isShowing()){
					dlg.dismiss();
				}
			}
			if (result != null){
				if (!result.containsKey(Constants.KEY_ERROR_MESSAGE)){
					// results are valid
					// get the user id and token
					String userid = result.get(Constants.KEY_USERID).toString();
					String token = result.get(Constants.KEY_USERTOKEN).toString();
					//TODO: if expiration date is available from server, should
					//      use the standard constructor for AccessToken
					// save the token in a safe place
					AccessTokenManager.SaveAccessToken(
							getApplicationContext(), 
							new AccessToken(userid, token));
					
					// new 
					Toast.makeText(App.appContext, "注册成功", Toast.LENGTH_LONG).show();
					
					// start the home activity
					Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}else{
					// results are not valid
					GeneralHelpers.showAlertDialog(SignUpActivity.this, 
							"注册失败",
							result.get(Constants.KEY_ERROR_MESSAGE).toString());
				}
			}
		}
		
	}

}
