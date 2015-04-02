package nl.codemonkey.tasman.api;

import java.util.List;
import java.util.Map;

public interface DockerServiceMapping {

	public String getPort();

	public String getProtocol();

	public String getHostPort();

	public String getHostIp();

	public String getTags();

	public List<String> getTagList();

	public void setContainerPort(int port);

	public void setName(String value);

	public void setTags(String value);

	public void addKeyValue(String name, String value);

	public Map<String, String> getKeyValue();

	public String getName();

	
}