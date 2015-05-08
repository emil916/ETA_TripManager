package com.nyu.etatripmanager.ctrl;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class UpdateTripService extends Service {
	private static final String TAG = "UpdateTripService";

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_FROM_SERVICE = 3;
	public static final int MSG_FROM_ACTIVITY = 4;
	
	public static boolean ALIVE = false;
	private BackgroundThread bg;
	private Messenger mClient = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Service started...");

		ALIVE = true;

		if (bg == null) {
			bg = new BackgroundThread();
			bg.start();
		}

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	private class BackgroundThread extends Thread {
		@Override
		public void run() {
			super.run();

			Log.i(TAG, "Background Thread Running");

			// do background task here!

			Log.i(TAG, "Background Thread Done");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) { // Message class
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClient = msg.replyTo; // messenger from component for us to use
				Log.i(TAG, "Client registered with service");
				break;
			case MSG_UNREGISTER_CLIENT:
				mClient = null; // set mClient to null because component is done
				Log.i(TAG, "Client unregistered from service");
				break;
			case MSG_FROM_ACTIVITY:
				// handle request from other component
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
}
