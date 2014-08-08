package net.petsinamerica.askavet.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PiaSQLiteHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "pia_notifications.db";
    private static final int DATABASE_VERSION = 4;
    public static final String NOTIFICATION_TABLE_NAME = "notifications";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_SUBJECT = "subject";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CREATEAT = "created_at";
    public static final String COLUMN_STATUS = "status";
    
    
    // Database creation sql statement
    private static final String NOTIFICATION_TABLE_CREATE = "CREATE TABLE " 
    			+ NOTIFICATION_TABLE_NAME + " (" 
    			+ COLUMN_ID + " integer primary key autoincrement," 
    			+ COLUMN_UID + " integer not null," 
    			+ COLUMN_TYPE + " integer not null, " 
    			+ COLUMN_SUBJECT + " text,"
    			+ COLUMN_CONTENT + " text,"
    			+ COLUMN_CREATEAT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
    			+ COLUMN_STATUS + " integer not null);";

    PiaSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NOTIFICATION_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(PiaSQLiteHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATION_TABLE_NAME);
		onCreate(db);
		
	}
}
