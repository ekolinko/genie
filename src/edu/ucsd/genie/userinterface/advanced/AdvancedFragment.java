package edu.ucsd.genie.userinterface.advanced;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.ucsd.genie.R;
import edu.ucsd.genie.datamanager.DataManager;
import edu.ucsd.genie.userinterface.TabFragment;

/**
 * Genie home activity that allows for reading the environment conditions and
 * controlling the HVAC.
 */
public class AdvancedFragment extends TabFragment {

	/**
	 * The detail information panels that are part of this user interface.
	 */
	private SimplePanel mWarmCoolAdjustPanel;
	private SimplePanel mActualCoolingSetpointPanel;
	private SimplePanel mActualHeatingSetpointPanel;
	private SimplePanel mDamperPositionPanel;
	private SimplePanel mSupplyFlowPanel;
	private SimplePanel mReheatValveCommandPanel;
	private SimplePanel mCommonSetpointPanel;
	private SimplePanel mDamperCommandPanel;
	private SimplePanel mSupplyFlowSetpointPanel;
	private SimplePanel mCoolingMaxFlowPanel;
	private SimplePanel mOccupiedCoolingMinPanel;
	private SimplePanel mOccupiedHeatingFlowPanel;
	private SimplePanel mCoolingCommandPanel;
	private SimplePanel mHeatingCommandPanel;
	private SimplePanel mFanCommandPanel;
	private SimplePanel mCoolingPowerPanel;
	private SimplePanel mElectricalPowerPanel;
	private SimplePanel mHeatingPowerPanel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View advancedView = inflater.inflate(R.layout.advanced, container, false);

		// Detail Information Panels
		mWarmCoolAdjustPanel = (SimplePanel) advancedView.findViewById(R.id.panel_warm_cool_adjust);
		mActualCoolingSetpointPanel = (SimplePanel) advancedView.findViewById(R.id.panel_actual_cooling_setpoint);
		mActualHeatingSetpointPanel = (SimplePanel) advancedView.findViewById(R.id.panel_actual_heating_setpoint);
		mDamperPositionPanel = (SimplePanel) advancedView.findViewById(R.id.panel_damper_position);
		mSupplyFlowPanel = (SimplePanel) advancedView.findViewById(R.id.panel_supply_flow);
		mReheatValveCommandPanel = (SimplePanel) advancedView.findViewById(R.id.panel_reheat_valve_command);
		mCommonSetpointPanel = (SimplePanel) advancedView.findViewById(R.id.panel_common_setpoint);
		mDamperCommandPanel = (SimplePanel) advancedView.findViewById(R.id.panel_damper_command);
		mSupplyFlowSetpointPanel = (SimplePanel) advancedView.findViewById(R.id.panel_supply_flow_setpoint);
		mCoolingMaxFlowPanel = (SimplePanel) advancedView.findViewById(R.id.panel_cooling_max_flow);
		mOccupiedCoolingMinPanel = (SimplePanel) advancedView.findViewById(R.id.panel_occupied_cooling_min_flow);
		mOccupiedHeatingFlowPanel = (SimplePanel) advancedView.findViewById(R.id.panel_occupied_heating_flow);
		mCoolingCommandPanel = (SimplePanel) advancedView.findViewById(R.id.panel_cooling_command);
		mHeatingCommandPanel = (SimplePanel) advancedView.findViewById(R.id.panel_heating_command);
		mFanCommandPanel = (SimplePanel) advancedView.findViewById(R.id.panel_fan_command);
		mCoolingPowerPanel = (SimplePanel) advancedView.findViewById(R.id.panel_cooling_power);
		mElectricalPowerPanel = (SimplePanel) advancedView.findViewById(R.id.panel_electrical_power);
		mHeatingPowerPanel = (SimplePanel) advancedView.findViewById(R.id.panel_heating_power);
		update();
		return advancedView;
	}
	
	public void update() {
		if(isAdded()) {
			DataManager dataManager = DataManager.getInstance(getActivity());
			
			// Update detail information panels
			if (mWarmCoolAdjustPanel != null)
				mWarmCoolAdjustPanel.update(dataManager.getSensorData(getString(R.string.sensor_warm_cool_adjust)));
			if (mActualCoolingSetpointPanel != null)
				mActualCoolingSetpointPanel.update(dataManager.getSensorData(getString(R.string.sensor_cooling_setpoint)));
			if (mActualHeatingSetpointPanel != null)
				mActualHeatingSetpointPanel.update(dataManager.getSensorData(getString(R.string.sensor_heating_setpoint)));
			if (mDamperPositionPanel != null)
				mDamperPositionPanel.update(dataManager.getSensorData(getString(R.string.sensor_damper_position)));
			if (mSupplyFlowPanel != null)
				mSupplyFlowPanel.update(dataManager.getSensorData(getString(R.string.sensor_supply_flow)));
			if (mReheatValveCommandPanel != null)
				mReheatValveCommandPanel.update(dataManager.getSensorData(getString(R.string.sensor_reheat_valve_command)));
			if (mCommonSetpointPanel != null)
				mCommonSetpointPanel.update(dataManager.getSensorData(getString(R.string.sensor_common_setpoint)));
			if (mDamperCommandPanel != null)
				mDamperCommandPanel.update(dataManager.getSensorData(getString(R.string.sensor_damper_command)));
			if (mSupplyFlowSetpointPanel != null)
				mSupplyFlowSetpointPanel.update(dataManager.getSensorData(getString(R.string.sensor_supply_flow_setpoint)));
			if (mCoolingMaxFlowPanel != null)
				mCoolingMaxFlowPanel.update(dataManager.getSensorData(getString(R.string.sensor_cooling_max_flow)));
			if (mOccupiedCoolingMinPanel != null)
				mOccupiedCoolingMinPanel.update(dataManager.getSensorData(getString(R.string.sensor_occupied_cooling_min_flow)));
			if (mOccupiedHeatingFlowPanel != null)
				mOccupiedHeatingFlowPanel.update(dataManager.getSensorData(getString(R.string.sensor_occupied_heating_flow)));
			if (mCoolingCommandPanel != null)
				mCoolingCommandPanel.update(dataManager.getSensorData(getString(R.string.sensor_cooling_command)));
			if (mHeatingCommandPanel != null)
				mHeatingCommandPanel.update(dataManager.getSensorData(getString(R.string.sensor_heating_command)));
			if (mFanCommandPanel != null)
				mFanCommandPanel.update(dataManager.getSensorData(getString(R.string.sensor_fan_command)));
			if (mCoolingPowerPanel != null)
				mCoolingPowerPanel.update(dataManager.getSensorData(getString(R.string.sensor_cooling_power)));
			if (mElectricalPowerPanel != null)
				mElectricalPowerPanel.update(dataManager.getSensorData(getString(R.string.sensor_electrical_power)));
			if (mHeatingPowerPanel != null)
				mHeatingPowerPanel.update(dataManager.getSensorData(getString(R.string.sensor_heating_power)));
		}
	}
	
	@Override
	public String getTabTitle(Context context) {
		return context.getString(R.string.tab_advanced);
	};
}
