package com.dexels.docker.registrator.impl;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.docker.registrator.DockerClient;
import com.dexels.docker.registrator.DockerContainer;
import com.dexels.docker.registrator.DockerServiceMapping;

@Component(name = "docker.registrator.config",immediate=true)
public class ConfigurationComponent implements EventHandler, Runnable {

	private DockerClient dockerClient = null;

	private ConfigurationAdmin configAdmin = null;
	private Set<Configuration> ownedConfiguration = new HashSet<>();
	private final static Logger logger = LoggerFactory.getLogger(ConfigurationComponent.class);

	private final ExecutorService pool = Executors.newFixedThreadPool(1);

	private boolean running = false;
	@Activate
	public void activate() throws IOException {
		logger.info("Activating config components");
		running = true;
		pool.submit(this);
	}

	private void refreshConfig() throws IOException {
		Set<Configuration> copy = new HashSet<>(ownedConfiguration);
		dockerClient.refresh();
		Set<DockerContainer> containers = dockerClient.getContainers();
		int containerCount = 0;
		for (DockerContainer dockerContainer : containers) {
			for (Entry<String, DockerServiceMapping> serviceMapping : dockerContainer
					.getMappings().entrySet()) {
				String id = dockerContainer.getId();
				String serviceId = generateServiceId(id,serviceMapping.getValue());
				String servicePid = generateServicePid(serviceMapping.getValue());
				Configuration newConfig = injectConfig(serviceId, servicePid,serviceMapping.getValue());
				copy.remove(newConfig);
			}
			containerCount++;
		}
		for (Configuration orphan : copy) {
			orphan.delete();
			ownedConfiguration.remove(orphan);
		}
		System.err.println("# orphans: "+copy.size()+" containers: "+containerCount);
	}

	private String generateServicePid(DockerServiceMapping mapping) {
		Map<String, String> mm = mapping.getKeyValue();
		String driver = mm.get("DRIVER");
		if(driver!=null) {
			return "docker.registrator."+driver;
		}

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
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.error("Error: ", e);
		}
		pool.submit(this);
	}

	private Configuration injectConfig(String serviceId, String servicePid,DockerServiceMapping mapping) throws IOException {
		Configuration c = createOrReuse(servicePid,"(id="+serviceId+")", true);
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("id", serviceId);
		for (Entry<String,String> e : mapping.getKeyValue().entrySet()) {
			if(e.getKey().equals("DRIVER") || e.getKey().equals("NAME")) {
				continue;
			}
			properties.put(e.getKey(),e.getValue());
		}  
		properties.put("host.ip", mapping.getHostIp());
		properties.put("host.port", mapping.getHostPort());
		properties.put("container.port", mapping.getPort());
		properties.put("host.protocol", mapping.getProtocol());
		properties.put("host.tags", mapping.getTags());
		c.update(properties);
		ownedConfiguration.add(c);
		return c;
	}

	@Deactivate
	public void deactivate() {
		Set<Configuration> copy = new HashSet<>(ownedConfiguration);
		for (Configuration configuration : copy) {
			try {
				configuration.delete();
			} catch (IOException e) {
				logger.error("Error: ", e);
			}
		}
		ownedConfiguration.clear();
		running = false;
		pool.shutdown();
	}
	private Configuration createOrReuse(String pid, final String filter,
			boolean factory) throws IOException {
		Configuration configuration = null;
		try {
			Configuration[] c = configAdmin.listConfigurations(filter);
			if (c != null && c.length > 1) {
				logger.warn("Multiple configurations found for filter: {}",
						filter);
			}
			if (c != null && c.length > 0) {
				configuration = c[0];
			}
		} catch (InvalidSyntaxException e) {
			logger.error("Error in filter: {}", filter, e);
		}
		if (configuration == null) {
			if (factory) {
				configuration = configAdmin.createFactoryConfiguration(pid,
						null);
			} else {
				configuration = configAdmin.getConfiguration(pid, null);
			}
		}
		return configuration;
	}

	private String generateServiceId(String id,
			DockerServiceMapping serviceMapping) {
		return id + "_" + serviceMapping.getHostIp() + ":"
				+ serviceMapping.getHostPort();
	}

	@Reference(unbind = "clearDockerClient", policy = ReferencePolicy.DYNAMIC)
	public synchronized void setDockerClient(DockerClient dc) {
		this.dockerClient = dc;
	}

	public synchronized DockerClient getDockerClient() {
		return dockerClient;
	}

	public void clearDockerClient(DockerClient dc) {
		this.dockerClient = null;
	}

	@Reference(unbind = "clearConfigAdmin", policy = ReferencePolicy.DYNAMIC)
	public void setConfigAdmin(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;
	}

	public void clearConfigAdmin(ConfigurationAdmin configAdmin) {
		this.configAdmin = null;
	}

	@Override
	public void handleEvent(Event event) {
		try {
			refreshConfig();
		} catch (IOException e) {
			logger.error("Error: ", e);
		}
	}



}
