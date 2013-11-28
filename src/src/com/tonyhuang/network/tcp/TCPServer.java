package com.tonyhuang.network.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer implements Runnable, TCPClientListener {
	private TCPServerListener mTCPServerListener;
	private ServerSocket mServerSocket;
	private int mPort = -1;
	
	private int mBufferSize = 1024;
	
	private boolean isRun = false;
	private boolean isClosing = false;
	private Thread mThread;
	
	private List<TCPConnection> mClientSocket = new ArrayList<TCPConnection>();

	public TCPServer(TCPServerListener listener) {
		mTCPServerListener = listener;
		isRun = false;
		mClientSocket.clear();
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				Socket clientSocket = mServerSocket.accept();
				TCPConnection channel = new TCPConnection(clientSocket, this);
				mClientSocket.add(channel);
				channel.setBufferSize(mBufferSize);
				channel.start();
				mTCPServerListener.onConnect(channel);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		close();
	}

	@Override
	public void recv(TCPConnection client, byte[] data, int len) {
		mTCPServerListener.recv(client, data, len);
	}

	@Override
	public void onClose(TCPConnection client) {
		if (mClientSocket.contains(client)) {
			int index = mClientSocket.indexOf(client);
			if (!isClosing)
				mTCPServerListener.onDisconnect(mClientSocket.get(index));
			mClientSocket.remove(index);
		}
	}

	@Override
	public void onError(int code) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected() {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * Private functions
	 */
	private void close() {
		closeAllClient();
		try {
			mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mServerSocket = null;
		isRun = false;
		isClosing = false;
	}

	private void closeAllClient() {
		for (int i = 0 ; i < mClientSocket.size() ; i++) {
			if (mClientSocket.get(i) != null)
				mClientSocket.get(i).stop();
		}
	}

	/*
	 * Public functions
	 */
	public boolean start(int port) {
		if (isRun) return false;
		mPort = port;
		if (mPort < 1) return false;
		
		try {
			mServerSocket = new ServerSocket(mPort);
			mThread = new Thread(this);
			mThread.start();
			isRun = true;
			isClosing = false;
			return true;
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + mPort);
		}
		return false;
	}

	public void stop() {
		isClosing = true;
		if (mThread != null) {
			mThread.interrupt();
			try {
				mThread.join(1000);
			} catch (InterruptedException e) {}
			mThread = null;
		}
	}

	public void setBufferSize(int size) {
		mBufferSize = size;
		for (int i = 0 ; i < mClientSocket.size() ; i++) {
			if (mClientSocket.get(i) != null)
				mClientSocket.get(i).setBufferSize(mBufferSize);
		}
	}
}
