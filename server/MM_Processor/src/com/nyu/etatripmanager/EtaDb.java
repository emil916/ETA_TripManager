package com.nyu.etatripmanager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;




public class EtaDb {
	
	public class EtaInfo {
		public String dist = "Unknown";
		public String duration = "Unknown";
		public Integer durationUnits = -1;
	};
	
	private MongoClient mongoClient;
	private String userName;
	private String database;
	private char[] password;
	private DBCollection tripColl, locationColl;
	final static Logger logger = Logger.getLogger(EtaDb.class);
	private final String USER_AGENT = "Mozilla/5.0";
	
	/**
	 * @throws UnknownHostException
	 * 
	 */
	public EtaDb() {

		// MongoCredential credential =
		// MongoCredential.createCredential(userName, database, password);
		//
		// mongoClient = new MongoClient(new ServerAddress(),
		// Arrays.asList(credential));

		// To directly connect to a single MongoDB server (note that this will
		// not auto-discover the primary even
		// if it's a member of a replica set:
		try {
			mongoClient = new MongoClient("52.0.227.218");
			//mongoClient = new MongoClient();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		// or
		// MongoClient mongoClient = new MongoClient( "localhost" );
		// or
		// MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		// or, to connect to a replica set, with auto-discovery of the primary,
		// supply a seed list of members

		// MongoClient mongoClient = new MongoClient(Arrays.asList(new
		// ServerAddress("localhost", 27017),
		// new ServerAddress("localhost", 27018),
		// new ServerAddress("localhost", 27019)));
		//
		DB db = mongoClient.getDB("etatripmanager");
		tripColl = db.getCollection("trips");
		locationColl = db.getCollection("location");

	}

	public String insertTrip(JSONObject tripData) {
		Object tripObject = com.mongodb.util.JSON.parse(tripData.toString());
		DBObject tripJDBObj = (DBObject) tripObject;

		tripColl.insert(tripJDBObj);
		ObjectId id = (ObjectId) tripJDBObj.get("_id");
		return id.toString();
	}

	public int updateLocation(JSONObject jsonObject) {
		Object locationObject = com.mongodb.util.JSON.parse(jsonObject.toString());
		DBObject tripJDBObj = (DBObject) locationObject;
		
		
		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("email", jsonObject.get("email"));
		DBCursor cursor = locationColl.find(whereQuery);

		while(cursor.hasNext()) {
			DBObject loc = cursor.next();
			locationColl.remove(loc);
		}
		
		locationColl.insert(tripJDBObj);
		
		return 0;
	}
	
	private EtaInfo calculateEta(String origin, String destination) throws Exception {
		 
		String url = "https://maps.googleapis.com/maps/api/distancematrix/json??key=AIzaSyDDmIEcWMd1569TJgr2oELCSHcMOKyD4rU";
 
		url = url + "&origins=" + origin + "&destinations=" + destination;
		
		url = url + "&units=imperial";
		
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		
		logger.info(response.toString());
		JSONObject jsonObject = JSONObject.fromObject(response.toString());
		
		//logger.info("Obj" + jsonObject.toString(4));
		
		JSONArray row = jsonObject.getJSONArray("rows");
		//logger.info("Row: "+ row.toString());
		
		JSONObject elements = row.getJSONObject(0);
		JSONArray element = elements.getJSONArray("elements");
		
		//logger.info("Element: "+ element.toString(4));
		
		JSONObject element1 = element.getJSONObject(0);
		
		String status = element1.getString("status");

		EtaInfo etaInfo = new EtaInfo();

		if(status.equals("ZERO_RESULTS")) {
			etaInfo.dist = "Unknown";
			etaInfo.duration = "Unknown";
			etaInfo.durationUnits = -1;
			return etaInfo;
		}
		
		JSONObject distance = element1.getJSONObject("distance");
		String dist = distance.getString("text");
		
		JSONObject duration = element1.getJSONObject("duration");
		String dur = duration.getString("text");
		
		Integer durUnits = duration.getInt("value");
		
		logger.info("Distance: "+ distance.toString(4));
		logger.info("Duration: "+ duration.toString(4));
		//logger.info("Distance: " + dist);
		//logger.info("Duration: " + dur);
		
		in.close();
		etaInfo.dist = dist;
		etaInfo.duration = dur;
		etaInfo.durationUnits = durUnits;
		
		return etaInfo;
	}
	
	public JSONObject findEta(String user, String trip_id) {
		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("_id", new ObjectId(trip_id));
		BasicDBObject fields = new BasicDBObject();
		fields.put("people", 1);
		fields.put("location", 1);
		DBCursor cursor = tripColl.find(whereQuery, fields);
		Integer rUserTime = -1;
		Integer totalDuration = 0;
		Integer numPeople = 0;
		
		JSONArray etaTrip = new JSONArray();

		if (cursor.hasNext()) {
			DBObject cursor1 = cursor.next();
			logger.info(cursor1);
			
			JSONArray locationArray = JSONArray.fromObject(cursor1.get("location"));
			
			JSONArray peopleList = JSONArray.fromObject(cursor1.get("people"));
			
			String lat = locationArray.get(2).toString();
			String lng = locationArray.get(3).toString();
			String destination = lat + "," + lng;
			
			logger.info("Destination: " + destination);
			logger.info("People List: " + peopleList.toString());

			EtaInfo etaInfo = null;
			
			for (int i = 0; i < peopleList.size(); i++) {

				JSONObject person_eta = new JSONObject();

				BasicDBObject whereQuery2 = new BasicDBObject();
				whereQuery2.put("email", peopleList.get(i));
				BasicDBObject fields2 = new BasicDBObject();
				fields.put("latitude", 1);
				fields.put("longitude", 1);
				fields.put("datetime", 1);
				DBCursor cursor2 = locationColl.find(whereQuery2, fields2);
				
				if (cursor2.hasNext()) {
					DBObject cursorT = cursor2.next();
					logger.info(cursorT);
					
					String latD = cursorT.get("latitude").toString();
					String lngD = cursorT.get("longitude").toString();
					String origin = latD + "," + lngD;
					
					try {
						etaInfo = calculateEta(origin, destination);
					} catch (Exception e) {
						logger.error(e);
					}
					
					person_eta.put("email", peopleList.get(i));
					person_eta.put("latitude", cursorT.get("latitude"));
					person_eta.put("longitude", cursorT.get("longitude"));
					person_eta.put("datetime", cursorT.get("datetime"));
					person_eta.put("distance_left", etaInfo.dist);
					person_eta.put("time_left", etaInfo.duration);
					
					
					if(peopleList.get(i).toString().toLowerCase().equals(user.toLowerCase())) {
						rUserTime = etaInfo.durationUnits;
					} else {
						totalDuration = totalDuration + etaInfo.durationUnits;
						numPeople++;
					}
					
				} else {
					person_eta.put("email", peopleList.get(i));
					person_eta.put("latitude", 0);
					person_eta.put("longitude", 0);
					person_eta.put("datetime", 0);
					person_eta.put("distance_left", "Unknown");
					person_eta.put("time_left", "Unknown");
				}
				etaTrip.add(person_eta);
			}
		}
		
		Integer suggestedTime = 0;
		if(numPeople > 0) {
			suggestedTime = totalDuration / numPeople - rUserTime;
		}
		if ( suggestedTime < 0) {
			suggestedTime = 0;
		}
		JSONObject eta_response = new JSONObject();
		eta_response.put("suggested_time_to_leave", suggestedTime);
		eta_response.put("people", etaTrip);
		
		logger.info(eta_response);

		return eta_response;
	}

	public JSONObject getTrips(String user) {
		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("people", user);
		//BasicDBObject fields = new BasicDBObject();
		DBCursor cursor = tripColl.find(whereQuery);
		
		JSONArray trips = new JSONArray();
		
		while (cursor.hasNext()) {
			DBObject cursor1 = cursor.next();
			logger.info(cursor1);
			JSONObject trip = JSONObject.fromObject(cursor1);
			trip.remove("_id");
			trip.put("id", cursor1.get("_id").toString());
			
			trips.add(trip);
		}

		JSONObject tripResponse = new JSONObject();
		tripResponse.put("trips", trips);
		
		logger.info(tripResponse);
		
		return tripResponse;
	}

}
