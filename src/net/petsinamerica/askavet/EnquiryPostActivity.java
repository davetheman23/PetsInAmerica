package net.petsinamerica.askavet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.petsinamerica.askavet.utils.AccessToken;
import net.petsinamerica.askavet.utils.AccessTokenManager;
import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.CallPiaApiInBackground;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;
import net.petsinamerica.askavet.utils.UserInfoManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.meetme.android.horizontallistview.HorizontalListView;

public class EnquiryPostActivity extends FragmentActivity {
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	
	
	private static HorizontalListView mHLview = null;
	
	private static PetListAdapter petAdapter = null;
	
	private ArrayList<Bundle> imageData = null;
	
	private Uri tmpFileUri = null;
	
	private static int imageId = 0;
	
	private ProgressDialog uploadProgDialog = null;
	
	/*
	 * the class that holds all questions entries
	 */
	private static Bundle mUserInputs;
	private static final String TITLE = Constants.KEY_TITLE;
	private static final String PET_ID = Constants.KEY_ENQUIRY_PETID;
	private static final String PET_DIET = Constants.KEY_ENQUIRY_PET_DIETTYPE;
	private static final String PET_DIETDESCRIPTION = Constants.KEY_ENQUIRY_PET_DIETDESCR;
	private static final String PET_WEIGHT = Constants.KEY_ENQUIRY_PET_WEIGHT;
	private static final String PET_MENTATION = Constants.KEY_ENQUIRY_PET_RESPONSIVE;
	private static final String PET_APPETITE = Constants.KEY_ENQUIRY_PET_APPETITE;
	private static final String PET_STOOL = Constants.KEY_ENQUIRY_PET_STOOL;
	private static final String PROBLEM_DESCRIPTION = Constants.KEY_CONTENT;
	private static final String SHOW_PUBLIC = "public";

	private static final String KEY_URI = "uri";
	private static final String KEY_URL = "url";
	private static final String KEY_ID = "id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enquiry_post);
		
		// inhibit the auto-popup of the soft key board when creating the activity
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
		
		/*
		 *  set up the listener for inserting an image to the post, it should be 
		 *  either picking an image from local storage or taking a picture
		 */
		//Button btnInsertImage = (Button) findViewById(R.id.activity_enquiry_btn_insertImage);
		LinearLayout ll_InsertImage = (LinearLayout) findViewById(R.id.activity_enquiry_ll_insertImage);
		ll_InsertImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// make an intent to bring up a built-in camera
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				File imageFile = GeneralHelpers.getOutputMediaFile(
									GeneralHelpers.MEDIA_TYPE_IMAGE, false);
				if (imageFile != null){
					tmpFileUri = Uri.fromFile(imageFile);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpFileUri);
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
		
