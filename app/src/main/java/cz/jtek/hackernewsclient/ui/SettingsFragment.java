package cz.jtek.hackernewsclient.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import cz.jtek.hackernewsclient.R;

public class SettingsFragment
        extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener
{

    @SuppressWarnings("unused")
    private static final String TAG = SettingsFragment.class.getSimpleName();

    /**
     * This method is called when shared preferences are created. It takes care of updating
     * all preferences summaries to their current value.
     *
     * @param savedInstanceState savedInstanceState
     * @param rootKey            rootKey
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference);

        // Update preference summary based on current stored value
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int prefCount = prefScreen.getPreferenceCount();
        for (int i = 0; i < prefCount; i++) {
            Preference p = prefScreen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                updatePrefSummary(p, value);
            }
        }
    }

    /**
     * This method updates selected preference summary to given string
     *
     * @param preference Preference to update summary of
     * @param value      New summary value
     */
    private void updatePrefSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register Preference change listener
        getPreferenceScreen()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister Preference change listener
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Shared preference change listener. Changed preference gets its summary updated.
     *
     * @param sharedPreferences Shared preferences instance
     * @param key               Changed preference key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Update changed preference summary based on new value
        Preference preference = findPreference(key);
        if (preference != null) {
            if (!(preference instanceof CheckBoxPreference)) {
                updatePrefSummary(preference, sharedPreferences.getString(key, ""));
            }
        }
    }

}
