package org.ricts.abstractmachine.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.fragments.CpuBasicsFragment;
import org.ricts.abstractmachine.ui.utils.wizard.WizardActivity;

public class CpuConfigureActivity extends WizardActivity implements CpuBasicsFragment.PagerAdapterUpdater {
    private static final String TAG = "CpuConfigureActivity";

    public static final String ARCH_TYPE = "architectureType";
    public static final String CORE_TYPE = "coreType";
    public static final String CORE_NAME = "coreName";
    public static final String CORE_DATA_WIDTH = "coreDataWidth";

    @Override
    public void updateWizardPageCount() {
        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    protected PagerAdapter createAdapter() {
        CpuConfigureAdapter adapter = new CpuConfigureAdapter(getSupportFragmentManager(), dataBundle);
        adapter.setHarvardString(getString(R.string.architecture_type_harvard));
        return adapter;
    }

    @Override
    protected Intent nextActivityIntent() {
        String archType = dataBundle.getString(ARCH_TYPE);

        if(archType.equals(getString(R.string.architecture_type_von_neumann))){
            return new Intent(this, VonNeumannActivity.class);
        }
        else if(archType.equals(getString(R.string.architecture_type_harvard))){
            return new Intent(this, VonNeumannActivity.class); // TODO: replace with Harvard when created
        }

        return null;
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
                    return new CpuBasicsFragment(); // TODO: change me!!!
                    //return new InstrMemFragment();
                case 2:
                    return new CpuBasicsFragment(); // TODO: change me too!!!
                    default:
                        return null;
            }
        }

        @Override
        public int getCount() {
            if(dataBun.getString(ARCH_TYPE) != null){
                return dataBun.getString(ARCH_TYPE).equals(harvardArchType) ? 3 : 2;
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