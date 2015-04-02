package com.ecwid.consul.v1.agent;

import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Check;
import com.ecwid.consul.v1.agent.model.Member;
import com.ecwid.consul.v1.agent.model.NewCheck;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Self;
import com.ecwid.consul.v1.agent.model.Service;

/**
 * @author Vasily Vasilkov (vgv@ecwid.com)
 */
public interface AgentClient {

	public Response<Map<String, Check>> getAgentChecks();

	public Response<Map<String, Service>> getAgentServices();

	public Response<List<Member>> getAgentMembers();

	public Response<Self> getAgentSelf();

	public Response<Void> agentJoin(String address, boolean wan);

	public Response<Void> agentForceLeave(String node);

	public Response<Void> agentCheckRegister(NewCheck newCheck);

	public Response<Void> agentCheckDeregister(String checkId);

	public Response<Void> agentCheckPass(String checkId);

	public Response<Void> agentCheckPass(String checkId, String note);

	public Response<Void> agentCheckWarn(String checkId);

	public Response<Void> agentCheckWarn(String checkId, String note);

	public Response<Void> agentCheckFail(String checkId);

	public Response<Void> agentCheckFail(String checkId, String note);

	public Response<Void> agentServiceRegister(NewService newService);

	public Response<Void> agentServiceDeregister(String serviceId);
}
