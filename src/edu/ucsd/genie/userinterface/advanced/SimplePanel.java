package edu.ucsd.genie.userinterface.advanced;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.ucsd.genie.R;
import edu.ucsd.genie.datamanager.datastructures.SensorData;

public class SimplePanel extends LinearLayout  {

	/**
	 * The UI elements associated with this panel
	 */
	private TextView mValueField;
	private TextView mUpdatedField;
	
	public SimplePanel(Context context) {
		super(context);
	}
	
	public SimplePanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * When inflate has finished, initialize the references to the UI elements.
	 */
	@Override
	protected void onFinishInflate () {
		mValueField = (TextView)findViewById(R.id.field_value);
		mUpdatedField = (TextView)findViewById(R.id.field_updated);
	}
	
	/**
	 * Update all the UI elements with the specified sensor data.
	 * 
	 * @param sensorData the sensor data with which to update all the UI elements.
	 */
	public void update(SensorData sensorData) {
		if (sensorData == null)
			return;
		
		mValueField.setText(sensorData.getValue() + sensorData.getUnits());
		mUpdatedField.setText(sensorData.getTime());
	}
}
