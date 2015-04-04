package nl.codemonkey.tasman.unix.socket;

import java.io.IOException;
import java.util.HashMap;

import org.codehaus.jackson.JsonNode;

import com.dexels.unix.socket.impl.UnixDockerClient;

public class TestUnixSocket {

	public static void main(String[] args) throws IOException {
		UnixDockerClient u = new UnixDockerClient();
		HashMap<String, Object> settings = new HashMap<String, Object>();
		settings.put("path", args[0]);
		u.activate(settings);
		System.err.println("Activated: "+args[0]);
		JsonNode r = u.callUrl(args[1]);
		System.err.println("node: "+r);
	}

}
