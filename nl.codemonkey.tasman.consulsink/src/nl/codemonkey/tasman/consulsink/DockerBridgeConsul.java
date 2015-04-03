package nl.codemonkey.tasman.consulsink;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.codemonkey.tasman.api.DockerClient;
import nl.codemonkey.tasman.api.DockerContainer;
import nl.codemonkey.tasman.api.DockerServiceMapping;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.agent.AgentClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.kv.KeyValueClient;

@Component(name="tasman.docker.consul", service={}, immediate=true)
public class DockerBridgeConsul implements Runnable {
	private DockerClient dockerClient;

	private final static Logger logger = LoggerFactory.getLogger(DockerBridgeConsul.class);

	private final ExecutorService pool = Executors.newFixedThreadPool(1);

	private boolean running = false;

	private AgentClient agentClient;
	private KeyValueClient keyValueClient;
	
	@Activate
	public void activate() throws IOException {
		running = true;
		pool.submit(this);
	}

	private void refreshConfig() throws IOException {
		dockerClient.refresh();
		Set<DockerContainer> containers = dockerClient.getContainers();
		Map<String,Service> services = agentClient.getAgentServices().getValue();
		Map<String,Service> copy = new HashMap<>();
		for (Service s : services.values()) {
			copy.put(s.getId(), s);
		}
		for (DockerContainer dockerContainer : containers) {
			for (Entry<String, DockerServiceMapping> serviceMapping : dockerContainer
					.getMappings().entrySet()) {
				String id = dockerContainer.getId();
				Service service = services.get(dockerContainer.getId());
				String serviceId = generateServiceId(id,serviceMapping.getValue());
				String servicePid = generateServicePid(serviceMapping.getValue());
				if(service!=null) {
					// as we can't change, there isn't anything to do.
					copy.remove(serviceId);
					continue;
				}
				if(servicePid!=null) {
					insertConsul(serviceId, servicePid,serviceMapping.getValue());
					copy.remove(serviceId);
				} else {
					logger.info("Skipping incomplete mapping: "+serviceMapping.getValue());
				}
			}
		}
		for (Entry<String,Service> entry : copy.entrySet()) {
			if("consul".equals(entry.getValue().getService())) {
				// skip consul, it isn't identified by containerid
				continue;
			}
			logger.info("Deleting agent entry: "+entry.getKey());
			agentClient.agentServiceDeregister(entry.getKey());
			deleteAllFor(entry.getKey());
		}
	}

	private void insertConsul(String serviceId, String servicePid,
			DockerServiceMapping value) {
		NewService ns = new NewService();
		ns.setAddress(value.getHostIp());
		ns.setPort(Integer.parseInt(value.getHostPort()));
		ns.setTags(value.getTagList());
		ns.setId(serviceId);
		ns.setName(value.getName());
		agentClient.agentServiceRegister(ns);
		insertKv(serviceId,value.getKeyValue());
	}

	private void insertKv(String serviceId, Map<String, String> keyValue) {
		for (Map.Entry<String,String> e : keyValue.entrySet()) {
			keyValueClient.setKVValue(serviceId+"/"+e.getKey(), e.getValue());
		}
	}

	private void deleteAllFor(String serviceId) {
		keyValueClient.deleteKVValues(serviceId);
	}

	private String generateServiceId(String id,
			DockerServiceMapping serviceMapping) {
		String serviceId = id.substring(0,8) + "-" + serviceMapping.getHostIp() + "-"
						+ serviceMapping.getHostPort();
		logger.info("serviceId: {}",serviceId);
		return serviceId;
	}
	private String generateServicePid(DockerServiceMapping mapping) {
		Map<String, String> mm = mapping.getKeyValue();
		String driver = mm.get("DRIVER");
		if(driver!=null) {
			return "tasman."+driver;
		}
		logger.info("servicePid missing: "+mm);
		return null;
	}
	
	@Override
	public void run() {
		if(running ) {
			try {
				refreshConfig();
			} catch (Throwable e) {
				logger.error("Error: ", e);
			}
		} 
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			logger.error("Error: ", e);
		}
		pool.submit(this);
	}

	@Reference(unbind = "clearDockerClient", policy = ReferencePolicy.DYNAMIC)
	public synchronized void setDockerClient(DockerClient dc) {
		this.dockerClient = dc;
	}

	public synchronized DockerClient getDockerClient() {
		return dockerClient;
	}
	
	public void clearDockerClient(DockerClient dockerClient) {
		this.dockerClient = null;
	}



	@Reference(unbind = "clearAgentClient", policy = ReferencePolicy.DYNAMIC)
	public void setAgentClient(AgentClient agentClient) {
		this.agentClient = agentClient;
	}
	public void clearAgentClient(AgentClient agentClient) {
		this.agentClient = null;
	}

	@Reference(unbind = "clearKeyValueClient", policy = ReferencePolicy.DYNAMIC)
	public void setKeyValueClient(KeyValueClient keyValueClient) {
		this.keyValueClient = keyValueClient;
	}
	public void clearKeyValueClient(KeyValueClient keyValueClient) {
		this.keyValueClient = null;
	}
}
