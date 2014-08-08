package net.petsinamerica.askavet.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.UserInfoManager.Listener;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.igexin.sdk.PushConsts;

public class PushReceiver extends BroadcastReceiver {
	
	private String cid;
	private boolean userCidBindCompleted = false;
	
	private static onReceiveNotificationListener mPiaNotificationListener = null;
	public static interface onReceiveNotificationListener {
		public void onReceivedNotification(PiaNotification notification);
	}
	public static void registerPiaNotificationListener (onReceiveNotificationListener listener){
		mPiaNotificationListener = listener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		//Log.d("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));
		switch (bundle.getInt(PushConsts.CMD_ACTION)) {
			case PushConsts.GET_MSG_DATA:
				// 获取透传（payload）数据
				byte[] payload = bundle.getByteArray("payload");
				
				if (payload != null)
				{
					String data = new String(payload);
					handlePayloadMessage(data, context);
				}
				break;
			case PushConsts.GET_CLIENTID:
				// 获取ClientID(CID)
				cid = bundle.getString("clientid");
				Log.d("GetuiSdkDemo", "Got ClientID:" + cid);
				
				BindUserCidInBackground bindUser = new BindUserCidInBackground();
				bindUser.execute(Constants.URL_BIND_USER_CID);
				
				break;
			/*case PushConsts.BIND_CELL_STATUS:
				String cell = bundle.getString("cell");

				//Log.d("GetuiSdkDemo", "BIND_CELL_STATUS:" + cell);
				if (GexinSdkDemoActivity.tLogView != null)
					GexinSdkDemoActivity.tLogView.append("BIND_CELL_STATUS:" + cell + "\n");
					break;*/
			default:
				break;
		}


	}
	
	public void handlePayloadMessage(String message, Context context){
		
		NotificationsDataSource dataSource = new NotificationsDataSource(context);
		dataSource.open();
		
		long type = 1;
		String subject = "", content = "";
		Map<String, Object> pushContent = null;
		try {
			JSONObject pushObject = (JSONObject) new JSONTokener(message).nextValue();
			pushContent = JsonHelper.toMap(pushObject);
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		if (pushContent != null && pushContent.containsKey(PiaSQLiteHelper.COLUMN_TYPE)){
			type = (Integer) pushContent.get(PiaSQLiteHelper.COLUMN_TYPE);
		}
		if (pushContent != null && pushContent.containsKey(PiaSQLiteHelper.COLUMN_SUBJECT)){
			subject = pushContent.get(PiaSQLiteHelper.COLUMN_SUBJECT).toString();
		}
		if (pushContent != null && pushContent.containsKey(PiaSQLiteHelper.COLUMN_CONTENT)){
			content = pushContent.get(PiaSQLiteHelper.COLUMN_CONTENT).toString();
		}
		
		AccessToken token = AccessTokenManager.readAccessToken(context);
		
		PiaNotification notification = dataSource.createNotification(
				type, token.getUserId(), subject, content);
		
		if (mPiaNotificationListener != null){
			mPiaNotificationListener.onReceivedNotification(notification);
		}
		
		dataSource.close();
	}
	
	class BindUserCidInBackground extends GeneralHelpers.CallPiaApiInBackground{

		@Override
		protected void onCallCompleted(Map<String, Object> result) {
			if (result != null){
				userCidBindCompleted = true;
			}
		}

		@Override
		protected void addParamstoPost(HttpPost post, Context context) 
												throws UnsupportedEncodingException {
			
			AccessToken token = AccessTokenManager.readAccessToken(context);
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			
			// add user login information
			nameValuePairs.add(new BasicNameValuePair(Constants.KEY_USERID, token.getUserId()));
			nameValuePairs.add(new BasicNameValuePair(Constants.KEY_USERTOKEN, token.getToken()));
			nameValuePairs.add(new BasicNameValuePair("cid", cid));
			
			HttpParams httpParams = mClient.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10*1000); // time in 10 second
			
			// add the params into the post, make sure to include encoding UTF_8 as follows
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
		}
		
	}
	
	
}
