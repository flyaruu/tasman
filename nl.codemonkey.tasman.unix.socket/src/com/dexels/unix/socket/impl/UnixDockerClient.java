package com.dexels.unix.socket.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import nl.codemonkey.tasman.api.JsonClient;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.unix.socket.UnixSocketFactory;

@Component(name = "docker", immediate=true)
public class UnixDockerClient implements JsonClient {

	private CloseableHttpClient httpclient;
	
	private final static Logger logger = LoggerFactory
			.getLogger(UnixDockerClient.class);
	
	@Activate
	public void activate(Map<String, Object> settings) {
		String path = (String) settings.get("path");
		if (path == null) {
			path = "/var/run/docker.sock";
		}
		try {
			this.httpclient = createSocketClient(path);
		} catch (URISyntaxException e) {
			logger.error("Error: ", e);
		}
	}

	private CloseableHttpClient createSocketClient(String path) throws URISyntaxException {
		Registry<ConnectionSocketFactory> registry = RegistryBuilder
				.<ConnectionSocketFactory> create()
				.register("http",
						PlainConnectionSocketFactory.getSocketFactory())
				.register("unix", new UnixSocketFactory(new URI( "unix://localhost"+path))).build();
		HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
				registry);
		CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm)
				.setConnectionManager(cm).build();
		return httpclient;
	}
	
	@Deactivate
	public void deactivate() {
		try {
			if(httpclient!=null) {
				httpclient.close();
			}
		} catch (IOException e) {
			logger.error("Error: ", e);
		}
	}
	
	@Override
	public JsonNode callUrl(String u) throws IOException {
		String url = u;
		HttpGet httpget = new HttpGet(url);
		logger.info("Opening url: "+url);
		
		CloseableHttpResponse response = httpclient.execute(httpget);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		response.getEntity().writeTo(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		JsonNode node = mapper.readTree(bais);
		response.close();
		return node;
	}

	@Override
	public String getHostname() {
		return "";
	}

}
