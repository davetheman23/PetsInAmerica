package net.petsinamerica.askavet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.CallPiaApiInBackground;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignUpActivity extends Activity {
	
	private ProgressDialog progressDialog;
	
	private EditText et_Username = null;
	private EditText et_Email = null;
	private EditText et_Password = null;
	private EditText et_PasswordConfirm = null;
	
	private String validUsername = null;
	private String validEmail = null;
	private String validPassword = null;
	
	private SignupInBackground signupInBackground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		
		// get a reference to each EditText view in the layout
		et_Username = (EditText) findViewById(R.id.signup_activity_reg_username);
		et_Email = (EditText) findViewById(R.id.signup_activity_reg_email);
		et_Password = (EditText) findViewById(R.id.signup_activity_reg_password);
		et_PasswordConfirm = (EditText) findViewById(R.id.signup_activity_reg_password_confirm);
		
		progressDialog = new ProgressDialog(this);
		
		Button signup = (Button) findViewById(R.id.signup_activity_button_register);
		signup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//GeneralHelpers.showMessage(SignUpActivity.this, "注册功能还在完善中");
				if (isUserEntriesValid()){					
					signupInBackground = (SignupInBackground) new SignupInBackground()
						.setParameters(SignUpActivity.this, CallPiaApiInBackground.TYPE_RETURN_MAP, false)
						.setProgressDialog(true, "请稍等，正在注册...")
						.setErrorDialog(true)
						.execute(Constants.URL_SIGN_UP);
				}
			}
		});
		
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
	}
	
	
	
	@Override
	public void onBackPressed() {
		if (progressDialog!= null && progressDialog.isShowing()){
			GeneralHelpers.showMessage(this, "请稍后，注册正在进行中，暂时不能被取消！");
		}
		super.onBackPressed();
	}



	@Override
	protected void onDestroy() {
		if (signupInBackground!= null){
			signupInBackground.cancel(true);
		}
		super.onDestroy();
	}



	private boolean isUserEntriesValid(){
		String errorString = "";
		boolean hasError = false;
		if (!isUserNameValid()){
			errorString = errorString + "- " + getString(R.string.label_username_format) + "\n";
			hasError = true;
		}
		
		if (!isEmailValid()){
			errorString = errorString + "- " + "请使用正确的电子邮箱地址格式" + "\n";
			hasError = true;
		}
		
		if (!isPasswordValid()){
			errorString = errorString + "- " + "密码输入有误" + "\n";
			hasError = true;
		}
		
		if (hasError){
			GeneralHelpers.showAlertDialog(
					this, 
					"注册信息错误, 请重新输入", 
					errorString);
			return false;
		}
		return true;
	}
	
	
	private boolean isUserNameValid(){
		String username = et_Username.getText().toString();
		username = username.trim();
		if (username.equals("")){
			return false;
		}		
		if (!username.matches("[a-zA-Z_0-9]+")){
			return false;
		}
	    username = username.toLowerCase();
		
		validUsername = username;
		return true;
	}
	
	private boolean isEmailValid(){
		String email = et_Email.getText().toString();
		email = email.trim();
		if (email.equals("")){
			return false;
		}
		validEmail = email;
		return true;
	}
	
	private boolean isPasswordValid(){
		String pass = et_Password.getText().toString();
		String passConfirm = et_PasswordConfirm.getText().toString();
		pass = pass.trim();
		if (pass.equals("")){
			return false;
		}
		if (!pass.equals(passConfirm)){
			return false;
		}
		validPassword = pass;
		return true;
	}
	
	private class SignupInBackground extends CallPiaApiInBackground{

		@Override
		protected void onCallCompleted(List<Map<String, Object>> result) {}
		
		@Override
		protected void onCallCompleted(Map<String, Object> result) {
			if (result != null && !result.containsKey(Constants.KEY_ERROR_MESSAGE)){
				// obtain the userid & access token returned from the server
				String userid = result.get(Constants.KEY_USERID).toString();
				String token = result.get(Constants.KEY_USERTOKEN).toString();
				
				//TODO: if expiration date is available from server, should
				//      use the standard constructor for AccessToken
				// save the token in a safe place
				AccessTokenManager.SaveAccessToken(
						getApplicationContext(), 
						new AccessToken(userid, token));
				Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		}
		
		@Override
		protected void addParamstoPost(HttpPost post, Context context) 
												throws UnsupportedEncodingException {
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			
			// add user login information
			nameValuePairs.add(new BasicNameValuePair(Constants.KEY_USERNAME, validUsername));
			nameValuePairs.add(new BasicNameValuePair(Constants.KEY_EMAIL, validEmail));
			nameValuePairs.add(new BasicNameValuePair(Constants.KEY_PASSWORD, validPassword));
			
			HttpParams httpParams = mClient.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10*1000); // time in 10 second
			
			// add the params into the post, make sure to include encoding UTF_8 as follows
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
		}
		
	}

}
