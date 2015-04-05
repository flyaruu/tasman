package com.dexels.unix.socket;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

public class UnixSocketFactory implements ConnectionSocketFactory {

	private final File socketFile;

	public UnixSocketFactory(final URI socketUri) {
	    final String filename = socketUri.toString()
		        .replaceAll("^unix:///", "unix://localhost/")
		        .replaceAll("^unix://localhost", "");

		    this.socketFile = new File(filename);
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
	public Socket createSocket(HttpContext context) throws IOException {
		AFUNIXSocket socket = AFUNIXSocket.newInstance();
		return socket;
	}



	  public static URI sanitizeUri(final URI uri) {
	    if (uri.getScheme().equals("unix")) {
	      return URI.create("unix://localhost:80");
	    } else {
	      return uri;
	    }
	  }


	  @Override
	  public Socket connectSocket(final int connectTimeout,
	                              final Socket socket,
	                              final HttpHost host,
	                              final InetSocketAddress remoteAddress,
	                              final InetSocketAddress localAddress,
	                              final HttpContext context) throws IOException {
	    try {
	      socket.connect(new AFUNIXSocketAddress(socketFile), connectTimeout);
	    } catch (SocketTimeoutException e) {
	      throw new ConnectTimeoutException(e, null, remoteAddress.getAddress());
	    }

	    return socket;
	  }
}
