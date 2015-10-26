package edu.ucsd.genie.datamanager.datastructures;

import java.util.ArrayList;

/**
 * Class for storing the latest data for a specified room.
 */
public class LatestData {

	private final String mBuilding;

	private final String mRoom;

	private final String mUser;

	private final String mTimestamp;

	private final boolean mIsWeekday;

	private final boolean mIsWorktime;

	private final boolean mIsControlEnabled;

	private final ArrayList<SensorData> mSensorData;

	public LatestData(String building, String room, String user,
			String timestamp, boolean isWeekday, boolean isWorktime,
			boolean isControlEnabled, ArrayList<SensorData> sensorData) {
		mBuilding = building;
		mRoom = room;
		mUser = user;
		mTimestamp = timestamp;
		mIsWeekday = isWeekday;
		mIsWorktime = isWorktime;
		mIsControlEnabled = isControlEnabled;
		mSensorData = sensorData;
	}

	public String getBuilding() {
		return mBuilding;
	}

	public String getRoom() {
		return mRoom;
	}

	public String getUser() {
		return mUser;
	}

	public String getTimestamp() {
		return mTimestamp;
	}

	public boolean isIsWeekday() {
		return mIsWeekday;
	}

	public boolean isWorktime() {
		return mIsWorktime;
	}

	public boolean isControlEnabled() {
		return mIsControlEnabled;
	}

	public ArrayList<SensorData> getSensorData() {
		return mSensorData;
	}
}