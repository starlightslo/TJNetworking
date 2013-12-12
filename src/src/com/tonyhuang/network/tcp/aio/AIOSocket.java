package com.tonyhuang.network.tcp.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

public class AIOSocket {
	
	public class Error {
		public static final int SEND_FAILED = 3;
	}
	
	private String mIP = "";
	private int mPort = -1;
	private long mRecvTimeout = -1;
	
	private int mSendBufferSize = 16 * 1024;
	private int mRecvBufferSize = 16 * 1024;
	private ByteBuffer mSendBuffer = ByteBuffer.allocate(mSendBufferSize);

	private AsynchronousSocketChannel mChannel;
	private AIOListener mAIOListener;

	private boolean isConnected = false;
	private boolean isSending = false;

	private CompletionHandler mCompletionHandler = new CompletionHandler<Integer, Void>() {
		@Override
		public void completed(Integer result, Void attachment) {
			if (!isConnected) {
				isConnected = true;
				mAIOListener.onConnected(AIOSocket.this);
				read();
			}
		}
	
		@Override
		public void failed(Throwable exc, Void attachment) {
			mAIOListener.onDisconnected(AIOSocket.this);
		}
	};

	public AIOSocket(AsynchronousSocketChannel channel, AIOListener listener) throws IOException {
		isConnected = true;
		mChannel = channel;
		mAIOListener = listener;
	}

	public AIOSocket(AIOListener listener) throws IOException {
		isConnected = false;
		mAIOListener = listener;
		
		mChannel = AsynchronousSocketChannel.open();
		mChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		mChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		mChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
	}
	
	/*
	 * Private functions
	 */
	private void receive(byte[] data, Integer len) {
		mAIOListener.onReceived(this, data, len);
	}

	/*
	 * Protected functions
	 */
	protected void read() {
		//mChannel.read(mRecvBuffer, mTimeout, TimeUnit.MILLISECONDS, null, mCompletionHandler);
		//mChannel.read(mRecvBuffer, null, mCompletionHandler);
		final ByteBuffer buffer = ByteBuffer.allocate(mRecvBufferSize);
		if (mRecvTimeout != -1) {
			mChannel.read(buffer, mRecvTimeout, TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer, Void>() {
				@Override
				public void completed(Integer result, Void attachment) {
					if (!isConnected) {
						isConnected = true;
						mAIOListener.onConnected(AIOSocket.this);
					} else {
						if (result > 0) {
							buffer.flip();
							byte[] data = new byte[result];
							buffer.get(data);
							receive(data, result);
							buffer.clear();
						} else if (result == -1) {
							mAIOListener.onDisconnected(AIOSocket.this);
							stop();
						}
					}
					mChannel.read(buffer, null, this);
				}
		
				@Override
				public void failed(Throwable exc, Void attachment) {
					mAIOListener.onDisconnected(AIOSocket.this);
				}
			});
		} else {
			mChannel.read(buffer, null, new CompletionHandler<Integer, Void>() {
				@Override
				public void completed(Integer result, Void attachment) {
					if (!isConnected) {
						isConnected = true;
						mAIOListener.onConnected(AIOSocket.this);
					} else {
						if (result > 0) {
							buffer.flip();
							byte[] data = new byte[result];
							buffer.get(data);
							receive(data, result);
							buffer.clear();
						} else if (result == -1) {
							mAIOListener.onDisconnected(AIOSocket.this);
							stop();
						}
					}
					mChannel.read(buffer, null, this);
				}
		
				@Override
				public void failed(Throwable exc, Void attachment) {
					mAIOListener.onDisconnected(AIOSocket.this);
				}
			});
		}
	}
	/*
	 * Public functions
	 */
	public void start(String ip, int port) throws IOException {
		mIP = ip;
		mPort = port;
		mChannel.connect(new InetSocketAddress(mIP, mPort), null, mCompletionHandler);
	}

	public void stop() {
		if (mChannel != null && mChannel.isOpen()) {
			try {
				mChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		isConnected = false;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean send(byte[] data) {
		if (!isConnected) return false;
		
		while (isSending) {
			
		}
		
		isSending = true;
		if (data.length > mSendBufferSize) {
			try {
				setSendBufferSize(data.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		mSendBuffer.clear();
		mSendBuffer.put(data);
		mSendBuffer.flip();
		mChannel.write(mSendBuffer, null, new CompletionHandler<Integer, Void>(){
			@Override
			public void completed(Integer result, Void attachment) {
				isSending = false;
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				mAIOListener.onError(AIOSocket.this, Error.SEND_FAILED);
				isSending = false;
			}
		});
		return true;
	}

	public SocketAddress getAddress() {
		if (mChannel != null && mChannel.isOpen()) {
			try {
				return mChannel.getRemoteAddress();
			} catch (IOException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public String getIP() {
		InetSocketAddress address = (InetSocketAddress) getAddress();
		if (address != null) {
			return address.getHostName();
		} else {
			return null;
		}
	}

	public int getPort() {
		InetSocketAddress address = (InetSocketAddress) getAddress();
		if (address != null) {
			return address.getPort();
		} else {
			return -1;
		}
	}
	
	public void setBufferSize(int size) throws IOException {
		setSendBufferSize(size);
		setReceiverBufferSize(size);
	}

	public void setSendBufferSize(int size) throws IOException {
		mSendBufferSize = size;
		mSendBuffer = ByteBuffer.allocate(size);
		mChannel.setOption(StandardSocketOptions.SO_SNDBUF, size);
	}

	public void setReceiverBufferSize(int size) throws IOException {
		mRecvBufferSize = size;
		mChannel.setOption(StandardSocketOptions.SO_RCVBUF, size);
	}

	public void setTimeout(long milliSecond) {
		mRecvTimeout = milliSecond;
	}

	public void setOption(SocketOption name, boolean value) throws IOException {
		mChannel.setOption(name, value);
	}
}
