package com.dexels.docker.registrator;

import java.util.Map;

import com.dexels.docker.registrator.DockerServiceMapping;

public interface DockerContainer {

	public Map<String, DockerServiceMapping> getMappings();

	public String getId();

}