package net.petsinamerica.askavet;

import java.util.List;

import net.petsinamerica.askavet.utils.NotificationsDataSource;
import net.petsinamerica.askavet.utils.PiaNotification;
import net.petsinamerica.askavet.utils.PushReceiver;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NotificationCenterActivity extends FragmentActivity {
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
	
	public static class NotificationListFragment extends ListFragment implements 
		PushReceiver.onReceiveNotificationListener{
		private NotificationsDataSource dataSource;
		private ArrayAdapter<PiaNotification> adapter;
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			dataSource = new NotificationsDataSource(activity);
			dataSource.open();
			// register to the notification listener so as to make changes to the 
			// list of notifications as soon as the notification arrives
			PushReceiver.registerPiaNotificationListener(this);
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			if (getListAdapter() == null){
				List<PiaNotification> notifications = dataSource.getAllNotifications();
				
				adapter = new NotificationAdapter(getActivity(), 
												  R.layout.list_notification_item,
												  notifications);
				setListAdapter(adapter);
			}
		}
		
		@Override
		public void onResume() {
			dataSource.open();
			super.onResume();
		}
		
		@Override
		public void onPause() {
			dataSource.close();
			super.onPause();
		}

		@Override
		public void onReceivedNotification(PiaNotification notification) {
			adapter.add(notification);
			adapter.notifyDataSetChanged();
		}
		
		/**
		 * an adapter to show the notifications in custom views		 *
		 */
		private class NotificationAdapter extends ArrayAdapter<PiaNotification>{
			private Context mContext;
			private int mResource;
			
			class ViewHolder{
				TextView tv_subject;
				TextView tv_content;
				boolean isNew;
			}

			public NotificationAdapter(Context context, int layoutResourceId,
					List<PiaNotification> objects) {
				super(context, layoutResourceId, objects);
				mContext = context;
				mResource = layoutResourceId;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				
				// reuse views - for faster loading, avoid inflation everytime
				ViewHolder viewHolder = null;
				View rowview = convertView;
				if (rowview == null){
					LayoutInflater inflater = (LayoutInflater) getActivity().
								getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					// if no rowview before, new viewholder is created
					viewHolder = new ViewHolder();
					
					// inflate the layout view, and get individual views
					rowview = inflater.inflate(mResource, parent, false);
					viewHolder.tv_subject =(TextView) rowview.findViewById(R.id.list_notification_subject);	
					viewHolder.tv_content = (TextView) rowview.findViewById(R.id.list_notification_content);

					// set tag for future reuse of the view
					rowview.setTag(viewHolder);
				}else{
					viewHolder = (ViewHolder) rowview.getTag();
				}
				
				PiaNotification notification = getItem(position);
				
				String str1 = notification.getSubject();
				String str2 = notification.getMessage();
				
				viewHolder.tv_subject.setText(str1);
				
				viewHolder.tv_content.setText(str2);
				
				
				return rowview;
			}
			
			
		}
		
	}

}
