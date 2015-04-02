package com.dexels.docker.consulsink;

import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.codec.binary.Base64;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.kv.model.GetValue;

public class ExampleComponentTest extends TestCase {

    public void testExample() throws Exception {
    	ConsulClient client = new ConsulClient("192.168.59.103",8500);
    	Response<GetValue> kvValue = client.getKVValue("monkey");
    	if(kvValue.getValue()==null) {
    		System.err.println("null");
        	client.setKVValue("monkey", "1");
        	kvValue = client.getKVValue("monkey");
    	}
		String s = kvValue.getValue().getValue();
    	System.err.println("s: "+s);
    	String result = new String(Base64.decodeBase64(s));
    	System.err.println("result: "+result);
    	client.setKVValue("monkey", result+"1");
    	NewService newService = new NewService();
    	newService.setId("myapp_01");
    	newService.setName("myapp");
    	newService.setPort(8080);
    	client.agentServiceRegister(newService);
    	Map<String,Service> services = client.getAgentServices().getValue();
    	for (Map.Entry<String, Service> e : services.entrySet()) {
			System.err.println("e: "+e.getKey()+" = "+e.getValue().getPort());
		}
    }
}