		GetPetListTask getPetListTask = (GetPetListTask) new GetPetListTask()
			.setParameters(this, CallPiaApiInBackground.TYPE_RETURN_LIST, true);
		String url = Constants.URL_USERPETS + Integer.parseInt(UserInfoManager.userid);
		getPetListTask.execute(url);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE){
			if (resultCode == Activity.RESULT_OK){
				
				Toast.makeText(this, "Image taken", Toast.LENGTH_LONG).show();
				
				// add the uri to an array that will be used to upload images separated from 
				addUploadImageUri(tmpFileUri);
				
				// TODO compress file to a reasonable size
				//File file = new File(tmpFileUri.getPath());

			}else if(resultCode == Activity.RESULT_CANCELED){
				Toast.makeText(this, "Action cancelled", Toast.LENGTH_LONG).show();
			}else{
				// Image capture failed, advise user
			}
		}
	}
	
	private boolean isEntryValid(){
		extractUserInputs();
		if (mUserInputs == null){
			return false;
		}
		if (mUserInputs.getInt(PET_ID, -1) == -1){
			Toast.makeText(getApplication(), "请选择宠物", Toast.LENGTH_LONG).show();
			return false;
		}
		if (mUserInputs.getInt(PET_WEIGHT, -999) < 0){
			Toast.makeText(getApplication(), "请确认宠物重量是否输入正确", Toast.LENGTH_LONG).show();
		}
		if (mUserInputs.get(TITLE).toString().equals("")){
			Toast.makeText(getApplication(), "请输入一个标题", Toast.LENGTH_LONG).show();
		}
		return true;
	}
	
	private void showErrorDialog(){
		
	}
	/*private void showInvalidInputDialog(View v){
		
	}*/
	
	private void addUploadImageUri(Uri uriFile){
		if (imageData == null){
			imageData = new ArrayList<Bundle>();
		}
		imageId = imageId + 1;
		
		Bundle bundle = new Bundle();
		bundle.putString(KEY_URI, uriFile.toString());;
		bundle.putInt(KEY_ID, imageId);
		bundle.putString(KEY_URL, null);
		imageData.add(bundle);
		
		//String savedImageName = uriFile.getLastPathSegment();
		EditText problemView = (EditText) findViewById(R.id.activity_enquiry_problem_description);
		//problemView.append("[Img_"+ imageId + ":" + savedImageName + "]");
		problemView.append("\n[Img_"+ imageId + "]\n");
		
		new HttpUploadImage()
			.setImageId(imageId)
			.execute(uriFile);
	}
	
	/**
	 * when the button post is clicked, it does three things:
	 * 1. extract the data from the form
	 * 2. replace the image tag in the text with image url
	 * 3. setup an async Task to upload data
	 */
	private void submitEnquiry(){		
		
		// get the inputs everytime the submit button is hit
		extractUserInputs();
		
		/*
		 * if user has inserted some image
		 */
		if (imageData != null){
			for (Bundle data : imageData){
				String imageurl = data.getString(KEY_URL);
				
				//if url is still null, then it means, async task is running in the background 
				if (imageurl == null){
					Toast.makeText(getApplication(), 
								"上传图片还在进行中，请稍后再试", 
								Toast.LENGTH_LONG).show();
					return;
				}
			}
			EditText problemView = (EditText) findViewById(
									R.id.activity_enquiry_problem_description);
			String text = problemView.getText().toString();
			
			for (Bundle data : imageData){
				// get the image data, including uri, url, id.
				String imageurl = data.getString(KEY_URL);
				//Uri imageUri = Uri.parse(data.getString(KEY_URI));
				//String imageFileName = imageUri.getLastPathSegment();
				int id = data.getInt(KEY_ID);
				
				/*
				 *  replace the image tag in the paragraph to a html readable string that
				 *  has also embedded the url of the image 
				 */
				//String pattern = "\\[Img_"+ id + ":" + imageFileName + "\\]";
				String pattern = "\\[Img_"+ id + "\\]";
				
				text = text.replaceFirst(pattern, "[img]" + imageurl + "[/img]");
				
				mUserInputs.putString(PROBLEM_DESCRIPTION, text);
				
			}
		}
		
		// upload enquiry only after all images are uploaded 
		new HttpUploadEnquiry()
			.setParameters(this,CallPiaApiInBackground.TYPE_RETURN_MAP,true)
			.setErrorDialog(true)
			.setProgressDialog(true, "请稍等，正在上传提问 ...")
			.execute(Constants.URL_NEWENQUIRY);
		
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
	private class GetPetListTask extends CallPiaApiInBackground{

		@Override
		protected void onCallCompleted(Map<String, Object> result) {
			
		}

		@Override
		protected void onCallCompleted(List<Map<String, Object>> result) {
			if (result != null){
				if (result.size() > 0 && !result.get(0).containsKey(Constants.KEY_ERROR_MESSAGE)){
					// if no error
					petAdapter = new PetListAdapter(EnquiryPostActivity.this, 
							R.layout.list_pet_item_with_selection, result);
					mHLview.setAdapter(petAdapter);
					if (result.size() == 0){
						Toast.makeText(EnquiryPostActivity.this, "您还没有任何宠物，请先添加宠物信息！", Toast.LENGTH_LONG).show();
					}
				}
			}else{
				GeneralHelpers.showAlertDialog(EnquiryPostActivity.this, 
						"获取宠物信息出错", "对不起，出错啦！错误原因不清，请稍后再试。");
			}
		}
		
	}
	
	private class HttpUploadEnquiry extends CallPiaApiInBackground{

		@Override
		protected void onCallCompleted(Map<String, Object> result) {			
			if (result != null){
				if (!result.containsKey(Constants.KEY_ERROR_MESSAGE)){
					// results are valid
					// do something
					GeneralHelpers.showAlertDialog(EnquiryPostActivity.this,
							"提问成功！",
							"您的提问已经成功，我们会尽快给您回复！");
				}
			}
		}

		@Override
		protected void onCallCompleted(List<Map<String, Object>> result) {
		}

		@Override
		protected HttpPost addParamstoPost(HttpPost post, Context context)
				throws UnsupportedEncodingException, IOException {
			
			List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(post.getEntity());
			
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
			
			// add the params into the post, make sure to include encoding UTF_8 as follows
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
			
			return post;
		}
		
		
		
	}
	
	
	private class HttpUploadImage extends AsyncTask<Uri, Void, Map<String, Object>>{
		
		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
		private Uri mFileUri = null;
		
		/*
		 * the local id of image currently in the text, needs to be set
		 * before execute() the asyncTask; 
		 */
		private int mImageLocalId = -1; 
		
		public HttpUploadImage setImageId(int id){
			mImageLocalId = id;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (uploadProgDialog == null){
				uploadProgDialog = new ProgressDialog(EnquiryPostActivity.this);
			}
			uploadProgDialog.setMessage("请稍等，正在上传图片 ...");
			uploadProgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			uploadProgDialog.show();
		}
		
		@Override
		protected Map<String, Object> doInBackground(Uri... params) {
			if (mImageLocalId == -1){
				return null;
			}
			mFileUri = params[0];
			
			File imageFile = new File(URI.create(mFileUri.toString()));
			
			HttpPost post = new HttpPost(Constants.URL_UPLOAD_IMAGE);
			
			AccessToken token = AccessTokenManager.readAccessToken(App.appContext);
			if (token.isExpired()){
				// TODO do something to report it
			}
			
			try {
				// creating a file body consisting of the file that we want to
				// send to the server
				FileBody fileBody = new FileBody(imageFile);
				
				// build the multipart request
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				builder.setCharset(Charset.forName(HTTP.UTF_8));
				
				// add the parts of file and user Id
				builder.addPart("userfile", fileBody);
				builder.addTextBody(Constants.KEY_USERID, token.getUserId());
				builder.addTextBody(Constants.KEY_USERTOKEN, token.getToken());
				
				
				post.setEntity(builder.build());
		
				// execute post
				HttpResponse response = mClient.execute(post);
				
				
				return GeneralHelpers.handlePiaResponse(response);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}finally{
				if (mClient!=null){
					mClient.close();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Map<String, Object> result) {
			super.onPostExecute(result);
			if (uploadProgDialog!= null){
				uploadProgDialog.dismiss();
			}
			if (result != null && imageData !=null){
				Toast.makeText(getApplication(), "上传成功", Toast.LENGTH_LONG).show();
				String url = result.get(KEY_URL).toString();
				
				for (Bundle data : imageData){
					int id = data.getInt(KEY_ID);
					if (id == imageId){
						data.putString(KEY_URL, url);
					}
				}
				
			}else{
				Toast.makeText(getApplication(), "上传不成功，请再试一次", Toast.LENGTH_LONG).show();
			}
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
