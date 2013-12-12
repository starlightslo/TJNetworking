package com.tonyhuang.network.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tonyhuang.network.tcp.aio.AIOListener;
import com.tonyhuang.network.tcp.aio.AIOSocket;

public class AIOClientMain {
	private static final String IP = "192.168.2.110";
	private static final int PORT = 14858;
	private static final int SIZE = 3000;
	
	private static List<AIOSocket> sockets = new ArrayList<AIOSocket>();

	public static void main(String[] args) {
		try {
			for(int i = 0 ; i < SIZE ; i++) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							sockets.add(new AIOSocket(new AIOListener(){
								@Override
								public void onReceived(AIOSocket socket, byte[] data, int len) {
									int index = sockets.indexOf(socket);
									// System.out.println(index + "] recv: " + new String(data));
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									socket.send(("Hello, I'm " + index + ".").getBytes());
								}

								@Override
								public void onError(AIOSocket socket, int code) {
									int index = sockets.indexOf(socket);
									System.out.println(index + "] error: " + code);
								}

								@Override
								public void onConnected(AIOSocket socket) {
									int index = sockets.indexOf(socket);
									System.out.println(index + "] connected!");
									socket.send(("Hello, I'm " + index + ".").getBytes());
								}

								@Override
								public void onDisconnected(AIOSocket socket) {
									int index = sockets.indexOf(socket);
									System.out.println(index + "] disconnected!");
								}
							}));
							sockets.get(sockets.size() - 1).start(IP, PORT);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Thread.sleep(Integer.MAX_VALUE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
