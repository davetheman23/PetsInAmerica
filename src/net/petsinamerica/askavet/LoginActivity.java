package net.petsinamerica.askavet;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;
import net.petsinamerica.askavet.utils.JsonHelper;
import net.petsinamerica.askavet.utils.WeiboConstants;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
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
import android.app.ProgressDialog;
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

import com.igexin.sdk.PushManager;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

public class LoginActivity extends Activity{
	
	private static String KEY_LOGIN = App.appContext.getString(R.string.JSON_tag_login);
	private String mUsername;
	private String mPassword;

	private ProgressDialog progressDialog;

    private WeiboAuth mWeiboAuth;
    
    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// initialize push service 
		PushManager.getInstance().initialize(this.getApplicationContext());
		
		setContentView(R.layout.activity_login);
		
		progressDialog = new ProgressDialog(this);

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
            	//Toast.makeText(getApplication(), "微博登陆暂不支持未关联的用户", Toast.LENGTH_LONG).show();
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
				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair(Constants.KEY_USERNAME, mUsername));
				nameValuePairs.add(new BasicNameValuePair(Constants.KEY_PASSWORD, mPassword));
				
				new LoginTask().execute(Constants.URL_LOGIN, nameValuePairs);
			}
		});
		
	}

	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		AccessToken token = AccessTokenManager.readAccessToken(getApplicationContext());	    
		if (token != null && !token.isExpired()){
			Intent intent = new Intent(this, HomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
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
	private class LoginTask extends AsyncTask<Object, Void, Map<String, Object>> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
		
		@Override
		protected void onPreExecute() {
			if (progressDialog != null){
				progressDialog.setMessage("请稍等，正在登录 ...");
				progressDialog.show();
			}
		}

		@Override
		protected Map<String, Object> doInBackground(Object... params) {
			String url = params[0].toString();	
			@SuppressWarnings("unchecked")
			List<NameValuePair> nameValuePairs = (ArrayList<NameValuePair>)params[1];
			
			try {
			
			HttpPost post = new HttpPost(url);
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				HttpResponse response = mClient.execute(post);
				
				// obtain response from login
				String loginResponse = new BasicResponseHandler().handleResponse(response);
				
				// parse response as JSON object
				JSONObject responseObject = (JSONObject) new JSONTokener(loginResponse).nextValue();

				Map<String, Object> responseMap = JsonHelper.toMap(responseObject);
				
				if (responseMap.containsKey(KEY_LOGIN)){
					// this is the case that use PIA login directly
				String loginToken = responseMap.get(KEY_LOGIN).toString();
					 
					if (Integer.parseInt(loginToken) == 0){
						// if login failed
						responseMap.put(Constants.KEY_ERROR_MESSAGE, "请检查登录信息");
					}
					return responseMap;
				}else{
					// this is the case that uses weibo login
					return GeneralHelpers.handlePiaResponseString(loginResponse);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Map<String, Object> result) {
			if (null != mClient)
				mClient.close();
			if (progressDialog!= null){
				if (progressDialog.isShowing()){
					progressDialog.dismiss();
				}
			}
			if (result != null){
				if (!result.containsKey(Constants.KEY_ERROR_MESSAGE)){
					// obtain the userid & access token returned from the server
					String userid = result.get(Constants.KEY_USERID).toString();
					String token = result.get(Constants.KEY_USERTOKEN).toString();
					
					//TODO: if expiration date is available from server, should
					//      use the standard constructor for AccessToken
					// save the token in a safe place
					AccessTokenManager.SaveAccessToken(
							getApplicationContext(), 
							new AccessToken(userid, token));
					Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				}else{
					GeneralHelpers.showAlertDialog(LoginActivity.this, 
							"登录失败", result.get(Constants.KEY_ERROR_MESSAGE).toString());
				}
			}
			
		}

		@Override
		protected void onCancelled() {
			// TODO develop handler for cancel event if necessary
			super.onCancelled();
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
                
                AccessTokenManager.SaveWeiboAccessToken(getApplicationContext(), weiboToken);

                Date now = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = sdf.format(now);
                String dateEncodedStr = new String(Hex.encodeHex(DigestUtils.md5(dateString)));

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair(Constants.KEY_WEIBO_USERNAME, weiboToken.getUid()));
				nameValuePairs.add(new BasicNameValuePair(Constants.KEY_WEIBO_PASSWORD, dateEncodedStr));
				
				new LoginTask().execute(Constants.URL_WEIBO_LOGIN, nameValuePairs);

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
