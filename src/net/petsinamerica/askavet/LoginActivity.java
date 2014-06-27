package net.petsinamerica.askavet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.JsonHelper;
import net.petsinamerica.askavet.utils.WeiboConstants;

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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

public class LoginActivity extends Activity{
	
	private static final int sLOGIN_FAIL = 0;
	private static final int sLOGIN_SUCCEED = 1;
	private static String KEY_LOGIN;
	private String mUsername;
	private String mPassword;

    private WeiboAuth mWeiboAuth;
    
    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		int id = 1;
		
		
		KEY_LOGIN = getResources().getString(R.string.JSON_tag_login);

		// 创建微博实例
		mWeiboAuth = new WeiboAuth(this, WeiboConstants.APP_KEY, 
										 WeiboConstants.REDIRECT_URL, 
										 WeiboConstants.SCOPE);
        
        // SSO 授权
        findViewById(R.id.button_login_weibo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSsoHandler = new SsoHandler(LoginActivity.this, mWeiboAuth);
                mSsoHandler.authorize(new AuthListener());
            }
        });
        
		
		// = set click listener for sign-up link
		TextView tv_signup = (TextView) findViewById(R.id.link_to_register);
		tv_signup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), 
										SignUpActivity.class);
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
				new LoginTask().execute(Constants.URL_LOGIN);
			}
		});
		
	}

	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		/*AccessToken token = AccessTokenManager.readAccessToken(getApplicationContext());	    
		if (token != null && !token.isExpired()){
			Intent intent = new Intent(this, HomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}*/
	}


	@Override
	protected void onPause() {
		super.onPause();
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    // SSO 授权回调
	    // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResult
	    if (mSsoHandler != null) {
	        mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
	    }
	 }


	/**
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
				nameValuePairs.add(new BasicNameValuePair(Constants.TAG_USERNAME, mUsername));
				nameValuePairs.add(new BasicNameValuePair(Constants.TAG_PASSWORD, mPassword));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				HttpResponse response = mClient.execute(post);
				
				// obtain response from login
				String loginResponse = new BasicResponseHandler().handleResponse(response);
				
				// parse response as JSON object
				JSONObject responseObject = (JSONObject) new JSONTokener(loginResponse).nextValue();

				Map<String, Object> responseMap = JsonHelper.toMap(responseObject);
				
				String loginToken = responseMap.get(KEY_LOGIN).toString();
				if (Integer.parseInt(loginToken) == 1){
					Log.i("Http_Post", "Login successfully");
					 
					// obtain the userid & access token returned from the server
					String userid = responseMap.get("userid").toString();
					String token = responseMap.get("password").toString();
					
					
					//TODO: if expiration date is available from server, should
					//      use the standard constructor for AccessToken
					// save the token in a safe place
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
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
	
	
	/**
     * 登入按钮的监听器，接收授权结果。
     */
    private class AuthListener implements WeiboAuthListener {
    	
        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken weiboToken = Oauth2AccessToken.parseAccessToken(values);
            if (weiboToken != null && weiboToken.isSessionValid()) {
                //String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                //        new java.util.Date(weiboToken.getExpiresTime()));
                //String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
                //mTokenView.setText(String.format(format, accessToken.getToken(), date));
                
                AccessTokenManager.SaveWeiboAccessToken(getApplicationContext(), weiboToken);
                Intent intent = new Intent(getApplicationContext(),
											HomeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(LoginActivity.this, 
                    //R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_SHORT).show();
            		"登录取消", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 登出按钮的监听器，接收登出处理结果。（API 请求结果的监听器）
     */
    /*private class LogOutRequestListener implements RequestListener {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                try {
                    JSONObject obj = new JSONObject(response);
                    String value = obj.getString("result");

                    if ("true".equalsIgnoreCase(value)) {
                    	AccessTokenManager.clear(LoginActivity.this);
                        //mTokenView.setText(R.string.weibosdk_demo_logout_success);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }     

		@Override
		public void onWeiboException(WeiboException e) {
			//mTokenView.setText(R.string.weibosdk_demo_logout_failed);
		}
    }*/
	
}
