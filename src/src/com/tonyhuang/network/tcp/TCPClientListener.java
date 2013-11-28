package com.tonyhuang.network.tcp;

public interface TCPClientListener extends NetworkListener {
	public void onConnected();
	public void onClose(TCPConnection client);
}
