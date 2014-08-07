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
			PiaSQLiteHelper.COLUMN_TYPE,
			PiaSQLiteHelper.COLUMN_SUBJECT,
			PiaSQLiteHelper.COLUMN_CONTENT
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
	
	public PiaNotification createNotification(long type, String subject, String message){
		ContentValues values = new ContentValues();
		values.put(PiaSQLiteHelper.COLUMN_TYPE, type);
		values.put(PiaSQLiteHelper.COLUMN_SUBJECT, subject);
		values.put(PiaSQLiteHelper.COLUMN_CONTENT, message);
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
				allcolumns, null, null, null, null, null);
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
	 * get the count of all rows in the SQL database
	 * @return
	 */
	public Integer getCount(){
		String queryStr = "SELECT * FROM " + PiaSQLiteHelper.NOTIFICATION_TABLE_NAME;
		Cursor cursor = database.rawQuery(queryStr, null);
		int cnt = cursor.getCount();
		cursor.close();
		return cnt;
	}

	private PiaNotification cursorToNotification(Cursor cursor) {
		PiaNotification notification = new PiaNotification();
		notification.setId(cursor.getLong(0));
		notification.setType(cursor.getLong(1));
		notification.setSubject(cursor.getString(2));
		notification.setMessage(cursor.getString(3));
		return notification;
	}
	
}
