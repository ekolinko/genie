package edu.ucsd.genie.datamanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import android.content.Context;
import edu.ucsd.genie.R;
import edu.ucsd.genie.datamanager.datastructures.LatestData;
import edu.ucsd.genie.datamanager.datastructures.SensorData;
import edu.ucsd.genie.datamanager.datastructures.User;
import edu.ucsd.genie.datamanager.datastructures.Zone;

/**
 * Class that holds of the data for this application.
 */
public class DataManager {

	/**
	 * The context of the application.
	 */
	private final Context mContext;
	
	/**
	 * The user of the application.
	 */
	private User mUser;
	
	/**
	 * The current selected room of the user.
	 */
	private String mCurrentRoom;
	
	/**
	 * The zone information associated with the current room.
	 */
	private Zone mZone;
	
	/**
	 * The list of sensors mapped by sensor type for fast access.
	 */
	private HashMap<String, SensorData> mSensors = new HashMap<String, SensorData>();
	
	/**
	 * The flag indicating whether the HVAC is enabled or not.
	 */
	private boolean mIsControlEnabled = false;
	
	/**
	 * The start and end of the HVAC schedule.
	 */
	private String mScheduleStart;
	private String mScheduleEnd;

	/**
	 * The list of all rooms in a building.
	 */
	private ArrayList<String> mRooms = new ArrayList<String> ();

	private static DataManager instance = null;

	private DataManager(Context context) {
		mContext = context;
	}

	public static DataManager getInstance(Context context) {
		if (instance == null) {
			synchronized (DataManager.class) {
				if (instance == null) {
					instance = new DataManager(context);
				}
			}
		}
		return instance;
	}

	public void setUser(User user) {
		if (user != null) {
			ArrayList<String> rooms = user.getRooms();
			if (rooms.size() > 0)
				mCurrentRoom = rooms.get(0);
		}
		mUser = user;
	}
	
	public User getUser() {
		return mUser;
	}
	
	public String getCurrentRoom() {
		return mCurrentRoom;
	}

	/**
	 * Set the latest data for the current room. Update each sensor with the latest data.
	 */
	public void setLatestData(LatestData latestData) {
		if (latestData == null)
			return;
		
		mIsControlEnabled = latestData.isControlEnabled();
		
		for (SensorData data : latestData.getSensorData()) {
			String type = data.getType();
			if (type.equals(mContext.getResources().getString(R.string.sensor_cooling_power_raw)))
				type = mContext.getResources().getString(R.string.sensor_cooling_power);
			else if (type.equals(mContext.getResources().getString(R.string.sensor_electrical_power_raw)))
				type = mContext.getResources().getString(R.string.sensor_electrical_power);
			else if (type.equals(mContext.getResources().getString(R.string.sensor_heating_power_raw)))
				type = mContext.getResources().getString(R.string.sensor_heating_power);
			mSensors.put(type, data);
		}
	}

	/**
	 * Get the sensor data for the sensor with the specified type.
	 * 
	 * @param sensorType the type of the sensor for which to get the data.
	 * @return the sensor data associated with the specified type.
	 */
	public SensorData getSensorData(String sensorType) {
		return mSensors.get(sensorType);
	}
	
	/**
	 * Reset the room data for the data manager. Should be called when switching rooms.
	 */
	public void resetRoomData() {
		mIsControlEnabled = false;
		mSensors.clear();
	}
	
	/**
	 * Reset the data manager. Should be reset when the user is logged out.
	 */
	public void reset() {
		mUser = null;
		mCurrentRoom = null;
		mIsControlEnabled = false;
		mSensors.clear();
	}

	public void setControlEnabled(boolean enabled) {
		mIsControlEnabled = enabled;
	}
	
	public boolean isControlEnabled() {
		return mIsControlEnabled;
	}

	public void setCurrentRoom(String room) {
		mCurrentRoom = room;
	}

	public void setRooms(ArrayList<String> rooms) {
		if (rooms != null)
			mRooms = rooms;
	}
	
	public ArrayList<String> getRooms() {
		return mRooms;
	}

	public void setSchedule(String scheduleString) {
		StringTokenizer tokenizer = new StringTokenizer(scheduleString);
		if (tokenizer.hasMoreTokens())
			mScheduleStart = tokenizer.nextToken();
		if (tokenizer.hasMoreTokens())
			tokenizer.nextToken();
		if (tokenizer.hasMoreTokens())
			mScheduleEnd = tokenizer.nextToken();
	}
	
	public String getScheduleStart() {
		if (mScheduleStart == null)
			mScheduleStart = mContext.getString(R.string.no_data);
			
		return mScheduleStart;
	}
	
	public String getScheduleEnd() {
		if (mScheduleEnd == null)
			mScheduleEnd = mContext.getString(R.string.no_data);
			
		return mScheduleEnd;
	}

	public void setZone(Zone zone) {
		mZone = zone;
	}

	public Zone getZone() {
		return mZone;
	}
}
