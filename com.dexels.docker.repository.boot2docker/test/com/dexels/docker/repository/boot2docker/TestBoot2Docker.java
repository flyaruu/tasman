package com.dexels.docker.repository.boot2docker;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.junit.Before;
import org.junit.Test;

import com.dexels.docker.registrator.JsonClient;


public class TestBoot2Docker {

	private JsonClient b2d;

	@Before
	public void setup() throws DockerCertificateException {
		b2d = new Boot2DockerClient();
    	Map<String,Object> settings = new HashMap<>();
		settings.put("path",System.getProperty("user.home") + "/"
				+ ".boot2docker/certs/boot2docker-vm" );
		settings.put("url", "https://192.168.59.103:2376");
		b2d.activate(settings);
	}
//
//	@Before
//	public void setup() throws DockerCertificateException {
//		b2d = new Boot2DockerClient();
//    	Map<String,Object> settings = new HashMap<>();
//		settings.put("path",System.getProperty("user.home") + "/"
//				+ ".boot2docker/certs/boot2docker-vm" );
//		settings.put("url", "https://192.168.59.103:2376");
//		b2d.activate(settings);
//	}

	
	@Test
    public void testHttp() throws Exception {
    	ArrayNode nodes = (ArrayNode) b2d.callUrl("/containers/json");
    	assertTrue(nodes.size()>=1);
    	for (JsonNode jsonNode : nodes) {
    		String id = jsonNode.get("Id").getTextValue();
			System.err.println("jsonNode: "+id);
		}
    }
}
