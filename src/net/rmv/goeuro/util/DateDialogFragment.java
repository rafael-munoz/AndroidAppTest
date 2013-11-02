package net.rmv.goeuro.util;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

public class DateDialogFragment extends DialogFragment {

	static Context sContext;
	static Calendar sDate;
	static DateDialogFragmentListener sListener;

	public static DateDialogFragment newInstance(Context context, Calendar date) {
		
		DateDialogFragment dialog = new DateDialogFragment();
		sContext = context;
		sDate = date;
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new DatePickerDialog(sContext, dateSetListener, sDate.get(Calendar.YEAR), sDate.get(Calendar.MONTH), sDate.get(Calendar.DAY_OF_MONTH));
	}

	public void setDateDialogFragmentListener(DateDialogFragmentListener listener) {
		sListener = listener;
	}

	private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

			Calendar newDate = Calendar.getInstance();
			newDate.set(year, monthOfYear, dayOfMonth);

			sListener.dateDialogFragmentDateSet(newDate);
		}
	};

	public interface DateDialogFragmentListener {
		public void dateDialogFragmentDateSet(Calendar date);
	}
}