package net.petsinamerica.askavet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.BaseListFragment;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;
import net.petsinamerica.askavet.utils.UserInfoManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.meetme.android.horizontallistview.HorizontalListView;

public class EnquiryFormActivity extends FragmentActivity {
	
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	
	private static final String KEY_RESULT = App.appContext.getResources()
												.getString(R.string.JSON_tag_result);
	
	
	private static HorizontalListView mHLview = null;
	
	private static PetListAdapter petAdapter = null;
	
	private static String savedImageFilePath = null;
	
	/*
	 * the class that holds all questions entries
	 */
	private static Bundle mUserInputs;
	private static final String TITLE = "title";
	private static final String PET_ID = "petid";
	private static final String PET_DIET = "diettype";
	private static final String PET_DIETDESCRIPTION = "dietdescr";
	private static final String PET_WEIGHT = "weight";
	private static final String PET_MENTATION = "responsive";
	private static final String PET_APPETITE = "appetite";
	private static final String PET_STOOL = "stool";
	private static final String PROBLEM_DESCRIPTION = "content";
	private static final String SHOW_PUBLIC = "public";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enquiry_form);
		
		
		// inhibit the auto-popup of the soft key board when creating the activity
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
		
		/*
		 *  set up the listener for inserting an image to the post, it should be 
		 *  either picking an image from local storage or taking a picture
		 */
		Button btnInsertImage = (Button) findViewById(R.id.activity_enquiry_btn_insertImage);
		btnInsertImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// make an intent to bring up a built-in camera
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				File imageFile = GeneralHelpers.getOutputMediaFile(
									GeneralHelpers.MEDIA_TYPE_IMAGE);
				if (imageFile != null){
					Uri uriSavedImage = Uri.fromFile(imageFile);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
					startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				}else{
					// do something to advise user
				}

			}
		});
		
		/*
		 *  set up the listener for posting the enquiry, it needs to first check for invalid
		 *  entries, then setup a service to upload the post whenever the internet is ready
		 */
		Button btnPostEnquiry = (Button) findViewById(R.id.activity_enquiry_btn_post); 
		btnPostEnquiry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// if not all entries are valid
				if (!isEntryValid()){
					showErrorDialog();
					return;
				}
				// if all entries are valid
				submitEnquiry();
			}
		});
		
		/*
		 *  set up a horizontal listview for a list of pets 
		 */
		mHLview = (HorizontalListView) findViewById(R.id.activity_enquiry_petlist_horizontal_view);
		mHLview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentView, View view, int position,
					long itemId) {
				
				if (petAdapter!= null){
					petAdapter.setItemSelected(view, position);
				}
			}
		});
		
		// set up a fragment, just to load the pet data into an adapter in a background thread 
		PetListFragment petListFragment = new PetListFragment();
		petListFragment.setParameters(Constants.URL_USERPETS, KEY_RESULT,false);
		petListFragment.setPage(Integer.parseInt(UserInfoManager.userid));
		petListFragment.setUserDataFlag(true);
		getSupportFragmentManager()
			.beginTransaction()
			.add(petListFragment, "PetListFragment_wo_layout")
			.commit();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE){
			if (resultCode == Activity.RESULT_OK){
				// TODO change the icon of the upload button, such bring in a preview of the picture taken
				Toast.makeText(this, "Image taken", Toast.LENGTH_LONG).show();
				
				File imageFile = GeneralHelpers.getOutputMediaFile(
						GeneralHelpers.MEDIA_TYPE_IMAGE);
				Uri uriFile = Uri.fromFile(imageFile);
				savedImageFilePath = uriFile.getPath();
				TextView imagePathView = (TextView) findViewById(R.id.activity_enquiry_tv_imagepath);
				imagePathView.setText("暂存在 " + uriFile.getLastPathSegment());
				
			}else if(resultCode == Activity.RESULT_CANCELED){
				Toast.makeText(this, "Action cancelled", Toast.LENGTH_LONG).show();
			}else{
				// Image capture failed, advise user
			}
		}
		
	}
	
	private boolean isEntryValid(){
		return true;
	}
	
	private void showErrorDialog(){
		
	}
	private void showInvalidInputDialog(View v){
		
	}
	
	private void submitEnquiry(){
		// get the inputs everytime the submit button is hit
		extractUserInputs();
		
		new HttpUploadEnquiry().execute(Constants.URL_NEWENQUIRY);
		
	}
	
	private void extractUserInputs(){
		EditText titleView = (EditText) findViewById(R.id.activity_enquiry_title);
		
		// get a reference to each view in the UI
		Spinner dietView = (Spinner) findViewById(R.id.activity_enquiry_pet_diet_sel);
		EditText descrView = (EditText) findViewById(R.id.activity_enquiry_pet_diet_description);
		EditText weightView = (EditText) findViewById(R.id.activity_enquiry_body_weight);
		Spinner mentationView = (Spinner) findViewById(R.id.activity_enquiry_mentation);
		Spinner appetiteView = (Spinner) findViewById(R.id.activity_enquiry_appetite);
		Spinner bowelView = (Spinner) findViewById(R.id.activity_enquiry_bowel_movement);
		EditText problemView = (EditText) findViewById(R.id.activity_enquiry_problem_description);
		CheckBox publicView = (CheckBox) findViewById(R.id.activity_enquiry_show_public);
		
		// store the data current from the UI to the userinput bundle
		mUserInputs = new Bundle();
		mUserInputs.putString(TITLE, titleView.getText().toString());
		mUserInputs.putString(PET_DIET, dietView.getSelectedItem().toString());
		mUserInputs.putString(PET_DIETDESCRIPTION, descrView.getText().toString());
		mUserInputs.putInt(PET_WEIGHT, Integer.parseInt(weightView.getText().toString()));
		mUserInputs.putString(PET_MENTATION, mentationView.getSelectedItem().toString());
		mUserInputs.putString(PET_APPETITE, appetiteView.getSelectedItem().toString());
		mUserInputs.putString(PET_STOOL, bowelView.getSelectedItem().toString());
		mUserInputs.putString(PROBLEM_DESCRIPTION, problemView.getText().toString());
		mUserInputs.putInt(SHOW_PUBLIC, (publicView.isChecked() ? 1 : 0));
		if (petAdapter != null){
			mUserInputs.putInt(PET_ID, petAdapter.getSelectedItemId());
		}
	}
	
	public static class PetListFragment extends BaseListFragment{

		@Override
		protected void onHttpDoneSetAdapter(
				List<Map<String, Object>> resultArray) {
			petAdapter = new PetListAdapter(getActivity(), 
						R.layout.pet_list_item_with_selection, resultArray);
			mHLview.setAdapter(petAdapter);
		}

		@Override
		protected void onItemClickAction(View v, int position, long id) {			
			return;
		}
		
	}
	
	private class HttpUploadEnquiry extends AsyncTask<String, Void, Exception>{
		
		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");

		@Override
		protected Exception doInBackground(String... params) {
			String url = params[0];
			HttpPost post = new HttpPost(url);
			
			AccessToken token = AccessTokenManager.readAccessToken(App.appContext);
			if (token.isExpired()){
				// TODO do something to report it
			}
			
			// construct the parameter list
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(11);
			
			// add user login information
			nameValuePairs.add(new BasicNameValuePair(Constants.TAG_USERID, token.getUserId()));
			nameValuePairs.add(new BasicNameValuePair(Constants.TAG_USERTOKEN, token.getToken()));
			
			// add user enquiries
			nameValuePairs.add(new BasicNameValuePair(TITLE, getCode(TITLE)));
			nameValuePairs.add(new BasicNameValuePair(PROBLEM_DESCRIPTION, getCode(PROBLEM_DESCRIPTION)));
			nameValuePairs.add(new BasicNameValuePair(PET_APPETITE, getCode(PET_APPETITE)));
			nameValuePairs.add(new BasicNameValuePair(PET_STOOL, getCode(PET_STOOL)));
			nameValuePairs.add(new BasicNameValuePair(PET_DIET, getCode(PET_DIET)));
			nameValuePairs.add(new BasicNameValuePair(PET_DIETDESCRIPTION, getCode(PET_DIETDESCRIPTION)));
			nameValuePairs.add(new BasicNameValuePair(PET_WEIGHT, Integer.toString(mUserInputs.getInt(PET_WEIGHT))));
			nameValuePairs.add(new BasicNameValuePair(PET_MENTATION, getCode(PET_MENTATION)));
			nameValuePairs.add(new BasicNameValuePair(SHOW_PUBLIC, getCode(SHOW_PUBLIC)));
			nameValuePairs.add(new BasicNameValuePair(PET_ID, Integer.toString(mUserInputs.getInt(PET_ID))));
			
			try {
				HttpParams httpParams = mClient.getParams();
				//httpParams.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, Charset.forName("UTF-8"));
				HttpConnectionParams.setConnectionTimeout(httpParams, 10*1000); // time in 10 second
				
				// add the params into the post, make sure to include encoding UTF_8 as follows
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
				
				
				// execute post
				HttpResponse response = mClient.execute(post);
				
				String responseString = new BasicResponseHandler().handleResponse(response);
				
				JSONObject responseObject = (JSONObject) new JSONTokener(responseString).nextValue(); 
				
				int i = 3;
				i = i+1;
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return e;
			} catch (IOException e) {
				e.printStackTrace();
				return e;
			} catch (JSONException e) {
				e.printStackTrace();
				return e;
			}finally{
				if (mClient!=null){
					mClient.close();
				}
			}
			return null;
		}
		
	}
	
	/**
	 * the server have specific codes for the options of each form element 
	 * 
	 * @param Key	the key to identify which user input 
	 * @return code to be recognized by the server
	 */
	private String getCode(String key){
		String input = mUserInputs.getString(key);
		if (key.equals(PET_DIET)){
			if (input.contains("Commercial Diet")){
				return "Commercial Diet";
			}else if (input.contains("Homemade Diet")){
				return "Homemade Diet";
			}
		}else if (key.contains(PET_MENTATION)){
			if (input.contains("Quiet Alert Responsive(QAR)")){
				return "Quiet Alert Responsive(QAR)";
			}else if (input.contains("Bright Alert Responsive (BAR)")){
				return "Bright Alert Responsive (BAR)";
			}else if (input.contains("Depressed")){
				return "Depressed";
			}else if (input.contains("Obtunded")){
				return "Obtunded";
			}else if (input.contains("Comatose")){
				return "Comatose";
			}
		}else if (key.contains(PET_APPETITE)){
			if (input.contains("Normal")){
				return "Normal";
			}else if (input.contains("Decreased appetite")){
				return "Decreased appetite";
			}else if (input.contains("Poor appetite")){
				return "Poor appetite";
			}else if (input.contains("No appetite")){
				return "No appetite";
			}
		}else if (key.contains(PET_STOOL)){
			if (input.contains("Normal")){
				return "Normal";
			}else if (input.contains("Constipated")){
				return "Constipated";
			}else if (input.contains("Dry")){
				return "Dry";
			}else if (input.contains("Diarrhea")){
				return "Diarrhea";
			}
		}
		return input;
	}
	
	
	
	

}
