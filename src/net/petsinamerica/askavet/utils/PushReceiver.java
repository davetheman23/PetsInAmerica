package net.petsinamerica.askavet.utils;

import java.util.Map;

import net.petsinamerica.askavet.utils.UserInfoManager.Listener;

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
				String cid = bundle.getString("clientid");
				Log.d("GetuiSdkDemo", "Got ClientID:" + cid);
				// TODO: 
				/* 第三方应用需要将ClientID上传到第三方服务器，并且将当前用户帐号和ClientID进行关联，以便以后通过用户帐号查找ClientID进行消息推送
				有些情况下ClientID可能会发生变化，为保证获取最新的ClientID，请应用程序在每次获取ClientID广播后，都能进行一次关联绑定 */

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
		
		/*Intent newIntent = new Intent(context, PushActivity.class);
		newIntent.putExtra("payload", message);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(newIntent);*/
		
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
		
		PiaNotification notification = dataSource.createNotification(type, subject, content);
		
		if (mPiaNotificationListener != null){
			mPiaNotificationListener.onReceivedNotification(notification);
		}
	}
	
	
}
