package net.rmv.goeuro;

import net.rmv.goeuro.location.LocationManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import android.app.Application;


public class GoEuroApplication extends Application {

    /**
     * Log or request TAG
     */
    public static final String TAG = "GOEURO";

    /**
     * Global request queue for Volley
     */
    private RequestQueue requestQueue;
    
    
    /**
     * Global Location Manager
     */
    private LocationManager locationManager;
    
    

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static GoEuroApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the singleton
        instance = this;
        
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        locationManager = new LocationManager(getApplicationContext());
    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized GoEuroApplication getInstance() {
        return instance;
    }

    /**
     * @return The Volley Request queue
     */
    public synchronized RequestQueue getRequestQueue() {
        return requestQueue;
    }
    

    /**
     * @return The Location manager
     */
    public synchronized LocationManager getLocationManager() {
        return locationManager;
    }


    /**
     * Adds the specified request to the global queue 
     * 
     * @param req
     */
    public <T> void addToRequestQueue(Request<T> request) {
    	request.setTag(TAG);
        VolleyLog.d("Adding request to queue: %s", request.getUrl());
        getRequestQueue().add(request);
    }

    /**
     * Cancels all pending requests 
     * 
     * @param tag
     */
    public void cancelPendingRequests() {
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
    
    
}
