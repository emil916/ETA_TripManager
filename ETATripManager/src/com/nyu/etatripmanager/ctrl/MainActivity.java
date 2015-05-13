package com.nyu.etatripmanager.ctrl;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nyu.etatripmanager.R;
import com.nyu.etatripmanager.login.SessionActivity;
import com.nyu.etatripmanager.models.Person;
import com.nyu.etatripmanager.models.Trip;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	
	private static final String TAG = "MainActivity";
	LocationManager locationManager;
	MyLocationListener mylistener;
	boolean locRequested = false;
	private Button btn_go_create_trip, btn_go_view_trip, btn_delete_all_trips;
	TextView tv_activeTripInfo, tv_lat, tv_long;
	private final int CreateTripRequest = 1;
	private final int ViewTripRequest = 2;
	private final int SessionActRequest = 3;
	public static final int RESULT_LOGGED_OUT = 100;
	private final int REFRESH_INTERVAL = 10*000; // 10sec
	Trip active_trip = null;
	AsyncTask<String, Void, String[]> asyncTask;
	double lat_info = 0.0, long_info = 0.0;


	Handler handler = new Handler();
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			asyncTask = new PostToServerTask().execute(HttpRequestHelper.URL);
		}

	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		
		tv_activeTripInfo = (TextView)findViewById(R.id.tv_main_activeTripInfo);
		tv_lat = (TextView)findViewById(R.id.tv_main_lat);
		tv_long = (TextView)findViewById(R.id.tv_main_long);
		
		btn_go_create_trip = (Button)findViewById(R.id.button_main_createTrip);
		btn_go_view_trip = (Button)findViewById(R.id.button_main_viewTrip);
		btn_delete_all_trips = (Button)findViewById(R.id.button_main_deleteAllTrips);
		
		btn_events();
		
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mylistener = new MyLocationListener();

		Log.i(TAG, "onCreate");
		
//		if(UpdateTripService.ALIVE) 
//			doBindService();
	}

	/*
	private Messenger mService = null;  // Messenger for communicating with service. 
    boolean mIsBound; // Flag indicating whether we have called bind on the service. 

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UpdateTripService.MSG_FROM_SERVICE:
                    // handle request from service
                    break;
                default:
                    break;
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());
 
    
	private ServiceConnection mConnection = new ServiceConnection() {

	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service) {
	        mService = new Messenger(service);

	        Message msg = Message.obtain(null, UpdateTripService.MSG_REGISTER_CLIENT);
	        msg.replyTo = mMessenger;
	       
	        try {
	            mService.send(msg);
	        } catch (RemoteException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	    }

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void doBindService() {
		bindService(new Intent(MainActivity.this, UpdateTripService.class), 
				mConnection, 
				Context.BIND_AUTO_CREATE); // Create service if not exists already
	}
	
	void doUnbindService() {
	    if (mIsBound) {
	        // If we registered with the service, then now is the time to unregister.
	        if (mService != null) {
	             try {
	                Message msg = Message.obtain(null, UpdateTripService.MSG_UNREGISTER_CLIENT);
	                msg.replyTo = mMessenger;
	                mService.send(msg);
	            } catch (RemoteException e) {
	                // There is nothing special we need to do if the service has crashed.
	            }
	        }
	        // Detach our existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
//	    doUnbindService();
	}
*/
	
	/**
	 * This method sets onClick events for the buttons in the activity
	 */
	private void btn_events() {
		btn_go_create_trip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startCreateTripActivity();
			}
		});
		
		btn_go_view_trip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TripDatabaseHelper trip_db = new TripDatabaseHelper(MainActivity.this);
				if (trip_db.getAllTrips().size() > 0)
					startViewTripActivity();
				else
					Toast.makeText(MainActivity.this, "No trip to view", 
							Toast.LENGTH_SHORT).show();
				trip_db.close();
			}
		});
		
		btn_delete_all_trips.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final TripDatabaseHelper trip_db = new TripDatabaseHelper(MainActivity.this);
				if (trip_db.getAllTrips().size() > 0) {
					new AlertDialog.Builder(MainActivity.this)
				.setTitle("Confirmation")
				.setMessage("Are you sure you want to delete all trips?")
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,	int which) {
								trip_db.deleteAllTrips();
								if(asyncTask!=null && !asyncTask.isCancelled()) {
									asyncTask.cancel(true);
								}
								handler.removeCallbacks(runnable);
								SharedPreferenceHelper.removeKey(
										MainActivity.this, SharedPreferenceHelper.ACTIVE_TRIP_ID);
								SharedPreferenceHelper.removeKey(
										MainActivity.this, SharedPreferenceHelper.I_ARRIVED);
								tv_activeTripInfo.setText("No active trip");
								tv_lat.setText("");
								tv_long.setText("");
								Toast.makeText(MainActivity.this, "All trips are deleted!", 
										Toast.LENGTH_LONG).show();
							}

						}).create().show();
				} else
					Toast.makeText(MainActivity.this, "No trip to delete", 
							Toast.LENGTH_SHORT).show();
				
				trip_db.close();
			}
		});
	}
		
	/**
	 * This method should start the
	 * Activity responsible for creating
	 * a Trip.
	 */
	public void startCreateTripActivity() {
		Intent intent = new Intent(this, CreateTripActivity.class);
		startActivityForResult(intent, CreateTripRequest);
	}
	
	/**
	 * This method should start the
	 * Activity responsible for viewing
	 * a Trip.
	 */
	public void startViewTripActivity() {
		Intent intent = new Intent(this, TripHistoryActivity.class);
		startActivity(intent);
	}

	
	@Override
	public void onPause() {
		super.onPause();
		setLocationStuff(false);
		if(asyncTask!=null && !asyncTask.isCancelled()) {
			asyncTask.cancel(true);
		}
		handler.removeCallbacks(runnable);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		String trip_id = SharedPreferenceHelper.readString(
				MainActivity.this, SharedPreferenceHelper.ACTIVE_TRIP_ID, null);
		if (trip_id == null) {
			handler.removeCallbacks(runnable);
			tv_activeTripInfo.setText("No active trip");
			tv_lat.setText("");
			tv_long.setText("");
			return;
		}
		TripDatabaseHelper trip_db = new TripDatabaseHelper(MainActivity.this);
		active_trip = trip_db.getTrip(trip_id);
		trip_db.close();
		
		if(active_trip == null)
			return;
//		if(!UpdateTripService.ALIVE) 
//			startService(new Intent(MainActivity.this, UpdateTripService.class));
		if(!SharedPreferenceHelper.readBoolean(
						MainActivity.this, SharedPreferenceHelper.I_ARRIVED, false))
			setLocationStuff(true);
		handler.post(runnable);
	}
	
