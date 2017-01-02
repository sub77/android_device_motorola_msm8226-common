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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Special preference type that allows the calibration of the display
 * for each of the RGB colors independently
 */
public class ColorCalibrationPreference extends DialogPreference implements OnClickListener {

    private static final int RED_SEEKBAR_ID = R.id.color_red_seekbar;
    private static final int GREEN_SEEKBAR_ID = R.id.color_green_seekbar;
    private static final int BLUE_SEEKBAR_ID = R.id.color_blue_seekbar;

    private static final int RED_VALUE_DISPLAY_ID = R.id.color_red_value;
    private static final int GREEN_VALUE_DISPLAY_ID = R.id.color_green_value;
    private static final int BLUE_VALUE_DISPLAY_ID = R.id.color_blue_value;

    private static final String RED = "red";
    private static final String GREEN = "green";
    private static final String BLUE = "blue";

    private static final String FILE_PATH = "/sys/class/graphics/fb0/rgb";

    private ColorCalibrationSeekBar mRedSeekBar = new ColorCalibrationSeekBar();
    private ColorCalibrationSeekBar mGreenSeekBar = new ColorCalibrationSeekBar();
    private ColorCalibrationSeekBar mBlueSeekBar = new ColorCalibrationSeekBar();

    private static final int MAX_VALUE = 32768;

    /**
     * Track instances to know when to restore original value
     * When the orientation changes, a new dialog is created before the old one is destroyed.
     */
    private static int sInstances = 0;

    public ColorCalibrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.preference_dialog_color_calibration);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        sInstances++;

        SeekBar redSeekBar = (SeekBar) view.findViewById(RED_SEEKBAR_ID);
        TextView redValueDisplay = (TextView) view.findViewById(RED_VALUE_DISPLAY_ID);
        mRedSeekBar = new ColorCalibrationSeekBar(redSeekBar, redValueDisplay, RED);

        SeekBar greenSeekBar = (SeekBar) view.findViewById(GREEN_SEEKBAR_ID);
        TextView greenValueDisplay = (TextView) view.findViewById(GREEN_VALUE_DISPLAY_ID);
        mGreenSeekBar = new ColorCalibrationSeekBar(greenSeekBar, greenValueDisplay, GREEN);

        SeekBar blueSeekBar = (SeekBar) view.findViewById(BLUE_SEEKBAR_ID);
        TextView blueValueDisplay = (TextView) view.findViewById(BLUE_VALUE_DISPLAY_ID);
        mBlueSeekBar = new ColorCalibrationSeekBar(blueSeekBar, blueValueDisplay, BLUE);

        SetupButtonClickListener(view);
    }

    private void SetupButtonClickListener(View view) {

        Button mResetButton = (Button)view.findViewById(R.id.color_calibration_reset);
        mResetButton.setOnClickListener(this);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        sInstances--;

        if (positiveResult) {
            mRedSeekBar.save();
            mGreenSeekBar.save();
            mBlueSeekBar.save();
        } else if (sInstances == 0) {
            /* Reset sliders and values */
            mRedSeekBar.reset();
            mGreenSeekBar.reset();
            mBlueSeekBar.reset();
            /* Reset sysfs */
            resetSysfs();
        }
    }

    /**
     * Restore screen color tuning from SharedPreferences (write to kernel).
     * @param context       The context to read the SharedPreferences from
     */
    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        int redValue = sharedPrefs.getInt(RED, MAX_VALUE);
        int greenValue = sharedPrefs.getInt(GREEN, MAX_VALUE);
        int blueValue = sharedPrefs.getInt(BLUE, MAX_VALUE);
        Utils.writeValue(FILE_PATH, String.valueOf(redValue) + " " + String.valueOf(greenValue) +
            " " + String.valueOf(blueValue));
    }

    public static boolean isSupported() {
        boolean supported = true;
        if (!Utils.fileExists(FILE_PATH)) {
            supported = false;
        }

        return supported;
    }

    class ColorCalibrationSeekBar implements SeekBar.OnSeekBarChangeListener {

        protected String mColor;
        protected int mOriginal;
        protected SeekBar mSeekBar;
        protected TextView mValueDisplay;

        public ColorCalibrationSeekBar(SeekBar seekBar, TextView valueDisplay, String color) {
            mColor = color;
            mSeekBar = seekBar;
            mValueDisplay = valueDisplay;

            /* Read original value */
            SharedPreferences sharedPreferences = getSharedPreferences();
            mOriginal = sharedPreferences.getInt(mColor, MAX_VALUE);

            seekBar.setMax(MAX_VALUE);
            reset();
            seekBar.setOnSeekBarChangeListener(this);
        }

        /* For inheriting class */
        protected ColorCalibrationSeekBar() {
        }

        public TextView getValue() {
            return mValueDisplay;
        }

        public int getOriginalValue() {
            return mOriginal;
        }

        public void reset() {
            mSeekBar.setProgress(mOriginal);
            updateValue(mOriginal);
        }

        public void set(int value) {
            mSeekBar.setProgress(value);
            updateValue(value);
        }

        public void save() {
            Editor editor = getEditor();
            editor.putInt(mColor, mSeekBar.getProgress());
            editor.commit();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            /* Make sure to update value first */
            updateValue(progress);
            /* Write all values to kernel */
            updateSysfs();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            /* Do nothing */
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            /* Do nothing */
        }

        protected void updateValue(int progress) {
            mValueDisplay.setText(String.valueOf(progress));
        }
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.color_calibration_reset:
                resetDefault();
                break;
        }
    }
    public void resetDefault() {
        /* Set sliders and values to max */
        mRedSeekBar.set(MAX_VALUE);
        mGreenSeekBar.set(MAX_VALUE);
        mBlueSeekBar.set(MAX_VALUE);
        /* Write to kernel */
        Utils.writeValue(FILE_PATH, String.valueOf(MAX_VALUE) +
            " " + String.valueOf(MAX_VALUE) +
            " " + String.valueOf(MAX_VALUE));
    }
    public void resetSysfs() {
        Utils.writeValue(FILE_PATH, String.valueOf(mRedSeekBar.getOriginalValue()) +
            " " + String.valueOf(mGreenSeekBar.getOriginalValue()) +
            " " + String.valueOf(mBlueSeekBar.getOriginalValue()));
    }
    public void updateSysfs() {
        Utils.writeValue(FILE_PATH, mRedSeekBar.getValue().getText().toString() +
            " " + mGreenSeekBar.getValue().getText().toString() +
            " " + mBlueSeekBar.getValue().getText().toString());
    }
}
