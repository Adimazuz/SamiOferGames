package com.gamealerts;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        DataManager dm = new DataManager(this.getContext());
        int hour = dm.getNotificationHour();
        int minute = dm.getNotificationMinute();

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        DataManager dataManager = new DataManager(this.getContext());
        AlarmSetter alarmSetter = new AlarmSetter();
        dataManager.setNotificationHour(hourOfDay);
        dataManager.setNotificationMinute(minute);
        alarmSetter.setAlarm(this.getContext());


    }
}
