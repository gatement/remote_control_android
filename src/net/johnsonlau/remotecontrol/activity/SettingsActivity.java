package net.johnsonlau.remotecontrol.activity;

import net.johnsonlau.remotecontrol.R;
import net.johnsonlau.remotecontrol.db.DbAdapter;
import net.johnsonlau.remotecontrol.db.DbOpenHelper;
import net.johnsonlau.tool.Utilities;
import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	private TextView mMsgTextView;
	private Spinner mServerSpinner;
	private EditText mServer1IpEditText;
	private EditText mServer2IpEditText;
	private EditText mServer3IpEditText;
	private EditText mServerPortEditText;
	private EditText mPasswordEditText;
	private EditText mPointerSpeedEditText;
	private EditText mWheelSpeedEditText;
	private EditText mResponsePrecisionEditText;
	private EditText mKeyIntervalEditText;
	private EditText mPingIntervalEditText;
	private CheckBox mTapToClickCheckBox;
	private Button mSaveButton;

	private DbAdapter mDbAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);

		initMembers();
		populateData();
		bindEvents();
	}

	// == initialization methods
	// ===============================================================

	private void initMembers() {
		mDbAdapter = new DbAdapter(this).open();

		mMsgTextView = (TextView) findViewById(R.id.settings_msg);
		mServerSpinner = (Spinner) findViewById(R.id.settings_server);
		mServer1IpEditText = (EditText) findViewById(R.id.settings_server1_ip);
		mServer2IpEditText = (EditText) findViewById(R.id.settings_server2_ip);
		mServer3IpEditText = (EditText) findViewById(R.id.settings_server3_ip);
		mServerPortEditText = (EditText) findViewById(R.id.settings_server_port);
		mPasswordEditText = (EditText) findViewById(R.id.settings_password);
		mPointerSpeedEditText = (EditText) findViewById(R.id.settings_pointer_speed);
		mWheelSpeedEditText = (EditText) findViewById(R.id.settings_wheel_speed);
		mResponsePrecisionEditText = (EditText) findViewById(R.id.settings_response_precision);
		mKeyIntervalEditText = (EditText) findViewById(R.id.settings_key_interval);
		mPingIntervalEditText = (EditText) findViewById(R.id.settings_ping_interval);
		mTapToClickCheckBox = (CheckBox) findViewById(R.id.settings_tap_to_click);
		mSaveButton = (Button) findViewById(R.id.settings_save);

		String[] orderings = { "1", "2", "3" };
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, orderings);
		mServerSpinner.setAdapter(spinnerAdapter);
	}

	private void populateData() {
		try {
			Cursor settingsCursor = mDbAdapter.fetchSettings();
			settingsCursor.moveToFirst();
			startManagingCursor(settingsCursor);

			int server = settingsCursor.getInt(settingsCursor
					.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVER));
			mServerSpinner.setSelection(server - 1);

			mServer1IpEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVER1_IP)));
			mServer2IpEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVER2_IP)));
			mServer3IpEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVER3_IP)));
			mServerPortEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVER_PORT)));
			mPasswordEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_PASSWORD)));
			mPointerSpeedEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_POINTER_SPEED)));
			mWheelSpeedEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_WHEEL_SPEED)));
			mResponsePrecisionEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_RESPONSE_PRECISION)));
			mKeyIntervalEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_KEY_INTERVAL)));
			mPingIntervalEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_PING_INTERVAL)));
			int tapToClick = settingsCursor
					.getInt(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_TAP_TO_CLICK));
			mTapToClickCheckBox.setChecked(tapToClick > 0);
		} catch (SQLException ex) {
			mMsgTextView.setText("Load settings error!");
		}
	}

	private void bindEvents() {
		this.mSaveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// get setting values

				String serverStr = mServerSpinner.getSelectedItem().toString();
				String server1Ip = mServer1IpEditText.getText().toString()
						.trim();
				String server2Ip = mServer2IpEditText.getText().toString()
						.trim();
				String server3Ip = mServer3IpEditText.getText().toString()
						.trim();
				String serverPortText = mServerPortEditText.getText()
						.toString().trim();
				String password = mPasswordEditText.getText().toString().trim();
				String pointerSpeedText = mPointerSpeedEditText.getText()
						.toString().trim();
				String wheelSpeedText = mWheelSpeedEditText.getText()
						.toString().trim();
				String responsePrecisionText = mResponsePrecisionEditText
						.getText().toString().trim();
				String keyIntervalText = mKeyIntervalEditText.getText()
						.toString().trim();
				String pingIntervalText = mPingIntervalEditText.getText()
						.toString().trim();
				int tapToClick = mTapToClickCheckBox.isChecked() ? 1 : 0;

				if (Utilities.isEmptyOrNull(server1Ip)
						|| Utilities.isEmptyOrNull(server2Ip)
						|| Utilities.isEmptyOrNull(server3Ip)
						|| Utilities.isEmptyOrNull(serverPortText)
						|| Utilities.isEmptyOrNull(password)
						|| Utilities.isEmptyOrNull(pointerSpeedText)
						|| Utilities.isEmptyOrNull(wheelSpeedText)
						|| Utilities.isEmptyOrNull(responsePrecisionText)
						|| Utilities.isEmptyOrNull(pingIntervalText)
						|| Utilities.isEmptyOrNull(keyIntervalText)) {
					mMsgTextView
							.setText("Please fill in valid values for all requred fields.");
					return;
				}

				Integer serverPort = Integer.valueOf(serverPortText);
				Float pointerSpeed = Float.valueOf(pointerSpeedText);
				Float wheelSpeed = Float.valueOf(wheelSpeedText);
				Integer responsePrecision = Integer
						.valueOf(responsePrecisionText);
				Integer keyInterval = Integer.valueOf(keyIntervalText);
				Integer pingInterval = Integer.valueOf(pingIntervalText);
				Integer server = Integer.valueOf(serverStr);

				// save settings and return
				mDbAdapter.updateSettings(server, server1Ip, server2Ip,
						server3Ip, serverPort, password, pointerSpeed,
						wheelSpeed, responsePrecision, keyInterval,
						pingInterval, tapToClick);
				goToMainActivity();
			}
		});
	}

	// == override methods
	// =====================================================================

	protected void onDestroy() {
		super.onDestroy();

		mDbAdapter.close();
	}

	// == helpers
	// ============================================================================

	private void goToMainActivity() {
		setResult(RESULT_OK);
		finish();
	}
}