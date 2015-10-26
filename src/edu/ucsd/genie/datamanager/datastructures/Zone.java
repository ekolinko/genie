package edu.ucsd.genie.datamanager.datastructures;


/**
 * Class for storing session data for a zone.
 */
public class Zone {

	/**
	 * The default common setpoint.
	 */
	private final double mDefaultCommonSetpoint;
	
	/**
	 * The area of the zone. 
	 */
	private final String mArea;
	
	/**
	 * Rooms contained in the zone (Comma separated value).
	 */
	private final String mRooms;
	
	public Zone(double defaultCommonSetpoint, String area, String rooms) {
		mDefaultCommonSetpoint = defaultCommonSetpoint;
		mArea = area;
		mRooms = rooms;
	}

	public double getDefaultCommonSetpoint() {
		return mDefaultCommonSetpoint;
	}

	public String getArea() {
		return mArea;
	}

	public String getRooms() {
		return mRooms;
	}
}