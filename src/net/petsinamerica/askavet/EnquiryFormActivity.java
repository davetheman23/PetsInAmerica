package net.petsinamerica.askavet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class EnquiryFormActivity extends Activity {
	
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enquiry_form);
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
		
		Button btnUploadImage = (Button) findViewById(R.id.frag_enquiry_btn_uploadImg);
		btnUploadImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
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
		
		Button btnPostEnquiry = (Button) findViewById(R.id.frag_enquiry_btn_post); 
		btnPostEnquiry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {				
				
			}
		});
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE){
			if (resultCode == Activity.RESULT_OK){
				// TODO change the icon of the upload button, such bring in a preview of the picture taken
				Toast.makeText(this, "Image taken", Toast.LENGTH_LONG).show();
				/*Intent intent = new Intent();
				intent.setType("image/*");
				
				intent.putExtra(MediaStore.EXTRA_OUTPUT, data.getParcelableExtra("data"));
				intent.setAction(Intent.ACTION_SEND);
				startActivityForResult(intent, 301);*/
				
			}else if(resultCode == Activity.RESULT_CANCELED){
				Toast.makeText(this, "Action cancelled", Toast.LENGTH_LONG).show();
			}else{
				// Image capture failed, advise user
			}
		}
		
	}
	
	
	
	

}
