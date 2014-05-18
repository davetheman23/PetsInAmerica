package net.petsinamerica.askavet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class RegistrationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		// set click listener for login link
		TextView tv_login = (TextView) findViewById(R.id.link_to_login);
		tv_login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), 
										LoginActivity.class);
				startActivity(intent);
			}
		});
		
	}
}
