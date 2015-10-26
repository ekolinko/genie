package edu.ucsd.genie.userinterface;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.ucsd.genie.R;

/**
 * The adapter for showing rooms for a specific building.
 */
public class RoomAdapter extends ArrayAdapter<String> {

	/**
	 * The text view resource id.
	 */
	private final int mTextViewResourceId;
	
	/**
	 * The building associated with this adapter.
	 */
	private String mBuilding;
	
	/**
	 * The list of rooms contained in this adapter
	 */
	private ArrayList<String> mRooms;

	/**
	 * Flag indicating whether this adapter is used for a dialog. In this case, the standard view returned is the
	 * dropdown view.
	 */
	private boolean mUsedInDialog;
	
	public RoomAdapter(Context context, int textViewResourceId, String building, ArrayList<String> rooms) {
		this(context, textViewResourceId, building, rooms, false);
	}
	
	public RoomAdapter(Context context, int textViewResourceId, String building, ArrayList<String> rooms, boolean usedInDialog) {
		super(context, textViewResourceId, rooms);
		mTextViewResourceId = textViewResourceId;
		mRooms = rooms;
		mBuilding = building;
		mUsedInDialog = usedInDialog;
	}

	public View getView(int position, View convertView, ViewGroup parent){
		if(mUsedInDialog)
			return super.getDropDownView(position, convertView, parent);
		
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(mTextViewResourceId, null);
		}
		String room = mRooms.get(position);
		if (room != null) {
			TextView textView = (TextView) v.findViewById(android.R.id.text1);
			textView.setText(String.format(getContext().getString(R.string.home_room_format), mBuilding, room));
		}

		return v;
	}
}
