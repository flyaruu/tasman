package com.dexels.redis.client;

import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
	
@Component
public class RedisTester {
	
	private final static Logger logger = LoggerFactory
			.getLogger(RedisTester.class);
	
	@Reference(cardinality=ReferenceCardinality.MULTIPLE,policy=ReferencePolicy.DYNAMIC)
	public void addRedisPool(JedisPool pool) {
		testPool(pool);
	}

	
	public void removeRedisPool(JedisPool pool) {
		logger.info("Pool removed");
	}
	
	private void testPool(JedisPool pool) {
		Jedis resource = null;
		try {
			logger.info("Pool added");
			resource = pool.getResource();
			resource.lpush("monkeylist",new Date().toString());
		} catch (Exception e) {
			logger.error("Error: ", e);
		} finally {
			if(resource!=null) {
				pool.returnResource(resource);
			}
		}
	}

}
