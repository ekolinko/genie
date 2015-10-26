package edu.ucsd.genie.userinterface;

import edu.ucsd.genie.GenieService;
import edu.ucsd.genie.R;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);        
		addPreferencesFromResource(R.xml.preferences);
		// Setup the enable location services preference
		final CheckBoxPreference enableBackgroundServices = (CheckBoxPreference) findPreference(getString(R.string.preferences_key_enable_background_services));
		if (enableBackgroundServices != null) {
			enableBackgroundServices
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							if (newValue instanceof Boolean) {
								Boolean enableBackgroundServices = (Boolean) newValue;
								GenieService service = GenieService
										.getService();
								if (service != null) {
									if (enableBackgroundServices) {
										service.enableLocationServices();
										service.enableNetworkServices();
									} else {
										service.disableLocationServices();
										service.disableNetworkServices();
									}
								}
							}
							return true;
						}
					});
		}
	}
}
