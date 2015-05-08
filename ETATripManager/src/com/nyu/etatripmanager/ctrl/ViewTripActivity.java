package com.nyu.etatripmanager.ctrl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.nyu.etatripmanager.R;
import com.nyu.etatripmanager.models.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ViewTripActivity extends Activity {

	private static final String TAG = "ViewTripActivity";
	TextView tv_tripName, tv_tripLoc, tv_tripDate, tv_ppl;
	Button btn_startTrip, btn_arrived, btn_stopTrip;
	Trip cur_trip;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_trip);

		tv_tripName = (TextView)findViewById(R.id.tv_vt_tripName);
		tv_tripLoc = (TextView)findViewById(R.id.tv_vt_tripLoc);
		tv_tripDate = (TextView)findViewById(R.id.tv_vt_tripDate);
		tv_ppl = (TextView)findViewById(R.id.tv_vt_ppl);
		
		btn_startTrip = (Button)findViewById(R.id.button_vt_start_trip);
		btn_arrived = (Button)findViewById(R.id.button_vt_arrived);
		btn_stopTrip = (Button)findViewById(R.id.button_vt_stopTrip);
		
		btn_init();
		
		cur_trip = getTrip(getIntent());
		viewTrip(cur_trip);
		
		long trip_id = SharedPreferenceHelper.readLong(
				ViewTripActivity.this, SharedPreferenceHelper.ACTIVE_TRIP_ID, -1);
		boolean isArrived = SharedPreferenceHelper.readBoolean(
				ViewTripActivity.this, SharedPreferenceHelper.I_ARRIVED, false);
				
		
		if(trip_id == cur_trip.getId()){
			btn_startTrip.setEnabled(false);
			btn_stopTrip.setEnabled(true);
			
			btn_arrived.setEnabled(isArrived ? false : true);
		}
		
		
		Log.i(TAG, "onCreate");
	}
	
	private void btn_init() {
		btn_startTrip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ConnectivityManager connMgr = (ConnectivityManager)
						getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					btn_startTrip.setEnabled(false);
					btn_arrived.setEnabled(true);
					btn_stopTrip.setEnabled(true);
					
					SharedPreferenceHelper.writeLong(ViewTripActivity.this, 
							SharedPreferenceHelper.ACTIVE_TRIP_ID, cur_trip.getId());
				} else
					Toast.makeText(ViewTripActivity.this, 
							"No Internet connection", Toast.LENGTH_SHORT).show();
			}
		});
		
		btn_arrived.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferenceHelper.writeBoolean(ViewTripActivity.this, 
						SharedPreferenceHelper.I_ARRIVED, true);
				btn_arrived.setEnabled(false);
			}
		});
		
		btn_stopTrip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferenceHelper.removeKey(
						ViewTripActivity.this, SharedPreferenceHelper.ACTIVE_TRIP_ID);
				SharedPreferenceHelper.removeKey(
						ViewTripActivity.this, SharedPreferenceHelper.I_ARRIVED);
				btn_arrived.setEnabled(false);
				btn_stopTrip.setEnabled(false);
				
			}
		});
	}
	
	/**
	 * Create a Trip object via the recent trip that
	 * was passed to TripViewer via an Intent.
	 * 
	 * @param i The Intent that contains
	 * the most recent trip data.
	 * 
	 * @return The Trip that was most recently
	 * passed to TripViewer, or null if there
	 * is none.
	 */
	public Trip getTrip(Intent i) {
		Trip trip;
		trip = (Trip)i.getParcelableExtra("trip");
		return trip;
	}

	/**
	 * Populate the View using a Trip model.
	 * 
	 * @param trip The Trip model used to
	 * populate the View.
	 */
	public void viewTrip(Trip trip) {
		tv_tripName.setText(trip.getName());
		tv_tripLoc.setText(trip.getLocation()[0]+", "+trip.getLocation()[1]);

		SimpleDateFormat formatter = new SimpleDateFormat("MMM/dd/yyyy HH:mm",
				Locale.US);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(trip.getDate());
		String tripDateStr = formatter.format(cal.getTime());
		tv_tripDate.setText(tripDateStr);
		
		Person [] ppl = trip.getGuests();
		for (Person person : ppl) {
			tv_ppl.append(person.getName()+"\n");
			tv_ppl.append(person.getPhone()+"\n");
		}
	}
}
