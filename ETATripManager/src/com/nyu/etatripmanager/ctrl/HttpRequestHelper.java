package com.nyu.etatripmanager.ctrl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.util.Log;

public class HttpRequestHelper {
	final static String TAG = "HttpRequestHelper";
	
	static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;
    
    public static final String URL = "http://meetingmanager-env.elasticbeanstalk.com/JSONServlet";
    
    public static final String JSON_CREATE_TRIP = "CREATE_TRIP";
    public static final String JSON_UPDATE_LOCATION = "UPDATE_LOCATION";
    public static final String JSON_TRIP_STATUS = "TRIP_STATUS";
    
    public HttpRequestHelper() {	 
    }
    
    /**
     * Making service call with no params
     * @url - url to make request
     * @method - http request method
     * */
    public static String makeServiceCall(String url, int method) {
        return makeServiceCall(url, method, null);
    }
    
    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     * */
    public static String makeServiceCall(String url, int method,
            JSONObject json /*List<NameValuePair> params*/) {
        try {
            // http client
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;
             
            // Checking http request method type
            if (method == POST) {
                HttpPost httpPost = new HttpPost(url);
                // adding post params
                if (json != null) {
                    httpPost.setEntity(new StringEntity(json.toString(), "UTF-8"));
                }
 
                httpResponse = httpClient.execute(httpPost);
 
            } /* 
            else if (method == GET) {
                // appending params to url
                if (json != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }
                HttpGet httpGet = new HttpGet(url);
 
                httpResponse = httpClient.execute(httpGet);
 
            } */
            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);
 
        } catch (UnsupportedEncodingException e) {
        	Log.e(TAG, e.getMessage());
        } catch (ClientProtocolException e) {
        	Log.e(TAG, e.getMessage());
        } catch (IOException e) {
        	Log.e(TAG, e.getMessage());
        }
         
        return response;
 
    }
}
