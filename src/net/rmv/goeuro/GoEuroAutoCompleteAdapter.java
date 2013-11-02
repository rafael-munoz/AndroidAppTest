package net.rmv.goeuro;

import static net.rmv.goeuro.GoEuroApplication.TAG;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.util.LruCache;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

public class GoEuroAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

	// small cache to speed up a bit the autocomplete
	private static LruCache<String, Result> cache = new LruCache<String, Result>(100);

	private List<String> results;

	private final String baseUrl;

	public GoEuroAutoCompleteAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.baseUrl = context.getString(R.string.autocomplete_url);
	}

	@Override
	public int getCount() {
		return results.size();
	}

	@Override
	public String getItem(int index) {
		return results.get(index);
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();

				if (constraint != null) {
					results = getResults(constraint.toString());

					// Assign the data to the FilterResults
					filterResults.values = results;
					filterResults.count = results.size();
				}
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				if (results != null && results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		};
		return filter;
	}

	private List<String> getResults(String input) {

		long start = System.currentTimeMillis();

		List<String> resultList = null;

		String url = MessageFormat.format(baseUrl, input);

		// TODO Do this fully async and cancel still pending requests
		// GoEuroApplication.getInstance().cancelPendingRequests();

		RequestFuture<JSONObject> future = RequestFuture.newFuture();
		JsonObjectRequest request = new JsonObjectRequest(url, null, future, future);
		GoEuroApplication.getInstance().addToRequestQueue(request);

		try {
			JSONObject json = future.get();
			Log.d(TAG, "    Request done on " + (System.currentTimeMillis() - start) + " ms.");

			long startProcesing = System.currentTimeMillis();

			JSONArray results = json.getJSONArray("results");

			resultList = new ArrayList<String>(results.length());

			GlobalPosition userPosition = GoEuroApplication.getInstance().getLocationManager().getPosition();
			if (userPosition != null) {

				List<Result> objResults = new ArrayList<Result>(results.length());
				for (int i = 0; i < results.length(); i++) {
					objResults.add(Result.getResult(results.getJSONObject(i), userPosition));
				}

				Collections.sort(objResults);

				for (Result result : objResults) {
					resultList.add(result.getName());
				}

			} else {
				for (int i = 0; i < results.length(); i++) {
					resultList.add(results.getJSONObject(i).getString("name"));
				}
			}

			Log.d(TAG, "    Result processing done on " + (System.currentTimeMillis() - startProcesing) + " ms.");

		} catch (Exception e) {
			Log.e(TAG, "Error getting auto-complete results:" + e.getMessage(), e);
			resultList = Collections.<String> emptyList();
		}

		Log.d(TAG, "Get results done on " + (System.currentTimeMillis() - start) + " ms.");

		return resultList;
	}

	private static class Result implements Comparable<Result> {

		private static GeodeticCalculator geoCalc = new GeodeticCalculator();

		private String name;
		private double distance;

		public static Result getResult(JSONObject object, GlobalPosition userPosition) throws JSONException {
			String cacheKey = object.getString("_id") + "_" + userPosition.toString();
			Result result = cache.get(cacheKey);
			if (result == null) {
				result = new Result(object, userPosition);
				cache.put(cacheKey, result);
			}
			return result;
		}

		private Result(JSONObject object, GlobalPosition userPosition) throws JSONException {
			this.name = object.getString("name");
			this.distance = userPosition != null ? calculateDistance(name, object, userPosition) : -1;
		}

		private double calculateDistance(String name, JSONObject object, GlobalPosition userPosition) throws JSONException {
			JSONObject location = object.getJSONObject("geo_position");
			GlobalPosition resultPosition = new GlobalPosition(location.getDouble("latitude"), location.getDouble("longitude"), 0.0);
			return geoCalc.calculateGeodeticCurve(Ellipsoid.WGS84, userPosition, resultPosition).getEllipsoidalDistance();
		}

		public double getDistance() {
			return this.distance;
		}

		public String getName() {
			return name;
		}

		@Override
		public int compareTo(Result another) {
			double diff = this.getDistance() - another.getDistance();
			return diff == 0 ? 0 : (diff > 0) ? 1 : -1;
		}

	}

}