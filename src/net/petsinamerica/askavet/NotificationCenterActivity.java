package net.petsinamerica.askavet;

import net.petsinamerica.askavet.MyPetActivity.PetListFragment;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.View;

public class NotificationCenterActivity extends FragmentActivity{
	public FragmentManager fm = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notifications);
		
		// setup the list fragment that shows a list of all pets of the user		
		NotificationListFragment notifyListFragment = new NotificationListFragment();
		
		fm = getSupportFragmentManager(); 
		fm.beginTransaction()
			.add(R.id.activity_notifications_container, notifyListFragment)
			.commit();
		
	}
	
	public static class NotificationListFragment extends ListFragment{

		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			
		}
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			
		}

		
		
	}
}
