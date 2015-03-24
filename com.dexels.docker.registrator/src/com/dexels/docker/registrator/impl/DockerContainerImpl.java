package com.dexels.docker.registrator.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.docker.registrator.DockerContainer;
import com.dexels.docker.registrator.DockerServiceMapping;

public class DockerContainerImpl implements DockerContainer {
	private final Map<String, String> env = new HashMap<>();
	private final JsonNode core;
	private final Map<String, DockerServiceMapping> mappings= new HashMap<>();
	private final String id;

	
	
	private final static Logger logger = LoggerFactory
			.getLogger(DockerContainerImpl.class);
	
	public DockerContainerImpl(JsonNode core) {
		this.id = core.get("Id").asText();
		this.core = core;
		load();
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public Map<String, DockerServiceMapping> getMappings() {
		return Collections.unmodifiableMap(mappings);
	}

	private void load() {
		loadEnv();
		loadMappings();
		loadServices();		
	}

	private void loadEnv() {
		ArrayNode envNode = (ArrayNode) core.get("Config").get("Env");
		for (JsonNode jsonNode : envNode) {
			String element = jsonNode.getTextValue();
			String[] split = element.split("=");
			env.put(split[0], split[1]);
		}
	}
	
	private void loadServices() {
//		SERVICE_3306_MYSQL
		String defaultName = env.get("SERVICE_NAME");
		if(defaultName!=null) {
			DockerServiceMapping ds = null;
			if(mappings.size()==1) {
				ds = mappings.values().iterator().next();
				logger.info("Service detected: "+defaultName+" at ip: "+ds.getHostIp()+"port: "+ds.getHostPort());
				
			} else if(mappings.size()==0){
				logger.warn("Default service name: "+defaultName+" defined, but no mappings found.");
			} else {
				logger.warn("Default service name: "+defaultName+" defined, but multiple mappings found, using first.");
				ds = mappings.values().iterator().next();
			}
			if(ds==null) {
				return;
			}
			for (Entry<String,String> element : env.entrySet()) {
				String key = element.getKey();
				if(key.equals("SERVICE_TAGS")) {
					ds.setTags(element.getValue());
				} else {
					if(key.startsWith("SERVICE_")) {
						int index = key.indexOf("_");
						String kvName = key.substring(index+1,key.length());
						ds.addKeyValue(kvName, element.getValue());
					}
				}
			}
		} else {
			for (Entry<String,String> element : env.entrySet()) {
				if(element.getKey().startsWith("SERVICE_")) {
					String[] parts = element.getKey().split("_");
					if(parts.length==3) {
						int port = Integer.parseInt(parts[1]);
						String name = parts[2];
						String value = element.getValue();
						DockerServiceMapping ds = mappings.get(parts[1]);
						ds.setContainerPort(port);
						if(name.equals("NAME")) {
							ds.setName(value);
						} else if(name.equals("TAGS")) {
							ds.setTags(value);
						} else {
							ds.addKeyValue(name,value);
						}
						logger.info("Service detected: "+value+" at ip: "+ds.getHostIp()+"port: "+ds.getHostPort());
					}
				}
			}
		}
	}
	private void loadMappings() {
		ObjectNode ports = (ObjectNode) core.get("NetworkSettings")
				.get("Ports");
		Iterator<Entry<String, JsonNode>> it = ports.getFields();
		while (it.hasNext()) {
			Entry<String, JsonNode> element = it.next();
			String key = element.getKey();
			// strip /tcp /udp suffix?
			if (ports.has(key)) {
				String[] parts = key.split("/");
				String port = null;
				String protocol = "tcp";
				if (parts.length > 1) {
					port = parts[0];
					protocol = parts[1];
				} else {
					port = key;
				}
				 JsonNode value = element.getValue();
				if (value instanceof ArrayNode) {
					ArrayNode mappings = (ArrayNode) element.getValue();
					if (mappings.size() == 0) {
						System.err.println("Not mapped");
					} else if (mappings.size() > 1) {
						System.err.println("Warning, multiple mappings. I don't know what that means, ignoring others");
					}
					String hostIp = mappings.get(0).get("HostIp").asText();
					String hostPort = mappings.get(0).get("HostPort").asText();
					DockerServiceMapping ds = new DockerServiceMappingImpl(port, protocol,
							hostPort, hostIp);
					this.mappings.put(port, ds);
				}
			}
		}
	}
}
