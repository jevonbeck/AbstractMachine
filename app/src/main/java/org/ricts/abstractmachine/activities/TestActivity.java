package org.ricts.abstractmachine.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.devices.compute.core.BasicScalar;
import org.ricts.abstractmachine.devices.compute.core.BasicScalarEnums;
import org.ricts.abstractmachine.ui.compute.CpuCoreView;
import org.ricts.abstractmachine.ui.storage.RamView;

import java.util.ArrayList;

public class TestActivity extends Activity {
    private static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

    }

}
