package net.petsinamerica.askavet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// = set click listener for sign-up link
		TextView tv_signup = (TextView) findViewById(R.id.link_to_register);
		tv_signup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), 
										RegistrationActivity.class);
				startActivity(intent);
				
			}
		});
		
		
		// = collect user inputs
		// - collect username/email
		
		// - collect password
		
		// = set click listener for login button
		Button btn_login = (Button) findViewById(R.id.button_login);
		btn_login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (authentication()){
					Intent intent = new Intent(getApplicationContext(),
											HomeActivity.class);
					startActivity(intent);
				}
			}
		});
	}
	
	private boolean authentication(){
		return true;
	}
	
}
