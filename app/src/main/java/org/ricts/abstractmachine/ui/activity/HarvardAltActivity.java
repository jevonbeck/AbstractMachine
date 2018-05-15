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
import org.ricts.abstractmachine.components.observable.ObservableDecoderUnit;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableReadPort;
import org.ricts.abstractmachine.components.storage.ROM;
import org.ricts.abstractmachine.components.system.HarvardAltArchitecture;
import org.ricts.abstractmachine.components.system.SystemAltArchitecture;
import org.ricts.abstractmachine.ui.fragment.HarvardAltCoreFragment;
import org.ricts.abstractmachine.ui.fragment.HarvardAltSystemFragment;
import org.ricts.abstractmachine.ui.fragment.HarvardCoreFragment;

/**
 * Created by Jevon on 13/08/2016.
 */
public class HarvardAltActivity extends InspectAltActivity<UniMemoryComputeAltCore> {
    @Override
    protected SystemAltArchitecture createSystemArchitecture(UniMemoryComputeAltCore core, Bundle options) {
        return new HarvardAltArchitecture(core, 10, 5); // TODO: change me
    }

    @Override
    protected void initSystemArchitecture(SystemAltArchitecture architecture, Bundle options) {
        HarvardAltArchitecture harvardArchitecture = (HarvardAltArchitecture) architecture;
        harvardArchitecture.reset();
        harvardArchitecture.initInstructionCache(options.getIntegerArrayList(PROGRAM_MEMORY));
        harvardArchitecture.initDataMemory(options.getIntegerArrayList(DATA_MEMORY));
    }

    @Override
    protected PagerAdapter createAdapter(SystemAltArchitecture architecture, ObservableDecoderUnit observableDecoderUnit) {
        return new SystemViewAdapter(getSupportFragmentManager(), this,
                (HarvardAltArchitecture) architecture, observableDecoderUnit);
    }

    private static class SystemViewAdapter extends FragmentPagerAdapter {
        private static final String TAG = "SystemViewAdapter";

        private ObservableComputeAltCore mainCore;
        private ObservableReadPort<ROM> instructionCache;
        private ObservableMemoryPort dataMemory;
        private ObservableDecoderUnit decoderUnit;
        private ControlUnitAltCore cu;

        private String systemString, coreString;

        public SystemViewAdapter(FragmentManager fm, Context context,
                                 HarvardAltArchitecture architecture, ObservableDecoderUnit observableDecoderUnit) {
            super(fm);
            mainCore = architecture.getComputeCore();
            instructionCache = architecture.getInstructionCache();
            dataMemory = architecture.getDataMemory();
            cu = architecture.getControlUnit();
            decoderUnit = observableDecoderUnit;

            systemString = context.getString(R.string.architecture_activity_system_label);
            coreString = context.getString(R.string.architecture_activity_core_label);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return HarvardAltSystemFragment.newInstance(mainCore, decoderUnit, instructionCache, dataMemory, cu);
                case 1:
                    return HarvardAltCoreFragment.newInstance(mainCore, decoderUnit, instructionCache, dataMemory, cu);
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
