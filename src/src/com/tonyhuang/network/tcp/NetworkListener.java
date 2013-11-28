package com.tonyhuang.network.tcp;

public interface NetworkListener {
	public void recv(TCPConnection client, byte[] data, int len);
	public void onError(int code);
}
