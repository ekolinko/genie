package edu.ucsd.genie.userinterface.settings;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import edu.ucsd.genie.R;
import edu.ucsd.genie.connectionmanager.ConnectionManager;
import edu.ucsd.genie.datamanager.DataManager;
import edu.ucsd.genie.datamanager.datastructures.User;
import edu.ucsd.genie.userinterface.RoomAdapter;
import edu.ucsd.genie.userinterface.TabFragment;

/**
 * Genie home activity that allows for reading the environment conditions and
 * controlling the HVAC.
 */
public class SettingsFragment extends TabFragment {

	/**
	 * Set schedule panel UI elements.
	 */
	private Button mSetScheduleButton;
	private ProgressBar mSetScheduleProgress;
	private TextView mSetScheduleError;
	private TextView mSetScheduleStatus;
	
	/**
	 * Update password panel UI elements.
	 */
	private TextView mUpdatePasswordErrorField;
	private EditText mPasswordField;
	private EditText mRepeatPasswordField;
	private EditText mOldPasswordField;
	private Button mUpdatePasswordButton;
	private ProgressBar mUpdatePasswordProgress;

	/**
	 * Update rooms panel UI elements.
	 */
	private TextView mUpdateRoomsErrorField;
	private Button mAddRoomButton;
	private Button mRemoveRoomButton;
	private ProgressBar mAddRoomProgress;
	private ProgressBar mRemoveRoomProgress;
	
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View settingsView = inflater.inflate(R.layout.settings, container,
				false);

		final DataManager dataManager = DataManager.getInstance(getActivity());
		
