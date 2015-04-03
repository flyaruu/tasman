package nl.codemonkey.tasman.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.codemonkey.tasman.api.DockerClient;
import nl.codemonkey.tasman.api.DockerContainer;
import nl.codemonkey.tasman.api.JsonClient;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name="tasman.docker.client")
public class DockerClientImpl implements DockerClient {

	private JsonClient jsonClient = null;
	private final Map<String,DockerContainer> entries = new HashMap<>();
	
	
	private final static Logger logger = LoggerFactory
			.getLogger(DockerClientImpl.class);
	
	@Activate
	public void activate() throws IOException {
		try {
			refresh();
		} catch (Throwable e) {
			logger.error("Error: ", e);
		}
	}

	@Override
	public void refresh() throws IOException {
		entries.clear();
		List<String> ids = getIds();
		for (String id : ids) {
			DockerContainer dei = loadEntry(id);
			entries.put(id, dei);
		}
	}
	
	@Reference(unbind="clearJsonClient",policy=ReferencePolicy.DYNAMIC)
	public void setJsonClient(JsonClient jsonClient) {
		this.jsonClient = jsonClient;
	}

	public void clearJsonClient(JsonClient jsonClient) {
		this.jsonClient = null;
	}

	@Override
	public Set<DockerContainer> getContainers() {
		Set<DockerContainer> result = new HashSet<>();
		result.addAll(entries.values());
		return Collections.unmodifiableSet(result);
	}
	
	@Override
	public List<String> getIds() throws IOException {
		List<String> result = new ArrayList<>();
    	ArrayNode nodes = (ArrayNode) jsonClient.callUrl("/containers/json");
    	for (JsonNode jsonNode : nodes) {
    		String id = jsonNode.get("Id").getTextValue();
			result.add(id);
		}
		return result;
	}

	public DockerContainer loadEntry(String id) throws IOException {
    	JsonNode nodes = jsonClient.callUrl("/containers/"+id+"/json");
//    	dumpNode(nodes);

    	return new DockerContainerImpl(nodes,jsonClient.getHostname());
	}

	public Map<String, String> getEnv(String id) throws IOException {
		Map<String,String> result = new HashMap<String, String>();
    	ObjectNode nodes = (ObjectNode) jsonClient.callUrl("/containers/"+id+"/json");
    	ArrayNode env = (ArrayNode) nodes.get("Config").get("Env");
//    	ObjectMapper m = new ObjectMapper();
//		m.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
//    	m.writerWithDefaultPrettyPrinter().writeValue(System.err, nodes);
    	for (JsonNode jsonNode : env) {
			String element = jsonNode.getTextValue();
			String[] split = element.split("=");
			result.put(split[0], split[1]);
			
		}
    	return result;
	}
	
	public Map<String, String> getServices(String id) throws IOException {
		Map<String,String> result = new HashMap<String, String>();
    	ObjectNode nodes = (ObjectNode) jsonClient.callUrl("/containers/"+id+"/json");
    	ArrayNode env = (ArrayNode) nodes.get("Config").get("Env");
    	ObjectNode ports = (ObjectNode) nodes.get("NetworkSettings").get("Ports");
    	for (JsonNode jsonNode : env) {
			String element = jsonNode.getTextValue();
			String[] split = element.split("=");
			result.put(split[0], split[1]);
		}
    	Iterator<Entry<String, JsonNode>> it = ports.getFields();
		while( it.hasNext()) {
    		Entry<String,JsonNode> element = it.next();
    		// strip /tcp /udp suffix?
    		ArrayNode mappings = (ArrayNode) element.getValue();
    		if(mappings.size()==0) {
    			System.err.println("Not mapped");
    		} else if (mappings.size()>1) {
    			System.err.println("Warning, multiple mappings. I don't know what that means.");
    		}
    	}
    	return result;
	}

//	private void dumpNode(JsonNode n) {
//		try {
//			new ObjectMapper().configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false).writerWithDefaultPrettyPrinter().writeValue(System.out, n);
//		} catch (Throwable e) {
//			logger.error("Error: ", e);
//		}
//	}
}
