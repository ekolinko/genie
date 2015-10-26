package edu.ucsd.genie.userinterface.feedback;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import edu.ucsd.genie.R;
import edu.ucsd.genie.connectionmanager.ConnectionManager;
import edu.ucsd.genie.userinterface.TabFragment;

/**
 * Genie feedback fragment that allows a user to send feedback about a room.
 */
public class FeedbackFragment extends TabFragment {

	/**
	 * User interface fields of this screen.
	 */
	private Spinner mSendFeedbackSpinner;
	private EditText mSendFeedbackEditText;
	private Button mSendFeedbackButton;
	private ProgressBar mSendFeedbackProgress;
	private TextView mSendFeedbackErrorField;
	private Button mContactFacilityManagerButton;
	private ProgressBar mContactFacilityManagerProgress;
	private TextView mContactFacilityManagerErrorField;
	
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View feedbackView = inflater.inflate(R.layout.feedback, container, false);
		String[] temperatures = getResources().getStringArray(R.array.panel_send_feedback_values);
		int[] colors = getResources().getIntArray(R.array.panel_send_feedback_colors);
		mSendFeedbackSpinner = (Spinner) feedbackView.findViewById(R.id.feedback_spinner);
		TemperatureAdapter adapter = new TemperatureAdapter(getActivity(), R.layout.genie_feedback_item, temperatures, colors);
		mSendFeedbackSpinner.setAdapter(adapter);
		mSendFeedbackEditText = (EditText) feedbackView.findViewById(R.id.feedback_text);
		mSendFeedbackButton = (Button) feedbackView.findViewById(R.id.feedback_send_button);
		mSendFeedbackProgress = (ProgressBar) feedbackView.findViewById(R.id.feedback_send_progress);
		mSendFeedbackErrorField = (TextView) feedbackView.findViewById(R.id.feedback_error);
		mSendFeedbackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SendFeedbackTask().execute();
			}
		});
		mContactFacilityManagerButton = (Button) feedbackView.findViewById(R.id.feedback_contact_facility_manager);
		mContactFacilityManagerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				View contactFacilityManagerView = inflater.inflate(R.layout.dialog_contact_facility_manager, null, false);
				final EditText commentEditText = (EditText) contactFacilityManagerView.findViewById(R.id.dialog_contact_facility_manager_comment);
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
						getActivity())
						.setTitle(getString(R.string.dialog_contact_facility_manager_title))
						.setView(contactFacilityManagerView)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String comment = commentEditText.getText().toString();
								new ContactFacilityManagerTask(comment).execute();
							}
						})
						.setNegativeButton(android.R.string.cancel, null);
				dialogBuilder.show();
			
			}
		});
		mContactFacilityManagerProgress = (ProgressBar) feedbackView.findViewById(R.id.feedback_contact_facility_manager_progress);
		mContactFacilityManagerErrorField = (TextView) feedbackView.findViewById(R.id.feedback_contact_facility_manager_error);
		return feedbackView;
	}
	
	@Override
	public void update() {
	}
	
	@Override
	public String getTabTitle(Context context) {
		return context.getString(R.string.tab_feedback);
	};
	
	/**
	 * The adapter for showing different levels of temperature.
	 */
	private class TemperatureAdapter extends ArrayAdapter<String> {

		/**
		 * The resource id used for this adapter.
		 */
		private final int mTextViewResourceId;
		
		/**
		 * The list of temperatures contained in this adapter
		 */
		private final String[] mTemperatures;
		
		/**
		 * The list of colors associated with the temperatures.
		 */
		private final int[] mColors;

		public TemperatureAdapter(Context context, int textViewResourceId, String[] temperatures, int[] colors) {
			super(context, textViewResourceId, temperatures);
			mTextViewResourceId = textViewResourceId;
			mTemperatures = temperatures;
			mColors = colors;
		}
		

		public View getView(int position, View convertView, ViewGroup parent){
			View v = convertView;
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(mTextViewResourceId, null);
			}
			String temperature = mTemperatures[position];
			if (temperature != null) {
				TextView textView = (TextView) v.findViewById(android.R.id.text1);
				textView.setText(temperature);
				if (temperature.equals(getString(R.string.panel_send_feedback_neutral)))
					textView.setTextColor(getResources().getColor(R.color.black));
				else
					textView.setTextColor(getResources().getColor(R.color.white));
				textView.setBackgroundColor(mColors[position]);
			}

			return v;
		}
		
		public View getDropDownView(int position, View convertView, ViewGroup parent){
			View v = convertView;
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
			}
			String temperature = mTemperatures[position];
			if (temperature != null) {
				TextView textView = (TextView) v.findViewById(android.R.id.text1);
				if(temperature.equals(getString(R.string.panel_send_feedback_neutral)))
					textView.setTextColor(getResources().getColor(R.color.black));
				else
					textView.setTextColor(getResources().getColor(R.color.white));
				textView.setText(temperature);
				textView.setBackgroundColor(mColors[position]);
			}

			return v;
		}
	}

	/**
     * Task for sending Genie feedback.
     */
    private class SendFeedbackTask extends AsyncTask<Void, Void, ConnectionManager.Status> {
    	
    	/**
    	 * The value provided as part of the feedback.
    	 */
    	private int mFeedbackValue;
    	
    	/**
    	 * The comment provided as part of the feedback.
    	 */
    	private String mFeedbackComment;
    	
        @Override
        protected void onPreExecute() {
        	// convert the index to a value (convert 0 to 6 index to -3 to +3 value)
        	mFeedbackValue = mSendFeedbackSpinner.getSelectedItemPosition() - getResources().getInteger(R.integer.send_feedback_offset);
        	mFeedbackComment = mSendFeedbackEditText.getText().toString();
			mSendFeedbackButton.setText(getString(R.string.panel_send_feedback_updating));
			mSendFeedbackButton.setEnabled(false);
			mSendFeedbackProgress.setVisibility(View.VISIBLE);
			mSendFeedbackErrorField.setVisibility(View.GONE);
        }

        @Override
        protected ConnectionManager.Status doInBackground(Void... params) {
        	ConnectionManager connectionManager = ConnectionManager.getInstance(getActivity());
        	connectionManager.sendReport(mFeedbackComment);
        	return connectionManager.sendFeedback(mFeedbackValue, mFeedbackComment);
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPostExecute(ConnectionManager.Status status) {
        	mSendFeedbackErrorField.setVisibility(View.VISIBLE);
        	mSendFeedbackButton.setText(getString(R.string.panel_send_feedback_action));
        	mSendFeedbackButton.setEnabled(true);
        	mSendFeedbackProgress.setVisibility(View.GONE);
        	if (status == ConnectionManager.Status.SUCCESS) {
        		mSendFeedbackEditText.setText("");
        		mSendFeedbackErrorField.setTextColor(getActivity().getResources()
						.getColor(R.color.genie_green));
        		mSendFeedbackErrorField.setText(getString(R.string.panel_send_feedback_success));
        	} else {
        		mSendFeedbackErrorField.setTextColor(getActivity().getResources()
						.getColor(R.color.red));
        		mSendFeedbackErrorField.setText(status.toString());
        	}
        }
    };
    
    /**
     * Task for sending a report to the Genie facility manager.
     */
    private class ContactFacilityManagerTask extends AsyncTask<Void, Void, ConnectionManager.Status> {
    	
    	/**
    	 * The comment to send to the facility manager.
    	 */
    	private final String mComment;
    	
    	public ContactFacilityManagerTask(String comment) {
    		mComment = comment;
    	}
    	
        @Override
        protected void onPreExecute() {
			mContactFacilityManagerButton.setText(getString(R.string.panel_contact_facility_manager_sending));
			mContactFacilityManagerButton.setEnabled(false);
			mContactFacilityManagerProgress.setVisibility(View.VISIBLE);
			mContactFacilityManagerErrorField.setVisibility(View.GONE);
        }

        @Override
        protected ConnectionManager.Status doInBackground(Void... params) {
        	return ConnectionManager.getInstance(getActivity()).sendReport(mComment);
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPostExecute(ConnectionManager.Status status) {
        	mContactFacilityManagerErrorField.setVisibility(View.VISIBLE);
        	mContactFacilityManagerButton.setText(getString(R.string.panel_contact_facility_manager_action));
        	mContactFacilityManagerButton.setEnabled(true);
        	mContactFacilityManagerProgress.setVisibility(View.GONE);
        	if (status == ConnectionManager.Status.SUCCESS) {
        		mContactFacilityManagerErrorField.setTextColor(getActivity().getResources()
						.getColor(R.color.genie_green));
        		mContactFacilityManagerErrorField.setText(getString(R.string.panel_contact_facility_manager_success));
        	} else {
        		mContactFacilityManagerErrorField.setTextColor(getActivity().getResources()
						.getColor(R.color.red));
        		mContactFacilityManagerErrorField.setText(status.toString());
        	}
        }
    };
	
}
