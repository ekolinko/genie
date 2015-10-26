package edu.ucsd.genie.userinterface.home;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import edu.ucsd.genie.R;
import edu.ucsd.genie.connectionmanager.ConnectionManager;
import edu.ucsd.genie.datamanager.DataManager;
import edu.ucsd.genie.datamanager.datastructures.SensorData;
import edu.ucsd.genie.datamanager.datastructures.Zone;

public class TemperaturePanel extends LinearLayout {

	/**
	 * The UI elements associated with this panel
	 */
	private SeekBar mTemperatureSeekBar;
	private TextView mValueField;
	private TextView mUpdatedField;

	public TemperaturePanel(Context context) {
		super(context);
	}

	public TemperaturePanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * When inflate has finished, initialize the references to the UI elements.
	 */
	@Override
	protected void onFinishInflate() {
		mTemperatureSeekBar = (SeekBar) findViewById(R.id.temperature_seekbar);
		mTemperatureSeekBar.setEnabled(false);
		mValueField = (TextView) findViewById(R.id.field_value);
		mUpdatedField = (TextView) findViewById(R.id.field_updated);
	}

	/**
	 * Update the temperature panel with the latest data.
	 * 
	 * @param sensorData
	 *            the sensor data for the HVAC temperature.
	 * @param isControlEnabled
	 *            flag indicating whether HVAC controls are enabled.
	 */
	public void update(final SensorData sensorData, boolean isControlEnabled) {
		if (sensorData == null)
			return;

		DataManager dataManager = DataManager.getInstance(getContext());
		Zone zone = dataManager.getZone();
		float defaultCommonSetpoint = (zone != null) ? (float) zone
				.getDefaultCommonSetpoint() : getContext().getResources()
				.getDimension(R.dimen.units_default_common_setpoint);
		final float minTemperature = defaultCommonSetpoint
				- getContext().getResources().getDimension(
						R.dimen.units_fahrenheit_range);
		final int seekBarMax = getContext().getResources().getInteger(
				R.integer.hvac_temperature_seekbar_max);
		final int seekBarStep = getContext().getResources().getInteger(
				R.integer.hvac_temperature_seekbar_step);
		double temperature = (sensorData.getValue() - minTemperature);
		mTemperatureSeekBar.setEnabled(isControlEnabled);
		mTemperatureSeekBar.setMax(seekBarMax);
		mTemperatureSeekBar.setProgress((int) (temperature * seekBarStep));
		mTemperatureSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					/**
					 * The new temperature to send to Genie.
					 */
					private float mNewTemperature;

					/**
					 * The starting progress.
					 */
					private int mStartingProgress;

					/**
					 * The starting label;
					 */
					private String mStartingLabel;

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						DataManager.getInstance(getContext())
								.setControlEnabled(false);
						mTemperatureSeekBar.setEnabled(false);
						new SendHVACTemperatureTask(
								mNewTemperature,
								Integer.toString(getResources()
										.getInteger(
												R.integer.hvac_temperature_default_duration)),
								mStartingProgress, mStartingLabel).execute();
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						mStartingProgress = seekBar.getProgress();
						mStartingLabel = mValueField.getText().toString();
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (fromUser) {
							mNewTemperature = minTemperature + progress
									/ (float) (seekBarStep);
							mValueField.setText(mNewTemperature
									+ sensorData.getUnits());
						}
					}
				});
		mValueField.setText(sensorData.getValue() + sensorData.getUnits());
		mUpdatedField.setText(sensorData.getTime());
	}

	/**
	 * Task for sending an HVAC temperature command.
	 */
	private class SendHVACTemperatureTask extends
			AsyncTask<Void, Void, ConnectionManager.Status> {

		/**
		 * The new value for the HVAC temperature.
		 */
		private float mValue;

		/**
		 * The duration of the new HVAC temperature.
		 */
		private String mDuration;

		/**
		 * The starting progress of the seekbar in case this command fails.
		 */
		private int mStartingProgress;

		/**
		 * The starting label of the seekbar in case this command fails.
		 */
		private String mStartingLabel;

		public SendHVACTemperatureTask(float value, String duration,
				int startingProgress, String startingLabel) {
			mValue = value;
			mDuration = duration;
			mStartingProgress = startingProgress;
			mStartingLabel = startingLabel;
		}

		@Override
		protected ConnectionManager.Status doInBackground(Void... params) {
			return ConnectionManager.getInstance(getContext())
					.sendHVACTemperatureCommand(mValue, mDuration);
		}

		@Override
		protected void onCancelled() {
		}

		@Override
		protected void onPostExecute(ConnectionManager.Status status) {
			if (status == ConnectionManager.Status.SUCCESS) {
				Toast.makeText(
						getContext(),
						String.format(
								getContext()
										.getString(
												R.string.panel_hvac_send_temperature_message_success),
								Float.toString(mValue)), Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(getContext(),
						R.string.panel_hvac_send_temperature_message_failure,
						Toast.LENGTH_LONG).show();
				mTemperatureSeekBar.setProgress(mStartingProgress);
				mValueField.setText(mStartingLabel);
			}

		}
	};
}
