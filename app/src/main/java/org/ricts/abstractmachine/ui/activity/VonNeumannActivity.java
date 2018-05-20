package org.ricts.abstractmachine.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.core.AbstractUniMemoryComputeCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeCore;
import org.ricts.abstractmachine.components.observable.ObservableDecoderUnit;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableMultiMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableMultiplexer;
import org.ricts.abstractmachine.components.system.SystemArchitecture;
import org.ricts.abstractmachine.components.system.VonNeumannArchitecture;
import org.ricts.abstractmachine.ui.fragment.VonNeumannCoreFragment;
import org.ricts.abstractmachine.ui.fragment.VonNeumannSystemFragment;

public class VonNeumannActivity extends InspectActivity<AbstractUniMemoryComputeCore> {

    @Override
    protected SystemArchitecture createSystemArchitecture(AbstractUniMemoryComputeCore core, Bundle options) {
        return new VonNeumannArchitecture(core, 10); // TODO: change me
    }

    @Override
    protected void initSystemArchitecture(SystemArchitecture architecture, Bundle options) {
        VonNeumannArchitecture vonNeumannArchitecture = (VonNeumannArchitecture) architecture;
        vonNeumannArchitecture.reset();
        vonNeumannArchitecture.initMemory(options.getIntegerArrayList(PROGRAM_MEMORY));
    }

    @Override
    protected PagerAdapter createAdapter(SystemArchitecture architecture, ObservableDecoderUnit observableDecoderUnit) {
        return new SystemViewAdapter(getSupportFragmentManager(), this,
                (VonNeumannArchitecture) architecture, observableDecoderUnit);
    }

    private static class SystemViewAdapter extends FragmentPagerAdapter {
        private static final String TAG = "SystemViewAdapter";

        private ObservableComputeCore mainCore;
        private ObservableMemoryPort mainMemory;
        private ControlUnitCore cu;
        private ObservableMultiplexer muxSelect;
        private ObservableMultiMemoryPort muxPorts;
        private ObservableDecoderUnit decoderUnit;

        private String systemString, coreString;

        public SystemViewAdapter(FragmentManager fm, Context context,
                                 VonNeumannArchitecture architecture, ObservableDecoderUnit observableDecoderUnit) {
            super(fm);
            mainCore = architecture.getComputeCore();
            mainMemory = architecture.getMainMemory();
            cu = architecture.getControlUnit();
            muxSelect = architecture.getMultiplexer();
            muxPorts = architecture.getMultiplexerPorts();
            decoderUnit = observableDecoderUnit;

            systemString = context.getString(R.string.architecture_activity_system_label);
            coreString = context.getString(R.string.architecture_activity_core_label);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return VonNeumannSystemFragment.newInstance(mainCore, decoderUnit, mainMemory, cu);
                case 1:
                    return VonNeumannCoreFragment.newInstance(mainCore, decoderUnit, mainMemory, cu,
                            muxSelect, muxPorts);
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
