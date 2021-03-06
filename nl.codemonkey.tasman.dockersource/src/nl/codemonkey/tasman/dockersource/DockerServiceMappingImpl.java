package nl.codemonkey.tasman.dockersource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.codemonkey.tasman.api.DockerServiceMapping;

public class DockerServiceMappingImpl implements DockerServiceMapping {
	private final String port;
	private final String protocol;
	private final String hostPort;
	private final String hostIp;
	private String tags;
	private final Map<String,String> kv = new HashMap<>();
	
	private String name;
//	private int containerPort;
	
	public DockerServiceMappingImpl(String port, String protocol, String hostPort, String hostIp) {
		this.port = port;
		this.protocol = protocol;
		this.hostPort = hostPort;
		this.hostIp = hostIp;
	}
	
	public String toString() {
		return "Name: "+name+" hostIp: "+hostIp+" hostPort: "+hostPort+" port: "+port+" protocol: "+protocol+" tags: "+tags+" :: "+kv;
	}
	/* (non-Javadoc)
	 * @see com.dexels.docker.http.impl.DockerMapping#getPort()
	 */
	@Override
	public String getPort() {
		return port;
	}

	/* (non-Javadoc)
	 * @see com.dexels.docker.http.impl.DockerMapping#getProtocol()
	 */
	@Override
	public String getProtocol() {
		return protocol;
	}

	/* (non-Javadoc)
	 * @see com.dexels.docker.http.impl.DockerMapping#getHostPort()
	 */
	@Override
	public String getHostPort() {
		return hostPort;
	}

	/* (non-Javadoc)
	 * @see com.dexels.docker.http.impl.DockerMapping#getHostIp()
	 */
	@Override
	public String getHostIp() {
		return hostIp;
	}


	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getTags() {
		return this.tags;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}


	@Override
	public void setTags(String tags) {
		this.tags = tags;
//		String[] tagString = tags.split(",");
//		for (String element : tagString) {
//			this.tags.add(element);
//		}
	}

	@Override
	public List<String> getTagList() {
		if(this.tags==null) {
			return Collections.emptyList();
		}
		String[] elts = tags.split(",");
		return Arrays.asList(elts);
		
	}
	
	@Override
	public void addKeyValue(String name, String value) {
		kv.put(name, value);
	}
	
	@Override
	public Map<String,String> getKeyValue() {
		return Collections.unmodifiableMap(kv);
	}

	@Override
	public void setContainerPort(int port) {
		// TODO Auto-generated method stub
		
	}
}
