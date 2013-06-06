package net.johnsonlau.remotecontrol.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbAdapter {

	private final Context mContext;
	private DbOpenHelper mDbOpenHelper;
	private SQLiteDatabase mDb;

	public DbAdapter(Context content) {
		this.mContext = content;
	}

	public DbAdapter open() throws SQLException {
		mDbOpenHelper = new DbOpenHelper(mContext);
		mDb = mDbOpenHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbOpenHelper.close();
	}

	// == settings ============================================================

	public boolean updateSettings(int server, String server1Ip,
			String server2Ip, String server3Ip, int serverPort,
			String password, float pointerSpeed, float wheelSpeed,
			int responsePrecision, int keyInterval, int pingInterval,
			int tapToClick) {
		ContentValues args = new ContentValues();
		args.put(DbOpenHelper.TABLE_SETTINGS_SERVER, server);
		args.put(DbOpenHelper.TABLE_SETTINGS_SERVER1_IP, server1Ip);
		args.put(DbOpenHelper.TABLE_SETTINGS_SERVER2_IP, server2Ip);
		args.put(DbOpenHelper.TABLE_SETTINGS_SERVER3_IP, server3Ip);
		args.put(DbOpenHelper.TABLE_SETTINGS_SERVER_PORT, serverPort);
		args.put(DbOpenHelper.TABLE_SETTINGS_PASSWORD, password);
		args.put(DbOpenHelper.TABLE_SETTINGS_POINTER_SPEED, pointerSpeed);
		args.put(DbOpenHelper.TABLE_SETTINGS_WHEEL_SPEED, wheelSpeed);
		args.put(DbOpenHelper.TABLE_SETTINGS_RESPONSE_PRECISION,
				responsePrecision);
		args.put(DbOpenHelper.TABLE_SETTINGS_KEY_INTERVAL, keyInterval);
		args.put(DbOpenHelper.TABLE_SETTINGS_PING_INTERVAL, pingInterval);
		args.put(DbOpenHelper.TABLE_SETTINGS_TAP_TO_CLICK, tapToClick);

		return mDb.update(DbOpenHelper.TABLE_SETTINGS, args, null, null) > 0;
	}

	public Cursor fetchSettings() throws SQLException {
		Cursor cursor = mDb.query(true, DbOpenHelper.TABLE_SETTINGS,
				new String[] { DbOpenHelper.TABLE_SETTINGS_SERVER,
						DbOpenHelper.TABLE_SETTINGS_SERVER1_IP,
						DbOpenHelper.TABLE_SETTINGS_SERVER2_IP,
						DbOpenHelper.TABLE_SETTINGS_SERVER3_IP,
						DbOpenHelper.TABLE_SETTINGS_SERVER_PORT,
						DbOpenHelper.TABLE_SETTINGS_PASSWORD,
						DbOpenHelper.TABLE_SETTINGS_POINTER_SPEED,
						DbOpenHelper.TABLE_SETTINGS_WHEEL_SPEED,
						DbOpenHelper.TABLE_SETTINGS_RESPONSE_PRECISION,
						DbOpenHelper.TABLE_SETTINGS_KEY_INTERVAL,
						DbOpenHelper.TABLE_SETTINGS_PING_INTERVAL,
						DbOpenHelper.TABLE_SETTINGS_TAP_TO_CLICK }, null, null,
				null, null, null, null);

		return cursor;
	}
}
