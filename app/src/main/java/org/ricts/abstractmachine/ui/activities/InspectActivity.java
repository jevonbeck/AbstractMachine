package org.ricts.abstractmachine.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.system.SystemArchitecture;
import org.ricts.abstractmachine.devices.compute.core.BasicScalar;
import org.ricts.abstractmachine.ui.fragments.InspectFragment;

public abstract class InspectActivity extends AppCompatActivity implements InspectFragment.StepActionListener {
    private static final String TAG = "InspectActivity";

    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    private TextView sysClockTextView;
    private boolean isRunning;

    private int pagerAdapterCount, pagerOffScreenLimit;
    private SystemArchitecture architecture;

    protected abstract SystemArchitecture createSystemArchitecture(ComputeCore core, Bundle options);
    protected abstract void initSystemArchitecture(SystemArchitecture architecture, Bundle options);
    protected abstract PagerAdapter createAdapter(SystemArchitecture architecture);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspect);

        /** Initialise main data **/
        Bundle options = getIntent().getExtras();
        architecture = createSystemArchitecture(getComputeCore(options), options);
        initSystemArchitecture(architecture, options);
        pagerAdapter = createAdapter(architecture);

        /** Setup UI **/
        isRunning = false;

        Button advanceButton = (Button) findViewById(R.id.stepButton);
        advanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                advanceTime();
            }
        });

        Button runButton = (Button) findViewById(R.id.runButton);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                isRunning = true;
                advanceTime();
            }
        });

        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                isRunning = false;
            }
        });

        sysClockTextView = (TextView) findViewById(R.id.sysClockText);
        sysClockTextView.setText(String.valueOf(0));

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);

        // Synchronise Tab and Slide UI
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(pager));
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Get data from pager and pagerAdapter
        pagerAdapterCount = pagerAdapter.getCount();
        pagerOffScreenLimit = pager.getOffscreenPageLimit();
    }

    @Override
    public void onStepActionCompleted() {
        if(isRunning) {
            Log.d(TAG, "onStepActionCompleted() advanceTime() start");
            advanceTime();
            Log.d(TAG, "onStepActionCompleted() advanceTime() end");
        }

        Log.d(TAG, "onStepActionCompleted()");
        Log.d(TAG, "isRunning = " + isRunning);
    }

    private void advanceTime(){
        int currentItemIndex = pager.getCurrentItem();
        int min = currentItemIndex - pagerOffScreenLimit;
        int max = currentItemIndex + pagerOffScreenLimit;

        if(min < 0){
            min = 0;
        }
        if(max >= pagerAdapterCount){
            max = pagerAdapterCount - 1;
        }

        for(int x=min; x <= max; ++x){
            ((InspectFragment) pagerAdapter.instantiateItem(pager, x))
                    .setUserVisibility(x == currentItemIndex);
        }

        // Initiate animations
        Log.d(TAG, "architecture.advanceTime() start");
        architecture.advanceTime();
        Log.d(TAG, "architecture.advanceTime() end");
        sysClockTextView.setText(String.valueOf(architecture.timeElapsed()));
    }

    private ComputeCore getComputeCore(Bundle options){
        int byteMultiplierWidth = 0;
        int dAdWidth = 3;
        int iAdWidth = 3;

        int stkAdWidth = 3;
        int dRegAdWidth = 3;
        int dAdrRegAdWidth = 1;
        int iAdrRegAdWidth = 1;

        return new BasicScalar(byteMultiplierWidth, dAdWidth, iAdWidth,
                stkAdWidth,dRegAdWidth, dAdrRegAdWidth, iAdrRegAdWidth);
    }
}
