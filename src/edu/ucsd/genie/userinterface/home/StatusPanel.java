package edu.ucsd.genie.userinterface.home;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.ucsd.genie.R;
import edu.ucsd.genie.connectionmanager.ConnectionManager;
import edu.ucsd.genie.datamanager.datastructures.SensorData;

public class StatusPanel extends LinearLayout  {

	/**
	 * The current status of the HVAC.
	 */
	private int mStatus;
	
	/**
	 * The UI elements associated with this panel
	 */
	private ImageButton mStatusButton;
	private TextView mValueField;
	private TextView mUpdatedField;
	
	public StatusPanel(Context context) {
		super(context);
	}
	
	public StatusPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * When inflate has finished, initialize the references to the UI elements.
	 */
	@Override
	protected void onFinishInflate () {
		mStatusButton = (ImageButton)findViewById(R.id.status_button);
		mStatusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int startingStatus = mStatus;
				int status;
				mStatusButton.setEnabled(false);
				if (startingStatus == getContext().getResources().getInteger(R.integer.hvac_status_off) || 
						startingStatus == getContext().getResources().getInteger(R.integer.hvac_status_standby)) {
					status = getContext().getResources().getInteger(R.integer.hvac_status_on);
				} else
					status = getContext().getResources().getInteger(R.integer.hvac_status_off);
				new SendHVACCommandTask(status, startingStatus, Integer.toString(getResources().getInteger(R.integer.hvac_temperature_default_duration))).execute();
			}
		});
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
		
		mValueField.setText(getTextForValue(sensorData.getValue()));
		mUpdatedField.setText(sensorData.getTime());
	}
	
	/**
	 * Get the status text based on the sensor value.
	 * 
	 * @param the specified sensor value.
	 * @return the status text based on the specified sensor value.
	 */
	private String getTextForValue(double value) {
		mStatus = (int)Math.round(value);
		if (mStatus == getContext().getResources().getInteger(R.integer.hvac_status_off)) {
			return getContext().getString(R.string.panel_hvac_status_off);
		} else if (mStatus == getContext().getResources().getInteger(R.integer.hvac_status_standby)) {
			return getContext().getString(R.string.panel_hvac_status_standby);
		} else if (mStatus == getContext().getResources().getInteger(R.integer.hvac_status_on)) {
			return getContext().getString(R.string.panel_hvac_status_on);
		} else {
			return getContext().getString(R.string.panel_hvac_status_unknown);
		}
	}
	
	/**
     * Task for sending a command for turning the HVAC on or off.
     */
    private class SendHVACCommandTask extends AsyncTask<Void, Void, ConnectionManager.Status> {
    	
    	/**
    	 * The new status of the HVAC.
    	 */
    	private int mStatus;
    	
    	/**
    	 * The starting status of the HVAC.
    	 */
    	private int mStartingStatus;
    	
    	/**
    	 * The duration of the HVAC command.
    	 */
    	private String mDuration;
    	
    	public SendHVACCommandTask(int status, int startingStatus, String duration) {
    		mStatus = status;
    		mStartingStatus = startingStatus;
    		mDuration = duration;
    	}

        @Override
        protected ConnectionManager.Status doInBackground(Void... params) {
        	return ConnectionManager.getInstance(getContext()).sendHVACControlCommand(mStatus, mDuration);
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPostExecute(ConnectionManager.Status status) {
        	if (status == ConnectionManager.Status.SUCCESS) {
        		String statusText = getTextForValue(mStatus);
        		Toast.makeText(getContext(), String.format(getContext().getString(R.string.panel_hvac_send_command_message_success), statusText), Toast.LENGTH_LONG).show();
        		mValueField.setText(statusText);
        	} else {
        		Toast.makeText(getContext(), R.string.panel_hvac_send_command_message_failure, Toast.LENGTH_LONG).show();
				mValueField.setText(getTextForValue(mStartingStatus));
        	}
        }
    };
}
