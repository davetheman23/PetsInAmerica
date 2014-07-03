package net.petsinamerica.askavet.utils;

import net.petsinamerica.askavet.PushActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.igexin.sdk.PushConsts;

public class PushReceiver extends BroadcastReceiver {

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
					//Log.d("GetuiSdkDemo", "Got Payload:" + data);
					// TODO:接收处理透传（payload）数据 
					Intent newIntent = new Intent(context, PushActivity.class);
					newIntent.putExtra("payload", data);
					newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(newIntent);
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

}
