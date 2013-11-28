package com.tonyhuang.network.examples;

import com.tonyhuang.network.tcp.TCPConnection;
import com.tonyhuang.network.tcp.TCPServer;
import com.tonyhuang.network.tcp.TCPServerListener;

public class Main {
	private static final int PORT = 15555;
	private static TCPServer mTCPServer;
	public static void main(String[] args) {
		mTCPServer = new TCPServer(new TCPServerListener(){
			@Override
			public void onError(int code) {
				System.out.println("error: " + code);
			}

			@Override
			public void recv(TCPConnection client, byte[] data, int len) {
				System.out.println(client.getIP() + ":" + client.getPort() + "> " + new String(data, 0, len));
				client.send("Got it!".getBytes());
			}

			@Override
			public void onConnect(TCPConnection client) {
				System.out.println(client.getIP() + ":" + client.getPort() + " is connected.");
			}

			@Override
			public void onDisconnect(TCPConnection client) {
				System.out.println(client.getIP() + ":" + client.getPort() + " is disconnect.");
			}
		});
		if (mTCPServer.start(PORT)) {
			System.out.println("TCP Server is starting...");
		} else {
			System.err.println("TCP Server start failed.");
		}
	}

}
