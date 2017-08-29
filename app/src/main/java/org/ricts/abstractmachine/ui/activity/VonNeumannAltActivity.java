package org.ricts.abstractmachine.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.core.UniMemoryComputeAltCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitAltCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableMultiMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableMultiplexer;
import org.ricts.abstractmachine.components.system.SystemAltArchitecture;
import org.ricts.abstractmachine.components.system.VonNeumannAltArchitecture;
import org.ricts.abstractmachine.ui.fragment.VonNeumannAltCoreFragment;
import org.ricts.abstractmachine.ui.fragment.VonNeumannAltSystemFragment;

public class VonNeumannAltActivity extends InspectAltActivity<UniMemoryComputeAltCore> {

    @Override
    protected SystemAltArchitecture createSystemArchitecture(UniMemoryComputeAltCore core, Bundle options) {
        return new VonNeumannAltArchitecture(core, 10); // TODO: change me
    }

    @Override
    protected void initSystemArchitecture(SystemAltArchitecture architecture, Bundle options) {
        VonNeumannAltArchitecture vonNeumannArchitecture = (VonNeumannAltArchitecture) architecture;
        vonNeumannArchitecture.reset();
        vonNeumannArchitecture.initMemory(options.getIntegerArrayList(PROGRAM_MEMORY));
    }

    @Override
    protected PagerAdapter createAdapter(SystemAltArchitecture architecture) {
        return new SystemViewAdapter(getSupportFragmentManager(), this, (VonNeumannAltArchitecture) architecture);
    }

    private static class SystemViewAdapter extends FragmentPagerAdapter {
        private static final String TAG = "SystemViewAdapter";

        private ObservableComputeAltCore mainCore;
        private ObservableMemoryPort mainMemory;
        private ControlUnitAltCore cu;
        private ObservableMultiplexer muxSelect;
        private ObservableMultiMemoryPort muxPorts;

        private String systemString, coreString;

        public SystemViewAdapter(FragmentManager fm, Context context, VonNeumannAltArchitecture architecture) {
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
                    return VonNeumannAltSystemFragment.newInstance(mainCore, mainMemory, cu);
                case 1:
                    return VonNeumannAltCoreFragment.newInstance(mainCore, mainMemory, cu, muxSelect, muxPorts);
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
