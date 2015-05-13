package com.nyu.etatripmanager.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.nyu.etatripmanager.ctrl.HttpRequestHelper;

public class Trip implements Parcelable {
	
	// Member fields should exist here, what else do you need for a trip?
	// Please add additional fields
	private String id;			// Trip ID
	private String name;		// Trip name
	private String trip_creator; // Trip creator email
	private String[] location;	// Trip location {loc name, address, lat, long}
	private long date;			// Trip date
	private Person[] guests;	// Trip guests

	
	/**
	 * Parcelable creator. Do not modify this function.
	 */
	public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
		public Trip createFromParcel(Parcel p) {
			return new Trip(p);
		}

		public Trip[] newArray(int size) {
			return new Trip[size];
		}
	};
	
	/**
	 * Create a Trip model object from a Parcel. This
	 * function is called via the Parcelable creator.
	 * 
	 * @param p The Parcel used to populate the
	 * Model fields.
	 */
	public Trip(Parcel p) {
		id = p.readString();
		name = p.readString();
		trip_creator = p.readString();
		location = new String[4];
		p.readStringArray(location);
		date = p.readLong();
		guests = (Person[])p.createTypedArray(Person.CREATOR);
	}
	
	/**
	 * Create a Trip model object from arguments
	 * 
	 * @param name  Add arbitrary number of arguments to
	 * instantiate Trip class based on member variables.
	 */
	public Trip(String Id, String Name, String TripCreator, String[] Loc, 
			long DAte, Person [] Guests) {
		this.id = Id;
		this.name = Name;
		this.trip_creator = TripCreator;
		this.location = Loc;
		this.date = DAte;
		this.guests = Guests;
	}

	/**
	 * Serialize Trip object by using writeToParcel. 
	 * This function is automatically called by the
	 * system when the object is serialized.
	 * 
	 * @param dest Parcel object that gets written on 
	 * serialization. Use functions to write out the
	 * object stored via your member variables. 
	 * 
	 * @param flags Additional flags about how the object 
	 * should be written. May be 0 or PARCELABLE_WRITE_RETURN_VALUE.
	 * In our case, you should be just passing 0.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(trip_creator);
		dest.writeStringArray(location);
		dest.writeLong(date);
		dest.writeTypedArray(guests, 0);
	}
	
	public JSONObject toJSON() throws JSONException {
		List<String> locList = Arrays.asList(location);

        JSONObject json = new JSONObject();
        json.put("command", HttpRequestHelper.JSON_CREATE_TRIP);
        json.put("email", trip_creator);
        json.put("location", new JSONArray(locList));
        json.put("datetime", date);
        json.put("people", new JSONArray(getGuestEmails()));
        return json;
    }
	
	/**
	 * Feel free to add additional functions as necessary below.
	 */
	
	/** Public getters */
	public String getId() {
		return id;
	}
	public void setId(String Id) {
		this.id = Id;
	}
	public String getName() {
		return name;
	}
	public String getTripCreator() {
		return trip_creator;
	}
	public String[] getLocation() {
		return location;
	}
	public long getDate() {
		return date;
	}
	public Person [] getGuests() {
		return guests;
	}
	public ArrayList<String> getGuestNames() {
		ArrayList<String> list = new ArrayList<String>();
		for (Person p : guests)
			list.add(p.getName());
		return list;
	}
	
	public ArrayList<String> getGuestEmails() {
		ArrayList<String> list = new ArrayList<String>();
		for (Person p : guests)
			list.add(p.getEmail());
		return list;
	}
	
	/**
	 * Do not implement
	 */
	@Override
	public int describeContents() {
		// Do not implement!
		return 0;
	}
}
