package com.nyu.etatripmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class JSONServlet
 */
@WebServlet("/JSONServlet")
public class JSONServlet extends HttpServlet {
	final static Logger logger = Logger.getLogger(JSONServlet.class);
	private static final long serialVersionUID = 1L;

	private static EtaDb db = new EtaDb();

	/***************************************************
	 * URL: /jsonservlet doPost(): receives JSON data, parse it, map it and send
	 * back as JSON
	 ****************************************************/
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();

		response.setContentType("application/json");

		int code = 0;
		
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) { /* report an error */
		}

		logger.info(jb.toString());
		JSONObject jsonObject = JSONObject.fromObject(jb.toString());
		logger.info(jsonObject.toString(4));

		
		String action = jsonObject.getString("command");
		if (action.equals("CREATE_TRIP")) {
			jsonObject.remove("command");
			String trip_id = db.insertTrip(jsonObject);

			if (trip_id == null) {
				code = -1;
			}
			JSONObject responseJSON = new JSONObject();
			responseJSON.put("response_code", code);
			responseJSON.put("trip_id", trip_id);
			out.println(responseJSON.toString());
		} else if (action.equals("UPDATE_LOCATION")) {
			jsonObject.remove("command");
			code = db.updateLocation(jsonObject);
			JSONObject responseJSON = new JSONObject();
			responseJSON.put("response_code", code);
			out.println(responseJSON.toString());
		} else if (action.equals("TRIP_STATUS")) {
			jsonObject.remove("command");
			String trip_id = jsonObject.getString("trip_id");
			String user = jsonObject.getString("email");

			JSONObject responseJSON = null;
			responseJSON = db.findEta(user, trip_id);
			responseJSON.put("response_code", code);
			out.println(responseJSON.toString());
		} else if (action.equals("TRIP_SYNC")) {
			jsonObject.remove("command");
			String user = jsonObject.getString("email");
			JSONObject responseJSON;
			responseJSON = db.getTrips(user);
			responseJSON.put("response_code", code);
			out.println(responseJSON.toString());
		}

		out.close();

	}
}
