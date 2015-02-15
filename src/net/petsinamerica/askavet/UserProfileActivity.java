package net.petsinamerica.askavet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import net.petsinamerica.askavet.utils.CallPiaApiInBackground;
import net.petsinamerica.askavet.utils.Constants;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserProfileActivity extends Activity implements Constants{
	
	private boolean mIsPosted = false;
	
	private Context mContext = this;
	
	private Map<String, Object> mProfiledata;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// inflate the layouts
		setContentView(R.layout.activity_userprofile);
		
		Button editProfile = (Button) findViewById(R.id.activity_userprofile_submit);
		editProfile.setOnClickListener(new  View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mIsPosted){
					SaveProfileInBackground saveProfileInBackground = new  SaveProfileInBackground();
					saveProfileInBackground
						.setParameters(mContext, CallPiaApiInBackground.TYPE_RETURN_MAP, true)
						.setErrorDialog(true)
						.setProgressDialog(true, "更新正在提交，请稍等！");
					saveProfileInBackground.execute(URL_USER_EDIT_PROF);
				}
				
			}
		});
		
		
		
	}
	
	private class SaveProfileInBackground extends CallPiaApiInBackground{

		@Override
		protected HttpPost addParamstoPost(HttpPost post, Context context)
				throws UnsupportedEncodingException, IOException {
			
			//implement HttpPost behavior to save profile changes to api
			
			return post;
		}

		@Override
		protected void onCallCompleted(Map<String, Object> result) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onCallCompleted(List<Map<String, Object>> result) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
