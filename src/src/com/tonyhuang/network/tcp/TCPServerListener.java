package com.tonyhuang.network.tcp;

public interface TCPServerListener extends NetworkListener {
	public void onConnect(TCPConnection client);
	public void onDisconnect(TCPConnection client);
}
