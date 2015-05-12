package com.ecwid.consul.v1.status;

import java.util.List;

import com.ecwid.consul.v1.Response;

/**
 * @author Vasily Vasilkov (vgv@ecwid.com)
 */
public interface StatusClient {

	public Response<String> getStatusLeader();

	public Response<List<String>> getStatusPeers();
}
