package org.ricts.abstractmachine.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.devices.compute.core.BasicScalar;
import org.ricts.abstractmachine.ui.activities.CpuConfigureActivity;
import org.ricts.abstractmachine.ui.utils.wizard.WizardFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jevon on 18/05/2016.
 */
public class InstrMemFragment extends WizardFragment {
    private static final String PREFERENCES_FILE = "lastUsedFiles";
    private static final String DATA_MNEUMONIC = "DATA";
    private static final String DIALOG_FRAGMENT_TAG = "instructionFragment";

    private ComputeCore mainCore;
    private ArrayList<AssemblyCodeData> adapterData;
    private ListView programListView;

    public InstrMemFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_instruction_memory, container, false);
        programListView = (ListView) rootView.findViewById(R.id.listView);
        return rootView;
    }

    @Override
    public void restorePageData(Bundle bundle) {
        adapterData = bundle.getParcelableArrayList(CpuConfigureActivity.PROGRAM_DATA);
        initialiseListView(bundle, adapterData == null);
    }

    @Override
    public void savePageData(Bundle bundle) {
        bundle.putParcelableArrayList(CpuConfigureActivity.PROGRAM_DATA, adapterData);

        ArrayList<Integer> program = new ArrayList<Integer>();
        for(AssemblyCodeData data: adapterData){
            String formattedValue = data.getNumericValue();
            program.add( Integer.parseInt(formattedValue.substring(formattedValue.indexOf('x') + 1)) );
        }
        bundle.putIntegerArrayList(CpuConfigureActivity.PROGRAM, program);
    }

    @Override
    public void updatePage(Bundle bundle) {
        initialiseListView(bundle, true);
    }

    private void initialiseListView(Bundle dataBundle, boolean initFromFile){
        Context context = getContext();

        String coreName = dataBundle.getString(CpuConfigureActivity.CORE_NAME);
        int coreDataWidth = dataBundle.getInt(CpuConfigureActivity.CORE_DATA_WIDTH);
        int instrAddrWidth = dataBundle.getInt(CpuConfigureActivity.INSTR_ADDR_WIDTH);
        int dataAddrWidth = dataBundle.getInt(CpuConfigureActivity.DATA_ADDR_WIDTH);

        /** Create adapter for ListView **/
        // Create appropriate ComputeCore
        CpuBasicsFragment.CoreNames coreType = Enum.valueOf(CpuBasicsFragment.CoreNames.class, coreName);
        switch (coreType){
            case BasicScalar:
                int byteMultiplierWidth; // log_2(coreDataWidth/8)
                switch (coreDataWidth){
                    case 16:
                        byteMultiplierWidth = 1;
                        break;
                    case 8:
                    default:
                        byteMultiplierWidth = 0;
                        break;
                }

                mainCore = new BasicScalar(byteMultiplierWidth, dataAddrWidth, instrAddrWidth, 3, 3, 1, 1);
                break;
        }

        if(initFromFile){
            // Search for assembly language programs that target the specified core
            String coreProgramsPath = coreName + "_" + coreDataWidth;
            File coreRootDir = context.getDir(coreProgramsPath, Context.MODE_PRIVATE);
            String [] availableFiles = coreRootDir.list();
            if(availableFiles.length == 0){ // no files available
                adapterData = new ArrayList<AssemblyCodeData>();
            }
            else{
                SharedPreferences preferences =
                        context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
                // load most recently used file OR 1st (and only file)
                adapterData = populateList(preferences.getString(coreProgramsPath, availableFiles[0]),
                        (int) Math.pow(2, instrAddrWidth));
            }
        } // else 'adapterData' has already been initialised

        // instantiate adapter
        final AssemblyCodeAdapter adapter = new AssemblyCodeAdapter(context, R.layout.listitem_instruction,
                mainCore.instrWidth(), mainCore.iAddrWidth(), adapterData);

        /** Configure ListView **/
        final AssemblyCodeDialogFragment.ListUpdater updater = new AssemblyCodeDialogFragment.ListUpdater(){
            @Override
            public void notifyUpdate() {
                adapter.notifyDataSetChanged();
            }
        };

        programListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Get current item data and pass it to new custom DialogFragment
                DialogFragment fragment = AssemblyCodeDialogFragment.newInstance(
                        (AssemblyCodeData) adapterView.getItemAtPosition(position),
                        mainCore, updater, "0123456789ABCDEF");

                // show the DialogFragment!
                fragment.show(InstrMemFragment.this.getActivity().getSupportFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        });
        programListView.setAdapter(adapter);
    }

    private ArrayList<AssemblyCodeData> populateList(String filename, int maxLines){
        ArrayList<AssemblyCodeData> result = new ArrayList<AssemblyCodeData>();

        try {
            BufferedReader buf = new BufferedReader(new FileReader(filename));
            int lineCount = 0;
            String line;
            while ((line = buf.readLine()) != null){
                // Instruction Format:  MNEUMONIC,OP1,OP2;label;comment
                String [] splits = line.split(";");

                String instructionLine = splits[0];
                String [] insLineSplits = instructionLine.split(",");
                String mneumonic = insLineSplits[0];
                int [] operands = new int[insLineSplits.length - 1];
                for(int x=0; x < operands.length; ++x){
                    operands[x] = Integer.parseInt(insLineSplits[x+1]);
                }

                String label = splits[1];
                String comment = splits[2];
                result.add(new AssemblyCodeData(mainCore, mneumonic, operands, label, comment));

                // limit list size to maxLines
                ++lineCount;
                if(lineCount > maxLines){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static class AssemblyCodeDialogFragment extends DialogFragment {
        private ComputeCore mainCore;
        private AssemblyCodeData instructionData;
        private ListUpdater mUpdater;
        private String permittedCharacters;

        public interface ListUpdater {
            void notifyUpdate();
        }

        public AssemblyCodeDialogFragment(){
            // Required empty public constructor
        }

        public static AssemblyCodeDialogFragment newInstance(AssemblyCodeData data, ComputeCore core,
                                                             ListUpdater updater, String charRange){
            AssemblyCodeDialogFragment fragment = new AssemblyCodeDialogFragment();
            fragment.init(data, core, updater, charRange);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Pass null as the parent view because its going in the dialog layout
            final View mainView = inflater.inflate(R.layout.fragment_instr_dialog, null);

            /** Setup UI interactions and initialise UI **/
            final EditText operandOneEditText = (EditText) mainView.findViewById(R.id.operandOneEditText);
            operandOneEditText.setKeyListener(DigitsKeyListener.getInstance(permittedCharacters));
            final EditText operandTwoEditText = (EditText) mainView.findViewById(R.id.operandTwoEditText);
            operandTwoEditText.setKeyListener(DigitsKeyListener.getInstance(permittedCharacters));
            final EditText operandThreeEditText = (EditText) mainView.findViewById(R.id.operandThreeEditText);
            operandThreeEditText.setKeyListener(DigitsKeyListener.getInstance(permittedCharacters));
            final EditText operandFourEditText = (EditText) mainView.findViewById(R.id.operandFourEditText);
            operandFourEditText.setKeyListener(DigitsKeyListener.getInstance(permittedCharacters));

            final EditText labelEditText = (EditText) mainView.findViewById(R.id.labelEditText);
            labelEditText.setText(instructionData.getLabel());

            final EditText commentEditText = (EditText) mainView.findViewById(R.id.commentEditText);
            commentEditText.setText(instructionData.getComment());

            final RadioGroup instrGroup = (RadioGroup) mainView.findViewById(R.id.instrRadioGroup);
            instrGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    RadioButton button = (RadioButton) radioGroup.findViewById(checkedId);

                    String mneumonic = button.getText().toString();
                    int operandCount = mneumonic.equals(DATA_MNEUMONIC) ?
                            1 : mainCore.getOperandCount(mneumonic);

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
                    Locale locale = Locale.getDefault();
                    int [] operands = instructionData.getOperands();
                    for(int x=0; x < operands.length; ++x){
                        switch (x){
                            case 0:
                                operandOneEditText.setText(String.format(locale, "%d", operands[x]));
                                break;
                            case 1:
                                operandTwoEditText.setText(String.format(locale, "%d", operands[x]));
                                break;
                            case 2:
                                operandThreeEditText.setText(String.format(locale, "%d", operands[x]));
                                break;
                            case 3:
                                operandFourEditText.setText(String.format(locale, "%d", operands[x]));
                                break;
                        }
                    }
                }
            });

            // Populate RadioGroup with instruction set
            Context context = getContext();
            String comparisonMneumonic = instructionData.getMneumonic();
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
                                String operandString = operandOneEditText.getText().toString();
                                instructionText = DATA_MNEUMONIC + " " + operandString;
                                encodedInstruction = Integer.parseInt(operandString);
                                operands = new int[1];
                                operands[0] = encodedInstruction;
                            }
                            else{
                                int operandCount = mainCore.getOperandCount(mneumonic);
                                operands = new int[operandCount];
                                for(int x=0; x < operandCount; ++x){
                                    int editTextValue = -1;
                                    switch (x){
                                        case 0:
                                            editTextValue = getSafeInt(operandOneEditText);
                                            break;
                                        case 1:
                                            editTextValue = getSafeInt(operandTwoEditText);
                                            break;
                                        case 2:
                                            editTextValue = getSafeInt(operandThreeEditText);
                                            break;
                                        case 3:
                                            editTextValue = getSafeInt(operandFourEditText);
                                            break;
                                    }
                                    operands[x] = editTextValue;
                                }

                                encodedInstruction = mainCore.encodeInstruction(mneumonic, operands);
                                instructionText = mainCore.instrString(encodedInstruction);
                            }

                            // update instruction data in list
                            instructionData.setMneumonic(mneumonic);
                            instructionData.setOperands(operands);
                            instructionData.setInstruction(instructionText);
                            instructionData.setNumericValue(mainCore.instrValueString(encodedInstruction));
                            instructionData.setLabel(labelEditText.getText().toString());
                            instructionData.setComment(commentEditText.getText().toString());
                            mUpdater.notifyUpdate();
                        }
                    })
                    .setNegativeButton(R.string.negative_button_text, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AssemblyCodeDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }

        public void init(AssemblyCodeData data, ComputeCore core, ListUpdater updater, String charRange){
            instructionData = data;
            mainCore = core;
            mUpdater = updater;
            permittedCharacters = charRange;
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
                button.setChecked(true); // TODO: check that this triggers visibility of EditTexts
            }
        }

        private int getSafeInt(EditText editText){
            String currentText = editText.getText().toString();
            return currentText.equals("") ? 0 : Integer.parseInt(currentText);
        }
    }

    private static class AssemblyCodeAdapter extends ArrayAdapter<AssemblyCodeData> {
        private int instrAddrWidth;

        public AssemblyCodeAdapter(Context context, int resource, int instWidth, int instAWidth,
                                   List<AssemblyCodeData> objects) {
            super(context, resource);
            instrAddrWidth = instAWidth;

            int objectCount = objects.size();
            int listSize = (int) Math.pow(2, instAWidth);
            for(int x=0; x < listSize; ++x){
                if(x < objectCount){
                    add(objects.get(x));
                }
                else{
                    // add newly created data element to list and adapter
                    // (for easier reference when saving page data)
                    AssemblyCodeData data = new AssemblyCodeData(instWidth);
                    objects.add(data);
                    add(data);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_instruction, parent, false);
            }

            AssemblyCodeData data = getItem(position);

            TextView indexTextView = (TextView) convertView.findViewById(R.id.index);
            indexTextView.setText(Device.formatNumberInHex(position, instrAddrWidth));

            TextView labelTextView = (TextView) convertView.findViewById(R.id.label);
            String labelText = data.getLabel();
            labelTextView.setText(labelText);
            labelTextView.setVisibility(labelText.equals("") ? View.GONE : View.VISIBLE);

            TextView instructionTextView = (TextView) convertView.findViewById(R.id.instruction);
            instructionTextView.setText(data.getInstruction());

            TextView valueTextView = (TextView) convertView.findViewById(R.id.value);
            valueTextView.setText(data.getNumericValue());

            TextView commentTextView = (TextView) convertView.findViewById(R.id.comment);
            String commentText = data.getComment();
            commentTextView.setText(commentText);
            commentTextView.setVisibility(commentText.equals("") ? View.GONE : View.VISIBLE);

            return convertView;
        }
    }

    private static class AssemblyCodeData implements Parcelable {
        private String label, instruction, numericValue, comment, mneumonic;
        private int [] operands;

        public AssemblyCodeData(int instructionWidth){
            mneumonic = DATA_MNEUMONIC;
            operands = new int[1];
            operands[0] = 0;
            label = "";
            comment = "";

            instruction = DATA_MNEUMONIC + " 0";
            numericValue = Device.formatNumberInHex(0, instructionWidth);
        }

        public AssemblyCodeData(ComputeCore core, String m, int []ops, String l, String c){
            mneumonic = m;
            operands = ops;
            label = l;
            comment = c;

            int instr = core.encodeInstruction(mneumonic, operands);
            instruction = core.instrString(instr);
            numericValue = core.instrValueString(instr);
        }

        protected AssemblyCodeData(Parcel in) {
            label = in.readString();
            instruction = in.readString();
            numericValue = in.readString();
            comment = in.readString();
            mneumonic = in.readString();
            operands = in.createIntArray();
        }

        public static final Creator<AssemblyCodeData> CREATOR = new Creator<AssemblyCodeData>() {
            @Override
            public AssemblyCodeData createFromParcel(Parcel in) {
                return new AssemblyCodeData(in);
            }

            @Override
            public AssemblyCodeData[] newArray(int size) {
                return new AssemblyCodeData[size];
            }
        };

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(label);
            parcel.writeString(instruction);
            parcel.writeString(numericValue);
            parcel.writeString(comment);
            parcel.writeString(mneumonic);
            parcel.writeIntArray(operands);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public String getLabel(){
            return label;
        }

        public String getInstruction(){
            return instruction;
        }

        public String getNumericValue(){
            return numericValue;
        }

        public String getComment() {
            return comment;
        }

        public String getMneumonic(){
            return mneumonic;
        }

        public int [] getOperands(){
            return operands;
        }

        public void setComment(String c) {
            comment = c;
        }

        public void setLabel(String l) {
            label = l;
        }

        public void setInstruction(String inst) {
            instruction = inst;
        }

        public void setNumericValue(String value) {
            numericValue = value;
        }

        public void setMneumonic(String m){
            mneumonic = m;
        }

        public void setOperands(int [] ops){
            operands = ops;
        }
    }
}
