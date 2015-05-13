package com.nyu.etatripmanager.login;
 
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.google.android.gms.plus.model.people.Person;
import com.nyu.etatripmanager.R;
import com.nyu.etatripmanager.ctrl.MainActivity;
import com.nyu.etatripmanager.ctrl.SharedPreferenceHelper;

 
public class LoginActivity extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener {
 
    private static final int RC_SIGN_IN = 0;
    // Logcat tag
    private static final String TAG = "LoginActivity";
  
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
 
    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;
 
    private boolean mSignInClicked;
 
    private ConnectionResult mConnectionResult;
 
    private SignInButton btnSignIn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
 
        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
 
        btnSignIn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				signInWithGplus();
			}
		});

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API, PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }
 
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
 
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
 
    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }
 
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }
 
        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;
 
            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all errors until user is signed in or they cancel.
                resolveSignInError();
            } else {
            	// Update the UI
            	showUI(true);
            }
        }
        
 
    }
 
    @Override
    protected void onActivityResult(int requestCode, int responseCode,
            Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }
 
            mIntentInProgress = false;
 
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }
 
    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();   
        
        // Update the UI after signin
        showUI(false);
        
        Person currentPerson = Plus.PeopleApi
                .getCurrentPerson(mGoogleApiClient);
        String personName = currentPerson.getDisplayName();
        String personEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);
 
        SharedPreferenceHelper.writeString(LoginActivity.this, 
        		SharedPreferenceHelper.TRIP_CREATOR_NAME, personName);
        SharedPreferenceHelper.writeString(LoginActivity.this, 
        		SharedPreferenceHelper.TRIP_CREATOR_EMAIL, personEmail);
        startMainActivity();
		finish();
    }
 
    /**
     * Updating the UI, showing/hiding buttons and profile layout
     * */
    private void showUI(boolean isSignedIn) {
        if (isSignedIn) {
            btnSignIn.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.GONE);
        }
    }
 

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
        showUI(true);
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
 
    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }
 
 
    private void startMainActivity() {
    	Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
    }
    
	@Override
	public void onBackPressed() {
		super.onBackPressed();
//		Intent intent = new Intent(Intent.ACTION_MAIN);
//		intent.addCategory(Intent.CATEGORY_HOME);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		startActivity(intent);
	}
}