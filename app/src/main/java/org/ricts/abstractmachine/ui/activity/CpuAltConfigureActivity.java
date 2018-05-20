package org.ricts.abstractmachine.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.fragment.CpuBasicsFragment;
import org.ricts.abstractmachine.ui.fragment.DataMemAltFragment;
import org.ricts.abstractmachine.ui.fragment.InstrMemAltFragment;
import org.ricts.abstractmachine.ui.utils.wizard.WizardActivity;
import org.ricts.abstractmachine.ui.utils.wizard.WizardFragment;

public class CpuAltConfigureActivity extends WizardActivity implements WizardFragment.DataSource,
        CpuBasicsFragment.PagerAdapterUpdater {
    private static final String TAG = "CpuConfigureActivity";

    @Override
    protected PagerAdapter createAdapter() {
        CpuConfigureAdapter adapter = new CpuConfigureAdapter(getSupportFragmentManager(), dataBundle);
        adapter.setHarvardString(getString(R.string.architecture_type_harvard));
        return adapter;
    }

    @Override
    protected Intent nextActivityIntent() {
        String archType = dataBundle.getString(InspectAltActivity.ARCH_TYPE);

        if(archType != null) {
            if (archType.equals(getString(R.string.architecture_type_von_neumann))) {
                return new Intent(this, VonNeumannAltActivity.class);
            } else if (archType.equals(getString(R.string.architecture_type_harvard))) {
                return new Intent(this, HarvardAltActivity.class);
            }
        }

        return null;
    }

    @Override
    public Bundle getWizardData() {
        return dataBundle;
    }

    @Override
    public void updateWizardPageCount() {
        pagerAdapter.notifyDataSetChanged();
    }

    private static class CpuConfigureAdapter extends FragmentStatePagerAdapter {
        private Bundle dataBun;
        private String harvardArchType;

        public CpuConfigureAdapter(FragmentManager fm, Bundle b) {
            super(fm);
            dataBun = b;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new CpuBasicsFragment();
                case 1:
                    return new InstrMemAltFragment();
                case 2:
                    return new DataMemAltFragment();
                    default:
                        return null;
            }
        }

        @Override
        public int getCount() {
            String archType = dataBun.getString(InspectAltActivity.ARCH_TYPE);
            if(archType != null){
                return archType.equals(harvardArchType) ? 3 : 2;
            }
            else {
                return 2;
            }
        }

        public void setHarvardString(String harvString){
            harvardArchType = harvString;
        }
    }
}
