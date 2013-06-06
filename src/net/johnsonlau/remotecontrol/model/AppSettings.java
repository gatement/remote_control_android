package net.johnsonlau.remotecontrol.model;

public class AppSettings {
	private int mServer;
	private String mServer1Ip;
	private String mServer2Ip;
	private String mServer3Ip;
	private int mServerPort;
	private String mPassword;
	private float mPointerSpeed;
	private float mWheelSpeed;
	private int mResponsePrecision;
	private int mKeyInterval;
	private int mPingInterval;
	private int mTapToClick;

	public AppSettings(int server, String server1Ip, String server2Ip,
			String server3Ip, int serverPort, String password,
			float pointerSpeed, float wheelSpeed, int responsePrecision,
			int keyInterval, int pingInterval, int tapToClick) {
		mServer = server;
		mServer1Ip = server1Ip;
		mServer2Ip = server2Ip;
		mServer3Ip = server3Ip;
		mServerPort = serverPort;
		mPassword = password;
		mPointerSpeed = pointerSpeed;
		mWheelSpeed = wheelSpeed;
		mResponsePrecision = responsePrecision;
		mKeyInterval = keyInterval;
		mPingInterval = pingInterval;
		mTapToClick = tapToClick;
	}

	public int getServer() {
		return mServer;
	}

	public String getServer1Ip() {
		return mServer1Ip;
	}

	public String getServer2Ip() {
		return mServer2Ip;
	}

	public String getServer3Ip() {
		return mServer3Ip;
	}

	public int getServerPort() {
		return mServerPort;
	}

	public String getPassword() {
		return mPassword;
	}

	public float getPointerSpeed() {
		return mPointerSpeed;
	}

	public float getWheelSpeed() {
		return mWheelSpeed;
	}

	public int getRespondPrecision() {
		return mResponsePrecision;
	}

	public int getKeyInterval() {
		return mKeyInterval;
	}

	public int getPingInterval() {
		return mPingInterval;
	}

	public boolean getTapToClick() {
		return mTapToClick > 0;
	}
}
