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

	private final File socketFile;

	public UnixSocketFactory(String path) {
		this.socketFile = new File(path);
	}

//	  @Override
//	  public void configure(HttpClient httpClient, String dockerHost) {
//	    this.socketFile = new File(dockerHost.replaceAll("unix://localhost", ""));
//	    Scheme unixScheme = new Scheme("unix", 0xffff, this);
//	    httpClient.getConnectionManager().getSchemeRegistry().register(unixScheme);
//	  }

	  
	public boolean supports(String scheme) {
		System.err.println("supports: "+scheme);
		return "unix".equals(scheme);
	}

	public String sanitize(String dockerHost) {
		String replaceAll = dockerHost.replaceAll("^unix://", "unix://localhost");
		System.err.println("sanitized: "+dockerHost+" to "+replaceAll);
		return replaceAll;
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
