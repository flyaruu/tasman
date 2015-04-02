package nl.codemonkey.tasman.consul.osgi;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.acl.AclClient;
import com.ecwid.consul.v1.acl.model.Acl;
import com.ecwid.consul.v1.acl.model.NewAcl;
import com.ecwid.consul.v1.acl.model.UpdateAcl;
import com.ecwid.consul.v1.agent.AgentClient;
import com.ecwid.consul.v1.agent.model.Check;
import com.ecwid.consul.v1.agent.model.Member;
import com.ecwid.consul.v1.agent.model.NewCheck;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Self;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.catalog.CatalogClient;
import com.ecwid.consul.v1.catalog.model.CatalogDeregistration;
import com.ecwid.consul.v1.catalog.model.CatalogNode;
import com.ecwid.consul.v1.catalog.model.CatalogRegistration;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.ecwid.consul.v1.catalog.model.Node;
import com.ecwid.consul.v1.event.EventClient;
import com.ecwid.consul.v1.event.model.Event;
import com.ecwid.consul.v1.event.model.EventParams;
import com.ecwid.consul.v1.health.HealthClient;
import com.ecwid.consul.v1.health.model.Check.CheckStatus;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.kv.KeyValueClient;
import com.ecwid.consul.v1.kv.model.GetBinaryValue;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.SessionClient;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import com.ecwid.consul.v1.status.StatusClient;

