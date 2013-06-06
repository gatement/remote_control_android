package net.johnsonlau.remotecontrol.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import net.johnsonlau.remotecontrol.Config;
import net.johnsonlau.remotecontrol.R;
import net.johnsonlau.remotecontrol.db.DbAdapter;
import net.johnsonlau.remotecontrol.db.DbOpenHelper;
import net.johnsonlau.remotecontrol.io.CommandProxy;
import net.johnsonlau.remotecontrol.model.AppSettings;
import net.johnsonlau.tool.CmdMessage;
import net.johnsonlau.tool.Utilities;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends TabActivity implements OnTouchListener {
	private float mThinkAsNoMovement = 0.7f;
	private float mDownX;
	private float mDownY;
	private float mMovingX;
	private float mLastMovingY = 0;
	private float mMovingY;
	private boolean mDoublePointer;
	private boolean mTriplePointer;
	private boolean mMovingMode;
	private String mTextInput;

	private long mKeyPressEventTime;
	private int mShutDownCountDown;

	Timer mPingTimer = null;
	TimerTask mPingTimerTask;

	private static final int MENU_ID_EXIT = Menu.FIRST;
	private static final int MENU_ID_ABOUT = Menu.FIRST + 1;
	private static final int MENU_ID_SETTINGS = Menu.FIRST + 2;

	private AppSettings mAppSettings;
	private Handler mMainHandler;
	private WifiManager mWifiManager;
	private CommandProxy mCommandProxy;

	private TextView mMsgTextView;
	private Button mVolumeUpButton;
	private Button mVolumeDownButton;
	private Button mMuteButton;
	private Button mShutdownButton;
	private View mMouseTrackView;
	private Button mMouseLeftButton;
	private Button mMouseRightButton;
	private LinearLayout mMouseButtonsLayout;
	private Button mSendTextButton;
	private EditText mTextBox;
	private Button mSendKeyEscButton;
	private Button mSendKeyBackspaceButton;
	private Button mSendKeyEnterButton;
	private Button mSendKeyUpButton;
	private Button mSendKeyDownButton;
	private Button mSendKeyLeftButton;
	private Button mSendKeyRightButton;
	private Button mSendKeyMagnifyButton;
	private Button mSendKeyMinifyButton;
	private Button mSendKeyOriginalButton;
	private Button mSendKeySpaceButton;

	// @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setupTabs();
		initMembers();
		bindEvents();

		turnOnWifi();
	}

	// == initialization methods
	// ===============================================================

	private void setupTabs() {
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;

		spec = tabHost
				.newTabSpec("mouse_track")
				.setIndicator("Touch Pad",
						res.getDrawable(R.drawable.ic_tab_mouse))
				.setContent(R.id.main_tab_mouse_track);
		tabHost.addTab(spec);

		spec = tabHost
				.newTabSpec("key_board")
				.setIndicator("Key Board",
						res.getDrawable(R.drawable.ic_tab_keyboard))
				.setContent(R.id.main_tab_key_board);
		tabHost.addTab(spec);

		spec = tabHost
				.newTabSpec("short_cuts")
				.setIndicator("Short Cuts",
						res.getDrawable(R.drawable.ic_tab_shortcuts))
				.setContent(R.id.main_tab_main_control);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}

	private void initMembers() {
		mMsgTextView = (TextView) findViewById(R.id.main_msg);
		mVolumeUpButton = (Button) findViewById(R.id.main_btn_volume_up);
		mVolumeDownButton = (Button) findViewById(R.id.main_btn_volume_down);
		mMuteButton = (Button) findViewById(R.id.main_btn_volume_mute);
		mShutdownButton = (Button) findViewById(R.id.main_btn_shutdown);
		mMouseTrackView = (View) findViewById(R.id.main_mouse_track);
		mMouseLeftButton = (Button) findViewById(R.id.main_btn_mouse_left);
		mMouseRightButton = (Button) findViewById(R.id.main_btn_mouse_right);
		mMouseButtonsLayout = (LinearLayout) findViewById(R.id.main_mouse_buttons);
		mSendTextButton = (Button) findViewById(R.id.main_btn_send_text);
		mTextBox = (EditText) findViewById(R.id.main_enter_text);
		mSendKeyEscButton = (Button) findViewById(R.id.main_btn_send_key_esc);
		mSendKeyBackspaceButton = (Button) findViewById(R.id.main_btn_send_key_backspace);
		mSendKeyEnterButton = (Button) findViewById(R.id.main_btn_send_key_enter);
		mSendKeyUpButton = (Button) findViewById(R.id.main_btn_send_key_up);
		mSendKeyDownButton = (Button) findViewById(R.id.main_btn_send_key_down);
		mSendKeyLeftButton = (Button) findViewById(R.id.main_btn_send_key_left);
		mSendKeyRightButton = (Button) findViewById(R.id.main_btn_send_key_right);
		mSendKeyMagnifyButton = (Button) findViewById(R.id.main_btn_send_key_magnify);
		mSendKeyMinifyButton = (Button) findViewById(R.id.main_btn_send_key_minify);
		mSendKeyOriginalButton = (Button) findViewById(R.id.main_btn_send_key_original);
		mSendKeySpaceButton = (Button) findViewById(R.id.main_btn_send_key_space);

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		mMainHandler = new Handler() {
			public void handleMessage(Message msg) {
				CmdMessage message = (CmdMessage) msg.obj;

				if (message.getCmd() == "Message") {
					mMsgTextView.setText(message.getValue());
				}
			}
		};
	}

	private void bindEvents() {
		mMouseTrackView.setOnTouchListener((OnTouchListener) this);

		mVolumeUpButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mKeyPressEventTime = 0;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					long eventTime = event.getEventTime();
					if (mKeyPressEventTime == 0
							|| (eventTime - mKeyPressEventTime) > mAppSettings
									.getKeyInterval()) {
						mKeyPressEventTime = eventTime;
						new VolumeUp().start();
					}
				}
				return false;
			}
		});
		mVolumeDownButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mKeyPressEventTime = 0;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					long eventTime = event.getEventTime();
					if (mKeyPressEventTime == 0
							|| (eventTime - mKeyPressEventTime) > mAppSettings
									.getKeyInterval()) {
						mKeyPressEventTime = eventTime;
						new VolumeDown().start();
					}
				}
				return false;
			}
		});
		mMuteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				new VolumeMute().start();
			}
		});
		mShutdownButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mKeyPressEventTime = 0;
					mShutDownCountDown = 4;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					((Button) v).setText(getResources().getString(
							R.string.main_btn_shutdown));
					if (mShutDownCountDown > 0) {
						new AlertDialog.Builder(MainActivity.this)
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setMessage(
										"Are you sure you want to shutdown the computer?")
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												shutdown();
											}
										}).setNegativeButton("No", null).show();

					}
				} else {
					long eventTime = event.getEventTime();
					if ((mKeyPressEventTime == 0 || (eventTime - mKeyPressEventTime) > 1000)
							&& mShutDownCountDown != 0) {
						mKeyPressEventTime = eventTime;
						mShutDownCountDown--;

						String buttonText0 = getResources().getString(
								R.string.main_btn_shutdown);
						String buttonText = buttonText0 + " ("
								+ String.valueOf(mShutDownCountDown) + ")";
						mShutdownButton.setText(buttonText);

						if (mShutDownCountDown == 0) {
							shutdown();
						}
					}
				}

				return true;
			}
		});
		mMouseLeftButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				new MouseLeftClick().start();
			}
		});
		mMouseRightButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				new MouseRightClick().start();
			}
		});
		mSendTextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String text = mTextBox.getText().toString().trim();
				if (!Utilities.isEmptyOrNull(text)) {
					mTextInput = text;
					new TextInput().start();
					mTextBox.setText("");
				}
			}
		});
		mSendKeyEscButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return keyPressHandler(event, "{ESC}");
			}
		});
		mSendKeyBackspaceButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return keyPressHandler(event, "{BACKSPACE}");
			}
		});
		mSendKeyEnterButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return keyPressHandler(event, "{ENTER}");
			}
		});
		mSendKeyUpButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return keyPressHandler(event, "{UP}");
			}
		});
		mSendKeyDownButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return keyPressHandler(event, "{DOWN}");
			}
		});
		mSendKeyLeftButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return keyPressHandler(event, "{LEFT}");
			}
		});
		mSendKeyRightButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return keyPressHandler(event, "{RIGHT}");
			}
		});
		mSendKeyMagnifyButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mTextInput = "^=";
				new TextInput().start();
			}
		});
		mSendKeyMinifyButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mTextInput = "^-";
				new TextInput().start();
			}
		});
		mSendKeyOriginalButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mTextInput = "^0";
				new TextInput().start();
			}
		});
		mSendKeySpaceButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return keyPressHandler(event, " ");
			}
		});
	}

	private void setMouseButtonsVisibility() {
		mMouseButtonsLayout
				.setVisibility(mAppSettings.getTapToClick() ? View.GONE
						: View.VISIBLE);
	}

	// == override methods
	// =====================================================================

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ID_EXIT, 0, R.string.main_menu_exit);
		menu.add(0, MENU_ID_SETTINGS, 1, R.string.main_menu_settings);
		menu.add(0, MENU_ID_ABOUT, 2, R.string.main_menu_about);

		return true;
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch (item.getItemId()) {

		case MENU_ID_EXIT:
			exit();
			return true;

		case MENU_ID_SETTINGS:
			goToSettingsActivity();
			return true;

		case MENU_ID_ABOUT:
			goToAboutActivity();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	public boolean onTouch(View arg0, MotionEvent arg1) {

		switch (arg1.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			mLastMovingY = 0;
			mDownX = arg1.getX();
			mDownY = arg1.getY();
			mDoublePointer = false;
			mTriplePointer = false;
			mMovingMode = false;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			switch (arg1.getPointerCount()) {
			case 2:
				mDoublePointer = true;
				break;
			case 3:
				mTriplePointer = true;
				break;
			default:
				mTriplePointer = true;
				break;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (!mMovingMode && mAppSettings.getTapToClick()) {
				float currentX = arg1.getX();
				float currentY = arg1.getY();
				// triple tap
				if (mTriplePointer) {
					// no use yet
				}
				// double tap
				else if (mDoublePointer) {
					new MouseRightClick().start();
				}
				// single tap
				else if (Math.abs(currentX - mDownX) < mThinkAsNoMovement
						&& Math.abs(currentY - mDownY) < mThinkAsNoMovement) {
					new MouseLeftClick().start();
				}
			}
			break;
		}

		if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
			if (!mTriplePointer && !mDoublePointer) {
				float currentX = arg1.getX();
				float currentY = arg1.getY();
				if (Math.abs(currentX - mDownX) > mThinkAsNoMovement
						|| Math.abs(currentY - mDownY) > mThinkAsNoMovement) {
					if ((Math.abs(currentX - mDownX) > mAppSettings
							.getRespondPrecision())
							|| (Math.abs(currentY - mDownY) > mAppSettings
									.getRespondPrecision())) {
						mMovingX = (currentX - mDownX)
								* mAppSettings.getPointerSpeed();
						mMovingY = (currentY - mDownY)
								* mAppSettings.getPointerSpeed();

						new MouseMove().start();
					}

					mDownX = currentX;
					mDownY = currentY;
					mMovingMode = true;
				}
			} else if (mDoublePointer) {
				float currentY = arg1.getY();
				if (Math.abs(currentY - mDownY) > mThinkAsNoMovement) {
					if ((Math.abs(currentY - mDownY) > mAppSettings
							.getRespondPrecision())) {
						mMovingY = (currentY - mDownY)
								* mAppSettings.getWheelSpeed();
						if (mLastMovingY == 0 || (mMovingY * mLastMovingY) > 0) {
							if ((mMovingY / mLastMovingY) > 2) {
								mMovingY = mMovingY / 2;
							}
							mLastMovingY = mMovingY;

							new MouseWheel().start();
						}
					}

					mDownY = currentY;
					mMovingMode = true;
				}
			}
		}

		return true;
	}

	protected void onResume() {
		super.onResume();

		fetchSettings();
		setMouseButtonsVisibility();

		startPing();
	}

	protected void onPause() {
		super.onPause();

		stopPing();
	}

	// == threads
	// =============================================================================

	private class VolumeUp extends Thread {
		public void run() {
			sendMessage(new CmdMessage("Message", "Sending command..."));
			sendCommand("volumeUp", "");
		}
	}

	private class VolumeDown extends Thread {
		public void run() {
			sendMessage(new CmdMessage("Message", "Sending command..."));
			sendCommand("volumeDown", "");
		}
	}

	private class VolumeMute extends Thread {
		public void run() {
			sendMessage(new CmdMessage("Message", "Sending command..."));
			sendCommand("volumeMute", "");
		}
	}

	private class MouseMove extends Thread {
		public void run() {
			sendMessage(new CmdMessage("Message", "Sending command..."));
			sendCommand("mouseMove", String.format(Locale.US, "%d,%d",
					(int) mMovingX, (int) mMovingY));
		}
	}

	private class MouseWheel extends Thread {
		public void run() {
			sendMessage(new CmdMessage("Message", "Sending command..."));
			sendCommand("mouseWheel", String.valueOf(-(int) mMovingY));
		}
	}

	private class TextInput extends Thread {
		public void run() {
			sendMessage(new CmdMessage("Message", "Sending command..."));
			sendCommand("textInput", mTextInput);
		}
	}

	private class MouseLeftClick extends Thread {
		public void run() {
			sendMessage(new CmdMessage("Message", "Sending command..."));
			sendCommand("mouseLeftClick", "");
		}
	}

	private class MouseRightClick extends Thread {
		public void run() {
			sendMessage(new CmdMessage("Message", "Sending command..."));
			sendCommand("mouseRightClick", "");
		}
	}

	private class Shutdown extends Thread {
		public void run() {
			sendMessage(new CmdMessage("Message", "Sending command..."));
			sendCommand("shutdown", "");
		}
	}

	// == helpers
	// =============================================================================

	private boolean keyPressHandler(MotionEvent event, String key) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mKeyPressEventTime = 0;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			long eventTime = event.getEventTime();
			if (mKeyPressEventTime == 0
					|| (eventTime - mKeyPressEventTime) > mAppSettings
							.getKeyInterval()) {
				mKeyPressEventTime = eventTime;
				mTextInput = key;
				new TextInput().start();
			}
		}
		return false;
	}

	private void exit() {
		setResult(RESULT_OK);
		finish();
	}

	private void sendMessage(CmdMessage msg) {
		Message toMain = mMainHandler.obtainMessage();
		toMain.obj = msg;
		mMainHandler.sendMessage(toMain);
	}

	private void fetchSettings() {
		int server = 1;
		String server1Ip = "";
		String server2Ip = "";
		String server3Ip = "";
		int serverPort = 0;
		String password = "";
		float pointerSpeed = 0;
		float wheelSpeed = 0;
		int responsePrecision = 0;
		int keyInterval = 0;
		int pingInterval = 0;
		int tapToClick = 0;

		DbAdapter dbAdapter = new DbAdapter(this).open();

		try {
			Cursor settingsCursor = dbAdapter.fetchSettings();
			settingsCursor.moveToFirst();

			server = settingsCursor.getInt(settingsCursor
					.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVER));
			server1Ip = settingsCursor
					.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVER1_IP));
			server2Ip = settingsCursor
					.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVER2_IP));
			server3Ip = settingsCursor
					.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVER3_IP));
			serverPort = settingsCursor
					.getInt(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVER_PORT));
			password = settingsCursor
					.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_PASSWORD));
			pointerSpeed = settingsCursor
					.getFloat(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_POINTER_SPEED));
			wheelSpeed = settingsCursor
					.getFloat(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_WHEEL_SPEED));
			responsePrecision = settingsCursor
					.getInt(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_RESPONSE_PRECISION));
			keyInterval = settingsCursor
					.getInt(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_KEY_INTERVAL));
			pingInterval = settingsCursor
					.getInt(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_PING_INTERVAL));
			tapToClick = settingsCursor
					.getInt(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_TAP_TO_CLICK));

			settingsCursor.close();
		} catch (SQLException ex) {
			mMsgTextView.setText("Load settings error!");
		}

		dbAdapter.close();

		mAppSettings = new AppSettings(server, server1Ip, server2Ip, server3Ip,
				serverPort, password, pointerSpeed, wheelSpeed,
				responsePrecision, keyInterval, pingInterval, tapToClick);

		String serverIp = getServerIp();
		mCommandProxy = new CommandProxy(password, getServerIp(), serverPort);
		Log.i(Config.LOG_TAG, "Server is " + serverIp + ":" + serverPort);
	}

	private void goToSettingsActivity() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	private void goToAboutActivity() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	public void sendCommand(String cmd, String val) {
		turnOnWifi();

		String result = mCommandProxy.sendCommand(cmd, val);
		sendMessage(new CmdMessage("Message", result));
	}

	private void turnOnWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	private void startPing() {
		stopPing();

		mPingTimerTask = new TimerTask() {
			public void run() {
				doPing();
			}
		};

		mPingTimer = new Timer();
		int interval = mAppSettings.getPingInterval() * 1000;
		mPingTimer.schedule(mPingTimerTask, 0, interval);
	}

	private void doPing() {
		Process process = null;
		InputStream inputStream = null;
		InputStream errorStream = null;

		try {
			String cmd = "/system/bin/ping -c 1 " + getServerIp();
			process = Runtime.getRuntime().exec(cmd);
			process.waitFor();

			inputStream = process.getInputStream();
			errorStream = process.getErrorStream();
			printInputStream(inputStream);
			printInputStream(errorStream);
		} catch (IOException ex) {
			Log.i(Config.LOG_TAG, "ping exception: " + ex.getMessage());
		} catch (InterruptedException ex) {
			Log.i(Config.LOG_TAG, "ping exception: " + ex.getMessage());
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (errorStream != null) {
					errorStream.close();
				}
			} catch (IOException ex) {
				Log.i(Config.LOG_TAG,
						"close stream exception: " + ex.getMessage());
			}

			if (process != null) {
				process.destroy();
			}
		}
	}

	private void printInputStream(InputStream inputStream) {
		try {
			String content = "";

			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, Charset.forName("UTF-8"));
			BufferedReader buffer = new BufferedReader(inputStreamReader);

			String inputLine = null;
			while ((inputLine = buffer.readLine()) != null) {
				content += inputLine + " ";
			}

			inputStreamReader.close();

			if (!Utilities.isEmptyOrNull(content)) {
				Log.i(Config.LOG_TAG, "InputStream: " + content);
			}
		} catch (IOException ex) {
			Log.i(Config.LOG_TAG,
					"read InputStream exception: " + ex.getMessage());
		}
	}

	private void stopPing() {
		if (mPingTimer != null) {
			mPingTimer.cancel();
			mPingTimer.purge();
		}

		Log.i(Config.LOG_TAG, "Ping stoped.");
	}

	private void shutdown() {
		new Shutdown().start();

		String shutdowning = getResources().getString(
				R.string.main_btn_shutdown);
		mShutdownButton.setText(shutdowning);
	}

	private String getServerIp() {
		String serverIp = "";

		int server = mAppSettings.getServer();

		if (server == 1) {
			serverIp = mAppSettings.getServer1Ip();
		} else if (server == 2) {
			serverIp = mAppSettings.getServer2Ip();
		} else {
			serverIp = mAppSettings.getServer3Ip();
		}

		return serverIp;
	}
}