package org.ricts.abstractmachine.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.compute.isa.OperandInfo;

/**
 * Created by Jevon on 02/07/2016.
 */
public class InstrMemFragment extends MemFragment {
    @Override
    protected String memoryTypeString() {
        return "instr";
    }

    @Override
    protected int titleStringResource() {
        return R.string.program_memory_title;
    }

    @Override
    protected AssemblyMemoryData.MemoryType memoryType() {
        return AssemblyMemoryData.MemoryType.INSTRUCTION;
    }

    @Override
    protected MemoryContentsDialogFragment getMemoryContentsDialogFragment(int position,
            AssemblyMemoryData data, ComputeCore core, MemoryContentsDialogFragment.ListUpdater updater) {
        return InstrMemoryDialogFragment.newInstance(position, data, core, updater);
    }

    public static class InstrMemoryDialogFragment extends MemoryContentsDialogFragment {
        
        public InstrMemoryDialogFragment(){
            // Required empty public constructor
        }

        public static InstrMemoryDialogFragment newInstance(int position, AssemblyMemoryData data, ComputeCore core,
                                                            ListUpdater updater){
            InstrMemoryDialogFragment fragment = new InstrMemoryDialogFragment();
            fragment.init(position, data, core, updater);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Pass null as the parent view because its going in the dialog layout
            // TODO: create mainView from scratch
            final View mainView = inflater.inflate(R.layout.fragment_instr_mem_dialog, null);

            /** Setup UI interactions and initialise UI **/
            // TODO: find a way to do hex-only input (soft keyboard?!)
            final EditText operandOneEditText = (EditText) mainView.findViewById(R.id.operandOneEditText);
            final EditText operandTwoEditText = (EditText) mainView.findViewById(R.id.operandTwoEditText);
            final EditText operandThreeEditText = (EditText) mainView.findViewById(R.id.operandThreeEditText);
            final EditText operandFourEditText = (EditText) mainView.findViewById(R.id.operandFourEditText);

            final EditText labelEditText = (EditText) mainView.findViewById(R.id.labelEditText);
            labelEditText.setText(memoryData.getLabel());

            final EditText commentEditText = (EditText) mainView.findViewById(R.id.commentEditText);
            commentEditText.setText(memoryData.getComment());

            final RadioGroup instrGroup = (RadioGroup) mainView.findViewById(R.id.instrRadioGroup);
            instrGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    RadioButton button = (RadioButton) radioGroup.findViewById(checkedId);

                    String mneumonic = button.getText().toString();
                    int operandCount = mneumonic.equals(DATA_MNEUMONIC) ?
                            1 : mainCore.getOperandInfoArray(mneumonic).length;

                    // update visibility of EditTexts to only enter appropriate number of operands
                    switch (operandCount){
                        case 4:
                            operandOneEditText.setEnabled(true);
                            operandOneEditText.setVisibility(View.VISIBLE);
                            operandTwoEditText.setVisibility(View.VISIBLE);
                            operandThreeEditText.setVisibility(View.VISIBLE);
                            operandFourEditText.setVisibility(View.VISIBLE);
                            break;
                        case 3:
                            operandOneEditText.setEnabled(true);
                            operandOneEditText.setVisibility(View.VISIBLE);
                            operandTwoEditText.setVisibility(View.VISIBLE);
                            operandThreeEditText.setVisibility(View.VISIBLE);
                            operandFourEditText.setVisibility(View.GONE);
                            break;
                        case 2:
                            operandOneEditText.setEnabled(true);
                            operandOneEditText.setVisibility(View.VISIBLE);
                            operandTwoEditText.setVisibility(View.VISIBLE);
                            operandThreeEditText.setVisibility(View.GONE);
                            operandFourEditText.setVisibility(View.GONE);
                            break;
                        case 1:
                            operandOneEditText.setEnabled(true);
                            operandOneEditText.setVisibility(View.VISIBLE);
                            operandTwoEditText.setVisibility(View.GONE);
                            operandThreeEditText.setVisibility(View.GONE);
                            operandFourEditText.setVisibility(View.GONE);
                            break;
                        case 0:
                            operandOneEditText.setEnabled(false);
                            operandOneEditText.setVisibility(View.VISIBLE);
                            operandTwoEditText.setVisibility(View.GONE);
                            operandThreeEditText.setVisibility(View.GONE);
                            operandFourEditText.setVisibility(View.GONE);
                            break;
                    }

                    // update EditText text
                    int [] operands = memoryData.getOperands();
                    for(int x=0; x < operands.length; ++x){
                        String text = Integer.toHexString(operands[x]);
                        switch (x){
                            case 0:
                                operandOneEditText.setText(text);
                                break;
                            case 1:
                                operandTwoEditText.setText(text);
                                break;
                            case 2:
                                operandThreeEditText.setText(text);
                                break;
                            case 3:
                                operandFourEditText.setText(text);
                                break;
                        }
                    }
                }
            });

            // Populate RadioGroup with instruction set
            Context context = getContext();
            String comparisonMneumonic = memoryData.getMneumonic();
            for(String mneumonic : mainCore.getMneumonicList()){
                addRadioButtonWithText(mneumonic, instrGroup, comparisonMneumonic, context);
            }

            // Add additional RadioButton for pure data entries (useful in Von Neumann scenario)
            addRadioButtonWithText(DATA_MNEUMONIC, instrGroup, comparisonMneumonic, context);

            /** Actually create the dialog **/
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(mainView)
                    // Add action buttons
                    .setPositiveButton(R.string.positive_button_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            /** save instruction data for location in adapter **/
                            RadioButton button = (RadioButton) instrGroup.findViewById(
                                    instrGroup.getCheckedRadioButtonId());
                            String mneumonic = button.getText().toString();

                            int [] operands;
                            int encodedInstruction;
                            String instructionText;
                            if(mneumonic.equals(DATA_MNEUMONIC)){
                                encodedInstruction = getSafeInt(operandOneEditText, mainCore.getDataOperandInfo());
                                instructionText = DATA_MNEUMONIC + " " +
                                        mainCore.instrValueString(encodedInstruction);
                                operands = new int[1];
                                operands[0] = encodedInstruction;
                            }
                            else{
                                OperandInfo[] operandInfoArray = mainCore.getOperandInfoArray(mneumonic);
                                int operandCount = operandInfoArray.length;
                                operands = new int[operandCount];
                                for(int x=0; x < operandCount; ++x){
                                    int editTextValue = -1;
                                    OperandInfo operandInfo = operandInfoArray[x];
                                    switch (x){
                                        case 0:
                                            editTextValue = getSafeInt(operandOneEditText, operandInfo);
                                            break;
                                        case 1:
                                            editTextValue = getSafeInt(operandTwoEditText, operandInfo);
                                            break;
                                        case 2:
                                            editTextValue = getSafeInt(operandThreeEditText, operandInfo);
                                            break;
                                        case 3:
                                            editTextValue = getSafeInt(operandFourEditText, operandInfo);
                                            break;
                                    }
                                    operands[x] = editTextValue;
                                }

                                encodedInstruction = mainCore.encodeInstruction(mneumonic, operands);
                                instructionText = mainCore.instrString(encodedInstruction);
                            }

                            // update instruction address mapping, if any
                            OperandInfo instrOpInfo = mainCore.getInstrAddrOperandInfo();
                            String labelText = labelEditText.getText().toString();
                            if(!labelText.equals("")){
                                instrOpInfo.addMapping(labelText, memoryAddress);
                            }
                            else {
                                instrOpInfo.removeMapping(memoryAddress);
                            }

                            // update instruction data in list
                            memoryData.setMneumonic(mneumonic);
                            memoryData.setOperands(operands);
                            memoryData.setMemoryContents(instructionText);
                            memoryData.setNumericValue(mainCore.instrValueString(encodedInstruction));
                            memoryData.setLabel(labelText);
                            memoryData.setComment(commentEditText.getText().toString());
                            mUpdater.notifyUpdate();
                        }
                    })
                    .setNegativeButton(R.string.negative_button_text, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            InstrMemoryDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }

        private void addRadioButtonWithText(String mneumonic, RadioGroup instrGroup,
                                            String comparisonMneumonic, Context context){
            RadioButton button = new RadioButton(context);
            button.setText(mneumonic);

            // add button to RadioGroup...
            instrGroup.addView(button, -1,
                    new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT));

            // ... then, check/select button with current mneumonic
            if(mneumonic.equals(comparisonMneumonic)){
                button.setChecked(true); // this triggers visibility of EditTexts
            }
        }
    }
}