private class PostToServerTask extends AsyncTask <String, Void, String[]>{
	boolean isArrived;
	
		@Override
		protected String[] doInBackground(String... params) {
	    	String jsonTripStatusResultStr = null ,jsonUpdateLocationResultStr = null;
	    	
			try {
				JSONObject jsonTripStatusObj = new JSONObject();
				jsonTripStatusObj.put("command", HttpRequestHelper.JSON_TRIP_STATUS);
				jsonTripStatusObj.put("trip_id", active_trip.getId());
				
				jsonTripStatusResultStr = HttpRequestHelper.makeServiceCall(
						 HttpRequestHelper.URL, HttpRequestHelper.POST, jsonTripStatusObj);
				
				isArrived = SharedPreferenceHelper.readBoolean(
						MainActivity.this, SharedPreferenceHelper.I_ARRIVED, false);
				
				if (!isArrived) {
					JSONObject jsonUpdateLocationObj = new JSONObject();
					jsonUpdateLocationObj.put("command", HttpRequestHelper.JSON_UPDATE_LOCATION);
					jsonUpdateLocationObj.put("latitude", lat_info);
					jsonUpdateLocationObj.put("longitude", long_info);
					jsonUpdateLocationObj.put("datetime",System.currentTimeMillis());
					
//					if(isArrived = SharedPreferenceHelper.readBoolean(
//						MainActivity.this, SharedPreferenceHelper.I_ARRIVED, false)) {
//						jsonUpdateLocationObj.put("latitude", active_trip.getLocation()[2]);
//						jsonUpdateLocationObj.put("longitude", active_trip.getLocation()[3]);
//					}
					jsonUpdateLocationResultStr = HttpRequestHelper
							.makeServiceCall(HttpRequestHelper.URL,	HttpRequestHelper.POST,
									jsonUpdateLocationObj);
				
				} 
				
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
			
			String[] resArray = {jsonTripStatusResultStr, jsonUpdateLocationResultStr}; 
			return resArray;
		}

		@Override
	     protected void onPostExecute(String[] jsonResultArray) {
			if (jsonResultArray != null) {
				String jsonTripStatusResultStr = jsonResultArray[0];
				String jsonUpdateLocationResultStr = jsonResultArray[1]; 
				
				try {	
				JSONObject jsonTripStatusResultObj = new JSONObject(jsonTripStatusResultStr);

					if (jsonTripStatusResultObj.has("people")) {
						JSONArray j_people_arr = jsonTripStatusResultObj.getJSONArray("people");
						int suggested_time = jsonTripStatusResultObj.getInt("suggested_time_to_leave");
						
						
						
						StringBuilder sb = new StringBuilder();
						sb.append("Active trip: ").append(active_trip.getName())
						.append("\n Location: ").append(active_trip.getLocation()[0]);
						
						TripDatabaseHelper trip_db = new TripDatabaseHelper(MainActivity.this);
						

						
						
						for (int i=0; i<j_people_arr.length(); i++) {
							JSONObject j_person = (JSONObject)j_people_arr.get(i);
							String email = j_person.getString("email");
							Double distance_left = j_person.getDouble("distance_left");
							Double lat = j_person.getDouble("latitude");
							Double lng = j_person.getDouble("longitude");
							int time_left = j_person.getInt("time_left");
							long timestamp = j_person.getLong("datetime");
							
							Person person = trip_db.getPerson(active_trip.getId(), email);
							if(person == null)
								person = new Person("Not found", email);
							
							sb.append("\n" + Integer.toString(i+1)+". Guest name: " + person.getName())
							.append("\n   - Guest email: " + person.getEmail())
							.append("\n   - Distance left: " + distance_left + "mls.")
							.append("\n   - Time left: " + time_left + "min.")
							.append("\n   - Suggested time to leave: after " + suggested_time + "min.");
						}
						
						if (!isArrived) {
							JSONObject jsonUpdateLocationResultObj = new JSONObject(jsonUpdateLocationResultStr);
							int response_code = -1;
							if (jsonUpdateLocationResultObj.has("response_code"))
								response_code = jsonUpdateLocationResultObj.getInt("response_code");
							if (response_code == 0) {
								sb.append("\n\n <Updated your location successfuly>");
								tv_lat.setText("Lat: " + Double.toString(lat_info));
								tv_long.setText("Long: " + Double.toString(long_info));
							}
						} else {
							sb.append("\n\n <You have arrived at the destination>");
						}
						
						tv_activeTripInfo.setText(sb.toString());
						
						trip_db.close();
						
						handler.postDelayed(runnable, REFRESH_INTERVAL); 
						
					} else {
						Toast.makeText(MainActivity.this,
								"Something went wrong, try again",
								Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
				}
	     
	 }
	}
	}

	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			// Initialize the location fields
			lat_info = location.getLatitude();
			long_info = location.getLongitude();

//			Toast.makeText(MainActivity.this, "Location changed!",
//					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
//			Toast.makeText(MainActivity.this,
//					provider + "'s status changed to " + status + "!",
//					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(MainActivity.this,
					"Provider " + provider + " enabled!", Toast.LENGTH_SHORT)
					.show();

		}

