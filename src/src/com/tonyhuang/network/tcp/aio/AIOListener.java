package com.tonyhuang.network.tcp.aio;

public interface AIOListener {
	public void onError(AIOSocket socket, int code);
	public void onReceived(AIOSocket socket, byte[] data, int len);
	public void onConnected(AIOSocket socket);
	public void onDisconnected(AIOSocket socket);
}
