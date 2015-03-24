package com.dexels.docker.registrator;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface DockerClient {
	
	public List<String> getIds() throws IOException;
	public Set<DockerContainer> getContainers();
	void refresh() throws IOException;
	
}
