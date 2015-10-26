package edu.ucsd.genie.datamanager.datastructures;

import java.util.ArrayList;

/**
 * Class for storing session data for specified user.
 */
public class User {

	/**
	 * The name of the building.
	 */
	private final String mBuilding;
	
	/**
	 * E-mail address of the user.
	 */
	private final String mEmail;
	
	/**
	 * First name of the user.
	 */
	private final String mFirstName;
	
	/**
	 * Middle name of the user.
	 */
	private final String mMiddleName;
	
	/**
	 * Last name of the user.
	 */
	private final String mLastName;
	
	/**
	 * Room list of the user.
	 */
	private final ArrayList<String> mRooms;
	
	public User(String building, String email, String firstName,
			String middleName, String lastName, ArrayList<String> rooms) {
		mBuilding = building;
		mEmail = email;
		mFirstName = firstName;
		mMiddleName = middleName;
		mLastName = lastName;
		mRooms = rooms;
	}

	public String getBuilding() {
		return mBuilding;
	}

	public String getEmail() {
		return mEmail;
	}

	public String getFirstName() {
		return mFirstName;
	}

	public String getMiddleName() {
		return mMiddleName;
	}

	public String getLastName() {
		return mLastName;
	}

	public ArrayList<String> getRooms() {
		return mRooms;
	}
}