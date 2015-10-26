package edu.ucsd.genie.userinterface;

import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * A fragment class that also has title that can be used to describe it in a tab.
 */
public abstract class TabFragment extends Fragment {

	/**
	 * Get the title that's used to describe this fragment in a tab.
	 */
	public abstract String getTabTitle(Context context);
	
	/**
	 * Indicates that Genie data has been updated.
	 */
	public void update() {
	};
}
