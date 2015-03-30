package nl.codemonkey.tasman.api;

import java.util.Map;

import nl.codemonkey.tasman.api.DockerServiceMapping;

public interface DockerContainer {

	public Map<String, DockerServiceMapping> getMappings();

	public String getId();

}