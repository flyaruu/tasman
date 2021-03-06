package com.ecwid.consul.v1.event;

import java.util.List;

import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.event.model.Event;
import com.ecwid.consul.v1.event.model.EventParams;

/**
 * @author Vasily Vasilkov (vgv@ecwid.com)
 */
public interface EventClient {

	public Response<Event> eventFire(String event, String payload, EventParams eventParams, QueryParams queryParams);

	public Response<List<Event>> eventList(QueryParams queryParams);

	public Response<List<Event>> eventList(String event, QueryParams queryParams);

}
