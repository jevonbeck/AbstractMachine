package org.ricts.abstractmachine.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.ui.fragments.MemFragment;

/**
 * Created by Jevon on 05/01/2017.
 */

public class InstrMemoryDialogActivity extends MemoryContentsDialogActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instr_mem_dialog);

        /** Setup main data variables **/
        Bundle dataBundle = getIntent().getExtras();
        final ComputeCore mainCore = InspectActivity.getComputeCore(dataBundle);
        final MemFragment.AssemblyMemoryData memoryData = dataBundle.getParcelable(MEM_DATA_KEY);
        final int memoryAddress = dataBundle.getInt(MEM_ADDR_KEY);
        final String memoryType = dataBundle.getString(MEM_TYPE_KEY);

        /** Setup UI interactions and initialise UI **/
        // TODO: find a way to do hex-only input (soft keyboard?!)
        final EditText operandOneEditText = (EditText) findViewById(R.id.operandOneEditText);
        final EditText operandTwoEditText = (EditText) findViewById(R.id.operandTwoEditText);
        final EditText operandThreeEditText = (EditText) findViewById(R.id.operandThreeEditText);
        final EditText operandFourEditText = (EditText) findViewById(R.id.operandFourEditText);

        final EditText labelEditText = (EditText) findViewById(R.id.labelEditText);
        labelEditText.setText(memoryData.getLabel());

        final EditText commentEditText = (EditText) findViewById(R.id.commentEditText);
        commentEditText.setText(memoryData.getComment());

        final RadioGroup instrGroup = (RadioGroup) findViewById(R.id.instrRadioGroup);
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
        String comparisonMneumonic = memoryData.getMneumonic();
        for(String mneumonic : mainCore.getMneumonicList()){
            addRadioButtonWithText(mneumonic, instrGroup, comparisonMneumonic);
        }

        // Add additional RadioButton for pure data entries (useful in Von Neumann scenario)
        addRadioButtonWithText(DATA_MNEUMONIC, instrGroup, comparisonMneumonic);

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                sendMemoryData(view.getContext(), memoryAddress, memoryData, memoryType);
                finish();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void addRadioButtonWithText(String mneumonic, RadioGroup instrGroup,
                                        String comparisonMneumonic){
        RadioButton button = new RadioButton(this);
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
