package com.tonyhuang.network.examples;

import java.io.IOException;

import com.tonyhuang.network.tcp.aio.AIOListener;
import com.tonyhuang.network.tcp.aio.AIOServerSocket;
import com.tonyhuang.network.tcp.aio.AIOSocket;

public class AIOServerMain {
	private static final int PORT = 14858;
	
	public static void main(String[] args) {
		try {
			AIOServerSocket server = new AIOServerSocket(new AIOListener(){
				@Override
				public void onError(AIOSocket socket, int code) {
					System.out.println("error [" + socket.getIP() + ":" + socket.getPort() +"]: " + code);
				}

				@Override
				public void onReceived(AIOSocket socket, byte[] data, int len) {
					System.out.println("recv [" + socket.getIP() + ":" + socket.getPort() +"]: " + new String(data));
					socket.send("got it!".getBytes());
				}

				@Override
				public void onConnected(AIOSocket socket) {
					System.out.println("connect: " + socket.getIP() + ":" + socket.getPort());
				}

				@Override
				public void onDisconnected(AIOSocket socket) {
					System.out.println("disconnect: " + socket.getIP() + ":" + socket.getPort());
				}
				
			});
			server.start(PORT);
			System.out.println("Server is starting...");
			while(true){
				Thread.sleep(1000);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