		// Set Schedule Panel
		mSetScheduleButton = (Button) settingsView.findViewById(R.id.settings_set_schedule_button);
		mSetScheduleProgress = (ProgressBar) settingsView.findViewById(R.id.settings_set_schedule_progress);
		mSetScheduleError = (TextView) settingsView.findViewById(R.id.settings_set_schedule_error);
		mSetScheduleStatus = (TextView) settingsView.findViewById(R.id.settings_set_schedule_status);
		if (mSetScheduleStatus != null)
			mSetScheduleStatus.setText(String.format(getString(R.string.panel_set_schedule_status), dataManager.getScheduleStart(), dataManager.getScheduleEnd()));
		if (mSetScheduleButton != null) {
			mSetScheduleButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String currentRoom = DataManager.getInstance(getActivity()).getCurrentRoom();
					View setScheduleView = inflater.inflate(R.layout.dialog_set_schedule, null, false);
					final Spinner startSpinner = (Spinner) setScheduleView.findViewById(R.id.dialog_set_schedule_start_spinner);
					final ArrayAdapter<CharSequence> startAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.dialog_set_schedule_time_values, R.layout.genie_spinner_item_dark);
					startSpinner.setAdapter(startAdapter);
					final Spinner endSpinner = (Spinner) setScheduleView.findViewById(R.id.dialog_set_schedule_end_spinner);
					final ArrayAdapter<CharSequence> endAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.dialog_set_schedule_time_values, R.layout.genie_spinner_item_dark);
					endSpinner.setAdapter(endAdapter);
					int defaultEndTimePosition = getResources().getInteger(R.integer.settings_default_end_time_position);
					if(defaultEndTimePosition >=0 && defaultEndTimePosition < endAdapter.getCount())
						endSpinner.setSelection(defaultEndTimePosition);
					TextView currentScheduleTextView = (TextView) setScheduleView.findViewById(R.id.dialog_set_schedule_current_schedule_textview);
					currentScheduleTextView.setText(String.format(getString(R.string.dialog_set_schedule_current_schedule), dataManager.getScheduleStart(), dataManager.getScheduleEnd()));
					TextView reminderTextView = (TextView) setScheduleView.findViewById(R.id.dialog_set_schedule_reminder_textview);
					reminderTextView.setText(String.format(getString(R.string.dialog_set_schedule_reminder), currentRoom));
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
							getActivity())
							.setTitle(String.format(getString(R.string.dialog_set_schedule_title), currentRoom))
							.setView(setScheduleView)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									String startTime = startAdapter.getItem(startSpinner.getSelectedItemPosition()).toString();
									String endTime = endAdapter.getItem(endSpinner.getSelectedItemPosition()).toString();
									new SetScheduleTask(startTime, endTime).execute();
								}
							})
							.setNegativeButton(android.R.string.cancel, null);
					dialogBuilder.show();
				}
			});
		}
		
		// Update Password Panel
		mUpdatePasswordButton = (Button) settingsView
				.findViewById(R.id.settings_change_password_button);
		mPasswordField = (EditText) settingsView
				.findViewById(R.id.settings_new_password);
		mPasswordField.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mUpdatePasswordButton.setEnabled(!mPasswordField.getText()
						.toString().equals(""));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mRepeatPasswordField = (EditText) settingsView
				.findViewById(R.id.settings_repeat_password);
		mOldPasswordField = (EditText) settingsView
				.findViewById(R.id.settings_old_password);
		mOldPasswordField
				.setOnEditorActionListener(new OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						updatePassword();
						return true;
					}
				});
		mUpdatePasswordErrorField = (TextView) settingsView
				.findViewById(R.id.settings_password_error);
		mUpdatePasswordProgress = (ProgressBar) settingsView
				.findViewById(R.id.settings_change_password_progress);
		mUpdatePasswordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updatePassword();
			}
		});

		// Update Rooms Panel
		
		User user = dataManager.getUser();
		final ArrayList<String> userRooms = user.getRooms();
		final ArrayList<String> allRooms = dataManager.getRooms();
		mUpdateRoomsErrorField = (TextView) settingsView.findViewById(R.id.settings_update_rooms_error);
		mAddRoomButton = (Button) settingsView
				.findViewById(R.id.settings_add_room_button);
		if (mAddRoomButton != null) {
			mAddRoomButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final RoomAdapter adapter = new RoomAdapter(getActivity(),
							android.R.layout.simple_spinner_dropdown_item,
							getString(R.string.building), allRooms, true);
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
							getActivity())
							.setTitle(R.string.panel_update_rooms_add_action)
							.setNegativeButton(android.R.string.cancel, null);
					if (adapter.getCount() > 0)
						dialogBuilder.setSingleChoiceItems(adapter, 0, null).setPositiveButton(
								R.string.panel_update_rooms_add_action,
								new Dialog.OnClickListener() {
									@Override
									public void onClick(
											DialogInterface dialog,
											int which) {
										ListView listView = ((AlertDialog)dialog).getListView();
										if (listView != null) {
											int checkedItemPosition = listView.getCheckedItemPosition();
											if (checkedItemPosition >= 0 && checkedItemPosition < adapter.getCount()) {
												String room = adapter.getItem(checkedItemPosition);
												new UpdateRoomTask(room, userRooms, true).execute();
											}
										}
									}
								});
					else
						dialogBuilder.setMessage(getString(R.string.panel_update_rooms_no_rooms_available_to_add)).setPositiveButton(
								android.R.string.ok, null);
					dialogBuilder.show();
				}
			});
		}
		mAddRoomProgress = (ProgressBar) settingsView.findViewById(R.id.settings_add_room_progress);
		mRemoveRoomButton = (Button) settingsView
				.findViewById(R.id.settings_remove_room_button);
		if (mRemoveRoomButton != null) {
			mRemoveRoomButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final RoomAdapter adapter = new RoomAdapter(getActivity(),
							android.R.layout.simple_spinner_dropdown_item,
							getString(R.string.building), userRooms, true);
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
							getActivity())
							.setTitle(R.string.panel_update_rooms_remove_action)
							.setNegativeButton(android.R.string.cancel, null);
					if (adapter.getCount() > 0)
						dialogBuilder.setSingleChoiceItems(adapter, 0, null).setPositiveButton(
								R.string.panel_update_rooms_remove_action,
								new Dialog.OnClickListener() {
									@Override
									public void onClick(
											DialogInterface dialog,
											int which) {
										ListView listView = ((AlertDialog)dialog).getListView();
										if (listView != null) {
											int checkedItemPosition = listView.getCheckedItemPosition();
											if (checkedItemPosition >= 0 && checkedItemPosition < adapter.getCount()) {
												String room = adapter.getItem(checkedItemPosition);
												new UpdateRoomTask(room, userRooms, false).execute();
											}
										}
									}
								});
					else
						dialogBuilder.setMessage(getString(R.string.panel_update_rooms_no_rooms_available_to_remove)).setPositiveButton(
								android.R.string.ok, null);
					dialogBuilder.show();
				}
			});
		}
		mRemoveRoomProgress = (ProgressBar) settingsView.findViewById(R.id.settings_remove_room_progress);

		return settingsView;
	}

	/**
	 * Show an error with changing the password.
	 */
	public void showPasswordError(String error) {
		if (mUpdatePasswordErrorField != null) {
			mUpdatePasswordErrorField.setText(error);
			mUpdatePasswordErrorField.setTextColor(getActivity().getResources()
					.getColor(R.color.red));
			mUpdatePasswordErrorField.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Show success when the password has been successfully changed.
	 */
	public void showPasswordSuccess() {
		if (mUpdatePasswordErrorField != null) {
			mUpdatePasswordErrorField.setText(getActivity().getString(
					R.string.panel_update_password_success));
			mUpdatePasswordErrorField.setTextColor(getActivity().getResources()
					.getColor(R.color.genie_green));
			mUpdatePasswordErrorField.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Submit the action to change the password.
	 */
	public void updatePassword() {
		String password = mPasswordField.getText().toString();
		String repeatPassword = mRepeatPasswordField.getText().toString();
		if (password.length() < getActivity().getResources().getInteger(
				R.integer.settings_min_password_length)
				|| password.length() > getActivity().getResources().getInteger(
						R.integer.settings_max_password_length)) {
			showPasswordError(getActivity().getString(
					R.string.panel_update_password_error_invalid_length));
			return;
		}
		if (!password.equals(repeatPassword)) {
			showPasswordError(getActivity().getString(
					R.string.panel_update_password_error_mismatch));
			return;
		}

		String oldPassword = mOldPasswordField.getText().toString();
		if (oldPassword.equals("")) {
			showPasswordError(getActivity().getString(
					R.string.panel_update_password_old_password_required));
			return;
		}
		if (oldPassword.length() < getActivity().getResources().getInteger(
				R.integer.settings_min_password_length)
				|| oldPassword.length() > getActivity().getResources()
						.getInteger(R.integer.settings_max_password_length)) {
			showPasswordError(getActivity().getString(
					R.string.panel_update_old_password_error_invalid_length));
			return;
		}
		new ChangePasswordTask().execute();
	}

	@Override
	public void update() {
		DataManager dataManager = DataManager.getInstance(getActivity());
		if (mSetScheduleStatus != null)
			mSetScheduleStatus.setText(String.format(getString(R.string.panel_set_schedule_status), dataManager.getScheduleStart(), dataManager.getScheduleEnd()));
	}

	@Override
	public String getTabTitle(Context context) {
		return context.getString(R.string.tab_settings);
	};

	/**
	 * Task for changing the password.
	 */
	private class ChangePasswordTask extends
			AsyncTask<Void, Void, ConnectionManager.Status> {
		/**
		 * The old password.
		 */
		private String mOldPassword;

		/**
		 * The new password.
		 */
		private String mNewPassword;

		@Override
		protected void onPreExecute() {
			mOldPassword = mOldPasswordField.getText().toString();
			mNewPassword = mPasswordField.getText().toString();
			mUpdatePasswordButton
					.setText(getString(R.string.panel_update_password_updating));
			mUpdatePasswordButton.setEnabled(false);
			mUpdatePasswordProgress.setVisibility(View.VISIBLE);
			mUpdatePasswordErrorField.setVisibility(View.GONE);
		}

		@Override
		protected ConnectionManager.Status doInBackground(Void... params) {
			ConnectionManager.Status changePasswordStatus = ConnectionManager
					.getInstance(getActivity()).updatePassword(mNewPassword,
							mOldPassword);
			return changePasswordStatus;
		}

		@Override
		protected void onCancelled() {
		}

		@Override
		protected void onPostExecute(ConnectionManager.Status status) {

			mUpdatePasswordButton
					.setText(getString(R.string.panel_update_password_action));
			mUpdatePasswordButton.setEnabled(true);
			mUpdatePasswordProgress.setVisibility(View.GONE);
			if (status == ConnectionManager.Status.SUCCESS) {
				mOldPasswordField.setText("");
				mPasswordField.setText("");
				mRepeatPasswordField.setText("");
				showPasswordSuccess();
			} else
				showPasswordError(status.toString());
		}
	};
	
	/**
	 * Task for adding or removing a room.
	 */
	private class UpdateRoomTask extends
			AsyncTask<Void , Void, ConnectionManager.Status> {

		/**
		 * The room that should be updated.
		 */
		private final String mRoom;
		
		/**
		 * The updated list of rooms.
		 */
		private final ArrayList<String> mUpdatedRooms;
		
		/**
		 * The reference to the original list of rooms.
		 */
		private final ArrayList<String> mRooms;
		
		/**
		 * Flag indicating whether a room should be added. If this flag is true, the specified room
		 * should be added. Otherwise, the specified room should be removed.
		 */
		private final boolean mAddRoom;
		
		/**
		 * The UI elements and values associated with the specified command (add room or remove room).
		 */
		private final Button mCommandButton;
		private final ProgressBar mCommandProgress;
		private final String mCommandText;
		private final String mCommandExecutingText;
		private final String mCommandSuccessText;
		
		/**
		 * 
		 * @param room the room that should be updated.
		 * @param rooms the list of rooms on which to perform the operation.
		 * @param addRoom flag indicating whether a room should be added. If this flag is true, the specified room
		 * should be added. Otherwise, the specified room should be removed.
		 */
		public UpdateRoomTask(String room, ArrayList<String> rooms, boolean addRoom) {
			mRoom = room;
			mRooms = rooms;
			mUpdatedRooms = new ArrayList<String>(rooms);
			mAddRoom = addRoom;
			if (mAddRoom) {
				mCommandButton = mAddRoomButton;
				mCommandProgress = mAddRoomProgress;
				mCommandText = getString(R.string.panel_update_rooms_add_action);
				mCommandExecutingText = String.format(getString(R.string.panel_update_rooms_adding_room));
				mCommandSuccessText = String.format(getString(R.string.panel_update_rooms_add_room_success), mRoom);
				if(!mUpdatedRooms.contains(room))
					mUpdatedRooms.add(room);
			} else {
				mCommandButton = mRemoveRoomButton;
				mCommandProgress = mRemoveRoomProgress;
				mCommandText = getString(R.string.panel_update_rooms_remove_action);
				mCommandExecutingText = String.format(getString(R.string.panel_update_rooms_removing_room));
				mCommandSuccessText = String.format(getString(R.string.panel_update_rooms_remove_room_success), mRoom);
				mUpdatedRooms.remove(room);
			}
			mUpdateRoomsErrorField.setVisibility(View.GONE);
		}
		
		@Override
		protected void onPreExecute() {
			mAddRoomButton.setEnabled(false);
			mRemoveRoomButton.setEnabled(false);
			mCommandButton.setText(mCommandExecutingText);
			mCommandProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected ConnectionManager.Status doInBackground(Void... params) {
			return ConnectionManager.getInstance(getActivity()).updateRooms(mUpdatedRooms);
		}

		@Override
		protected void onCancelled() {
		}

		@Override
		protected void onPostExecute(ConnectionManager.Status status) {
			mAddRoomButton.setEnabled(true);
			mRemoveRoomButton.setEnabled(true);
			mCommandButton.setText(mCommandText);
			mCommandProgress.setVisibility(View.GONE);
			if (status == ConnectionManager.Status.SUCCESS) {
				mUpdateRoomsErrorField.setTextColor(getActivity().getResources()
						.getColor(R.color.genie_green));
				mUpdateRoomsErrorField.setText(mCommandSuccessText);
				mUpdateRoomsErrorField.setVisibility(View.VISIBLE);
				
				// Update the room in the original list. 
				if (mAddRoom) {
					if(!mRooms.contains(mRoom))
						mRooms.add(mRoom);
				} else
					mRooms.remove(mRoom);
			} else {
				mUpdateRoomsErrorField.setTextColor(getActivity().getResources()
						.getColor(R.color.red));
				mUpdateRoomsErrorField.setText(status.toString());
				mUpdateRoomsErrorField.setVisibility(View.VISIBLE);
			}
		}
	};
	
	/**
	 * Task for setting the schedule of the HVAC.
	 */
	private class SetScheduleTask extends
			AsyncTask<Void , Void, ConnectionManager.Status> {

		/**
		 * The start time of the schedule.
		 */
		private final String mStartTime;
		
		/**
		 * The end time of the schedule.
		 */
		private final String mEndTime;
		
		public SetScheduleTask(String startTime, String endTime) {
			mStartTime = startTime;
			mEndTime = endTime;
		}
		
		@Override
		protected void onPreExecute() {
			mSetScheduleError.setVisibility(View.GONE);
			mSetScheduleButton.setEnabled(false);
			mSetScheduleButton.setText(getString(R.string.panel_set_schedule_button_setting));
			mSetScheduleProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected ConnectionManager.Status doInBackground(Void... params) {
			return ConnectionManager.getInstance(getActivity()).sendHVACScheduleCommand(mStartTime, mEndTime);
		}

		@Override
		protected void onCancelled() {
		}

		@Override
		protected void onPostExecute(ConnectionManager.Status status) {
			mSetScheduleButton.setEnabled(true);
			mSetScheduleButton.setText(getString(R.string.panel_set_schedule_button));
			mSetScheduleProgress.setVisibility(View.GONE);
			if (status == ConnectionManager.Status.SUCCESS) {
				mSetScheduleError.setTextColor(getActivity().getResources()
						.getColor(R.color.genie_green));
				mSetScheduleError.setText(getString(R.string.panel_set_schedule_success));
				mSetScheduleError.setVisibility(View.VISIBLE);
			} else {
				mSetScheduleError.setTextColor(getActivity().getResources()
						.getColor(R.color.red));
				mSetScheduleError.setText(status.toString());
				mSetScheduleError.setVisibility(View.VISIBLE);
			}
		}
	};
}
