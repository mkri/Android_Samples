package com.aman.samples.detect_device;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;

public class DetectDeviceModelActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Build build = new Build();
        EditText et = (EditText)findViewById(R.id.device_name);
        et.setText(""+build.MANUFACTURER+" "+build.MODEL);
    }
}