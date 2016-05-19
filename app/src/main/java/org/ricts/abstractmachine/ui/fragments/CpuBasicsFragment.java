package org.ricts.abstractmachine.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.activities.CpuConfigureActivity;
import org.ricts.abstractmachine.ui.utils.wizard.WizardFragmentInterface;

public class CpuBasicsFragment extends Fragment implements WizardFragmentInterface {
    private static final String TAG = "CpuBasicsFragment";

    public enum CoreNames {
        BasicScalar, TestName, AnotherTest;

        @Override
        public String toString(){
            return name();
        }
    }

    private RadioGroup archRadiGroup, coreTypeRadioGroup, bitWidthRadioGroup;
    private Spinner coreSpinner;
    private boolean viewsAvailable = false;

    private static final CoreNames [] registerCores = {CoreNames.BasicScalar};
    private static final CoreNames [] accumulatorCores = {CoreNames.AnotherTest}; // TODO: put correct values
    private static final CoreNames [] stackCores = {CoreNames.TestName}; // TODO: put correct values
    private ArrayAdapter<CoreNames> registerCoreAdapter, accumCoreAdapter, stackCoreAdapter;

    protected PagerAdapterUpdater mUpdater;

    public interface PagerAdapterUpdater {
        void updateWizardPageCount();
    }

    @Override
    public void restorePageData(Bundle bundle) {
        if(viewsAvailable){
            CharSequence archType = bundle.getCharSequence(CpuConfigureActivity.ARCH_TYPE);
            if(archType.equals(getString(R.string.architecture_type_von_neumann))){
                archRadiGroup.check(R.id.vonNeumannButton);
            }
            else if(archType.equals(getString(R.string.architecture_type_harvard))){
                archRadiGroup.check(R.id.harvardButton);
            }

            CharSequence coreType = bundle.getCharSequence(CpuConfigureActivity.CORE_TYPE);
            if(coreType.equals(getString(R.string.compute_core_type_register))){
                coreTypeRadioGroup.check(R.id.registerTypeButton);
            }
            else if(coreType.equals(getString(R.string.compute_core_type_acc))){
                coreTypeRadioGroup.check(R.id.accumTypeButton);
            }
            else { // coreType.equals(getString(R.string.compute_core_type_stack))
                coreTypeRadioGroup.check(R.id.stackTypeButton);
            }

            int dataWidth = bundle.getInt(CpuConfigureActivity.CORE_DATA_WIDTH);
            switch (dataWidth){
                case 8:
                    bitWidthRadioGroup.check(R.id.eightBitButton);
                    break;
                case 16:
                    bitWidthRadioGroup.check(R.id.sixteenBitButton);
                    break;
                default:
            }

            String coreName = bundle.getString(CpuConfigureActivity.CORE_NAME);
            SpinnerAdapter spinAdapter = coreSpinner.getAdapter();
            int itemCount = spinAdapter.getCount();

            int index = 0; // default index
            for(int x=0; x < itemCount; ++x){
                String item = spinAdapter.getItem(x).toString();
                if(coreName.equals(item)){
                    index = x;
                    break;
                }
            }
            coreSpinner.setSelection(index);
        }
    }

    @Override
    public void savePageData(Bundle bundle) {
        bundle.putCharSequence(CpuConfigureActivity.ARCH_TYPE, getSelectedButtonText(archRadiGroup));
        bundle.putCharSequence(CpuConfigureActivity.CORE_TYPE, getSelectedButtonText(coreTypeRadioGroup));
        bundle.putString(CpuConfigureActivity.CORE_NAME, getComputeCoreName());
        bundle.putInt(CpuConfigureActivity.CORE_DATA_WIDTH, Integer.valueOf(
                getSelectedButtonText(bitWidthRadioGroup).toString() ));

        // update activity pagerAdapter
        mUpdater.updateWizardPageCount();
    }

    public CpuBasicsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CpuBasicsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CpuBasicsFragment newInstance(Bundle args) {
        CpuBasicsFragment fragment = new CpuBasicsFragment();
        fragment.restorePageData(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PagerAdapterUpdater) {
            mUpdater = (PagerAdapterUpdater) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PagerAdapterUpdater");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_cpu_basics, container, false);

        Context context = rootView.getContext();
        // create adapters for ComputeCore Spinner
        registerCoreAdapter = new ArrayAdapter<CoreNames>(context,
                android.R.layout.simple_spinner_item, registerCores);
        accumCoreAdapter = new ArrayAdapter<CoreNames>(context,
                android.R.layout.simple_spinner_item, accumulatorCores);
        stackCoreAdapter = new ArrayAdapter<CoreNames>(context,
                android.R.layout.simple_spinner_item, stackCores);

        // set coreSpinner adapter based on selected button in coreTypeRadioGroup
        coreSpinner = (Spinner) rootView.findViewById(R.id.coreSpinner);
        coreTypeRadioGroup = (RadioGroup) rootView.findViewById(R.id.coreTypeRadioGroup);
        coreTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // update coreSpinner data according to 'checkedId'
                setCoreSpinnerAdapter(checkedId);
                coreSpinner.setSelection(0);
            }
        });
        setCoreSpinnerAdapter(coreTypeRadioGroup.getCheckedRadioButtonId());

        archRadiGroup = (RadioGroup) rootView.findViewById(R.id.archRadioGroup);
        bitWidthRadioGroup = (RadioGroup) rootView.findViewById(R.id.bitWidthRadioGroup);

        viewsAvailable = true;

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mUpdater = null;
    }

    private CharSequence getSelectedButtonText(RadioGroup radioGroup){
        RadioButton selectedButton = (RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
        return selectedButton.getText();
    }

    private String getComputeCoreName(){
        return coreSpinner.getSelectedItem().toString();
    }

    private void setCoreSpinnerAdapter(int checkedId){
        switch (checkedId){
            case R.id.registerTypeButton:
                coreSpinner.setAdapter(registerCoreAdapter);
                break;
            case R.id.accumTypeButton:
                coreSpinner.setAdapter(accumCoreAdapter);
                break;
            case R.id.stackTypeButton:
                coreSpinner.setAdapter(stackCoreAdapter);
                break;
            default:
                break;
        }
    }
}
