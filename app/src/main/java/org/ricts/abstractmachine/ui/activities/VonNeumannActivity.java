package org.ricts.abstractmachine.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.system.SystemArchitecture;
import org.ricts.abstractmachine.components.system.VonNeumannArchitecture;
import org.ricts.abstractmachine.ui.fragments.VonNeumannCoreFragment;
import org.ricts.abstractmachine.ui.fragments.VonNeumannSystemFragment;

public class VonNeumannActivity extends InspectActivity {

    @Override
    protected SystemArchitecture createSystemArchitecture(ComputeCore core, Bundle options) {
        return new VonNeumannArchitecture(core, 10);
    }

    @Override
    protected void initSystemArchitecture(SystemArchitecture architecture, Bundle options) {
        VonNeumannArchitecture vonNeumannArchitecture = (VonNeumannArchitecture) architecture;
        vonNeumannArchitecture.initMemory(options.getIntegerArrayList(PROGRAM_MEMORY));
    }

    @Override
    protected PagerAdapter createAdapter(SystemArchitecture architecture) {
        return new SystemViewAdapter(getSupportFragmentManager(), (VonNeumannArchitecture) architecture);
    }

    private static class SystemViewAdapter extends FragmentPagerAdapter {
        private static final String TAG = "SystemViewAdapter";

        private ObservableComputeCore mainCore;
        private ObservableRAM mainMemory;
        private ObservableControlUnit cu;

        public SystemViewAdapter(FragmentManager fm, VonNeumannArchitecture architecture) {
            super(fm);
            mainCore = architecture.getComputeCore();
            mainMemory = architecture.getMainMemory();
            cu = architecture.getControlUnit();
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
