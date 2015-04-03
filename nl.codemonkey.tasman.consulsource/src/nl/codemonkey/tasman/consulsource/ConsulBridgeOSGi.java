package nl.codemonkey.tasman.consulsource;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.AgentClient;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.kv.KeyValueClient;
import com.ecwid.consul.v1.kv.model.GetBinaryValue;

@Component(name = "tasman.consul.osgi",immediate=true)
public class ConsulBridgeOSGi implements EventHandler, Runnable {

	private static final int DEFAULT_POLLING_SPEED = 3000;
	private ConfigurationAdmin configAdmin = null;
	private Map<String,Configuration> ownedConfiguration = new HashMap<>();
	private final static Logger logger = LoggerFactory.getLogger(ConsulBridgeOSGi.class);

	private final ExecutorService pool = Executors.newFixedThreadPool(1);

	private boolean running = false;
	private KeyValueClient keyValueClient;
	private AgentClient agentClient;
	
	@Activate
	public void activate() throws IOException {
		logger.debug("Activating config components");
		running = true;
		pool.submit(this);
	}

	private void refreshConfig() throws IOException {
		Map<String,Configuration> copy = new HashMap<>(ownedConfiguration);
		Map<String,Service> services = agentClient.getAgentServices().getValue();
		for (Map.Entry<String, Service> element : services.entrySet()) {
			String address = element.getValue().getAddress();
			if(isOnThisHost(address)) {
				String serviceId = generateServiceId(element.getValue());
				Map<String,String> settings = loadConsulSettings(serviceId); 
				String servicePid = generateServicePid(settings);
				if(servicePid!=null) {
					injectConfig(serviceId, servicePid,settings,element.getValue());
					copy.remove(serviceId);
				}
			}
		}

		for (Map.Entry<String, Configuration> orphanEntry : copy.entrySet()) {
			orphanEntry.getValue().delete();
			ownedConfiguration.remove(orphanEntry.getKey());
		}
	}

	private boolean isOnThisHost(String address) {
		String hostname = System.getenv("HOSTNAME");
		return address.equals(hostname);
	}

	private Map<String, String> loadConsulSettings(String serviceId) {
		Response<List<GetBinaryValue>> result = keyValueClient.getKVBinaryValues(serviceId);
		List<GetBinaryValue> list = result.getValue();
		Map<String, String> dict = new HashMap<>();
		if(list!=null) {
			for (GetBinaryValue element : list) {
				String value = new String(element.getValue());
				String key = element.getKey();
				String[] keyparts = key.split("/");
				if(value!=null) {
					dict.put(keyparts[1], value);
				}
			}
		}
		return dict;
	}

	private String generateServicePid(Map<String,String> settings) {
		String driver = settings.get("DRIVER");
		if(driver!=null) {
			return "tasman."+driver;
		}
		logger.info("servicePid missing: "+settings);
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
			Thread.sleep(DEFAULT_POLLING_SPEED);
		} catch (InterruptedException e) {
			logger.error("Error: ", e);
		}
		pool.submit(this);
	}

	private void injectConfig(String serviceId, String servicePid,Map<String,String> mapping, Service service) throws IOException {
		Configuration c = createOrReuse(servicePid,"(id="+serviceId+")", true);
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put("id", serviceId);
		for (Entry<String, String> e : mapping.entrySet()) {
			if(e.getKey().equals("DRIVER") || e.getKey().equals("NAME")) {
				continue;
			}
			properties.put(e.getKey(),e.getValue());
		}  
		properties.put("host.ip", service.getAddress());
		properties.put("host.port", service.getPort());
//		properties.put("container.port", service.getPort());
//		properties.put("host.protocol", service. mapping.getProtocol());
		List<String> tags = service.getTags();
		if(tags!=null) {
			properties.put("host.tags",join(tags,","));
		}
		appendIfChanged(c, properties);
		ownedConfiguration.put(serviceId,c);
	}
	
	private static String join(Iterable<? extends CharSequence> s, String delimiter) {
	    Iterator<? extends CharSequence> iter = s.iterator();
	    if (!iter.hasNext()) return "";
	    StringBuilder buffer = new StringBuilder(iter.next());
	    while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
	    return buffer.toString();
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
		Map<String,Configuration> copy = new HashMap<>(ownedConfiguration);
		for (Configuration configuration : copy.values()) {
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

	private String generateServiceId(Service service) {
		String id = service.getId();
		String serviceId = id.substring(0,Math.min(id.length(), 8)) + "-" + service.getAddress() + "-"
						+ service.getPort();
		logger.info("serviceId: {}",serviceId);
		return serviceId;
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
