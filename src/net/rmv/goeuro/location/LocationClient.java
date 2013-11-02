package net.rmv.goeuro.location;


public interface LocationClient {

	public void connect();

	public void disconnect();
	
	public void startUpdates();
	
	public void stopUpdates();
	
}
