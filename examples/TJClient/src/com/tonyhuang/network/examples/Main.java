package com.tonyhuang.network.examples;

import com.tonyhuang.network.tcp.TCPClientListener;
import com.tonyhuang.network.tcp.TCPConnection;

public class Main {
	private static final String IP = "127.0.0.1";
	private static final int PORT = 15555;
	private static TCPConnection mTCPConnection;
	public static void main(String[] args) {
		mTCPConnection = new TCPConnection(new TCPClientListener(){
			@Override
			public void onClose(TCPConnection server) {
				System.out.println(server.getIP() + ":" + server.getPort() + " is closed.");
			}

			@Override
			public void onError(int code) {
				System.out.println("error: " + code);
			}

			@Override
			public void recv(TCPConnection server, byte[] data, int len) {
				System.out.println(server.getIP() + ":" + server.getPort() + "> " + new String(data, 0, len));
				mTCPConnection.stop();
			}

			@Override
			public void onConnected() {
				System.out.println("Is connected.");
				mTCPConnection.send("Hello".getBytes());
			}
		});
		mTCPConnection.connect(IP, PORT);
	}
}
