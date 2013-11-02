package net.rmv.goeuro.location;

import static net.rmv.goeuro.GoEuroApplication.TAG;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

public class ManualLocationClient implements LocationListener, LocationClient {

	private Context context;
	private LocationManager manager;

	private boolean isGPSEnabled = false;
	private boolean isWifiEnabled = false;
	private boolean isNetworkConnected = false;

	private boolean listening = false;

	// Declaring a Location Manager
	protected android.location.LocationManager locationManager;

	public ManualLocationClient(Context context, LocationManager manager) {
		this.context = context;
		this.manager = manager;
	}

	@Override
	public void onLocationChanged(Location location) {
		manager.setLastLocation(location);
	}

	@Override
	public void connect() {
		startUpdates();

	}

	@Override
	public void disconnect() {
		if (listening) {
			stopUpdates();
		}
	}

	@Override
	public void startUpdates() {
		try {

			locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

			if (locationManager != null) {

				this.isGPSEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
				this.isWifiEnabled = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();

				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
				this.isNetworkConnected = activeNetwork.isConnected();

				boolean enough = false;

				if (isNetworkConnected || isWifiEnabled) {
					Location location = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
					enough = manager.setLastLocation(location);
					if (!enough) {
						locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 3000, 100, this);
						listening = true;
					}
				}

				if (isGPSEnabled && !enough) {
					Location location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
					enough = manager.setLastLocation(location);
					if (!enough) {
						locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 3000, 100, this);
						listening = true;
					}
				}
			} else {
				Log.w(TAG, "Not possible to retrieve the Location manager");
			}
		} catch (Exception e) {
			Log.e(TAG, "Error getting location", e);
		}

	}

	@Override
	public void stopUpdates() {
		if (locationManager != null && this.listening) {
			locationManager.removeUpdates(this);
			this.listening = false;
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}
