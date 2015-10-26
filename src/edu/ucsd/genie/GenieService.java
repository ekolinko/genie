package edu.ucsd.genie;

import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import edu.ucsd.genie.connectionmanager.ConnectionManager;
import edu.ucsd.genie.datamanager.DataManager;
import edu.ucsd.genie.datamanager.datastructures.SensorData;
import edu.ucsd.genie.userinterface.TabActivity;

public class GenieService extends Service {
    private NotificationManager mNotificationManager;

    /**
     * The different scan modes associated with this application.
     */
    private enum ScanMode {
    	NETWORK, GPS, WIFI
    }
    
    /**
     * Unique identification ID associated with this service.
     */
    private int NOTIFICATION_ID = 45438180;
    
    /**
     * Invalid HVAC status.
     */
    private final int INVALID_HVAC_STATUS = -1;
    
    /**
     * The static reference to this class.
     */
    private static GenieService mService;
    
    /**
     * The binder that receives interactions from clients.
     */
    private final IBinder mBinder = new LocalBinder();
    
    /**
	 * Timer for continuously updating the view.
	 */
	private Timer mTimer;
    
    /**
     * The main activity responsible for refreshing the data for all the views. Used for handling data callbacks.
     */
    private TabActivity mActivity;
    
    /**
     * The location of the building geofence when WiFi is turned on.
     */
    private RectF mWifiGeofence;
    
    /**
     * The location of the geofence where GPS is turned off and Network location is enabled.
     */
    private RectF mGpsGeofence;
    
    /**
	 * The manager for communication with the Android location services.
	 */
	private LocationManager mLocationManager;
    
    /**
	 * Listener that listens to location changes and determines whether the device is in the specified geofence.
	 */
	private LocationListener mLocationListener;
	
	/**
	 * The WiFi manager used for controlling the WiFi connection.
	 */
	private WifiManager mWifiManager;
	
	/**
	 * The current scan mode of the application.
	 */
	private ScanMode mScanMode = ScanMode.WIFI;
	
	/**
	 * Flag indicating whether location services have been enabled.
	 */
	private boolean mLocationServicesEnabled = false;
	
	/**
	 * Flag indicating whether network update services have been enabled.
	 */
	private boolean mNetworkServicesEnabled = false;
	
	/**
	 * The name of the building access point.
	 */
	private String mBuildingAccessPointName;
	
	/**
	 * Flag indicating whether the WiFi was force enabled by Genie.
	 */
	private boolean mWifiForceEnabled = false;
	
	/**
	 * Thread responsible for checking if a specified access point is no longer seen.
	 */
	private Timer mWifiScannerTimer;
	
    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mBuildingAccessPointName = getString(R.string.building_access_point);
        
		// Setup the WiFi geofence (switches from GPS to WiFi)
		float geofenceX = getResources().getDimension(R.dimen.geofence_x);
		float geofenceY = getResources().getDimension(R.dimen.geofence_y);
		float radius = getResources().getDimension(R.dimen.geofence_radius);
		float left = geofenceX - radius;
		float right = geofenceX + radius;
		float top = geofenceY - radius;
		float bottom = geofenceY + radius;
		mWifiGeofence = new RectF(left, top, right, bottom);
		
