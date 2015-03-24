package com.dexels.unix.socket;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.http.HttpHost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

public class UnixSocketFactory implements ConnectionSocketFactory {

	private File socketFile;

	public UnixSocketFactory() {
	}

	public boolean supports(String scheme) {
		return "unix".equals(scheme);
	}

	public String sanitize(String dockerHost) {
		return dockerHost.replaceAll("^unix://", "unix://localhost");
	}


	@Override
	public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host,
			InetSocketAddress remoteAddress, InetSocketAddress localAddress,
			HttpContext context) throws IOException {
		try {
//			 socket.setSoTimeout(soTimeout)
			socket.connect(new AFUNIXSocketAddress(socketFile), connectTimeout);
			// socket.connect(new AFUNIXSocketAddress(socketFile))
		} catch (SocketTimeoutException e) {
			throw new ConnectTimeoutException("Connect to '" + socketFile
					+ "' timed out");
		}

		return socket;
	}

	@Override
	public Socket createSocket(HttpContext context) throws IOException {
		AFUNIXSocket socket = AFUNIXSocket.newInstance();
		return socket;
	}

}
