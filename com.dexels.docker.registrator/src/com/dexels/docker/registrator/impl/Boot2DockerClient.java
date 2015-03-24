package com.dexels.docker.registrator.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.docker.registrator.JsonClient;
import com.dexels.unix.socket.UnixSocketFactory;


@Component(name="docker.registrator.boot2docker", configurationPolicy=ConfigurationPolicy.REQUIRE)
public class Boot2DockerClient implements JsonClient {

	private CloseableHttpClient httpclient;
	
	private String url = null;
	
	private final static Logger logger = LoggerFactory
			.getLogger(Boot2DockerClient.class);
	
	@Activate
	public void activate(Map<String,Object> settings)  {
		url = (String) settings.get("url");
		try {
			String path = (String) settings.get("path"); // home + "/"+ ".boot2docker/certs/boot2docker-vm"
			if(path==null) {
				path = 		System.getProperty("user.home") + "/"
						+ ".boot2docker/certs/boot2docker-vm";
			}
			this.httpclient = createBoot2DockerClient(path);
		} catch (DockerCertificateException e) {
			logger.error("Error: ", e);
		}
	}

	private CloseableHttpClient createBoot2DockerClient(String path)
			throws DockerCertificateException {
		DockerCertificates dc = new DockerCertificates(Paths.get(path));
		Registry<ConnectionSocketFactory> registry = RegistryBuilder
				.<ConnectionSocketFactory> create()
				.register("http",
						PlainConnectionSocketFactory.getSocketFactory())
				.register("unix",
						new UnixSocketFactory())
				.register("https",
						new SSLConnectionSocketFactory(dc.sslContext)).build();
		HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
				registry);
		CloseableHttpClient httpclient = HttpClients.custom()
				.setConnectionManager(cm).build();
		return httpclient;
	}
	

	
	@Override
	public JsonNode callUrl(String url) throws IOException {
		HttpGet httpget = new HttpGet(this.url + url);
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




}
