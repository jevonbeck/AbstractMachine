package org.ricts.abstractmachine.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.core.UniMemoryComputeCore;
import org.ricts.abstractmachine.components.compute.cu.ControlUnitCore;
import org.ricts.abstractmachine.components.observable.ObservableComputeCore;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableReadPort;
import org.ricts.abstractmachine.components.storage.ROM;
import org.ricts.abstractmachine.components.system.HarvardArchitecture;
import org.ricts.abstractmachine.components.system.SystemArchitecture;
import org.ricts.abstractmachine.ui.fragment.HarvardCoreFragment;
import org.ricts.abstractmachine.ui.fragment.HarvardSystemFragment;

/**
 * Created by Jevon on 13/08/2016.
 */
public class HarvardActivity extends InspectActivity<UniMemoryComputeCore> {
    @Override
    protected SystemArchitecture createSystemArchitecture(UniMemoryComputeCore core, Bundle options) {
        return new HarvardArchitecture(core, 10, 5); // TODO: change me
    }

    @Override
    protected void initSystemArchitecture(SystemArchitecture architecture, Bundle options) {
        HarvardArchitecture harvardArchitecture = (HarvardArchitecture) architecture;
        harvardArchitecture.reset();
        harvardArchitecture.initInstructionCache(options.getIntegerArrayList(PROGRAM_MEMORY));
        harvardArchitecture.initDataMemory(options.getIntegerArrayList(DATA_MEMORY));
    }

    @Override
    protected PagerAdapter createAdapter(SystemArchitecture architecture) {
        return new SystemViewAdapter(getSupportFragmentManager(), this, (HarvardArchitecture) architecture);
    }

    private static class SystemViewAdapter extends FragmentPagerAdapter {
        private static final String TAG = "SystemViewAdapter";

        private ObservableComputeCore mainCore;
        private ObservableReadPort<ROM> instructionCache;
        private ObservableMemoryPort dataMemory;
        private ControlUnitCore cu;

        private String systemString, coreString;

        public SystemViewAdapter(FragmentManager fm, Context context, HarvardArchitecture architecture) {
            super(fm);
            mainCore = architecture.getComputeCore();
            instructionCache = architecture.getInstructionCache();
            dataMemory = architecture.getDataMemory();
            cu = architecture.getControlUnit();

            systemString = context.getString(R.string.architecture_activity_system_label);
            coreString = context.getString(R.string.architecture_activity_core_label);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return HarvardSystemFragment.newInstance(mainCore, instructionCache, dataMemory, cu);
                case 1:
                    return HarvardCoreFragment.newInstance(mainCore, instructionCache, dataMemory, cu);
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
