package com.dexels.unix.socket.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.dexels.unix.socket.UnixSocketFactory;

@Component(name="docker")
public class DockerClient {

	@Activate
	public void activate() throws KeyManagementException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
	     Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
	                .register("http", PlainConnectionSocketFactory.getSocketFactory())
	                .register("https", new SSLConnectionSocketFactory(createClientContext()))
	                .register("unix", new UnixSocketFactory())
	                .build();
	        HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
	        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();
	        HttpGet httpget = new HttpGet("http://localhost/");
	        CloseableHttpResponse response = httpclient.execute(httpget);	
	        response.close();
	}

	private SSLContext createClientContext() throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, KeyManagementException {
        KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream instream = new FileInputStream(new File("my.keystore"));
        try {
            trustStore.load(instream, "nopassword".toCharArray());
        } finally {
            instream.close();
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .build();
        // Allow TLSv1 protocol only
//        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
//                sslcontext,
//                new String[] { "TLSv1" },
//                null,
//                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
//        CloseableHttpClient httpclient = HttpClients.custom()
//                .setSSLSocketFactory(sslsf)
//                .build();	}
        return sslcontext;
	}
}
