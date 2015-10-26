package edu.ucsd.genie.datamanager.datastructures;

/**
 * Class for storing session data for specified user.
 */
public class SensorData {

	/**
	 * The type of sensor.
	 */
	private final String mType;
	
	/**
	 * The sensor units.
	 */
	private final String mUnits;
	
	/**
	 * The latest value of the sensor.
	 */
	private final double mValue;
	
	/**
	 * The last update time of the sensor.
	 */
	private final String mTime;

	public SensorData(String type, String units, double value, String time) {
		mType = type;
		mUnits = units;
		mValue = value;
		mTime = time;
	}

	public String getType() {
		return mType;
	}

	public String getUnits() {
		return mUnits;
	}

	public double getValue() {
		return mValue;
	}

	public String getTime() {
		return mTime;
	}
}