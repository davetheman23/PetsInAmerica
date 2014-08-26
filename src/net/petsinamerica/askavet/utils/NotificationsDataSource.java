package net.petsinamerica.askavet.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class NotificationsDataSource {

	private SQLiteDatabase database;
	private PiaSQLiteHelper dbHelper;
	private String[] allcolumns = {
			PiaSQLiteHelper.COLUMN_ID,
			PiaSQLiteHelper.COLUMN_UID,
			PiaSQLiteHelper.COLUMN_TYPE,
			PiaSQLiteHelper.COLUMN_SUBJECT,
			PiaSQLiteHelper.COLUMN_CONTENT,
			PiaSQLiteHelper.COLUMN_CREATEAT,
			PiaSQLiteHelper.COLUMN_STATUS
	};
	
	public NotificationsDataSource(Context context){
		dbHelper = new PiaSQLiteHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		database.close();
	}
	
	public PiaNotification createNotification(long type, String uid,
					String subject, String content){
		ContentValues values = new ContentValues();
		values.put(PiaSQLiteHelper.COLUMN_TYPE, type);
		values.put(PiaSQLiteHelper.COLUMN_UID, uid);
		values.put(PiaSQLiteHelper.COLUMN_SUBJECT, subject);
		values.put(PiaSQLiteHelper.COLUMN_CONTENT, content);
		values.put(PiaSQLiteHelper.COLUMN_STATUS, PiaNotification.STATUS_RECEIVED);
		long insertId = database.insert(PiaSQLiteHelper.NOTIFICATION_TABLE_NAME, 
				null, values);
		Cursor cursor = database.query(PiaSQLiteHelper.NOTIFICATION_TABLE_NAME, allcolumns,
				PiaSQLiteHelper.COLUMN_ID + " = " + insertId, null,null,null,null);
		cursor.moveToFirst();
		PiaNotification newNotification = cursorToNotification(cursor);
		cursor.close();
		return newNotification;
	}
	
	public void deleteNotification(PiaNotification notification){
		long deleteId = notification.getId();
		database.delete(PiaSQLiteHelper.NOTIFICATION_TABLE_NAME, 
					PiaSQLiteHelper.COLUMN_ID + " = " + deleteId, null);
	}
	
	public List<PiaNotification> getAllNotifications(){
		List<PiaNotification> notifications = new ArrayList<PiaNotification>();
		Cursor cursor = database.query(PiaSQLiteHelper.NOTIFICATION_TABLE_NAME,
				allcolumns, null, null, null, null, PiaSQLiteHelper.COLUMN_CREATEAT + " DESC");
		cursor.moveToFirst();
		while (! cursor.isAfterLast()){
			PiaNotification notification = cursorToNotification(cursor);
			notifications.add(notification);
			cursor.moveToNext();
		}
		cursor.close();
		return notifications;
	}
	
	public List<PiaNotification> getAllRecentNotifications(){
		//TODO this requires an update of the database, to include a time column
		return null;
	}
	
	public List<PiaNotification> getUnreadNotifications(){
		List<PiaNotification> notifications = new ArrayList<PiaNotification>();
		Cursor cursor = database.query(PiaSQLiteHelper.NOTIFICATION_TABLE_NAME,
				allcolumns, PiaSQLiteHelper.COLUMN_STATUS + " = " + PiaNotification.STATUS_RECEIVED, 
				null, null, null, PiaSQLiteHelper.COLUMN_CREATEAT + " DESC");
		cursor.moveToFirst();
		while (! cursor.isAfterLast()){
			PiaNotification notification = cursorToNotification(cursor);
			notifications.add(notification);
			cursor.moveToNext();
		}
		cursor.close();
		return notifications;
	}
	
	/**
	 * @return the count of all notifications
	 */
	public Integer getCount(){
		String queryStr = "SELECT * FROM " + PiaSQLiteHelper.NOTIFICATION_TABLE_NAME;
		database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery(queryStr, null);
		int cnt = cursor.getCount();
		cursor.close();
		database.close();
		return cnt;
	}
	
	/**
	 * @return count of on the notifications that have not been read
	 */
	public Integer getUnreadCount(){
		String queryStr = "SELECT * FROM " + PiaSQLiteHelper.NOTIFICATION_TABLE_NAME
			+ " WHERE " + PiaSQLiteHelper.COLUMN_STATUS + " = " + PiaNotification.STATUS_RECEIVED;
		database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery(queryStr, null);
		int cnt = cursor.getCount();
		cursor.close();
		database.close();
		return cnt;
	}
	/**
	 * update the status for one notification record
	 */
	public void updateStatus(long id, int status){
		String strFilter = "_id = " + id;
		ContentValues values = new ContentValues();
		values.put(PiaSQLiteHelper.COLUMN_STATUS, status);
		database = dbHelper.getWritableDatabase();
		database.update(PiaSQLiteHelper.NOTIFICATION_TABLE_NAME, values, strFilter, null);
		database.close();
	}
	/**
	 * update the statuses for multiple notification records to the same value,
	 */
	public void updateStatuses(String[] ids, int status){
		String strFilter = "_id = ?";
		ContentValues values = new ContentValues();
		values.put(PiaSQLiteHelper.COLUMN_STATUS, status);
		database = dbHelper.getWritableDatabase();
		database.update(PiaSQLiteHelper.NOTIFICATION_TABLE_NAME, values, strFilter, ids);
		database.close();
	}

	private PiaNotification cursorToNotification(Cursor cursor) {
		PiaNotification notification = new PiaNotification();
		notification.setId(cursor.getLong(0));
		notification.setUid(cursor.getString(1));
		notification.setType(cursor.getLong(2));
		notification.setSubject(cursor.getString(3));
		notification.setContent(cursor.getString(4));
		notification.setCreated_at(cursor.getString(5));
		notification.setStatus(cursor.getInt(6));
		return notification;
	}
	
}
