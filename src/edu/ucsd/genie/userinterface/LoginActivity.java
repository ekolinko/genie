package edu.ucsd.genie.userinterface;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import edu.ucsd.genie.R;
import edu.ucsd.genie.connectionmanager.ConnectionManager;

/**
 * Activity for logging into BuildingDepot for reading environment conditions
 * and controlling the HVAC.
 */
public class LoginActivity extends Activity {
	
	/**
	 * User interface fields of the login screen.
	 */
	private EditText mUsernameField;
	private EditText mPasswordField;
	private TextView mErrorField;
	private Button mLoginButton;
	private ProgressBar mLoginProgress;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        mUsernameField = (EditText)findViewById(R.id.login_username);
        mPasswordField = (EditText)findViewById(R.id.login_password);
        mPasswordField.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				new LoginTask().execute();
				return true;
			}
		});
        mErrorField = (TextView)findViewById(R.id.login_error);
        mLoginButton = (Button)findViewById(R.id.login_button);
        mLoginProgress = (ProgressBar)findViewById(R.id.login_progress);
        mLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new LoginTask().execute();
			}
		});
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sharedPreferences.getString(getString(R.string.preferences_username), getString(R.string.empty));
        mUsernameField.setText(username);
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		menu.clear();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * Handle selection of menu items.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, Preferences.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
     * Task for logging into Genie.
     */
    private class LoginTask extends AsyncTask<Void, Void, ConnectionManager.Status> {
    	/**
    	 * The username with which to login.
    	 */
    	private String mUsername;
    	
    	/**
    	 * The password with which to login.
    	 */
    	private String mPassword;
    	
        @Override
        protected void onPreExecute() {
        	mUsername = mUsernameField.getText().toString();
			mPassword = mPasswordField.getText().toString();
			mLoginButton.setText(getString(R.string.logging_in));
			mLoginButton.setEnabled(false);
			mLoginProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected ConnectionManager.Status doInBackground(Void... params) {
        	ConnectionManager.Status loginStatus = ConnectionManager.getInstance(LoginActivity.this).login(mUsername, mPassword);
        	if (loginStatus == ConnectionManager.Status.SUCCESS) {
        		ConnectionManager.getInstance(LoginActivity.this).getRooms();
        		return ConnectionManager.getInstance(LoginActivity.this).getUserInformation();
        	} else
        		return loginStatus;
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPostExecute(ConnectionManager.Status status) {
        	mErrorField.setVisibility(View.VISIBLE);
        	mLoginButton.setText(getString(R.string.login_action));
        	mLoginButton.setEnabled(true);
        	mLoginProgress.setVisibility(View.GONE);
        	if (status == ConnectionManager.Status.SUCCESS) {
        		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        	    sharedPreferences.edit().putString(getString(R.string.preferences_username), mUsername).apply();
        		Intent intent = new Intent(LoginActivity.this, TabActivity.class);
        		startActivity(intent);
        		finish();
        	} else if (status == ConnectionManager.Status.ERROR_AUTHENTICATION_FAILURE || status == ConnectionManager.Status.ERROR_NOT_CONNECTED) {
        		mErrorField.setText(getString(R.string.login_error_authentication_failure));
        	} else {
        		mErrorField.setText(status.toString());
        	}
        }
    };
}
