package com.nyu.etatripmanager.ctrl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nyu.etatripmanager.R;
import com.nyu.etatripmanager.models.Person;
import com.nyu.etatripmanager.models.Trip;


public class CreateTripActivity extends Activity {
	
	// requestCode for onActivityResult:
	private static final int PICK_CONTACT_REQUEST = 1;
	private static final int SEARCH_LOC_REQUEST = 2;
	private static final int DATE_DIALOG_ID = 888;
	private static final int TIME_DIALOG_ID = 999;
	private static final String TAG = "CreateTripActivity";

	TextView tv_guests, tv_loc, tv_date, tv_time;
	EditText edit_tripName, edit_tripLoc, edit_tripLocType, edit_tripDate;
	Button btn_inviteFriends, btn_searchLoc, btn_createTrip, btn_cancelTrip;
	Button btn_pickDate, btn_pickTime;
	private int year, month, day, hour, min;
 
	Set<Person> guests;
	String[] locInfo;
	Trip myTrip;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_trip);
		// TODO - fill in here
		guests = new HashSet<Person>();

		tv_guests = (TextView) findViewById(R.id.tv_ct_guests);
		tv_loc = (TextView) findViewById(R.id.tv_ct_loc);
		tv_date = (TextView) findViewById(R.id.tv_ct_date);
		tv_time = (TextView) findViewById(R.id.tv_ct_time);
		edit_tripName = (EditText) findViewById(R.id.editText_tripName);
		edit_tripLoc = (EditText) findViewById(R.id.editText_tripLoc);
		edit_tripLocType = (EditText)findViewById(R.id.editText_tripLocType);
		btn_inviteFriends = (Button) findViewById(R.id.button_ct_inviteFriends);
		btn_searchLoc = (Button)findViewById(R.id.button_ct_searchLoc);
		btn_pickDate = (Button) findViewById(R.id.button_ct_pickDate);
		btn_pickTime = (Button) findViewById(R.id.button_ct_pickTime);
		btn_createTrip = (Button) findViewById(R.id.button_ct_createTrip);
		btn_cancelTrip = (Button) findViewById(R.id.button_ct_cancelTrip);

		// Get current date by calender
        
        final Calendar c = Calendar.getInstance();
        year  = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day   = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        min = c.get(Calendar.MINUTE);
        
		btn_events();
		Log.i(TAG, "onCreate");
	}

	/**
	 * This method sets onClick events for the buttons in the activity
	 */
	private void btn_events() {
		btn_inviteFriends.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pickContact();
			}
		});

		btn_searchLoc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ConnectivityManager connMgr = (ConnectivityManager)
						getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					searchLoc(edit_tripLoc.getText().toString(),
							edit_tripLocType.getText().toString());
				} else
					Toast.makeText(CreateTripActivity.this, 
							"No Internet connection", Toast.LENGTH_SHORT).show();
			}
		});
		
		btn_pickDate.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});
		
		btn_pickTime.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
			}
		});
		
		btn_createTrip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ConnectivityManager connMgr = (ConnectivityManager) 
						getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					myTrip = createTrip();
					if (myTrip != null) {
						saveTrip(myTrip);
					} else {
						Toast.makeText(CreateTripActivity.this,
								"Some fields are not filled!",
								Toast.LENGTH_LONG).show();
						return;
					}
				} else
					Toast.makeText(CreateTripActivity.this,
							"No Internet connection", Toast.LENGTH_SHORT)
							.show();
			}
		});

		btn_cancelTrip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelTrip();
			}
		});
	}
		
	/**
	 * This method starts a new activity to pick a contact from the Contacts
	 * list.
	 */
	private void pickContact() {
		Intent i = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		// ContactsContract.Contacts.CONTENT_URI = "content://contacts"
		i.setType(ContactsContract.CommonDataKinds.Email.CONTENT_TYPE); // Show user only contacts w/ email id
		startActivityForResult(i, PICK_CONTACT_REQUEST);
	}

	/**
	 * This method uses HW3API.apk API to find a desired location
	 * 
	 * @param loc The location to look for
	 * @param locType The type of the location to look for
	 */
	private void searchLoc(String loc, String locType) {
		if(loc.length() < 1 || locType.length() < 1) {
			Toast.makeText(CreateTripActivity.this, 
				"Location fields can't be empty", Toast.LENGTH_SHORT).show();
			return;
		}
		final String SearchKey = "searchVal";
		String value = loc + "::" + locType;
		
		Uri uri = Uri.parse("location://com.example.nyu.hw3api");
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		i.putExtra(SearchKey, value);
		try {
			startActivityForResult(i, SEARCH_LOC_REQUEST);
		} catch (ActivityNotFoundException ex) {
			Toast.makeText(CreateTripActivity.this, 
				"Location API not found!", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * This method should be used to instantiate a Trip model object.
	 * 
	 * @return The Trip as represented by the View.
	 */
	public Trip createTrip() {
		String trip_name = edit_tripName.getText().toString();
		String trip_creator_name = SharedPreferenceHelper.readString(CreateTripActivity.this, 
				SharedPreferenceHelper.TRIP_CREATOR_NAME, null);
		String trip_creator_email = SharedPreferenceHelper.readString(CreateTripActivity.this, 
				SharedPreferenceHelper.TRIP_CREATOR_EMAIL, null);
		if(trip_creator_name == null || trip_creator_email == null)
			return null;
		
		// Add the creator to the guests
		Person p_creator = new Person(trip_creator_name, trip_creator_email);
		guests.add(p_creator);
		
		Person[] guests_arr = (Person[])guests.toArray(new Person[guests.size()]);


		if (trip_name.length() < 3 || tv_loc.length() < 3
				|| tv_date.length() < 3 || tv_time.length() < 3 || guests.size()<1)
			return null;

		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day, hour, min);
		long trip_date = calendar.getTimeInMillis();
		Trip trip = new Trip("", trip_name, trip_creator_email, locInfo, trip_date, guests_arr);

		return trip;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		final String RetrieveVal = "retVal";
		
		if(resultCode == RESULT_CANCELED)
			return;
		
		if (requestCode == PICK_CONTACT_REQUEST) {
			// Get the URI that points to the selected contact
			Uri contactUri = data.getData();
			// We need the two columns, because there will be only two rows in the result
			String[] projection = { ContactsContract.Contacts.DISPLAY_NAME, 
					CommonDataKinds.Email.ADDRESS };

			/*
			 * Perform the query on the contact to get the DISPLAY_NAME and
			 * NUMBER columns. We don't need a selection or sort order (there's
			 * only one result for the given URI). CAUTION: The query() method
			 * should be called from a separate thread to avoid blocking your
			 * app's UI thread. (For simplicity of the sample, this code doesn't
			 * do that.) Consider using CursorLoader to perform the query.
			 */
			Cursor cursor = getContentResolver().query(contactUri, projection,
					null, null, null);
			cursor.moveToFirst();

			// Retrieve the email from the EMAIL column
			int column = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			String name = cursor.getString(column);
			column = cursor.getColumnIndex(CommonDataKinds.Email.ADDRESS);
			String email = cursor.getString(column);

			if (!guests.add(new Person(name, email)))
				Toast.makeText(this, "Contact already added",
						Toast.LENGTH_SHORT).show();
			showGuestList();
		}
		else if (requestCode == SEARCH_LOC_REQUEST) {
			ArrayList<String> locInfo_arrList = data.getStringArrayListExtra(RetrieveVal);
			locInfo = (String[])locInfo_arrList.
							toArray(new String[locInfo_arrList.size()]);
			
			tv_loc.setText("Trip location:\n");
			tv_loc.append(locInfo[0] + " " + locInfo[1]);
		}
	}

	/**
	 * This method prints the list of guests invited to the trip. It updates
	 * itself each time a new guest added.
	 */
	private void showGuestList() {
		Iterator<Person> it = guests.iterator();
		tv_guests.setText("Guests:\n");
		while (it.hasNext()) {
			Person p = it.next();
			tv_guests.append(p.getName() + "\n");
		}
	}

	/**
	 * For HW2 you should treat this method as a way of sending the Trip data
	 * back to the main Activity.
	 * 
	 * Note: If you call finish() here the Activity will end and pass an Intent
	 * back to the previous Activity using setResult().
	 * 
	 * @return whether the Trip was successfully saved.
	 */
	public boolean saveTrip(Trip trip) {
		new PostCreateTripToServerTask().execute(HttpRequestHelper.URL);
	
		return false;
	}

	private class PostCreateTripToServerTask extends AsyncTask <String, Void, String>{
	    
		@Override
		protected String doInBackground(String... params) {
	    	String jsonStr = null;
			try {
				jsonStr = HttpRequestHelper.makeServiceCall(
						 params[0], HttpRequestHelper.POST, myTrip.toJSON());
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
			return jsonStr;
		}

		@Override
	     protected void onPostExecute(String jsonResult) {
			if (jsonResult != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonResult);
                     
                    int response_code = -1;
                    String trip_id = null;
                    
                    if(jsonObj.has("response_code"))
                    	response_code = jsonObj.getInt("response_code");
                    if(response_code==0 && jsonObj.has("trip_id")) {
                    	trip_id = jsonObj.getString("trip_id");
                    	myTrip.setId(trip_id);
                    	
                    	TripDatabaseHelper trip_db = new TripDatabaseHelper(CreateTripActivity.this);
                		
                		// Insert trip into db
                		trip_db.insertTrip(myTrip);
                		
                		// Insert each guest into db
                		Iterator<Person> it = guests.iterator();
                		while (it.hasNext()) {
                			Person p = it.next();
                			trip_db.insertGuest(trip_id, p);
                		}
                		
                		setResult(RESULT_OK);
                		finish();
                    } else {
                    	Toast.makeText(CreateTripActivity.this, "Something went wrong, try again",
                    			Toast.LENGTH_LONG).show();
                    }
                    
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
	     }
	 }
	}
	
	/**
	 * This method should be used when a user wants to cancel the creation of a
	 * Trip.
	 * 
	 * Note: You most likely want to call this if your activity dies during the
	 * process of a trip creation or if a cancel/back button event occurs.
	 * Should return to the previous activity without a result using finish()
	 * and setResult().
	 */
	public void cancelTrip() {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setTitle("Confirmation")
				.setMessage("Are you sure you want to cancel the trip?")
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								cancelTrip();
							}

						}).create().show();
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
		   // set date picker as current date
		   return new DatePickerDialog(this, datePickerListener, 
                         year, month,day);
		case TIME_DIALOG_ID:
			   // set date picker as current date
			   return new TimePickerDialog(this, timePickerListener, 
	                        hour, min, true);
			
		}
		return null;
	}
 
	private DatePickerDialog.OnDateSetListener datePickerListener 
                = new DatePickerDialog.OnDateSetListener() {
 
		// when dialog box is closed, below method will be called.
		@Override
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			year = selectedYear;
			month = selectedMonth;
			day = selectedDay; 
			
			// set selected date into textview
			tv_date.setText(new StringBuilder().append("Trip date: ").
					append(month + 1).append("-").append(day)
					.append("-").append(year));
		}
	};
	
	private TimePickerDialog.OnTimeSetListener timePickerListener 
				= new TimePickerDialog.OnTimeSetListener() {

		// when dialog box is closed, below method will be called.
		@Override
		public void onTimeSet(TimePicker view, int selectedHour, int selectedMin) {
			hour = selectedHour;
			min = selectedMin;
			
			// set selected date into textview
			tv_time.setText(new StringBuilder().append("Trip time: ").
					append(hour).append(":").append(min));
		}
	};
}
