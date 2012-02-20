package mx.cinvestav.android.hardeninja.view;

import mx.cinvestav.android.hardeninja.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesView extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	
}
