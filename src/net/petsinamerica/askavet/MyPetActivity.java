package net.petsinamerica.askavet;

import java.util.Map;

import net.petsinamerica.askavet.utils.App;
import net.petsinamerica.askavet.utils.Constants;
import net.petsinamerica.askavet.utils.GeneralHelpers;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MyPetActivity extends Activity {
	
	TextView tvPetName;
	TextView tvPetSex;
	TextView tvPetBreed;
	TextView tvPetSpecies;
	TextView tvPetBDay;
	TextView tvPetNeuterAge;
	TextView tvPetInsurance;
	ImageView ivPetAvatar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_petinfo);
		
		// reference all views 
		tvPetName = (TextView) findViewById(R.id.activity_petinfo_petName);
		tvPetSex = (TextView) findViewById(R.id.activity_petinfo_petsex);
		tvPetBDay = (TextView) findViewById(R.id.activity_petinfo_pet_bday);
		tvPetBreed = (TextView) findViewById(R.id.activity_petinfo_petbreed);
		tvPetNeuterAge = (TextView) findViewById(R.id.activity_petinfo_pet_neuterage);
		tvPetSpecies = (TextView) findViewById(R.id.activity_petinfo_petspecies);
		tvPetInsurance = (TextView) findViewById(R.id.activity_petinfo_petinsurance);
		ivPetAvatar = (ImageView) findViewById(R.id.activity_petinfo_pet_pic);
		
		
		int petId = getIntent().getIntExtra("id", -1);
		if (petId != -1){
			new GetPetInfo().execute(Constants.URL_PETINFO + petId);
		}
		
	}
	
	private class GetPetInfo extends GeneralHelpers.CallInBackground{

		@Override
		protected void onCallCompleted(Map<String, Object> result) {
			String petName = result.get("name").toString();
			String petSex = result.get("sex").toString();
			String petBDay = result.get("birth").toString();
			String petBreed = result.get("breed").toString();
			String petNeuterAge = result.get("desex").toString();
			String petInsurance = result.get("insurance").toString();
			String petSpecies = result.get("species").toString();
			String petImageUrl = result.get("avatar").toString();
			if (petImageUrl!= null && !petImageUrl.startsWith("http")){
				petImageUrl = Constants.URL_FILE_STORAGE + petImageUrl;
			}
			
			tvPetName.setText(petName);
			tvPetSex.setText("(" + petSex + ")");
			tvPetBDay.setText(petBDay);
			tvPetBreed.setText(petBreed);
			tvPetNeuterAge.setText(petNeuterAge + " 个月");
			tvPetSpecies.setText(petSpecies);
			tvPetInsurance.setText(petInsurance);
			
			
			Picasso.with(App.appContext)
				   .load(petImageUrl)
				   .into(ivPetAvatar);
			
			
		}
		
	}
	
	
	
}
