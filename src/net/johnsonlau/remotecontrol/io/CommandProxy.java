package net.johnsonlau.remotecontrol.io;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import net.johnsonlau.remotecontrol.Config;
import android.util.Log;

public class CommandProxy {
	private String mPassword;
	private String mServerIpString;
	private int mServerPort;

	private DatagramSocket mDatagramSocket = null;
	private InetAddress mServerIp = null;

	public CommandProxy(String password, String ip, int port) {
		mPassword = password;
		mServerIpString = ip;
		mServerPort = port;

		try {
			mDatagramSocket = new DatagramSocket();
		} catch (SocketException ex) {
			Log.i(Config.LOG_TAG,
					"initilize DatagramSocket exception: " + ex.getMessage());
		}

		try {
			mServerIp = InetAddress.getByName(mServerIpString);
		} catch (UnknownHostException ex) {
			Log.i(Config.LOG_TAG,
					"initilize InetAddress exception: " + ex.getMessage());
		}
	}

	public String sendCommand(String cmd, String val) {
		String result = "sent.";

		String message = cmd + "|" + mPassword + "|" + val;
		byte[] bytes = message.getBytes();

		DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
				mServerIp, mServerPort);

		try {
			mDatagramSocket.send(packet);
		} catch (IOException ex) {
			Log.i(Config.LOG_TAG, "Send datagram exception: " + ex.getMessage());
			result = "error.";
		}

		return result;
	}
}
