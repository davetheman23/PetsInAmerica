package net.petsinamerica.askavet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class PushActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String data = getIntent().getStringExtra("payload");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("服务器端推送消息")
				.setMessage(data)
				.setPositiveButton("OK", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).create();
		builder.show();
	}
	
}
