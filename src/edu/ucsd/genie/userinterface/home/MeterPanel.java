package edu.ucsd.genie.userinterface.home;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.ucsd.genie.R;
import edu.ucsd.genie.datamanager.datastructures.SensorData;

public class MeterPanel extends LinearLayout  {

	/**
	 * The UI elements associated with this panel
	 */
	private MeterView mMeterView;
	private TextView mValueField;
	private TextView mUpdatedField;
	private TextView mAverageField;
	
	public MeterPanel(Context context) {
		super(context);
	}
	
	public MeterPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * When inflate has finished, initialize the references to the UI elements.
	 */
	@Override
	protected void onFinishInflate () {
		mMeterView = (MeterView)findViewById(R.id.meter_view);
		mValueField = (TextView)findViewById(R.id.field_value);
		mUpdatedField = (TextView)findViewById(R.id.field_updated);
		mAverageField = (TextView)findViewById(R.id.field_cse_avg);		
	}
	
	/**
	 * Update all the UI elements with the specified sensor data.
	 * 
	 * @param instantData the sensor data with which to update all the instantaneous value UI elements.
	 * @param averageData the sensor data with which to update all the average value UI elements.
	 */
	public void update(SensorData instantData, SensorData averageData ) {
		if (instantData == null)
			return;
		
		mMeterView.setValue(instantData.getValue());
		mMeterView.setUnits(instantData.getUnits());
		mMeterView.postInvalidate();
		mValueField.setText(instantData.getValue() + instantData.getUnits());
		mUpdatedField.setText(instantData.getTime());
		
		if (averageData == null)
			return;
		
		mAverageField.setText(averageData.getValue() + averageData.getUnits());
	}
}
