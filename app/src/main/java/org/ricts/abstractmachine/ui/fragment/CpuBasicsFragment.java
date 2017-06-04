package org.ricts.abstractmachine.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.activity.InspectActivity;
import org.ricts.abstractmachine.ui.activity.InspectActivity.CoreNames;
import org.ricts.abstractmachine.ui.utils.wizard.WizardFragment;

public class CpuBasicsFragment extends WizardFragment {
    private static final String TAG = "CpuBasicsFragment";

    private TextView dataAddrWidthLabel;
    private RadioGroup archRadiGroup, coreTypeRadioGroup,
            bitWidthRadioGroup, instrAddrWidthRadioGroup, dataAddrWidthRadioGroup;
    private Spinner coreSpinner;
    private boolean viewsAvailable = false;

    private static final String CORE_TYPE = "coreType"; // key for core type (only useful in this UI)
    private static final CoreNames [] registerCores = {CoreNames.BasicScalar};
    private static final CoreNames [] accumulatorCores = {CoreNames.AnotherTest}; // TODO: put correct values
    private static final CoreNames [] stackCores = {CoreNames.TestName}; // TODO: put correct values
    private ArrayAdapter<CoreNames> registerCoreAdapter, accumCoreAdapter, stackCoreAdapter;

    protected PagerAdapterUpdater mUpdater;

    public interface PagerAdapterUpdater {
        void updateWizardPageCount();
    }

    public CpuBasicsFragment() {
        // Required empty public constructor
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

        dataAddrWidthLabel = (TextView) rootView.findViewById(R.id.dataAddrWidthLabel);
        dataAddrWidthRadioGroup = (RadioGroup) rootView.findViewById(R.id.dataAddrWidthRadioGroup);

        archRadiGroup = (RadioGroup) rootView.findViewById(R.id.archRadioGroup);
        archRadiGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId){
                    case R.id.harvardButton:
                        dataAddrWidthLabel.setVisibility(View.VISIBLE);
                        dataAddrWidthRadioGroup.setVisibility(View.VISIBLE);
                        break;
                    case R.id.vonNeumannButton:
                        dataAddrWidthLabel.setVisibility(View.GONE);
                        dataAddrWidthRadioGroup.setVisibility(View.GONE);
                        break;
                }
            }
        });

        bitWidthRadioGroup = (RadioGroup) rootView.findViewById(R.id.bitWidthRadioGroup);
        instrAddrWidthRadioGroup = (RadioGroup) rootView.findViewById(R.id.instrAddrWidthRadioGroup);

        viewsAvailable = true;

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mUpdater = null;
    }

    @Override
    public void restorePageData(Bundle bundle) {
        // TODO: review likelihood of 'viewsAvailable' being false
        if(viewsAvailable){
            CharSequence archType = bundle.getCharSequence(InspectActivity.ARCH_TYPE);
            if(archType.equals(getString(R.string.architecture_type_von_neumann))){
                archRadiGroup.check(R.id.vonNeumannButton);
            }
            else if(archType.equals(getString(R.string.architecture_type_harvard))){
                archRadiGroup.check(R.id.harvardButton);

                switch (bundle.getInt(InspectActivity.DATA_ADDR_WIDTH)){
                    case 3:
                        dataAddrWidthRadioGroup.check(R.id.dataThreeBitButton);
                        break;
                    case 4:
                        dataAddrWidthRadioGroup.check(R.id.dataFourBitButton);
                        break;
                    case 5:
                        dataAddrWidthRadioGroup.check(R.id.dataFiveBitButton);
                        break;
                    case 6:
                        dataAddrWidthRadioGroup.check(R.id.dataSixBitButton);
                        break;
                    default:
                }
            }

            CharSequence coreType = bundle.getCharSequence(CORE_TYPE);
            if(coreType.equals(getString(R.string.compute_core_type_register))){
                coreTypeRadioGroup.check(R.id.registerTypeButton);
            }
            else if(coreType.equals(getString(R.string.compute_core_type_acc))){
                coreTypeRadioGroup.check(R.id.accumTypeButton);
            }
            else { // coreType.equals(getString(R.string.compute_core_type_stack))
                coreTypeRadioGroup.check(R.id.stackTypeButton);
            }

            switch (bundle.getInt(InspectActivity.CORE_DATA_WIDTH)){
                case 8:
                    bitWidthRadioGroup.check(R.id.eightBitButton);
                    break;
                case 16:
                    bitWidthRadioGroup.check(R.id.sixteenBitButton);
                    break;
                default:
            }

            switch (bundle.getInt(InspectActivity.INSTR_ADDR_WIDTH)){
                case 3:
                    instrAddrWidthRadioGroup.check(R.id.insThreeBitButton);
                    break;
                case 4:
                    instrAddrWidthRadioGroup.check(R.id.insFourBitButton);
                    break;
                case 5:
                    instrAddrWidthRadioGroup.check(R.id.insFiveBitButton);
                    break;
                case 6:
                    instrAddrWidthRadioGroup.check(R.id.insSixBitButton);
                    break;
                default:
            }

            String coreName = bundle.getString(InspectActivity.CORE_NAME);
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
        bundle.putCharSequence(CORE_TYPE, getSelectedButtonText(coreTypeRadioGroup));

        bundle.putCharSequence(InspectActivity.ARCH_TYPE, getSelectedButtonText(archRadiGroup));
        bundle.putString(InspectActivity.CORE_NAME, getComputeCoreName());
        bundle.putInt(InspectActivity.CORE_DATA_WIDTH, Integer.valueOf(
                getSelectedButtonText(bitWidthRadioGroup).toString() ));

        int instrMemSize = Integer.valueOf( getSelectedButtonText(instrAddrWidthRadioGroup).toString() );
        int instrAddrWidth = getAddressWidthFromSize(instrMemSize);
        bundle.putInt(InspectActivity.INSTR_ADDR_WIDTH, instrAddrWidth);

        switch (archRadiGroup.getCheckedRadioButtonId()){
            case R.id.vonNeumannButton:
                bundle.putInt(InspectActivity.DATA_ADDR_WIDTH, instrAddrWidth);
                break;
            case R.id.harvardButton:
                int dataMemSize = Integer.valueOf( getSelectedButtonText(dataAddrWidthRadioGroup).toString() );
                bundle.putInt(InspectActivity.DATA_ADDR_WIDTH, getAddressWidthFromSize(dataMemSize) );
                break;
        }

        // update activity pagerAdapter
        mUpdater.updateWizardPageCount();
    }

    @Override
    public void updatePage(Bundle bundle) {
        // No need to implement as this is the first page!
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

    private int getAddressWidthFromSize(int memSize){
        switch (memSize){
            case 8:
                return 3;
            case 16:
                return 4;
            case 32:
                return 5;
            case 64:
                return 6;
            default:
                return 0;
        }
    }
}
