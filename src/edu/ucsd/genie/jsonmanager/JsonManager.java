package edu.ucsd.genie.jsonmanager;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.stream.JsonReader;

import edu.ucsd.genie.R;
import edu.ucsd.genie.datamanager.DataManager;
import edu.ucsd.genie.datamanager.datastructures.LatestData;
import edu.ucsd.genie.datamanager.datastructures.SensorData;
import edu.ucsd.genie.datamanager.datastructures.SessionData;
import edu.ucsd.genie.datamanager.datastructures.User;
import edu.ucsd.genie.datamanager.datastructures.Zone;
import android.content.Context;

/**
 * Class that parses JSON messages into Genie data structures.
 */
public class JsonManager {

	/**
	 * The context of the application.
	 */
	private final Context mContext;

	private static JsonManager instance = null;

	private JsonManager(Context context) {
		mContext = context;
	}

	public static JsonManager getInstance(Context context) {
		if (instance == null) {
			synchronized (JsonManager.class) {
				if (instance == null) {
					instance = new JsonManager(context);
				}
			}
		}
		return instance;
	}

	/**
	 * Read the session data given a JsonReader.
	 * 
	 * @param reader
	 *            the reader from which to read the session data.
	 * @return the session data from the reader.
	 */
	public SessionData readSessionData(String username, JsonReader reader) {
		String apiKey = mContext.getString(R.string.empty);
		String authenticationToken = mContext.getString(R.string.empty);
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals(mContext.getString(R.string.json_auth_token))) {
					authenticationToken = reader.nextString();
				} else if (name.equals(mContext
						.getString(R.string.json_api_key))) {
					apiKey = reader.nextString();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException e) {
		}
		return new SessionData(username, authenticationToken, apiKey);
	}

	/**
	 * Read the user data given a JsonReader.
	 * 
	 * @param reader
	 *            the reader from which to read the session data.
	 * @return the sensor data from the reader.
	 */
	public User readUserInformation(JsonReader reader) {
		String building = mContext.getString(R.string.empty);
		String email = mContext.getString(R.string.empty);
		String firstName = mContext.getString(R.string.empty);
		String middleName = mContext.getString(R.string.empty);
		String lastName = mContext.getString(R.string.empty);
		ArrayList<String> rooms = new ArrayList<String>();

		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals(mContext.getString(R.string.json_building))) {
					building = reader.nextString();
				} else if (name.equals(mContext.getString(R.string.json_email))) {
					email = reader.nextString();
				} else if (name.equals(mContext
						.getString(R.string.json_first_name))) {
					firstName = reader.nextString();
				} else if (name.equals(mContext
						.getString(R.string.json_middle_name))) {
					middleName = reader.nextString();
				} else if (name.equals(mContext
						.getString(R.string.json_last_name))) {
					lastName = reader.nextString();
				} else if (name.equals(mContext.getString(R.string.json_rooms))) {
					reader.beginArray();
					while (reader.hasNext()) {
						rooms.add(reader.nextString());
					}
					reader.endArray();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException e) {
		}
		return new User(building, email, firstName, middleName, lastName, rooms);
	}

	/**
	 * Read the latest sensor data given a JsonReader.
	 * 
	 * @param reader
	 *            the reader from which to read the session data.
	 * @return the latest sensor data from the reader.
	 */
	public LatestData readLatestData(JsonReader reader) {
		String building = mContext.getString(R.string.empty);
		String room = mContext.getString(R.string.empty);
		String user = mContext.getString(R.string.empty);
		String timestamp = mContext.getString(R.string.empty);
		boolean isWeekday = false;
		boolean isWorktime = false;
		boolean isControlEnabled = false;
		ArrayList<SensorData> sensorData = new ArrayList<SensorData>();
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals(mContext.getString(R.string.json_building))) {
					building = reader.nextString();
				} else if (name.equals(mContext.getString(R.string.json_room))) {
					room = reader.nextString();
				} else if (name.equals(mContext.getString(R.string.json_user))) {
					user = reader.nextString();
				} else if (name.equals(mContext
						.getString(R.string.json_timestamp))) {
					timestamp = reader.nextString();
				} else if (name.equals(mContext
						.getString(R.string.json_is_weekday))) {
					isWeekday = reader.nextBoolean();
				} else if (name.equals(mContext
						.getString(R.string.json_is_worktime))) {
					isWorktime = reader.nextBoolean();
				} else if (name.equals(mContext
						.getString(R.string.json_is_control_enabled))) {
					isControlEnabled = reader.nextBoolean();
				} else if (name.equals(mContext
						.getString(R.string.json_sensor_data))) {
					reader.beginArray();
					while (reader.hasNext()) {
						SensorData sensor = readSensorData(reader);
						if (sensor != null)
							sensorData.add(sensor);
					}
					reader.endArray();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException e) {
		}
		return new LatestData(building, room, user, timestamp, isWeekday,
				isWorktime, isControlEnabled, sensorData);
	}

	/**
	 * Read the sensor data given a JsonReader.
	 * 
	 * @param reader
	 *            the reader from which to read the session data.
	 * @return the sensor data from the reader.
	 */
	public SensorData readSensorData(JsonReader reader) {
		String type = mContext.getString(R.string.empty);
		String unit = mContext.getString(R.string.empty);
		double value = 0;
		String time = mContext.getString(R.string.empty);
		String scheduleSensorType = mContext.getString(R.string.sensor_schedule_weekday);
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals(mContext.getString(R.string.json_type))) {
					type = reader.nextString();
				} else if (name.equals(mContext.getString(R.string.json_unit))) {
					unit = reader.nextString();
				} else if (name.equals(mContext.getString(R.string.json_value))) {
					if (type.equals(scheduleSensorType)) {
						DataManager.getInstance(mContext).setSchedule(reader.nextString());
					} else {
						value = reader.nextDouble();
					}
				} else if (name.equals(mContext.getString(R.string.json_time))) {
					time = reader.nextString();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException e) {
			return null;
		}
		return new SensorData(type, unit, value, time);
	}

	/**
	 * Read room data given a JsonReader.
	 * 
	 * @param reader
	 *            the reader from which to read the room data.
	 * @return the rooms from the reader.
	 */
	public ArrayList<String> readRooms(JsonReader reader) {
		ArrayList<String> rooms = new ArrayList<String>();
		try {
			reader.beginArray();
			while (reader.hasNext()) {
				rooms.add(reader.nextString());
			}
			reader.endArray();
		} catch (IOException e) {
		}
		return rooms;
	}

	/**
	 * Read zone data given a JsonReader.
	 * 
	 * @param reader the reader from which to read zone information from.
	 * @return the zone.
	 */
	public Zone readZone(JsonReader reader) {
		double defaultCommonSetpoint = 0;
		String area = mContext.getString(R.string.empty);
		String rooms = mContext.getString(R.string.empty);
		try {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals(mContext.getString(R.string.json_default_common_setpoint))) {
					defaultCommonSetpoint = reader.nextDouble();
				} else if (name.equals(mContext.getString(R.string.json_area))) {
					area = reader.nextString();
				} else if (name.equals(mContext.getString(R.string.json_rooms))) {
					rooms = reader.nextString();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException e) {
		}
		return new Zone(defaultCommonSetpoint, area, rooms);
	}

}
