package net.petsinamerica.askavet;

import net.petsinamerica.askavet.utils.GeneralHelpers;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SignUpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Complete the registration process
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		
		Button signup = (Button) findViewById(R.id.signup_activity_button_register);
		signup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				GeneralHelpers.showMessage(SignUpActivity.this, "注册功能还在完善中");
				
			}
		});
		
		// set click listener for login link
		TextView tv_login = (TextView) findViewById(R.id.signup_activity_link_to_login);
		tv_login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), 
										LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		
	}

}
