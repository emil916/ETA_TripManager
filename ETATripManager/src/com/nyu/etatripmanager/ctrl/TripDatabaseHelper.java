/**
 * 
 */
package com.nyu.etatripmanager.ctrl;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nyu.etatripmanager.models.*;

public class TripDatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "trips";

	private static final String TABLE_TRIP = "trip";
	private static final String COLUMN_TRIP_ID = "_id"; // convention
	private static final String COLUMN_TRIP_NAME = "t_name";
	private static final String COLUMN_TRIP_DATE = "date";
	private static final String COLUMN_TRIP_LOC_NAME = "location";
	private static final String COLUMN_TRIP_LOC_ADDRESS = "address";
	private static final String COLUMN_LOC_LAT = "latitude";
	private static final String COLUMN_LOC_LONG = "longitude";

	private static final String TABLE_GUEST = "guest";
	private static final String COLUMN_TRIPID = "trip_id";
	private static final String COLUMN_NAME = "p_name";
	private static final String COLUMN_PHONE = "phone";

	/**
	 * Public constructor
	 * 
	 * @param context
	 *            The context parameter
	 */
	public TripDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create trip table
		db.execSQL("create table " + TABLE_TRIP + "(" 
				+ COLUMN_TRIP_ID + " integer primary key, " //autoincrement, " 
				+ COLUMN_TRIP_NAME + " varchar(50), "
				+ COLUMN_TRIP_DATE + " bigint, " 
				+ COLUMN_TRIP_LOC_NAME + " varchar(50), "
				+ COLUMN_TRIP_LOC_ADDRESS + " varchar(100), " 
				+ COLUMN_LOC_LAT + " varchar(50), " 
				+ COLUMN_LOC_LONG + " varchar(50))");

		// create guest table
		db.execSQL("create table " + TABLE_GUEST + "(" 
				+ COLUMN_TRIPID	+ " integer references trip(_id) ON DELETE CASCADE, " 
				+ COLUMN_NAME + " varchar(50), " 
				+ COLUMN_PHONE + " varchar(50))");
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		/*
		 * This has to done every time a database is called
		 * in SQLite, otherwise Foreign Key Constraints won't work 
		 */
		db.execSQL("PRAGMA foreign_keys=ON");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if exists
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GUEST);

		// create tables again
		onCreate(db);
	}

	/**
	 * This method inserts the given trip into the db.
	 * @param trip the given trip
	 * @return the row ID of the newly inserted row, 
	 * or -1 if an error occurred 
	 */
	public long insertTrip(Trip trip) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_TRIP_ID, trip.getId());
		cv.put(COLUMN_TRIP_NAME, trip.getName());
		cv.put(COLUMN_TRIP_DATE, trip.getDate());
		cv.put(COLUMN_TRIP_LOC_NAME, trip.getLocation()[0]);
		cv.put(COLUMN_TRIP_LOC_ADDRESS, trip.getLocation()[1]);
		cv.put(COLUMN_LOC_LAT, trip.getLocation()[2]);
		cv.put(COLUMN_LOC_LONG, trip.getLocation()[3]);
		
		// return id of new trip
		return getWritableDatabase().insert(TABLE_TRIP, null, cv);
	}

	/**
	 * This method inserts the given person with the 
	 * given trip ID into the database.
	 * @param tripId the given trip
	 * @param person the given person to be inserted
	 * @return the row ID of the newly inserted row, 
	 * or -1 if an error occurred 
	 */
	public long insertGuest(long tripId, Person person) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_TRIPID, tripId);
		cv.put(COLUMN_NAME, person.getName());
		cv.put(COLUMN_PHONE, person.getPhone());

		// return id of new person
		return getWritableDatabase().insert(TABLE_GUEST, null, cv);
	}
	
	public Trip getTrip(long trip_id) {	
		Trip trip = null;
		
		SQLiteDatabase db = this.getReadableDatabase();
	    Cursor cursor = db.rawQuery(
	    		"select * from " + TABLE_TRIP
	    		+ " where " + COLUMN_TRIP_ID + "=" + trip_id, null);
	    
	    if(cursor.getCount() > 0) {
			cursor.moveToFirst();
			String[] loc_arr = { cursor.getString(3), cursor.getString(4),
					cursor.getString(5), cursor.getString(6) };
			Person[] guests_arr = getAllGuestsPerTrip(cursor.getLong(0));

			trip = new Trip(cursor.getLong(0), cursor.getString(1), loc_arr,
					cursor.getLong(2), guests_arr);
		}
		
		cursor.close();
		db.close();
		
		return trip;
	}
	/**
	 * This method gets all the trips created.
	 * @return an arraylist of all the created trips
	 */
	public List<Trip> getAllTrips() {
	    List<Trip> tripList = new ArrayList<Trip>();
	 
	    SQLiteDatabase db = this.getReadableDatabase();
	    Cursor cursor = db.rawQuery("select * from " + TABLE_TRIP, null);
	 
	    // loop through all query results
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			String[] loc_arr = { cursor.getString(3), cursor.getString(4),
					cursor.getString(5), cursor.getString(6) };
			Person[] guests_arr = getAllGuestsPerTrip(cursor.getLong(0));

			Trip trip = new Trip(cursor.getLong(0), cursor.getString(1), loc_arr,
					cursor.getLong(2), guests_arr);
			tripList.add(trip);
		}

	    return tripList;
	}
	
	/**
	 * This method gets all the guests that are 
	 * invited to the given trip.
	 * @param tripId the given trip
	 * @return an array of invited guests
	 */
	public Person [] getAllGuestsPerTrip(long tripId) {
	    List<Person> guests = new ArrayList<Person>();
	 
	    SQLiteDatabase db = this.getReadableDatabase();
	    Cursor cursor = db.rawQuery("select * from " + TABLE_GUEST
	    		+ " where " + COLUMN_TRIPID + "=" + tripId, null);

		// loop through all query results:
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Person p = new Person(cursor.getString(1), cursor.getString(2));
			guests.add(p);
		}
		
		// Convert guests arraylist into an array:
		Person[] guests_arr = (Person[])guests.toArray(new Person[guests.size()]);
		
		return guests_arr;
		
	}
	
	/**
	 * This method deletes all rows in both trip
	 * and guest tables.
	 */
	public void deleteAllTrips() {
		SQLiteDatabase db = this.getWritableDatabase();		
		db.execSQL("delete from " + TABLE_TRIP);
	}

}
