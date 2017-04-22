package uk.ac.ncl.burton.twyb.ZKPoK.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkConnectionServer extends NetworkConnection {
	
	public NetworkConnectionServer(){
		connectionType = "SERVER";
	}

	@Override
	public void run() {
		
		try {
			ServerSocket ss = new ServerSocket(NetworkConfig.NETWORK_PORT);
			if( NetworkConfig.LOG_SERVER ) System.out.println("[SERVER] Waiting for connection...");
			Socket s = ss.accept();
			
			if( NetworkConfig.LOG_SERVER ) System.out.println("[SERVER] Connected to " + s.getInetAddress());
			
			connection(s);
			
			s.close();
			ss.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		connection_closed = true;
		
	}
	
	
	
}