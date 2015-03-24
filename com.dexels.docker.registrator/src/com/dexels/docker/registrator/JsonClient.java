package com.dexels.docker.registrator;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonNode;

public interface JsonClient {
	public JsonNode callUrl(String url) throws IOException;
	public void activate(Map<String,Object> settings);
}
