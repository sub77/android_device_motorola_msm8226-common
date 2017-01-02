/*
* Copyright (C) 2013 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.omnirom.device;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.TwoStatePreference;
import android.view.MenuItem;

public class DeviceSettings extends PreferenceActivity  {

    public static final String KEY_COLOR_CALIBRATION = "color_calibration";
    public static final String KEY_SUNLIGHT_ENHANCEMENT = "sunlight_enhancement";
    public static final String KEY_VIBRATION = "vibration";

    private ColorCalibrationPreference mColorCalibration;
    private TwoStatePreference mSunlightEnhancement;
    private VibrationPreference mVibration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.main);

        mColorCalibration = (ColorCalibrationPreference) findPreference(KEY_COLOR_CALIBRATION);
        mColorCalibration.setEnabled(ColorCalibrationPreference.isSupported());

        mSunlightEnhancement = (TwoStatePreference) findPreference(KEY_SUNLIGHT_ENHANCEMENT);
        mSunlightEnhancement.setEnabled(SunlightEnhancement.isSupported());
        mSunlightEnhancement.setChecked(SunlightEnhancement.isEnabled(this));
        mSunlightEnhancement.setOnPreferenceChangeListener(new SunlightEnhancement());

        mVibration = (VibrationPreference) findPreference(KEY_VIBRATION);
        mVibration.setEnabled(VibrationPreference.isSupported());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
