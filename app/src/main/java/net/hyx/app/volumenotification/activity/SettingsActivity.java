/*
 * Copyright 2017 https://github.com/seht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.hyx.app.volumenotification.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
//import androidx.core.app.NavUtils;
//import androidx.preference.PreferenceScreen;

import net.hyx.app.volumenotification.R;
import net.hyx.app.volumenotification.controller.NotificationServiceController;
import net.hyx.app.volumenotification.model.SettingsModel;

/**
 * @see {https://developer.android.com/guide/topics/ui/settings/organize-your-settings}
 * @see {https://github.com/googlesamples/android-preferences}
 */
public class SettingsActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsModel settings = new SettingsModel(this);

        setTheme(settings.getAppTheme());
        setContentView(R.layout.activity_layout);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();

        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (getActivity() == null) {
                return;
            }
            if (key.equals("pref_boot")) {
                NotificationServiceController.newInstance(getActivity()).checkEnableStartAtBoot();
            }
            NotificationServiceController.newInstance(getActivity()).startService();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

    }

    public static class NotificationThemeFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.notification_theme_preferences, rootKey);
            if (getActivity() == null) {
                return;
            }
            final SettingsModel settings = new SettingsModel(getActivity());
            Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (settings.getColor(newValue.toString()) != 0) {
                        return true;
                    } else {
                        Toast.makeText(getActivity(), R.string.pref_custom_theme_color_error_message, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            };

            Preference backgroundColorPref = findPreference("pref_custom_theme_background_color");
            Preference iconColorPref = findPreference("pref_custom_theme_icon_color");

            if (backgroundColorPref != null) {
                backgroundColorPref.setOnPreferenceChangeListener(changeListener);
            }
            if (iconColorPref != null) {
                iconColorPref.setOnPreferenceChangeListener(changeListener);
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (getActivity() == null) {
                return;
            }
            NotificationServiceController.newInstance(getActivity()).startService();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

    }

}
