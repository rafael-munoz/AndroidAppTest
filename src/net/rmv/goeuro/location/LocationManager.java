package net.rmv.goeuro.location;

import static net.rmv.goeuro.GoEuroApplication.TAG;

import java.text.MessageFormat;

import org.gavaghan.geodesy.GlobalPosition;

import net.rmv.goeuro.R;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class LocationManager {

	private static enum Modes {
		GOOGLE_SERVICES, MANUAL, BOTH
	}
	
	private final Modes mode;
	
	private final Context context;
	private LocationClient client;
	private final int maxAccuracy;
	
	private GlobalPosition position;

	public LocationManager(Context context) {
		this.context = context;
		
		String locationMode = context.getString(R.string.location_mode);
		
		this.mode = Modes.valueOf(locationMode);
		
		switch (mode) {
		case GOOGLE_SERVICES:
			if(servicesConnected()) this.client = new GoogleServicesLocationClient(context, this);
			break;
		case MANUAL:
			this.client = new ManualLocationClient(context, this);
			break;
		case BOTH:
		default:
			if(servicesConnected()) {
				this.client = new GoogleServicesLocationClient(context, this);
				Log.i(TAG, "Google Play services present - using Google Services location client");
			} else {
				this.client = new ManualLocationClient(context, this);
				Log.i(TAG, "Google Play services not present - using manual location client");
			}
			break;
		}

		this.maxAccuracy = context.getResources().getInteger(R.integer.accuracy_meters);
	}

	public void connect() {
		if(this.client != null) this.client.connect();
	}

	public void disconnect() {
		if(this.client != null) this.client.disconnect();
	}
	
	public GlobalPosition getPosition() {
		return this.position;
	}
	
	boolean setLastLocation(Location location) {
		if(location == null) return false;
		Log.d(TAG, MessageFormat.format("Update Location: Latitude={0}, Longitude={1}, Accuracy={3}", location.getLatitude(), location.getLongitude(), location.getAccuracy()));
		this.position = new GlobalPosition(location.getLatitude(), location.getLongitude(), 0.0);
		if(location.getAccuracy() < maxAccuracy) {
			Log.i(TAG, "Location under max configured accuracy (maxAccuracy="+maxAccuracy+") - shutting location services done");
			this.client.stopUpdates();
			return true;
		} else {
			return false;
		}
	}
	
	// Check if Google Play services is available
	public boolean servicesConnected() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		return ConnectionResult.SUCCESS == resultCode;

	}

	public void showLocationErrorIfNeeded(Activity activity) {
		if (Modes.GOOGLE_SERVICES == this.mode) {
			int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
			if ( ConnectionResult.SUCCESS != resultCode) {			
				GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 10).show();
			}
		}
	}

}