@Component(configurationPolicy= ConfigurationPolicy.REQUIRE,name="tasman.consul",immediate=true)
public final class ConsulOSGiClient implements AclClient,
		AgentClient, CatalogClient, EventClient, HealthClient, KeyValueClient,
		SessionClient, StatusClient {

	
	private final static Logger logger = LoggerFactory
			.getLogger(ConsulOSGiClient.class);
	
	private ConsulClient wrapped = null;

	@Activate
	public void activate(Map<String, Object> settings) {
		try {
			System.err.println("Settings: "+settings);
			String host = (String) settings.get("host.ip");
			int port = Integer.parseInt((String) settings.get("host.port"));
			wrapped = new ConsulClient(host,port);
		} catch (NumberFormatException e) {
			logger.error("Error: ", e);
		}
	}

	@Deactivate
	public void deactivate() {
		this.wrapped = null;
	}
	
	@Override
	public Response<String> aclCreate(NewAcl newAcl, String token) {
		return wrapped.aclCreate(newAcl, token);
	}

	@Override
	public Response<Void> aclUpdate(UpdateAcl updateAcl, String token) {
		return wrapped.aclUpdate(updateAcl, token);
	}

	@Override
	public Response<Void> aclDestroy(String aclId, String token) {
		return wrapped.aclDestroy(aclId, token);
	}

	@Override
	public Response<Acl> getAcl(String id) {
		return wrapped.getAcl(id);
	}

	@Override
	public Response<String> aclClone(String aclId, String token) {
		return wrapped.aclClone(aclId, token);
	}

	@Override
	public Response<List<Acl>> getAclList(String token) {
		return wrapped.getAclList(token);
	}

	@Override
	public Response<Map<String, Check>> getAgentChecks() {
		return wrapped.getAgentChecks();
	}

	@Override
	public Response<Map<String, Service>> getAgentServices() {
		return wrapped.getAgentServices();
	}

	@Override
	public Response<List<Member>> getAgentMembers() {
		return wrapped.getAgentMembers();
	}

	@Override
	public Response<Self> getAgentSelf() {
		return wrapped.getAgentSelf();
	}

	@Override
	public Response<Void> agentJoin(String address, boolean wan) {
		return wrapped.agentJoin(address, wan);
	}

	@Override
	public Response<Void> agentForceLeave(String node) {
		return wrapped.agentForceLeave(node);
	}

	@Override
	public Response<Void> agentCheckRegister(NewCheck newCheck) {
		return wrapped.agentCheckRegister(newCheck);
	}

	@Override
	public Response<Void> agentCheckDeregister(String checkId) {
		return wrapped.agentCheckDeregister(checkId);
	}

	@Override
	public Response<Void> agentCheckPass(String checkId) {
		return wrapped.agentCheckPass(checkId);
	}

	@Override
	public Response<Void> agentCheckPass(String checkId, String note) {
		return wrapped.agentCheckPass(checkId, note);
	}

	@Override
	public Response<Void> agentCheckWarn(String checkId) {
		return wrapped.agentCheckWarn(checkId);
	}

	@Override
	public Response<Void> agentCheckWarn(String checkId, String note) {
		return wrapped.agentCheckWarn(checkId, note);
	}

	@Override
	public Response<Void> agentCheckFail(String checkId) {
		return wrapped.agentCheckFail(checkId);
	}

	@Override
	public Response<Void> agentCheckFail(String checkId, String note) {
		return wrapped.agentCheckFail(checkId, note);
	}

	@Override
	public Response<Void> agentServiceRegister(NewService newService) {
		return wrapped.agentServiceRegister(newService);
	}

	@Override
	public Response<Void> agentServiceDeregister(String serviceId) {
		return wrapped.agentServiceDeregister(serviceId);
	}

	@Override
	public Response<Void> catalogRegister(
			CatalogRegistration catalogRegistration) {
		return wrapped.catalogRegister(catalogRegistration);
	}

	@Override
	public Response<Void> catalogDeregister(
			CatalogDeregistration catalogDeregistration,
			CatalogDeregistration... catalogDeregistrations) {
		return wrapped.catalogDeregister(catalogDeregistration,
				catalogDeregistrations);
	}

	@Override
	public Response<List<String>> getCatalogDatacenters() {
		return wrapped.getCatalogDatacenters();
	}

	@Override
	public Response<List<Node>> getCatalogNodes(QueryParams queryParams) {
		return wrapped.getCatalogNodes(queryParams);
	}

	@Override
	public Response<Map<String, List<String>>> getCatalogServices(
			QueryParams queryParams) {
		return wrapped.getCatalogServices(queryParams);
	}

	@Override
	public Response<List<CatalogService>> getCatalogService(String serviceName,
			QueryParams queryParams) {
		return wrapped.getCatalogService(serviceName, queryParams);
	}

	@Override
	public Response<List<CatalogService>> getCatalogService(String serviceName,
			String tag, QueryParams queryParams) {
		return wrapped.getCatalogService(serviceName, tag, queryParams);
	}

	@Override
	public Response<CatalogNode> getCatalogNode(String nodeName,
			QueryParams queryParams) {
		return wrapped.getCatalogNode(nodeName, queryParams);
	}

	@Override
	public Response<Event> eventFire(String event, String payload,
			EventParams eventParams, QueryParams queryParams) {
		return wrapped.eventFire(event, payload, eventParams, queryParams);
	}

	@Override
	public Response<List<Event>> eventList(QueryParams queryParams) {
		return wrapped.eventList(queryParams);
	}

	@Override
	public Response<List<Event>> eventList(String event, QueryParams queryParams) {
		return wrapped.eventList(event, queryParams);
	}

	@Override
	public Response<List<com.ecwid.consul.v1.health.model.Check>> getHealthChecksForNode(
			String nodeName, QueryParams queryParams) {
		return wrapped.getHealthChecksForNode(nodeName, queryParams);
	}

	@Override
	public Response<List<com.ecwid.consul.v1.health.model.Check>> getHealthChecksForService(
			String serviceName, QueryParams queryParams) {
		return wrapped.getHealthChecksForService(serviceName, queryParams);
	}

	@Override
	public Response<List<HealthService>> getHealthServices(String serviceName,
			boolean onlyPassing, QueryParams queryParams) {
		return wrapped.getHealthServices(serviceName, onlyPassing, queryParams);
	}

	@Override
	public Response<List<HealthService>> getHealthServices(String serviceName,
			String tag, boolean onlyPassing, QueryParams queryParams) {
		return wrapped.getHealthServices(serviceName, tag, onlyPassing,
				queryParams);
	}

	@Override
	public Response<List<com.ecwid.consul.v1.health.model.Check>> getHealthChecksState(
			QueryParams queryParams) {
		return wrapped.getHealthChecksState(queryParams);
	}

	@Override
	public Response<List<com.ecwid.consul.v1.health.model.Check>> getHealthChecksState(
			CheckStatus checkStatus, QueryParams queryParams) {
		return wrapped.getHealthChecksState(checkStatus, queryParams);
	}

	@Override
	public Response<GetValue> getKVValue(String key) {
		return wrapped.getKVValue(key);
	}

	@Override
	public Response<GetValue> getKVValue(String key, String token) {
		return wrapped.getKVValue(key, token);
	}

	@Override
	public Response<GetValue> getKVValue(String key, QueryParams queryParams) {
		return wrapped.getKVValue(key, queryParams);
	}

	@Override
	public Response<GetValue> getKVValue(String key, String token,
			QueryParams queryParams) {
		return wrapped.getKVValue(key, token, queryParams);
	}

	@Override
	public Response<GetBinaryValue> getKVBinaryValue(String key) {
		return wrapped.getKVBinaryValue(key);
	}

	@Override
	public Response<GetBinaryValue> getKVBinaryValue(String key, String token) {
		return wrapped.getKVBinaryValue(key, token);
	}

	@Override
	public Response<GetBinaryValue> getKVBinaryValue(String key,
			QueryParams queryParams) {
		return wrapped.getKVBinaryValue(key, queryParams);
	}

	@Override
	public Response<GetBinaryValue> getKVBinaryValue(String key, String token,
			QueryParams queryParams) {
		return wrapped.getKVBinaryValue(key, token, queryParams);
	}

	@Override
	public Response<List<GetValue>> getKVValues(String keyPrefix) {
		return wrapped.getKVValues(keyPrefix);
	}

	@Override
	public Response<List<GetValue>> getKVValues(String keyPrefix, String token) {
		return wrapped.getKVValues(keyPrefix, token);
	}

	@Override
	public Response<List<GetValue>> getKVValues(String keyPrefix,
			QueryParams queryParams) {
		return wrapped.getKVValues(keyPrefix, queryParams);
	}

	@Override
	public Response<List<GetValue>> getKVValues(String keyPrefix, String token,
			QueryParams queryParams) {
		return wrapped.getKVValues(keyPrefix, token, queryParams);
	}

	@Override
	public Response<List<GetBinaryValue>> getKVBinaryValues(String keyPrefix) {
		return wrapped.getKVBinaryValues(keyPrefix);
	}

	@Override
	public Response<List<GetBinaryValue>> getKVBinaryValues(String keyPrefix,
			String token) {
		return wrapped.getKVBinaryValues(keyPrefix, token);
	}

	@Override
	public Response<List<GetBinaryValue>> getKVBinaryValues(String keyPrefix,
			QueryParams queryParams) {
		return wrapped.getKVBinaryValues(keyPrefix, queryParams);
	}

	@Override
	public Response<List<GetBinaryValue>> getKVBinaryValues(String keyPrefix,
			String token, QueryParams queryParams) {
		return wrapped.getKVBinaryValues(keyPrefix, token, queryParams);
	}

	@Override
	public Response<List<String>> getKVKeysOnly(String keyPrefix) {
		return wrapped.getKVKeysOnly(keyPrefix);
	}

	@Override
	public Response<List<String>> getKVKeysOnly(String keyPrefix,
			String separator, String token) {
		return wrapped.getKVKeysOnly(keyPrefix, separator, token);
	}

	@Override
	public Response<List<String>> getKVKeysOnly(String keyPrefix,
			QueryParams queryParams) {
		return wrapped.getKVKeysOnly(keyPrefix, queryParams);
	}

	@Override
	public Response<List<String>> getKVKeysOnly(String keyPrefix,
			String separator, String token, QueryParams queryParams) {
		return wrapped.getKVKeysOnly(keyPrefix, separator, token, queryParams);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value) {
		return wrapped.setKVValue(key, value);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value,
			PutParams putParams) {
		return wrapped.setKVValue(key, value, putParams);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value, String token,
			PutParams putParams) {
		return wrapped.setKVValue(key, value, token, putParams);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value,
			QueryParams queryParams) {
		return wrapped.setKVValue(key, value, queryParams);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value,
			PutParams putParams, QueryParams queryParams) {
		return wrapped.setKVValue(key, value, putParams, queryParams);
	}

	@Override
	public Response<Boolean> setKVValue(String key, String value, String token,
			PutParams putParams, QueryParams queryParams) {
		return wrapped.setKVValue(key, value, token, putParams, queryParams);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value) {
		return wrapped.setKVBinaryValue(key, value);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value,
			PutParams putParams) {
		return wrapped.setKVBinaryValue(key, value, putParams);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value,
			String token, PutParams putParams) {
		return wrapped.setKVBinaryValue(key, value, token, putParams);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value,
			QueryParams queryParams) {
		return wrapped.setKVBinaryValue(key, value, queryParams);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value,
			PutParams putParams, QueryParams queryParams) {
		return wrapped.setKVBinaryValue(key, value, putParams, queryParams);
	}

	@Override
	public Response<Boolean> setKVBinaryValue(String key, byte[] value,
			String token, PutParams putParams, QueryParams queryParams) {
		return wrapped.setKVBinaryValue(key, value, token, putParams,
				queryParams);
	}

	@Override
	public Response<Void> deleteKVValue(String key) {
		return wrapped.deleteKVValue(key);
	}

	@Override
	public Response<Void> deleteKVValue(String key, String token) {
		return wrapped.deleteKVValue(key, token);
	}

	@Override
	public Response<Void> deleteKVValue(String key, QueryParams queryParams) {
		return wrapped.deleteKVValue(key, queryParams);
	}

	@Override
	public Response<Void> deleteKVValue(String key, String token,
			QueryParams queryParams) {
		return wrapped.deleteKVValue(key, token, queryParams);
	}

	@Override
	public Response<Void> deleteKVValues(String key) {
		return wrapped.deleteKVValues(key);
	}

	@Override
	public Response<Void> deleteKVValues(String key, String token) {
		return wrapped.deleteKVValues(key, token);
	}

	@Override
	public Response<Void> deleteKVValues(String key, QueryParams queryParams) {
		return wrapped.deleteKVValues(key, queryParams);
	}

	@Override
	public Response<Void> deleteKVValues(String key, String token,
			QueryParams queryParams) {
		return wrapped.deleteKVValues(key, token, queryParams);
	}

	@Override
	public Response<String> sessionCreate(NewSession newSession,
			QueryParams queryParams) {
		return wrapped.sessionCreate(newSession, queryParams);
	}

	@Override
	public Response<Void> sessionDestroy(String session, QueryParams queryParams) {
		return wrapped.sessionDestroy(session, queryParams);
	}

	@Override
	public Response<Session> getSessionInfo(String session,
			QueryParams queryParams) {
		return wrapped.getSessionInfo(session, queryParams);
	}

	@Override
	public Response<List<Session>> getSessionNode(String node,
			QueryParams queryParams) {
		return wrapped.getSessionNode(node, queryParams);
	}

	@Override
	public Response<List<Session>> getSessionList(QueryParams queryParams) {
		return wrapped.getSessionList(queryParams);
	}

	@Override
	public Response<Session> renewSession(String session,
			QueryParams queryParams) {
		return wrapped.renewSession(session, queryParams);
	}

	@Override
	public Response<String> getStatusLeader() {
		return wrapped.getStatusLeader();
	}

	@Override
	public Response<List<String>> getStatusPeers() {
		return wrapped.getStatusPeers();
	}

}
