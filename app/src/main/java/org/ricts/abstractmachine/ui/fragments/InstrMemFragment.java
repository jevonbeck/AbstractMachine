package org.ricts.abstractmachine.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.ui.activities.InspectActivity;
import org.ricts.abstractmachine.ui.utils.wizard.WizardFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jevon on 18/05/2016.
 */
public class InstrMemFragment extends WizardFragment {
    private static final String TAG = "InstrMemFragment";

    private static final String PREFERENCES_FILE = "lastUsedFiles";
    private static final String DATA_MNEUMONIC = "DATA";
    private static final String INSTR_DIALOG_TAG = "instructionFragment";
    private static final String FILE_DIALOG_TAG = "filenameFragment";
    private static final String LOADFILE_DIALOG_TAG = "loadFileFragment";
    private static final String ADAPTER_DATA = "adapterData"; // key for program UI metadata
    private static final int HEX_RADIX = 16;
    private static final String INS_SEPERATOR = ",";
    private static final String INS_DATA_SEPERATOR = ";";

    private ComputeCore mainCore;
    private ArrayList<AssemblyCodeData> adapterData;
    private ListView programListView;
    private Button saveButton, loadButton, newButton;
    private String currentFile;

    public InstrMemFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_instruction_memory, container, false);
        programListView = (ListView) rootView.findViewById(R.id.listView);
        saveButton = (Button) rootView.findViewById(R.id.saveButton);
        loadButton = (Button) rootView.findViewById(R.id.loadButton);
        newButton = (Button) rootView.findViewById(R.id.newButton);
        return rootView;
    }

    @Override
    public void restorePageData(Bundle bundle) {
        adapterData = bundle.getParcelableArrayList(ADAPTER_DATA);
        initialiseViews(bundle, adapterData == null);
    }

    @Override
    public void savePageData(Bundle bundle) {
        bundle.putParcelableArrayList(ADAPTER_DATA, adapterData);

        ArrayList<Integer> program = new ArrayList<Integer>();
        for(AssemblyCodeData data: adapterData){
            String formattedValue = data.getNumericValue();
            program.add( parseHex( formattedValue.substring(formattedValue.indexOf('x') + 1) ) );
        }
        bundle.putIntegerArrayList(InspectActivity.PROGRAM, program);
    }

    @Override
    public void updatePage(Bundle bundle) {
        initialiseViews(bundle, true); // TODO: get better logic for boolean
    }

    private void initialiseViews(final Bundle dataBundle, boolean initFromFile){
        final Context context = getContext();

        // Create appropriate ComputeCore
        mainCore = InspectActivity.getComputeCore(dataBundle);

        /** Configure ListView **/
        if(initFromFile){
            // Search for assembly language programs that target the specified core
            File coreRootDir = getComputeCorePath(dataBundle, context);
            String [] availableFiles = coreRootDir.list();
            if(availableFiles.length == 0){ // no files available
                currentFile = null;
                adapterData = new ArrayList<AssemblyCodeData>();
            }
            else{
                SharedPreferences preferences =
                        context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
                // load most recently used file OR 1st (and only file)
                loadListViewData(preferences.getString(coreRootDir.getPath(), availableFiles[0]),
                        context, dataBundle);
            }
        } // else 'adapterData' has already been initialised

        populateListView(context);

        /** Configure Save button **/
        final FilenameDialogFragment.FileSaver saver = new FilenameDialogFragment.FileSaver(){
            @Override
            public void saveFile(String filename) {
                File targetFile = new File(getComputeCorePath(dataBundle, context), filename);

                if(saveListToFile(adapterData, targetFile, mainCore.instrValueString(0), context)) {
                    currentFile = filename; // last saved file is now current file
                }
            }
        };

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentFile != null){ // if file was previously loaded ...
                    // ... update current file
                    saver.saveFile(currentFile);
                }
                else { // prompt user for filename... and then save file
                    DialogFragment fragment = FilenameDialogFragment.newInstance(saver);
                    fragment.show(InstrMemFragment.this.getActivity().getSupportFragmentManager(),
                            FILE_DIALOG_TAG);
                }
            }
        });

        /** Configure Load button **/
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** load a file into the current list (limited by instruction address width) **/
                final String [] fileList = getComputeCorePath(dataBundle, context).list();

                // prompt user to select a file from list of available file names and
                // initialise ListView
                DialogFragment fragment = new DialogFragment(){
                    private DialogFragment fragmentReference = this;

                    @NonNull
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState){
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.file_list_dialog_title)
                                .setItems(fileList, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        loadListViewData(fileList[which], context, dataBundle);
                                        populateListView(context);
                                    }
                                })
                                .setNegativeButton(R.string.negative_button_text, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        fragmentReference.getDialog().cancel();
                                    }
                                });
                        return builder.create();
                    }
                };
                fragment.show(InstrMemFragment.this.getActivity().getSupportFragmentManager(),
                        LOADFILE_DIALOG_TAG);
            }
        });

        /** Configure New button **/
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentFile = null;
                adapterData = new ArrayList<AssemblyCodeData>();
                populateListView(context);
            }
        });
    }

    private void loadListViewData(String filename, Context context, Bundle dataBundle){
        currentFile = filename;

        File coreRootDir = getComputeCorePath(dataBundle, context);
        adapterData = loadListFromFile(new File(coreRootDir, currentFile),
                (int) Math.pow(2, mainCore.iAddrWidth()));

        // store last loaded file for the appropriate core
        SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(coreRootDir.getPath(), currentFile);
        editor.apply();
    }

    private void populateListView(Context context){
        final AssemblyCodeAdapter adapter = new AssemblyCodeAdapter(context, R.layout.listitem_instruction,
                mainCore, adapterData);

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
                        mainCore, updater);

                // show the DialogFragment!
                fragment.show(InstrMemFragment.this.getActivity().getSupportFragmentManager(), INSTR_DIALOG_TAG);
            }
        });
        programListView.setAdapter(adapter);
    }

    private ArrayList<AssemblyCodeData> loadListFromFile(File file, int maxLines){
        ArrayList<AssemblyCodeData> result = new ArrayList<AssemblyCodeData>();

        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            int lineCount = 0;
            String line;
            while ((line = buf.readLine()) != null){
                // File line Format:  MNEUMONIC[,OP1,OP2];label;comment
                Log.d(TAG, "'" + line + "'");
                String [] splits = line.split(INS_DATA_SEPERATOR, -1);

                String instructionLine = splits[0];
                String [] insLineSplits = instructionLine.split(INS_SEPERATOR);
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

    /**
     * This function saves the current dataList to the given file and returns whether the save
     * was successful
     * */
    private boolean saveListToFile(List<AssemblyCodeData> dataList, File file,
                                   String zeroString, Context context) {
        // Reduce file size by only saving data from start until last address with instruction/data (non-zero)
        int lastIndex;
        for(lastIndex = dataList.size() - 1; lastIndex >= 0; --lastIndex){
            AssemblyCodeData data = dataList.get(lastIndex);

            if(!zeroString.equals(data.getNumericValue())){
                break;
            }
        }

        // don't attempt to save file if there is nothing to save!
        if(lastIndex < 0){
            Toast.makeText(context, context.getString(R.string.nothing_to_save_message), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Save data in file according to custom format
        try {
            FileWriter fw = new FileWriter(file);
            String emptyString = "";
            for(int x=0; x <= lastIndex; ++x){
                AssemblyCodeData data = dataList.get(x);

                StringBuilder operandBuilder = new StringBuilder();
                int [] operands = data.getOperands();
                int lengthMinusOne = operands.length - 1;
                for(int y=0; y < operands.length; ++y){
                    operandBuilder.append(operands[y]);

                    if(y < lengthMinusOne){
                        operandBuilder.append(INS_SEPERATOR);
                    }
                }

                String tempString = operandBuilder.toString();
                String operandsString = !tempString.equals(emptyString) ?
                        INS_SEPERATOR + tempString : emptyString;

                // File line Format:  MNEUMONIC[,OP1,OP2];label;comment
                String line = data.getMneumonic() + operandsString + INS_DATA_SEPERATOR +
                        data.getLabel() + INS_DATA_SEPERATOR + data.getComment() + "\n";
                Log.d(TAG, line);
                fw.write(line);
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, context.getString(R.string.save_message_negative), Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(context, context.getString(R.string.save_message_positive), Toast.LENGTH_SHORT).show();
        return true;
    }

    private static File getComputeCorePath(Bundle dataBundle, Context context){
        String coreName = dataBundle.getString(InspectActivity.CORE_NAME);
        int coreDataWidth = dataBundle.getInt(InspectActivity.CORE_DATA_WIDTH);

        return context.getDir(coreName + "_" + coreDataWidth, Context.MODE_PRIVATE);
    }

    private static int parseHex(String text){
        return Integer.parseInt(text, HEX_RADIX);
    }

    public static class AssemblyCodeDialogFragment extends DialogFragment {
        private ComputeCore mainCore;
        private AssemblyCodeData instructionData;
        private ListUpdater mUpdater;

        public interface ListUpdater {
            void notifyUpdate();
        }

        public AssemblyCodeDialogFragment(){
            // Required empty public constructor
        }

        public static AssemblyCodeDialogFragment newInstance(AssemblyCodeData data, ComputeCore core,
                                                             ListUpdater updater){
            AssemblyCodeDialogFragment fragment = new AssemblyCodeDialogFragment();
            fragment.init(data, core, updater);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Pass null as the parent view because its going in the dialog layout
            // TODO: create mainView from scratch
            final View mainView = inflater.inflate(R.layout.fragment_instr_dialog, null);

            /** Setup UI interactions and initialise UI **/
            // TODO: find a way to do hex-only input (soft keyboard?!)
            final EditText operandOneEditText = (EditText) mainView.findViewById(R.id.operandOneEditText);
            final EditText operandTwoEditText = (EditText) mainView.findViewById(R.id.operandTwoEditText);
            final EditText operandThreeEditText = (EditText) mainView.findViewById(R.id.operandThreeEditText);
            final EditText operandFourEditText = (EditText) mainView.findViewById(R.id.operandFourEditText);

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
                    int [] operands = instructionData.getOperands();
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
                                encodedInstruction = getSafeInt(operandOneEditText);
                                instructionText = DATA_MNEUMONIC + " " +
                                        mainCore.instrValueString(encodedInstruction);
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

        public void init(AssemblyCodeData data, ComputeCore core, ListUpdater updater){
            instructionData = data;
            mainCore = core;
            mUpdater = updater;
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

        private int getSafeInt(EditText editText){
            String currentText = editText.getText().toString();
            return currentText.equals("") ? 0 : parseHex(currentText);
        }
    }

    private static class AssemblyCodeAdapter extends ArrayAdapter<AssemblyCodeData> {
        private ComputeCore mainCore;

        public AssemblyCodeAdapter(Context context, int resource, ComputeCore core,
                                   List<AssemblyCodeData> objects) {
            super(context, resource);
            mainCore = core;

            int objectCount = objects.size();
            int listSize = (int) Math.pow(2, mainCore.iAddrWidth());
            for(int x=0; x < listSize; ++x){
                if(x < objectCount){
                    add(objects.get(x));
                }
                else{
                    // add newly created data element to list and adapter
                    // (for easier reference when saving page data)
                    AssemblyCodeData data = new AssemblyCodeData(core);
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
            indexTextView.setText(mainCore.instrAddrValueString(position));

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

        public AssemblyCodeData(ComputeCore core){
            mneumonic = DATA_MNEUMONIC;
            operands = new int[1];
            operands[0] = 0;
            label = "";
            comment = "";

            initAsPureData(core.instrValueString(operands[0]));
        }

        public AssemblyCodeData(ComputeCore core, String m, int []ops, String l, String c){
            mneumonic = m;
            operands = ops;
            label = l;
            comment = c;

            if(mneumonic.equals(DATA_MNEUMONIC)){
                initAsPureData(core.instrValueString(operands[0]));
            }
            else{
                int instr = core.encodeInstruction(mneumonic, operands);
                instruction = core.instrString(instr);
                numericValue = core.instrValueString(instr);
            }
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

        private void initAsPureData(String value){
            numericValue = value;
            instruction = DATA_MNEUMONIC + " " + numericValue;
        }
    }
}