		// Setup the GPS geofence (switches from GPS to network)
		geofenceX = getResources().getDimension(R.dimen.geofence_x);
		geofenceY = getResources().getDimension(R.dimen.geofence_y);
		radius = getResources().getDimension(R.dimen.geofence_gps_radius);
		left = geofenceX - radius;
		right = geofenceX + radius;
		top = geofenceY - radius;
		bottom = geofenceY + radius;
		mGpsGeofence = new RectF(left, top, right, bottom);
		
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		Settings.System.putInt(getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_NEVER);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocationListener = new GenieLocationListener();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	boolean enableBackgroundServices = preferences.getBoolean(getResources().getString(R.string.preferences_key_enable_background_services), getResources().getBoolean(R.bool.preferences_enable_background_services));
    	if (enableBackgroundServices) {
    		enableNetworkServices();
    		enableLocationServices();
    	} else if (isInForeground())
    		enableNetworkServices();
    	mService = this;
    }

	@Override
    public void onDestroy() {
        mNotificationManager.cancel(NOTIFICATION_ID);
        stopForeground(true);
        disableLocationServices();
        disableNetworkServices();
        mService = null;
    }
    
    /**
     * The static reference to this service.
     */
    public static GenieService getService() {
    	return mService;
    }
    
    /**
     * Enable the network services.
     */
    public void enableNetworkServices() {
    	if (!mNetworkServicesEnabled) {
    		if (mTimer != null)
    			mTimer.cancel();
            mTimer = new Timer();
    		new Thread(new GetRoomInformationTask()).start();
    		mTimer.scheduleAtFixedRate(new UpdateTask(), 0, getResources()
    				.getInteger(R.integer.home_ui_update_interval));
    		mNetworkServicesEnabled = true;
    	}
    }
    
    /**
     * Disable the network services.
     */
    public void disableNetworkServices() {
    	if (mNetworkServicesEnabled) {
    		if (mTimer != null)
    			mTimer.cancel();
    		mNetworkServicesEnabled = false;
    	}
    }
    
    /**
     * Enable the location services. Turn on the WiFi, GPS, or network listeners accordingly.
     */
    public void enableLocationServices() {
    	if(!mLocationServicesEnabled) {
	    	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					getResources().getInteger(R.integer.gps_refresh_interval), 0, mLocationListener);
			IntentFilter filter = new IntentFilter();
			filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			registerReceiver(mWifiListener, filter);
			if (mWifiManager.isWifiEnabled())
				switchScanMode(ScanMode.WIFI);
			else
				switchScanMode(ScanMode.GPS);
			mLocationServicesEnabled = true;
    	}
    }
    
    /**
     * Disable the location services. Turn off the WiFi, GPS, or network listeners accordingly.
     */
    public void disableLocationServices() {
    	if(mLocationServicesEnabled) {
	    	mLocationManager.removeUpdates(mLocationListener);
	    	unregisterReceiver(mWifiListener);
	    	mLocationServicesEnabled = false;
    	}
    }
    
    /**
     * Enable the WiFi scanner thread that is responsible for checking if a specified access point is not longer seen.
     */
    public void enableWifiScanner() {
    	if (mWifiScannerTimer != null)
    		mWifiScannerTimer.cancel();
    	mWifiScannerTimer = new Timer();
    	mWifiScannerTimer.scheduleAtFixedRate(new WifiScannerTask(), getResources().getInteger(R.integer.wifi_scanner_update_interval), getResources().getInteger(R.integer.wifi_scanner_update_interval));
    }
    
    /**
     * Disable the WiFi scanner thread that is responsible for checking if a specified access point is no longer seen.
     */
    public void disableWifiScanner() {
    	if (mWifiScannerTimer != null)
			mWifiScannerTimer.cancel();
    }
    

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    public void setActivity(TabActivity activity) {
    	mActivity = activity;
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	boolean enableBackgroundServices = preferences.getBoolean(getResources().getString(R.string.preferences_key_enable_background_services), getResources().getBoolean(R.bool.preferences_enable_background_services));
    	if (activity != null) {
    		enableNetworkServices();
    	} else if (!enableBackgroundServices) {
    		disableNetworkServices();
    	}
    }
    
    public TabActivity getActivity() {
    	return mActivity;
    }
    
    public boolean isInForeground() {
		return mActivity != null;
	}
    
    /**
     * Show an HVAC notification.
     */
    private void showHvacNotification(int hvacStatus) {
    	showNotification(getHVACNotificationText(hvacStatus));
    }
    
    /**
     * Show a notification with the specified message.
     */
    private void showNotification(String message) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean enableNotifications = preferences.getBoolean(
				getResources().getString(
						R.string.preferences_key_enable_notifications),
				getResources().getBoolean(
						R.bool.preferences_enable_notifications));
		if (enableNotifications) {
			Intent intentForeground = new Intent(this, TabActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, intentForeground, 0);
			final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
					this).setSmallIcon(R.drawable.genie)
					.setContentTitle(getString(R.string.genie))
					.setContentText(message).setContentIntent(pendingIntent);
			mNotificationManager.notify(NOTIFICATION_ID,
					notificationBuilder.build());
		}
    }
    
    /**
     * Request update of room information.
     */
    public void requestRoomInformationUpdate() {
    	new Thread(new GetRoomInformationTask()).start();
    }
    
    /**
     * Request update of data.
     */
    public void requestDataUpdate() {
    	new Thread(new UpdateTask()).start();
    }
    
    /**
	 * Get the HVAC notification text based on status.
	 * 
	 * @param the specified sensor value.
	 * @return the status text based on the specified sensor value.
	 */
	private String getHVACNotificationText(int status) {
		if (status == getResources().getInteger(R.integer.hvac_status_off)) {
			return getString(R.string.hvac_notification_off);
		} else if (status == getResources().getInteger(R.integer.hvac_status_standby)) {
			return getString(R.string.hvac_notification_standby);
		} else if (status == getResources().getInteger(R.integer.hvac_status_on)) {
			return getString(R.string.hvac_notification_on);
		} else {
			return getString(R.string.hvac_notification_unknown);
		}
	}
	
	/**
	 * Switch the scan mode to the specified mode;
	 * 
	 * @param the new mode to switch into.
	 */
	public synchronized void switchScanMode(ScanMode mode) {
		mLocationManager.removeUpdates(mLocationListener);
		switch(mode) {
			case NETWORK:
				if (mScanMode == ScanMode.GPS)
					break;
				
				if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
							getResources().getInteger(R.integer.gps_refresh_interval), 0, mLocationListener);
					mScanMode = ScanMode.NETWORK;
					break;
				}
			case GPS:
				if (mScanMode == ScanMode.GPS)
					break;
				
				if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
							getResources().getInteger(R.integer.gps_refresh_interval), 0, mLocationListener);
					mScanMode = ScanMode.GPS;
					break;
				}
			case WIFI:
				mScanMode = ScanMode.WIFI;
				if (mWifiForceEnabled)
					enableWifiScanner();
				break;
		}
	}
    
    /**
	 * Task for getting information for the current room.
	 */
	private class GetRoomInformationTask extends TimerTask {
		@Override
		public void run() {
			ConnectionManager.Status status = ConnectionManager.getInstance(
					GenieService.this).getRoomInformation();
			if (status == ConnectionManager.Status.SUCCESS) {
				if (mActivity != null)
					mActivity.update();
			}
		}
	}
    
    /**
	 * Task for updating Genie data. Get the latest data from connection manager
	 * and update the UI once the request has succeeded.
	 */
	private class UpdateTask extends TimerTask {
		@Override
		public void run() {
			SensorData oldHvacSensorData = DataManager.getInstance(GenieService.this).getSensorData(getString(R.string.sensor_hvac_status));
			int oldHvacStatus = (oldHvacSensorData != null) ? (int) Math.round(oldHvacSensorData.getValue()) : INVALID_HVAC_STATUS;
			ConnectionManager.Status status = ConnectionManager.getInstance(
					GenieService.this).getLatestData();
			if (status == ConnectionManager.Status.SUCCESS) {
				if (mActivity != null)
					mActivity.update();
				
				if (oldHvacStatus != INVALID_HVAC_STATUS) {
					SensorData newHvacSensorData = DataManager.getInstance(GenieService.this).getSensorData(getString(R.string.sensor_hvac_status));
					if (newHvacSensorData != null) {
						int newHvacStatus = (int) Math.round(newHvacSensorData.getValue());
						if (oldHvacStatus != newHvacStatus) {
							showHvacNotification(newHvacStatus);
						}
					}
				}
			}
		}
	}
    
	/**
	 * Listener that checks whether Genie is connected to the UCSD-PROTECTED WiFi network.
	 */
    private BroadcastReceiver mWifiListener = new BroadcastReceiver() {
    	@Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            	int state = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            	switch (state) {
            		case WifiManager.WIFI_STATE_ENABLED:
            		case WifiManager.WIFI_STATE_ENABLING:
            			switchScanMode(ScanMode.WIFI);
            			break;
            		case WifiManager.WIFI_STATE_DISABLED:
	            	case WifiManager.WIFI_STATE_DISABLING:
	            	case WifiManager.WIFI_STATE_UNKNOWN:
            		default:
            			switchScanMode(ScanMode.GPS);
            			mWifiForceEnabled = false;
            			disableWifiScanner();
            			break;
            	}
            }
    	}
    };
	
	/**
	 * Location listener that checks whether Genie is within the radius of
	 * a bulding that supports the Genie API.
	 */
	private class GenieLocationListener implements LocationListener {
		public void onLocationChanged(Location loc) {
			if (loc != null) {
				double latitude = loc.getLatitude();
				double longitude = loc.getLongitude();
				
				// Check if inside the WiFi geofence
				if (latitude >= mWifiGeofence.left && latitude <= mWifiGeofence.right && longitude >= mWifiGeofence.top &&
						longitude <= mWifiGeofence.bottom) {
					if (!mWifiManager.isWifiEnabled())
						mWifiManager.setWifiEnabled(true);
					mWifiForceEnabled = true;
				} 
				// Check if in GPS scan mode and outside the GPS geofence
				else if (mScanMode == ScanMode.GPS && (latitude < mGpsGeofence.left || latitude > mGpsGeofence.right || longitude < mGpsGeofence.top ||
						longitude > mGpsGeofence.bottom)) {
					switchScanMode(ScanMode.NETWORK);
				}
				// Check if in Network scan mode and inside the GPS geofence
				if (mScanMode == ScanMode.NETWORK && latitude >= mWifiGeofence.left && latitude <= mWifiGeofence.right && longitude >= mWifiGeofence.top &&
						longitude <= mWifiGeofence.bottom) {
					switchScanMode(ScanMode.GPS);
				}
			}
		}

		public void onProviderDisabled(String provider) {
			// Not used
		}

		public void onProviderEnabled(String provider) {
			// Not used
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// Not used
		}
	}
	
	/**
	 * Task responsible for checking if a specified access point is no longer seen.
	 */
	private class WifiScannerTask extends TimerTask {
		@Override
		public void run() {
			mWifiManager.startScan();
			List<ScanResult> accessPoints = mWifiManager.getScanResults();
			boolean containsBuildingAccessPoint = false;
			for (ScanResult accessPoint : accessPoints) {
				if (accessPoint.SSID.equalsIgnoreCase(mBuildingAccessPointName)) {
					containsBuildingAccessPoint = true;
					break;
				}
			}
			if (!containsBuildingAccessPoint)
				mWifiManager.setWifiEnabled(false);
		}
	}

    /**
     * Class for clients to access.
     */
    public class LocalBinder extends Binder {
        public GenieService getService() {
            return GenieService.this;
        }
    }
}