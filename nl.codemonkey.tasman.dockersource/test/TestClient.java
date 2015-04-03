

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import nl.codemonkey.tasman.api.JsonClient;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class TestClient implements JsonClient {

	@Override
	public JsonNode callUrl(String url) throws IOException {
		if ("/containers/json".equals(url)) {
			return getNode("ids.json");
		}
		String[] parts = url.split("/");
		String s = parts[2];
		return getNode(s+".json");
//		return null;
//		public DockerContainer loadEntry(String id) throws IOException {
//	    	JsonNode nodes = jsonClient.callUrl("/containers/"+id+"/json");

	}

	private JsonNode getNode(String path) throws IOException {
		try (InputStream is = TestClient.class.getResourceAsStream(path);) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
			JsonNode node = mapper.readTree(is);
			return node;
		}
	}

	@Override
	public void activate(Map<String, Object> settings) {
		
	}

	@Override
	public String getHostname() {
		return "localhost";
	}
}
