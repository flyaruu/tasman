package com.dexels.redis.client;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPool;

@Component(property=Constants.SERVICE_PID+"="+"tasman.redis",immediate=true)
public class RedisUpdater implements ManagedServiceFactory {

	
	private final static Logger logger = LoggerFactory
			.getLogger(RedisUpdater.class);
	
	private final Map<String,JedisPool> pools = new HashMap<>();
	private final Map<JedisPool,ServiceRegistration<JedisPool>> serviceRegistrations = new HashMap<>();
	private BundleContext bundleContext;
	
	@Activate
	public void activate(BundleContext context) {
		this.bundleContext = context;
	}

	@Deactivate
	public void deactivate() {
		for (JedisPool pool : pools.values()) {
			ServiceRegistration<JedisPool> registered = serviceRegistrations.get(pool);
			pool.close();
			registered.unregister();
		}
	}

	@Override
	public String getName() {
		return null; //"docker.registrator.redis";
	}

	@Override
	public void updated(String pid, Dictionary<String, ?> properties)
			throws ConfigurationException {
		ClassLoader prev = null;
		try {
			prev = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			JedisPool pool = new JedisPool((String)properties.get("host.ip"),Integer.parseInt((String) properties.get("host.port")) );	
			pools.put(pid, pool);
			
			ServiceRegistration<JedisPool> registered = bundleContext.registerService(JedisPool.class, pool, properties);
			serviceRegistrations.put(pool, registered);
		} catch (Throwable e) {
			logger.error("Error: ", e);
		} finally {
			Thread.currentThread().setContextClassLoader(prev);
		}
		
	}

	@Override
	public void deleted(String pid) {
		JedisPool pool = this.pools.get(pid);
		ServiceRegistration<JedisPool> registered = serviceRegistrations.get(pool);
		pool.close();
		registered.unregister();
	}
	
}
