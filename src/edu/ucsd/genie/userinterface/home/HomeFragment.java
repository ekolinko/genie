package edu.ucsd.genie.userinterface.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import edu.ucsd.genie.R;
import edu.ucsd.genie.datamanager.DataManager;
import edu.ucsd.genie.datamanager.datastructures.Zone;
import edu.ucsd.genie.userinterface.TabFragment;

/**
 * Genie home activity that allows for reading the environment conditions and
 * controlling the HVAC.
 */
public class HomeFragment extends TabFragment {

	/**
	 * The main panels that are part of this user interface.
	 */
	private MeterPanel mEnergyUsagePanel;
	private MeterPanel mRoomTemperaturePanel;
	private StatusPanel mHvacStatusPanel;
	private TemperaturePanel mHvacTemperaturePanel;
	private TextView mSharingReminder;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View homeView = inflater.inflate(R.layout.home, container, false);

		// Main Panels
		mEnergyUsagePanel = (MeterPanel) homeView.findViewById(R.id.panel_energy_usage);
		mRoomTemperaturePanel = (MeterPanel) homeView.findViewById(R.id.panel_room_temperature);
		mHvacStatusPanel = (StatusPanel) homeView.findViewById(R.id.panel_hvac_status);
		mHvacTemperaturePanel = (TemperaturePanel) homeView.findViewById(R.id.panel_hvac_temperature);
		mSharingReminder = (TextView) homeView.findViewById(R.id.panel_sharing_reminder);
		
		update();
		return homeView;
	}
	
	public void update() {
		if(isAdded()) {
			DataManager dataManager = DataManager.getInstance(getActivity());
	
			// Update main panels
			if (mEnergyUsagePanel != null)
				mEnergyUsagePanel
						.update(dataManager
								.getSensorData(getString(R.string.sensor_energy_usage)),
								dataManager
										.getSensorData(getString(R.string.sensor_average_energy_usage)));
			if (mRoomTemperaturePanel != null)
				mRoomTemperaturePanel
						.update(dataManager
								.getSensorData(getString(R.string.sensor_zone_temperature)),
								dataManager
										.getSensorData(getString(R.string.sensor_average_zone_temperature)));
			if (mHvacStatusPanel != null)
				mHvacStatusPanel.update(dataManager
						.getSensorData(getString(R.string.sensor_hvac_status)));
			if (mHvacTemperaturePanel != null)
				mHvacTemperaturePanel.update(dataManager
						.getSensorData(getString(R.string.sensor_common_setpoint)),
						dataManager.isControlEnabled());
			if (mSharingReminder != null) {
				Zone zone = dataManager.getZone();
				if (zone != null)
					mSharingReminder.setText(String.format(getString(R.string.panel_hvac_sharing_reminder), zone.getRooms()));
			}
		}
	}
	
	@Override
	public String getTabTitle(Context context) {
		return context.getString(R.string.tab_home);
	};
}
