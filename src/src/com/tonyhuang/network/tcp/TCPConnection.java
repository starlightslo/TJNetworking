package com.tonyhuang.network.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPConnection implements Runnable {
	private TCPClientListener mTCPClientListener;
	private Socket mSocket = null;
	
	private String mIP = null;
	private int mPort = -1;
	private int mBufferSize;
	
	private InputStream in;
	private OutputStream out;
	
	private byte[] buffer = new byte[1024];
	
	private boolean isRun = false;
	private boolean isConnected = false;
	private Thread mThread;
	
	public TCPConnection(Socket socket, TCPClientListener listener) throws Exception {
		mTCPClientListener = listener;
		isRun = false;
		mSocket = socket;
		mIP = mSocket.getInetAddress().getHostAddress();
		mPort = mSocket.getPort();
		try {
			in = mSocket.getInputStream();
			out = mSocket.getOutputStream();
			isConnected = true;
		} catch (IOException e) {
			throw new Exception("socket i/o error.");
		}
	}
	
	public TCPConnection(TCPClientListener tcpcLientListener) {
		mTCPClientListener = tcpcLientListener;
		isRun = false;
	}
	
	@Override
	public void run() {
		if (mSocket == null) {
			try {
				mSocket = new Socket(mIP, mPort);
				if (mSocket.isConnected()) {
					in = mSocket.getInputStream();
					out = mSocket.getOutputStream();
					isConnected = true;
					mTCPClientListener.onConnected();
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		while (!Thread.interrupted() && isConnected && mSocket != null && mSocket.isConnected()) {
			try {
				int len = in.read(buffer);
				if (len > -1) {
					mTCPClientListener.recv(this, buffer, len);
				} else if (len == -1) {
					mTCPClientListener.onClose(this);
				}
			} catch (IOException e) {
				break;
			}
		}
		
		mTCPClientListener.onClose(this);
		close();
	}
	
	/*
	 * Private function
	 */
	private void close() {
		try {
			if (in != null) in.close();
			if (out != null) out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (mSocket != null) mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		in = null;
		out = null;
		mSocket = null;
		isRun = false;
	}
	
	/*
	 * Protected
	 */
	protected void start() {
		if (isRun) return;

		mThread = new Thread(this);
		mThread.start();
		isRun = true;
	}
	
	/*
	 * Public functions
	 */
	public boolean connect(String ip, int port) {
		if (isRun) return false;
		mIP = ip;
		mPort = port;
		if (mPort < 1) return false;
		
		mSocket = null;
		
		mThread = new Thread(this);
		mThread.start();
		isRun = true;
		return true;
	}
	
	public void stop() {
		isConnected = false;
		if (mThread != null) {
			mThread.interrupt();
			try {
				mThread.join(1000);
			} catch (InterruptedException e) {}
			mThread = null;
		}
	}
	
	public boolean send(byte[] data) {
		if (!isConnected) return false;
		try {
			out.write(data);
			return true;
		} catch (IOException e) {
			stop();
		}
		return false;
	}
	
	public String getIP() {
		return mIP;
	}
	
	public int getPort() {
		return mPort;
	}

	public synchronized void setBufferSize(int size) {
		mBufferSize = size;
		buffer = new byte[mBufferSize];
	}
}
