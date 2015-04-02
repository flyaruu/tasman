package nl.codemonkey.tasman.core;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.codemonkey.tasman.api.DockerClient;
import nl.codemonkey.tasman.api.DockerContainer;
import nl.codemonkey.tasman.api.DockerServiceMapping;

import org.osgi.framework.Constants;
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

@Component(name = "tasman.docker.osgi",immediate=true)
public class DockerBridgeOSGi implements EventHandler, Runnable {

	private DockerClient dockerClient = null;

	private ConfigurationAdmin configAdmin = null;
	private Set<Configuration> ownedConfiguration = new HashSet<>();
	private final static Logger logger = LoggerFactory.getLogger(DockerBridgeOSGi.class);

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
				if(servicePid!=null) {
					Configuration newConfig = injectConfig(serviceId, servicePid,serviceMapping.getValue());
					copy.remove(newConfig);
				} else {
					logger.info("Skipping incomplete mapping: "+serviceMapping.getValue());
				}
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
			return "tasman."+driver;
		}
		logger.info("servicePId missing: "+mm);

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

	private Configuration injectConfig(String serviceId, String servicePid,DockerServiceMapping mapping) throws IOException {
		Configuration c = createOrReuse(servicePid,"(id="+serviceId+")", true);
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
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
		if(mapping.getTags()!=null) {
			properties.put("host.tags", mapping.getTags());
		}
		appendIfChanged(c, properties);
//		c.update(properties);
		ownedConfiguration.add(c);
		return c;
	}

	// Ugly but necessary copying method for Dictionaries
	private static <K, V> Map<K, V> valueOf(Dictionary<K, V> dictionary) {
		  if (dictionary == null) {
		    return null;
		  }
		  Map<K, V> map = new HashMap<K, V>(dictionary.size());
		  Enumeration<K> keys = dictionary.keys();
		  while (keys.hasMoreElements()) {
		    K key = keys.nextElement();
		    map.put(key, dictionary.get(key));
		  }
		  return map;
		}
	
	// Need to strip the servicePid and factoryPid to accurately compare
	private boolean serviceConfigDictionariesEqual(Dictionary<String,Object> a,Dictionary<String,Object> b) {
		Map<String, Object> ac = valueOf(a);
		Map<String, Object> bc = valueOf(b);
		ac.remove(Constants.SERVICE_PID);
		ac.remove("service.factoryPid");
		bc.remove(Constants.SERVICE_PID);
		bc.remove("service.factoryPid");
		return ac.equals(bc);
	}
	@SuppressWarnings("unchecked")
	private void appendIfChanged(Configuration c,
			Hashtable<String, Object> settings) throws IOException {
		Dictionary<String, Object> old = c.getProperties();
		if (old != null) {
			if (!serviceConfigDictionariesEqual(old,settings)) {
				Dictionary<String, Object> merged = new Hashtable<String, Object>();
				Enumeration<String> keys = old.keys();
				while (keys.hasMoreElements()) {
					String next = keys.nextElement();
					merged.put(next, old.get(next));
				}
				keys = settings.keys();
				while (keys.hasMoreElements()) {
					String next = keys.nextElement();
					merged.put(next, settings.get(next));
				}
				
				c.update(merged);
			}
		} else {
			c.update(settings);
		}
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
		String serviceId = id.substring(0,8) + "-" + serviceMapping.getHostIp() + "-"
						+ serviceMapping.getHostPort();
		logger.info("serviceId: {}",serviceId);
		return serviceId;
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
