package com.dexels.docker.http;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.codemonkey.tasman.api.DockerContainer;
import nl.codemonkey.tasman.api.DockerServiceMapping;
import nl.codemonkey.tasman.api.JsonClient;
import nl.codemonkey.tasman.core.DockerClientImpl;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.junit.Before;
import org.junit.Test;


public class TestLocal {

	private JsonClient b2d;

	@Before
	public void setup() {
		b2d = new TestClient();
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

    
	@Test
    public void testClient() throws IOException {
//    	Map<String,Object> settings = new HashMap<>();
		DockerClientImpl dci = new DockerClientImpl();
//		settings.put("url", "https://192.168.59.103:2376");
		dci.setJsonClient(b2d);
//		dci.activate(settings);
		List<String> ids = dci.getIds();
		for (String id : ids) {
			 dci.getEnv(id);
		}
	}
	
	@Test
    public void testDockerEntries() throws IOException {
		DockerClientImpl dci = new DockerClientImpl();
		dci.setJsonClient(b2d);
		List<String> ids = dci.getIds();
		for (String id : ids) {
			DockerContainer dei = dci.loadEntry(id);
			System.err.println(": "+dei.toString());
			
		}
	}
	
	@Test
    public void testDocker() throws IOException {
		DockerClientImpl dci = new DockerClientImpl();
		dci.setJsonClient(b2d);
//		dci.activate(null);
		Set<DockerContainer> containers = dci.getContainers();
		System.err.println("container size: "+containers.size());
		for (DockerContainer dockerContainer : containers) {
			for (Entry<String, DockerServiceMapping> serviceMapping : dockerContainer.getMappings().entrySet()) {
				String id = dockerContainer.getId();
				System.err.println("id::: "+id);
				System.err.println("va::: "+serviceMapping.getValue().getHostIp()+":"+serviceMapping.getValue().getHostPort());
				System.err.println("ta::: "+serviceMapping.getValue().getTags());
				System.err.println("kv::: "+serviceMapping.getValue().getKeyValue());
				//				String serviceId = generateServiceId(id, serviceMapping.getValue());
//				String servicePid = generateServicePid(serviceMapping.getValue());
			}
		}

	}
}