		@Override
		public void onProviderDisabled(String provider) {
//			Toast.makeText(MainActivity.this,
//					"Provider " + provider + " disabled!", Toast.LENGTH_SHORT)
//					.show();
		}
	}

	private void setLocationStuff(boolean switchState) {		
		if(switchState) {
		  // Define the criteria how to select the location provider
		  Criteria criteria = new Criteria();
		  criteria.setAccuracy(Criteria.ACCURACY_FINE);	//default
		  
		  criteria.setCostAllowed(false); 
		  // get the best provider depending on the criteria
		  String provider = locationManager.getBestProvider(criteria, false);
	    
		  // the last known location of this provider
		  Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);		  
	
		  if (location != null) {
			  mylistener.onLocationChanged(location);
		  } else if(!locRequested){
			  // leads to the settings because there is no last known location
			  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			  startActivity(intent);
			  locRequested = true;
		  }
		  // location updates: at least 5 meter and 10secs change
		  locationManager.requestLocationUpdates(provider, 10000, 5, mylistener);
		} else {
			locationManager.removeUpdates(mylistener);
		}
	}
	
	/**
	 * Receive result from CreateTripActivity here.
	 * Can be used to save instance of Trip object
	 * which can be viewed in the ViewTripActivity.
	 * 
	 * Note: This method will be called when a Trip
	 * object is returned to the main activity. 
	 * Remember that the Trip will not be returned as
	 * a Trip object; it will be in the persisted
	 * Parcelable form. The actual Trip object should
	 * be created and saved in a variable for future
	 * use, i.e. to view the trip.
	 * 
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CreateTripRequest:
			if(resultCode == RESULT_CANCELED)
				Toast.makeText(this, "Trip canceled", Toast.LENGTH_SHORT).show();
			else if (resultCode == RESULT_OK) {
				Toast.makeText(this, "Trip created successfully", 
				Toast.LENGTH_LONG).show();
			}
			break;
		case ViewTripRequest:
			break;
		case SessionActRequest:
			if(resultCode == RESULT_LOGGED_OUT){
				finish();
			}
		default:
			break;
		}
			
	}
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager
				.beginTransaction()
				.replace(R.id.container,
						PlaceholderFragment.newInstance(position + 1)).commit();
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		}
	}

	@SuppressWarnings("deprecation")
	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SessionActivity.class);
			startActivityForResult(intent, SessionActRequest);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}

}
