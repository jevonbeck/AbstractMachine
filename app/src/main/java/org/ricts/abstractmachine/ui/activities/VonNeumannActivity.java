package org.ricts.abstractmachine.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.devices.compute.core.BasicScalar;
import org.ricts.abstractmachine.devices.compute.core.BasicScalarEnums;
import org.ricts.abstractmachine.ui.fragments.VonNeumannActivityFragment;
import org.ricts.abstractmachine.ui.fragments.VonNeumannCoreFragment;
import org.ricts.abstractmachine.ui.fragments.VonNeumannSystemFragment;

import java.util.ArrayList;

public class VonNeumannActivity extends AppCompatActivity implements VonNeumannActivityFragment.StepActionListener {
    private static final String TAG = "VonNeumannActivity";

    private ViewPager pager;
    private SystemViewAdapter pagerAdapter;
    private int pagerAdapterCount, pagerOffScreenLimit;

    private ObservableControlUnit controlUnit;
    private TextView sysClockTextView;
    private int sysClock; // system clock

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_von_neumann);

        int byteMultiplierWidth = 0;
        int dAdWidth = 3;
        int iAdWidth = 3;

        int stkAdWidth = 3;
        int dRegAdWidth = 3;
        int dAdrRegAdWidth = 1;
        int iAdrRegAdWidth = 1;

        BasicScalar core = new BasicScalar(byteMultiplierWidth, dAdWidth, iAdWidth,
                stkAdWidth,dRegAdWidth, dAdrRegAdWidth, iAdrRegAdWidth);

        ArrayList<Integer> memData = new ArrayList<Integer>();
        int [] operands;

        // JUMP 0x2
        operands = new int[1];
        operands[0] = 2;
        memData.add(core.encodeInstruction(BasicScalarEnums.InstrAddressLiteral.enumName(),
                BasicScalarEnums.InstrAddressLiteral.JUMP.name(), operands));

        memData.add(core.nopInstruction()); // NOP instruction

        // LOAD R3, 1 ; R3 <-- 1
        operands = new int[2];
        operands[0] = 3;
        operands[1] = 1;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataAssignLit.enumName(),
                BasicScalarEnums.DataAssignLit.LOAD.name(), operands));

        // LOAD R4, 7 ; R4 <-- 7
        operands[0] = 4;
        operands[1] = 7;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataAssignLit.enumName(),
                BasicScalarEnums.DataAssignLit.LOAD.name(), operands));

        // STOREA R3, A0 ; A0 <-- R3
        operands[0] = 3;
        operands[1] = 0;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataMemOps.enumName(),
                BasicScalarEnums.DataMemOps.STOREA.name(), operands));

        // ADD R5, R3, R4 ; R5 <-- R3 + R4
        operands = new int[3];
        operands[0] = 5;
        operands[1] = 3;
        operands[2] = 4;
        memData.add(core.encodeInstruction(BasicScalarEnums.AluOps.enumName(),
                BasicScalarEnums.AluOps.ADD.name(), operands));

        // STOREM R5, A0 ; MEM[A0] <-- R5
        operands = new int[2];
        operands[0] = 5;
        operands[1] = 0;
        memData.add(core.encodeInstruction(BasicScalarEnums.DataMemOps.enumName(),
                BasicScalarEnums.DataMemOps.STOREM.name(), operands));

        RAM memory = new RAM(core.instrWidth(), core.iAddrWidth(), 10);
        memory.setData(memData, 0);

        ObservableComputeCore mainCore = new ObservableComputeCore<BasicScalar>(core);
        ObservableRAM observableRAM = new ObservableRAM(memory);
        controlUnit = new ObservableControlUnit(
                new ControlUnit(mainCore, observableRAM, observableRAM));

        Button advanceButton = (Button) findViewById(R.id.stepButton);
        advanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                advanceTime();
            }
        });

        sysClock = 0;
        sysClockTextView = (TextView) findViewById(R.id.sysClockText);
        sysClockTextView.setText(String.valueOf(sysClock));

        pagerAdapter = new SystemViewAdapter(getSupportFragmentManager(),
                mainCore, observableRAM, controlUnit);

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
            ((VonNeumannActivityFragment) pagerAdapter.instantiateItem(pager, x))
                    .setUserVisibility(x == currentItemIndex);
        }

        // Initiate animations
        int result = controlUnit.nextActionDuration();
        controlUnit.performNextAction();

        sysClock += result;
        sysClockTextView.setText(String.valueOf(sysClock));
    }

    @Override
    public void onStepActionCompleted() {

    }

    private static class SystemViewAdapter extends FragmentPagerAdapter {
        private static final String TAG = "SystemViewAdapter";

        private ObservableComputeCore mainCore;
        private ObservableRAM mainMemory;
        private ObservableControlUnit cu;

        public SystemViewAdapter(FragmentManager fm, ObservableComputeCore core, ObservableRAM memory,
                                 ObservableControlUnit fsmData) {
            super(fm);
            mainCore = core;
            mainMemory = memory;
            cu = fsmData;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return VonNeumannSystemFragment.newInstance(mainCore, mainMemory, cu);
                case 1:
                    return VonNeumannCoreFragment.newInstance(mainCore, mainMemory, cu);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public String getPageTitle(int position){
            switch (position){
                case 0:
                    return "System";
                case 1:
                    return "Core";
                default:
                    return null;
            }
        }
    }
}
