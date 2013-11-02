package net.rmv.goeuro;

import java.text.MessageFormat;
import java.util.Calendar;

import net.rmv.goeuro.util.DateDialogFragment;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * Usually I would do most of this with AndroidAnnotations but let's do it simpler and minimizing dependencies this time
 * 
 * @author Rafael Muñoz Vega
 */
public class MainActivity extends Activity {

	private EditText selectDateView;
	private Button searchButton;
	private AutoCompleteTextView fromAutoCompleteView;
	private AutoCompleteTextView toAutoCompleteView;

	private Calendar selectedDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		SearchButtonListener searchButtonListener = new SearchButtonListener();
		searchButton = (Button) findViewById(R.id.searchButton);
		searchButton.setOnClickListener(searchButtonListener);

		SelectDateListener selectDateListener = new SelectDateListener();
		selectDateView = (EditText) findViewById(R.id.dateText);
		selectDateView.setOnClickListener(selectDateListener);
		selectDateView.setOnFocusChangeListener(selectDateListener);

		AutoCompleteViewListener autoCompleteListener = new AutoCompleteViewListener();
		

		GoEuroAutoCompleteAdapter adapter = new GoEuroAutoCompleteAdapter(this, R.layout.autocomplete_list);
		adapter.warmUp();

		fromAutoCompleteView = (AutoCompleteTextView) findViewById(R.id.fromAutoCompleteTextView);
		fromAutoCompleteView.setAdapter(adapter);
		fromAutoCompleteView.setOnItemClickListener(autoCompleteListener);
		fromAutoCompleteView.setOnFocusChangeListener(autoCompleteListener);

		toAutoCompleteView = (AutoCompleteTextView) findViewById(R.id.toAutoCompleteTextView);
		toAutoCompleteView.setAdapter(adapter);
		toAutoCompleteView.setOnItemClickListener(autoCompleteListener);
		toAutoCompleteView.setOnFocusChangeListener(autoCompleteListener);
	}

	@Override
	protected void onStart() {
		super.onStart();
		GoEuroApplication.getInstance().getLocationManager().showLocationErrorIfNeeded(this);
		GoEuroApplication.getInstance().getLocationManager().connect();
	}

	@Override
	protected void onStop() {
		GoEuroApplication.getInstance().getLocationManager().disconnect();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	protected void updateDate(Calendar date) {
		this.selectedDate = date;
		this.selectDateView.setText(date.get(Calendar.DAY_OF_MONTH) + "-" + date.get(Calendar.MONTH) + "-" + date.get(Calendar.YEAR));
		verifyData();
	}

	private void verifyData() {
		if (!TextUtils.isEmpty(getTextString(toAutoCompleteView)) && !TextUtils.isEmpty(getTextString(toAutoCompleteView))
				&& !TextUtils.isEmpty(getTextString(selectDateView))) {
			searchButton.setVisibility(View.VISIBLE);
		} else {
			searchButton.setVisibility(View.INVISIBLE);
		}
	}

	private String getTextString(EditText view) {
		return view.getText().toString().trim();
	}

	// Listeners

	private class SearchButtonListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.search_warning), Toast.LENGTH_LONG).show();
		}

	}

	private class AutoCompleteViewListener implements OnFocusChangeListener, AdapterView.OnItemClickListener {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus)
				verifyData();
		}

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			verifyData();
		}
	}

	private class SelectDateListener implements View.OnClickListener, OnFocusChangeListener {
		@Override
		public void onFocusChange(View view, boolean hasFocus) {
			if (hasFocus)
				showDateDialog();
		}

		@Override
		public void onClick(View view) {
			showDateDialog();
		}

		private void showDateDialog() {
			Calendar dateToShow = MainActivity.this.selectedDate != null ? MainActivity.this.selectedDate : Calendar.getInstance();

			DateDialogFragment ddf = DateDialogFragment.newInstance(MainActivity.this, dateToShow);
			ddf.setDateDialogFragmentListener(new DateDialogFragment.DateDialogFragmentListener() {
				@Override
				public void dateDialogFragmentDateSet(Calendar date) {
					MainActivity.this.updateDate(date);
				}
			});

			ddf.show(MainActivity.this.getFragmentManager(), "date picker dialog fragment");
		}

	}

}
