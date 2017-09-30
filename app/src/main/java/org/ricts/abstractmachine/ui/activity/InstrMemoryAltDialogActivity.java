package org.ricts.abstractmachine.ui.activity;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.ui.fragment.MemAltFragment;

/**
 * Created by Jevon on 05/01/2017.
 */

public class InstrMemoryAltDialogActivity extends MemoryContentsAltDialogActivity {
    private int lastSelectedSearchView = -1;
    private SearchView [] operandSearchViewArr;
    private DecoderUnit decoderUnit;
    private MemAltFragment.AssemblyMemoryData memoryData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instr_mem_dialog);

        /** Setup main data variables **/
        Bundle dataBundle = getIntent().getExtras();
        decoderUnit = InspectAltActivity.getDecoderUnit(getResources(), dataBundle);
        memoryData = dataBundle.getParcelable(MEM_DATA_KEY);
        final int memoryAddress = dataBundle.getInt(MEM_ADDR_KEY);
        final String memoryType = dataBundle.getString(MEM_TYPE_KEY);

        /** Setup UI interactions and initialise UI **/
        // TODO: find a way to do hex-only input (soft keyboard?!)
        int [] searchViewIdArr = new int [] {
                R.id.operandOneSearchView, R.id.operandTwoSearchView,
                R.id.operandThreeSearchView, R.id.operandFourSearchView
        };
        operandSearchViewArr = new SearchView[searchViewIdArr.length];
        for(int x=0; x < operandSearchViewArr.length; ++x) {
            operandSearchViewArr[x] = (SearchView) findViewById(searchViewIdArr[x]);
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        for(SearchView searchView : operandSearchViewArr) {
            searchView.setSearchableInfo(searchableInfo);
        }

        final EditText labelEditText = (EditText) findViewById(R.id.labelEditText);
        labelEditText.setText(memoryData.getLabel());

        final EditText commentEditText = (EditText) findViewById(R.id.commentEditText);
        commentEditText.setText(memoryData.getComment());

        final TextView formatTextView = (TextView) findViewById(R.id.formatTextView);
        final TextView descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);

        final Resources resources = getResources();

        final RadioGroup instrGroup = (RadioGroup) findViewById(R.id.instrRadioGroup);
        instrGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                RadioButton button = (RadioButton) radioGroup.findViewById(checkedId);

                String mneumonic = button.getText().toString();
                decoderUnit.setMneumonic(mneumonic);
                OperandInfo [] operandInfoArr = mneumonic.equals(DATA_MNEUMONIC) ?
                        new OperandInfo [] {decoderUnit.getDataOperandInfo()} :
                        decoderUnit.getOperandInfoArray();

                // update mneumonic usage and meaning details
                String format = mneumonic.equals(DATA_MNEUMONIC) ?
                        resources.getString(R.string.compute_core_data_memory_format) :
                        decoderUnit.getInstructionFormat();
                String description = mneumonic.equals(DATA_MNEUMONIC) ?
                        resources.getString(R.string.compute_core_data_memory_desc) :
                        decoderUnit.getDescription();
                String [] operandHints = mneumonic.equals(DATA_MNEUMONIC) ?
                        new String [] {resources.getString(R.string.compute_core_data_memory_label)} :
                        decoderUnit.getOperandLabels();

                formatTextView.setText(format);
                descriptionTextView.setText(description);

                // update visibility of SearchViews to only enter appropriate number of operands
                int operandCount = operandInfoArr.length;
                for(int x=0; x < operandSearchViewArr.length; ++x) {
                    operandSearchViewArr[x].setVisibility((x < operandCount) ? View.VISIBLE : View.GONE);
                }

                // update SearchView query hint
                for(int x=0; x < operandHints.length; ++x){
                    operandSearchViewArr[x].setQueryHint(operandHints[x]);
                }
            }
        });

        // Populate RadioGroup with instruction set
        String comparisonMneumonic = memoryData.getMneumonic();
        for(String mneumonic : decoderUnit.getMneumonicList()){
            addRadioButtonWithText(mneumonic, instrGroup, comparisonMneumonic);
        }

        // Add additional RadioButton for pure data entries (useful in Von Neumann scenario)
        addRadioButtonWithText(DATA_MNEUMONIC, instrGroup, comparisonMneumonic);

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** save instruction data for location in adapter **/
                try {
                    RadioButton button = (RadioButton) instrGroup.findViewById(
                            instrGroup.getCheckedRadioButtonId());
                    String mneumonic = button.getText().toString();

                    int [] operands;
                    int encodedInstruction;
                    String instructionText;
                    if(mneumonic.equals(DATA_MNEUMONIC)){
                        encodedInstruction = getSafeInt(operandSearchViewArr[0], decoderUnit.getDataOperandInfo());
                        instructionText = DATA_MNEUMONIC + " " +
                                decoderUnit.instrValueString(encodedInstruction);
                        operands = new int[1];
                        operands[0] = encodedInstruction;
                    }
                    else{
                        OperandInfo[] operandInfoArray = decoderUnit.getOperandInfoArray();
                        int operandCount = operandInfoArray.length;
                        operands = new int[operandCount];
                        for(int x=0; x < operandCount; ++x){
                            OperandInfo operandInfo = operandInfoArray[x];
                            operands[x] = getSafeInt(operandSearchViewArr[x], operandInfo);
                        }

                        encodedInstruction = decoderUnit.encodeInstruction(mneumonic, operands);
                        decoderUnit.decode(0, encodedInstruction);
                        decoderUnit.updateValues();
                        instructionText = decoderUnit.instrString();
                    }

                    // update instruction address mapping, if any
                    OperandInfo instrOpInfo = decoderUnit.getInstrAddrOperandInfo();
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
                    memoryData.setNumericValue(decoderUnit.instrValueString(encodedInstruction));
                    memoryData.setLabel(labelText);
                    memoryData.setComment(commentEditText.getText().toString());

                    sendMemoryData(view.getContext(), memoryAddress, memoryData, memoryType);
                }
                catch (NumberFormatException e) { // could not parse a given operand
                    Toast.makeText(getApplicationContext(), R.string.invalid_op_msg, Toast.LENGTH_SHORT).show();
                }
                finally {
                    finish();
                }
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // update Content Provider data source based on selected SearchView
        for(int x=0; x < operandSearchViewArr.length; ++x) {
            final int index = x;
            operandSearchViewArr[x].setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if(hasFocus){
                        provider.setOpInfo(getOperandInfo(instrGroup, index));
                        lastSelectedSearchView = index;
                    }
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(SUGGESTION_ACTION.equals(intent.getAction())) {
            String suggestion = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
            operandSearchViewArr[lastSelectedSearchView].setQuery(suggestion, true);
        }
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
            decoderUnit.setMneumonic(mneumonic);
            button.setChecked(true); // this triggers visibility of EditTexts

            // update SearchView query text
            OperandInfo [] operandInfoArr = mneumonic.equals(DATA_MNEUMONIC) ?
                    new OperandInfo [] {decoderUnit.getDataOperandInfo()} :
                    decoderUnit.getOperandInfoArray();

            int [] operands = memoryData.getOperands();
            for (int x = 0; x < operands.length; ++x) {
                String text = operandInfoArr[x].getPrettyValue(operands[x]);
                operandSearchViewArr[x].setQuery(text, true);
            }
        }
    }

    private OperandInfo getOperandInfo(RadioGroup instrGroup, int index) {
        RadioButton button = (RadioButton) instrGroup.findViewById(
                instrGroup.getCheckedRadioButtonId());
        String mneumonic = button.getText().toString();

        if(mneumonic.equals(DATA_MNEUMONIC)){
            return decoderUnit.getDataOperandInfo();
        }
        else {
            decoderUnit.setMneumonic(mneumonic);
            OperandInfo[] operandInfoArray = decoderUnit.getOperandInfoArray();
            return (index < operandInfoArray.length) ? operandInfoArray[index] : null;
        }
    }
}
