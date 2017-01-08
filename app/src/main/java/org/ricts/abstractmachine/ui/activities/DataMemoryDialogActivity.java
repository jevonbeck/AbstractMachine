package org.ricts.abstractmachine.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.ui.fragments.MemFragment;

/**
 * Created by Jevon on 05/01/2017.
 */

public class DataMemoryDialogActivity extends MemoryContentsDialogActivity {
    private static final String TAG = "DataMemoryDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_mem_dialog);

        /** Setup main data variables **/
        Bundle dataBundle = getIntent().getExtras();
        final ComputeCore mainCore = InspectActivity.getComputeCore(dataBundle);
        final MemFragment.AssemblyMemoryData memoryData = dataBundle.getParcelable(MEM_DATA_KEY);
        final int memoryAddress = dataBundle.getInt(MEM_ADDR_KEY);
        final String memoryType = dataBundle.getString(MEM_TYPE_KEY);

        /** Setup UI interactions and initialise UI **/
        // TODO: find a way to do hex-only input (soft keyboard?!)
        final EditText operandOneEditText = (EditText) findViewById(R.id.operandOneEditText);

        final EditText labelEditText = (EditText) findViewById(R.id.labelEditText);
        labelEditText.setText(memoryData.getLabel());

        final EditText commentEditText = (EditText) findViewById(R.id.commentEditText);
        commentEditText.setText(memoryData.getComment());

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** save memoryContents data for location in adapter **/
                int dataValue = getSafeInt(operandOneEditText, mainCore.getDataOperandInfo());
                Log.d(TAG, "dataValue = " + dataValue);

                String dataValueText = mainCore.dataValueString(dataValue);
                String dataText = DATA_MNEUMONIC + " " + dataValueText;
                int [] operands = new int[1];
                operands[0] = dataValue;

                // update data address mapping, if any
                OperandInfo dataAddrOpInfo = mainCore.getDataAddrOperandInfo();
                String labelText = labelEditText.getText().toString();
                if(!labelText.equals("")){
                    dataAddrOpInfo.addMapping(labelText, memoryAddress);
                }
                else {
                    dataAddrOpInfo.removeMapping(memoryAddress);
                }

                // update memoryContents data in list
                memoryData.setMneumonic(DATA_MNEUMONIC);
                memoryData.setOperands(operands);
                memoryData.setMemoryContents(dataText);
                memoryData.setNumericValue(dataValueText);
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
}