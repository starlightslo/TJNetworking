package com.tonyhuang.network.tcp.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AIOServerSocket implements AIOListener {

	public class Error {
		public static final int START_ERROR = 1;
		public static final int CONNECT_ERROR = 2;

	}

	private int mPort = -1;
	
	private int mSendBufferSize = 16 * 1024;
	private int mRecvBufferSize = 16 * 1024;

	private AsynchronousServerSocketChannel mServerSocket = null;
	private AIOListener mAIOListener;

	private ExecutorService executor;
	private AsynchronousChannelGroup mAsynchronousChannelGroup;

	private List<AIOSocket> mClients = new ArrayList<AIOSocket>();

	private CompletionHandler<AsynchronousSocketChannel, Object> mCompletionHandler = new CompletionHandler<AsynchronousSocketChannel, Object>() {
		@Override
		public void completed(AsynchronousSocketChannel channel, Object attachment) {
			// accept the next connection
			mServerSocket.accept(null, mCompletionHandler);
			
			try {
				AIOSocket socket = new AIOSocket(channel, AIOServerSocket.this);
				if (!mClients.contains(socket)) {
					socket.setSendBufferSize(mSendBufferSize);
					socket.setReceiverBufferSize(mRecvBufferSize);
					mClients.add(socket);
					mAIOListener.onConnected(socket);
				}
				socket.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void failed(Throwable exc, Object attachment) {
			System.err.println("failed event: " + exc.getMessage());
		}
	};

	public AIOServerSocket(AIOListener listener) throws IOException {
		mAIOListener = listener;
		executor = Executors.newCachedThreadPool();
		mAsynchronousChannelGroup = AsynchronousChannelGroup.withCachedThreadPool(executor, 1024);
		mServerSocket = AsynchronousServerSocketChannel.open(mAsynchronousChannelGroup);
		mServerSocket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
	}

	@Override
	public void onError(AIOSocket socket, int code) {
		mAIOListener.onError(socket, code);
	}

	@Override
	public void onConnected(AIOSocket socket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceived(AIOSocket client, byte[] data, int len) {
		mAIOListener.onReceived(client, data, len);
	}

	@Override
	public void onDisconnected(AIOSocket client) {
		if (!mClients.contains(client)) {
			mClients.remove(client);
		}
		mAIOListener.onDisconnected(client);
	}

	/*
	 * Public functions
	 */
	public void start(int port) throws IOException {
		mPort = port;
		mServerSocket.bind(new InetSocketAddress(mPort));
		mServerSocket.accept(null, mCompletionHandler);
	}
	
	public void stop() {
		try {
			if (mServerSocket != null && mServerSocket.isOpen()) {
				mServerSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setBufferSize(int size) throws IOException {
		setSendBufferSize(size);
		setReceiverBufferSize(size);
	}

	public void setSendBufferSize(int size) throws IOException {
		mSendBufferSize = size;
		for (int i = 0 ; i < mClients.size() ; i++) {
			mClients.get(i).setSendBufferSize(mSendBufferSize);
		}
	}

	public void setReceiverBufferSize(int size) throws IOException {
		mRecvBufferSize = size;
		mServerSocket.setOption(StandardSocketOptions.SO_RCVBUF, mRecvBufferSize);
		for (int i = 0 ; i < mClients.size() ; i++) {
			mClients.get(i).setReceiverBufferSize(mRecvBufferSize);
		}
	}

	public void setOption(SocketOption name, boolean value) throws IOException {
		mServerSocket.setOption(name, value);
	}

}
