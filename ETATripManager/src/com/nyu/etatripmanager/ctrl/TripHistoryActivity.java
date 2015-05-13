package com.nyu.etatripmanager.ctrl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import com.nyu.etatripmanager.models.*;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class TripHistoryActivity extends ListActivity {
	private static final String TAG = "TripHistoryActivity";
	
	List<Trip> tripList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get all the trips from the database
		TripDatabaseHelper trip_db = new TripDatabaseHelper(TripHistoryActivity.this);
		tripList = trip_db.getAllTrips();
		trip_db.close();
		
		// Create a String list of titles of the trips
		ArrayList<String> tripNames = new ArrayList<String>();
		ListIterator<Trip> it = tripList.listIterator();
		while (it.hasNext()) {
			Trip t = it.next();
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd",
					Locale.US);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(t.getDate());
			String dateString = formatter.format(cal.getTime());
			tripNames.add(t.getName() + " - " + dateString);
		}

		// Set the adapter to the list
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, tripNames);
		setListAdapter(adapter);

		Log.i(TAG, "onCreate");
	}

	@Override
	  protected void onListItemClick(ListView l, View v, int position, long id) {
	    startViewTripActivity(tripList.get((int)id));
	  }
	
	/**
	 * This method should start the Activity responsible for viewing a trip. 
	 * @param trip the trip object to pass through the new intent
	 */
	public void startViewTripActivity(Trip trip) {
		Intent intent = new Intent(this, ViewTripActivity.class);
		intent.putExtra("trip", trip);		
		startActivity(intent);
	}
}
