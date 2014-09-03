package net.petsinamerica.askavet.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.NotificationCenterActivity;
import net.petsinamerica.askavet.R;

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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

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
					Log.d("Got Payload", payload.toString());
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
		
		dataSource.close();
		
		
		// --- Make a notification and send to android notification center
		// 1. set custom remote view
		RemoteViews rm = new RemoteViews("net.petsinamerica.askavet",R.layout.list_notification_android_item);
		rm.setTextViewText(R.id.list_notification_content, content);
		rm.setTextViewText(R.id.list_notification_subject, subject);
		
		// 2. set action intent
		Intent intent = new Intent(context, NotificationCenterActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, 
								intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		// 3. build the notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
			.setTicker(subject)
			.setAutoCancel(true)
			.setSmallIcon(R.drawable.ic_launcher_new80x80)
			.setContentIntent(pendingIntent)
			.setContentTitle(subject)	// if need to use custom view, should comment this
			.setContentText(content);	// if need to use custom view, should comment this
			//.setContentInfo("haha");	// if need to use custom view, should comment this
			//.setContent(rm);	// if need to use custom view, can uncomment this
		
		//4. issue the notification
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(1, builder.getNotification());
		
		
		// notify all those registered to this channel
		if (mPiaNotificationListener != null){
			mPiaNotificationListener.onReceivedNotification(notification);
		}
	}
	
	class BindUserCidInBackground extends CallPiaApiInBackground{
		@Override
		protected void onCallCompleted(Integer result) {}
		
		@Override
		protected void onCallCompleted(List<Map<String, Object>> result) {}
		
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
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			
			// add user login information
			nameValuePairs.add(new BasicNameValuePair(Constants.KEY_USERID, token.getUserId()));
			nameValuePairs.add(new BasicNameValuePair(Constants.KEY_USERTOKEN, token.getToken()));
			nameValuePairs.add(new BasicNameValuePair("cid", cid));
			
			HttpParams httpParams = mClient.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10*1000); // time in 10 second
			
			// add the params into the post, make sure to include encoding UTF_8 as follows
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
		}

		@Override
		protected void handleInvalidSession() {
			AccessTokenManager.clearAllTokens(App.appContext);
			UserInfoManager.clearAllUserInfo();
		}


	}
}
