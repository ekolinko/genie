package edu.ucsd.genie.userinterface.about;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.ucsd.genie.R;
import edu.ucsd.genie.userinterface.TabFragment;

/**
 * Genie activity that shows the about information.
 */
public class AboutFragment extends TabFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View advancedView = inflater.inflate(R.layout.about, container, false);
		return advancedView;
	}
	
	public void update() {
		// not implemented for this view.
	}
	
	@Override
	public String getTabTitle(Context context) {
		return context.getString(R.string.tab_about);
	};
}
