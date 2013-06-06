package net.johnsonlau.remotecontrol.db;

import net.johnsonlau.remotecontrol.Config;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbOpenHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 1;

	// == table settings =============================================

	public static final String TABLE_SETTINGS = "settings";
	public static final String TABLE_SETTINGS_ROWID = "_id";
	public static final String TABLE_SETTINGS_SERVER = "server";
	public static final String TABLE_SETTINGS_SERVER1_IP = "server1_ip";
	public static final String TABLE_SETTINGS_SERVER2_IP = "server2_ip";
	public static final String TABLE_SETTINGS_SERVER3_IP = "server3_ip";
	public static final String TABLE_SETTINGS_SERVER_PORT = "server_port";
	public static final String TABLE_SETTINGS_PASSWORD = "password";
	public static final String TABLE_SETTINGS_POINTER_SPEED = "pointer_speed";
	public static final String TABLE_SETTINGS_WHEEL_SPEED = "wheel_speed";
	public static final String TABLE_SETTINGS_RESPONSE_PRECISION = "response_precision";
	public static final String TABLE_SETTINGS_KEY_INTERVAL = "key_interval";
	public static final String TABLE_SETTINGS_PING_INTERVAL = "ping_interval";
	public static final String TABLE_SETTINGS_TAP_TO_CLICK = "tab_to_click";

	private static final String TABLE_SETTINGS_CREATE = "CREATE TABLE "
			+ TABLE_SETTINGS + "(_id INTEGER PRIMARY KEY AUTOINCREMENT"
			+ ",server INTEGER NOT NULL" + ",server1_ip TEXT NOT NULL"
			+ ",server2_ip TEXT NOT NULL" + ",server3_ip TEXT NOT NULL"
			+ ",server_port INTEGER NOT NULL" + ",password TEXT NOT NULL"
			+ ",pointer_speed FLOAT NOT NULL" + ",wheel_speed FLOAT NOT NULL"
			+ ",response_precision INTEGER NOT NULL"
			+ ",key_interval INTEGER NOT NULL"
			+ ",ping_interval INTEGER NOT NULL"
			+ ",tab_to_click INTEGER NOT NULL);";

	private static final String TABLE_SETTINGS_INITIALIZE = "INSERT INTO settings "
			+ "(server, server1_ip, server2_ip, server3_ip, server_port, password, pointer_speed, wheel_speed, response_precision, key_interval, ping_interval, tab_to_click)"
			+ " VALUES(" + "1" // server
			+ ",'192.168.1.10'" // server1_ip
			+ ",'172.16.1.27'" // server2_ip
			+ ",'192.168.1.100'" // server3_ip
			+ ",9001" // server_port
			+ ",'123456'" // password
			+ ",0.8" // pointer_speed
			+ ",3" // wheel_speed
			+ ",2" // response_precision
			+ ",200" // key_interval, in milliseconds
			+ ",5" // ping_interval, in seconds
			+ ",1);"; // tab_to_click

	// == ovreride methods =========================================

	DbOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_SETTINGS_CREATE);
		db.execSQL(TABLE_SETTINGS_INITIALIZE);
	}

	// @Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			upgradeToVersion2();
		}
		if (oldVersion == 2 && newVersion == 3) {
			upgradeToVersion3();
		}

		Log.i(Config.LOG_TAG, "Upgraded database " + DATABASE_NAME
				+ " from version " + oldVersion + " to " + newVersion);
	}

	private void upgradeToVersion2() {
		// do upgrading job
	}

	private void upgradeToVersion3() {
		// do upgrading job
	}
}
