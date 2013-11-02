package net.rmv.goeuro.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class GoogleServicesLocationClient implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener, net.rmv.goeuro.location.LocationClient {

	private final LocationClient locationClient;
	private final LocationManager manager;
	
	private boolean listening;

	public GoogleServicesLocationClient(Context context, LocationManager manager) {
		this.locationClient = new LocationClient(context, this, this);
		this.manager = manager;
	}

	public Location getLastLocation() {
		return this.locationClient.getLastLocation();
	}

	public void connect() {
		locationClient.connect();
	}

	public void disconnect() {
		if (locationClient.isConnected()) {
			stopUpdates();
			locationClient.disconnect();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}

	@Override
	public void onConnected(Bundle bundle) {
		boolean enough = manager.setLastLocation(locationClient.getLastLocation());
		if (!enough) startUpdates();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location location) {
		manager.setLastLocation(location);
	}
	
	@Override
	public void startUpdates() {
		if (locationClient.isConnected() && !listening) {
			LocationRequest locationRequest = LocationRequest.create();
			locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
			// Set the update interval to 3 seconds
			locationRequest.setInterval(3000);
			// Set the fastest update interval to 1 second
			locationRequest.setFastestInterval(1000);

			locationClient.requestLocationUpdates(locationRequest, this);
			this.listening = true;
		}
	}

	@Override
	public void stopUpdates() {
		if (locationClient.isConnected() && listening) {
			this.locationClient.removeLocationUpdates(this);
			this.listening = false;
		}
	}

}
