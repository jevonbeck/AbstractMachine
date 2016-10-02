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

public abstract class InspectActivity extends AppCompatActivity implements InspectFragment.InspectActionListener {
    private static final String TAG = "InspectActivity";

    public static final String ARCH_TYPE = "architectureType";
    public static final String CORE_NAME = "coreName";
    public static final String CORE_DATA_WIDTH = "coreDataWidth";
    public static final String INSTR_ADDR_WIDTH = "instructionAddressWidth";
    public static final String DATA_ADDR_WIDTH = "dataAddressWidth";
    public static final String PROGRAM_MEMORY = "programMemory"; // instruction memory data (pure numbers)
    public static final String DATA_MEMORY = "dataMemory"; // data memory (pure numbers)

    public enum CoreNames {
        BasicScalar, TestName, AnotherTest;

        @Override
        public String toString(){
            return name();
        }
    }

    Button advanceButton, runButton, stopButton, resetButton;

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
        final Bundle options = getIntent().getExtras();
        architecture = createSystemArchitecture(getComputeCore(options), options);
        initSystemArchitecture(architecture, options);
        pagerAdapter = createAdapter(architecture);

        /** Setup UI **/
        isRunning = false;

        advanceButton = (Button) findViewById(R.id.stepButton);
        runButton = (Button) findViewById(R.id.runButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setEnabled(false);

        advanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                runButton.setEnabled(false);

                advanceTime();
            }
        });

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                advanceButton.setEnabled(false);
                stopButton.setEnabled(true);

                isRunning = true;
                advanceTime();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);

                isRunning = false;
            }
        });

        resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                advanceButton.setEnabled(false);
                runButton.setEnabled(false);
                stopButton.setEnabled(false);

                isRunning = false;
                updateFragmentVisibility();
                initSystemArchitecture(architecture, options);
                updateSystemTime();
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
        else {
            advanceButton.setEnabled(true);
            runButton.setEnabled(true);
            Log.d(TAG, "onStepActionCompleted()");
        }

        Log.d(TAG, "isRunning = " + isRunning);
    }

    @Override
    public void onResetCompleted() {
        advanceButton.setEnabled(true);
        runButton.setEnabled(true);
        resetButton.setEnabled(true);
        Log.d(TAG, "onResetCompleted()");
    }

    private void advanceTime(){
        updateFragmentVisibility();

        // Initiate animations
        Log.d(TAG, "architecture.advanceTime() start");
        architecture.advanceTime();
        Log.d(TAG, "architecture.advanceTime() end");
        updateSystemTime();
    }

    private void updateSystemTime(){
        sysClockTextView.setText(String.valueOf(architecture.timeElapsed()));
    }

    private void updateFragmentVisibility(){
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
    }

    public static ComputeCore getComputeCore(Bundle options){
        String coreName = options.getString(CORE_NAME);
        int coreDataWidth = options.getInt(CORE_DATA_WIDTH);
        int instrAddrWidth = options.getInt(INSTR_ADDR_WIDTH);
        int dataAddrWidth = options.getInt(DATA_ADDR_WIDTH);

        // Create appropriate ComputeCore
        CoreNames coreType = Enum.valueOf(CoreNames.class, coreName);
        switch (coreType){
            case TestName:
            case AnotherTest:
                // TODO: implement above cores
            case BasicScalar:
                int byteMultiplierWidth; // log_2(coreDataWidth/8)
                switch (coreDataWidth){
                    case 16:
                        byteMultiplierWidth = 1;
                        break;
                    case 8:
                    default:
                        byteMultiplierWidth = 0;
                        break;
                }

                int stkAdWidth = 3;
                int dRegAdWidth = 3;
                int dAdrRegAdWidth = 1;
                int iAdrRegAdWidth = 1;

                return new BasicScalar(byteMultiplierWidth, dataAddrWidth, instrAddrWidth,
                        stkAdWidth,dRegAdWidth, dAdrRegAdWidth, iAdrRegAdWidth);
            default:
                return null;
        }
    }
}
