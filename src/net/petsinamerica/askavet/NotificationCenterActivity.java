package net.petsinamerica.askavet;

import java.util.List;

import net.petsinamerica.askavet.utils.NotificationsDataSource;
import net.petsinamerica.askavet.utils.PiaNotification;
import net.petsinamerica.askavet.utils.PushReceiver;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
		
		private boolean mResumed = false;
		
		private ProgressBar pb;
		
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
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_standard_list, container, false);
			
			// get references to all objects
			pb = (ProgressBar) rootView.findViewById(android.R.id.progress);
			pb.setVisibility(View.VISIBLE);
			
			TextView tv = (TextView) rootView.findViewById(android.R.id.empty);
			tv.setText(getResources().getString(R.string.no_new_notifications));
			
			return rootView;
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
				pb.setVisibility(View.GONE);
				
			}
		}
		
		@Override
		public void onResume() {
			super.onResume();
			if (dataSource == null){
				dataSource.open();
			}
			mResumed = true;
		}
		
		@Override
		public void onPause() {
			dataSource.close();
			mResumed = false;
			super.onPause();
		}
		
		

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);
			
			int status = ((NotificationAdapter)getListAdapter()).getNotificationStatus(v);
			// if the status being clicked is the new notification
			if (status == PiaNotification.STATUS_RECEIVED){
				
				//TODO popup a dialog box to get further information
				
				// update the database on the status of the notification record being clicked
				long nId = ((NotificationAdapter)getListAdapter()).getNotificationId(v);
				dataSource.updateStatus(nId, PiaNotification.STATUS_VIEWED);
				
				// change the text color for the list item that has been clicked
				TextView tv_subject = (TextView) v.findViewById(R.id.list_notification_subject);
				TextView tv_content = (TextView) v.findViewById(R.id.list_notification_content);
				tv_subject.setTextColor(getResources().getColor(R.color.LightGrey));
				tv_content.setTextColor(getResources().getColor(R.color.LightGrey));
			}
		}

		@Override
		public void onReceivedNotification(PiaNotification notification) {
			// only add to the adapter if the activity is currently in resumed state
			if (mResumed){
				//adapter.add(notification);
				adapter.insert(notification, 0);
				adapter.notifyDataSetChanged();
			}
		}
		
		/**
		 * an adapter to show the notifications in custom views		 *
		 */
		private class NotificationAdapter extends ArrayAdapter<PiaNotification>{
			
			private int mResource;
			
			class ViewHolder{
				long id;
				RelativeLayout rl_container;
				TextView tv_subject;
				TextView tv_content;
				int status;
			}

			public NotificationAdapter(Context context, int layoutResourceId,
					List<PiaNotification> objects) {
				super(context, layoutResourceId, objects);
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
					viewHolder.rl_container = (RelativeLayout) rowview.findViewById(
																R.id.list_notification_relativelayout);
					viewHolder.tv_subject =(TextView) rowview.findViewById(R.id.list_notification_subject);	
					viewHolder.tv_content = (TextView) rowview.findViewById(R.id.list_notification_content);
				
					// set tag for future reuse of the view
					rowview.setTag(viewHolder);
				}else{
					viewHolder = (ViewHolder) rowview.getTag();
				}
				
				// get the data for the current record
				PiaNotification notification = getItem(position);
				long id = notification.getId();
				String str1 = notification.getSubject();
				String str2 = notification.getContent();
				int status = notification.getStatus();
				
				String createTime = notification.getCreated_at();
				//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
				
				// set values to the viewholder
				viewHolder.rl_container.setBackgroundResource(R.drawable.layer_card_background);
				viewHolder.tv_subject.setText(str1);
				//viewHolder.tv_content.setText(str2);
				//viewHolder.tv_content.setText(sdf.format(createTime));
				viewHolder.tv_content.setText(createTime);
				viewHolder.id = id;
				viewHolder.status = status;
				
				// dim the notification records that have been read already 
				if (status == PiaNotification.STATUS_VIEWED){
					viewHolder.tv_subject.setTextColor(getResources().getColor(R.color.LightGrey));
					viewHolder.tv_content.setTextColor(getResources().getColor(R.color.LightGrey));
				}
				
				return rowview;
			}
			
			public long getNotificationId(View v){
				ViewHolder vh = (ViewHolder) v.getTag();
				return vh.id;
			}
			
			public int getNotificationStatus(View v){
				ViewHolder vh = (ViewHolder) v.getTag();
				return vh.status;
			}
			
			
		}
		
	}

}
