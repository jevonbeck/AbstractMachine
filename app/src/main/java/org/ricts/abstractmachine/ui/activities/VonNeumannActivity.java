package org.ricts.abstractmachine.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.UniMemoryComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;
import org.ricts.abstractmachine.components.observables.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableMultiMemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableMultiplexer;
import org.ricts.abstractmachine.components.system.SystemArchitecture;
import org.ricts.abstractmachine.components.system.VonNeumannArchitecture;
import org.ricts.abstractmachine.ui.fragments.VonNeumannCoreFragment;
import org.ricts.abstractmachine.ui.fragments.VonNeumannSystemFragment;

public class VonNeumannActivity extends InspectActivity<UniMemoryComputeCore> {

    @Override
    protected SystemArchitecture createSystemArchitecture(UniMemoryComputeCore core, Bundle options) {
        return new VonNeumannArchitecture(core, 10); // TODO: change me
    }

    @Override
    protected void initSystemArchitecture(SystemArchitecture architecture, Bundle options) {
        VonNeumannArchitecture vonNeumannArchitecture = (VonNeumannArchitecture) architecture;
        vonNeumannArchitecture.reset();
        vonNeumannArchitecture.initMemory(options.getIntegerArrayList(PROGRAM_MEMORY));
    }

    @Override
    protected PagerAdapter createAdapter(SystemArchitecture architecture) {
        return new SystemViewAdapter(getSupportFragmentManager(), this, (VonNeumannArchitecture) architecture);
    }

    private static class SystemViewAdapter extends FragmentPagerAdapter {
        private static final String TAG = "SystemViewAdapter";

        private ObservableComputeCore mainCore;
        private ObservableMemoryPort mainMemory;
        private ObservableControlUnit cu;
        private ObservableMultiplexer muxSelect;
        private ObservableMultiMemoryPort muxPorts;

        private String systemString, coreString;

        public SystemViewAdapter(FragmentManager fm, Context context, VonNeumannArchitecture architecture) {
            super(fm);
            mainCore = architecture.getComputeCore();
            mainMemory = architecture.getMainMemory();
            cu = architecture.getControlUnit();
            muxSelect = architecture.getMultiplexer();
            muxPorts = architecture.getMultiplexerPorts();

            systemString = context.getString(R.string.architecture_activity_system_label);
            coreString = context.getString(R.string.architecture_activity_core_label);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return VonNeumannSystemFragment.newInstance(mainCore, mainMemory, cu);
                case 1:
                    return VonNeumannCoreFragment.newInstance(mainCore, mainMemory, cu, muxSelect, muxPorts);
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
                    return systemString;
                case 1:
                    return coreString;
                default:
                    return null;
            }
        }
    }
}
