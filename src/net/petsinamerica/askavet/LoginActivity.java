package net.petsinamerica.askavet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.widget.LoginButton;
import com.sina.weibo.sdk.widget.LoginoutButton;

public class LoginActivity extends Activity{
	
	private static final String sURL = "http://petsinamerica.net/new/api/login";
	private static final int sLOGIN_FAIL = 0;
	private static final int sLOGIN_SUCCEED = 1;
	private static final String sTAG_USERNAME = "username";
	private static final String sTAG_PASSWORD = "password";
	private static String sTAG_LOGIN;
	private String mUsername;
	private String mPassword;
	
	private LoginButton mLoginBtnDefault;
	
	/**
     * 该按钮用于记录当前点击的是哪一个 Button，用于在 {@link #onActivityResult}
     * 函数中进行区分。通常情况下，我们的应用中只需要一个合适的 {@link LoginButton} 
     * 或者 {@link LoginoutButton} 即可。
     */
    private Button mCurrentClickedButton;
	
	/** 登陆认证对应的listener */
    private AuthListener mLoginListener = new AuthListener();
    /** 登出操作对应的listener */
    private LogOutRequestListener mLogoutListener = new LogOutRequestListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		sTAG_LOGIN = getResources().getString(R.string.JSON_tag_login);
		
		// 创建授权认证信息
        AuthInfo authInfo = new AuthInfo(this, WeiboConstants.APP_KEY, WeiboConstants.REDIRECT_URL, WeiboConstants.SCOPE);
        
        mLoginBtnDefault = (LoginButton) findViewById(R.id.button_login_weibo);
        mLoginBtnDefault.setWeiboAuthInfo(authInfo, mLoginListener);
        
        /**
         * 注销按钮：该按钮未做任何封装，直接调用对应 API 接口
         */
       /* final Button logoutButton = (Button) findViewById(R.id.button_logout_weibo);
        logoutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new LogoutAPI(AccessTokenKeeper.readAccessToken(LoginActivity.this)).logout(mLogoutListener);
            }
        });*/
        
        /**
         * 请注意：为每个 Button 设置一个额外的 Listener 只是为了记录当前点击的
         * 是哪一个 Button，用于在 {@link #onActivityResult} 函数中进行区分。
         * 通常情况下，我们的应用不需要调用该函数。
         */
        mLoginBtnDefault.setExternalOnClickListener(mButtonClickListener);
		
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
				new LoginTask().execute(sURL);
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
	protected void onPause() {
		super.onPause();
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
     * 请注意：为每个 Button 设置一个额外的 Listener 只是为了记录当前点击的
     * 是哪一个 Button，用于在 {@link #onActivityResult} 函数中进行区分。
     * 通常情况下，我们的应用不需要定义该 Listener。
     */
    private OnClickListener mButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof Button) {
                mCurrentClickedButton = (Button)v;
            }
        }
    };
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (mCurrentClickedButton != null) {
            if (mCurrentClickedButton instanceof LoginButton) {
                ((LoginButton)mCurrentClickedButton).onActivityResult(requestCode, resultCode, data);
            } else if (mCurrentClickedButton instanceof LoginoutButton) {
                ((LoginoutButton)mCurrentClickedButton).onActivityResult(requestCode, resultCode, data);
            }
        }
	 }
	
	
	/**
     * 登入按钮的监听器，接收授权结果。
     */
    private class AuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid()) {
                String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                        new java.util.Date(accessToken.getExpiresTime()));
                //String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
                //mTokenView.setText(String.format(format, accessToken.getToken(), date));
                
                AccessTokenManager.SaveWeiboAccessToken(getApplicationContext(), accessToken);
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
    private class LogOutRequestListener implements RequestListener {
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
    }
	
}
