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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
public abstract class MemFragment extends WizardFragment {
    private static final String TAG = "MemFragment";

    protected abstract String memoryTypeString();
    protected abstract int titleStringResource();
    protected abstract AssemblyMemoryData.MemoryType memoryType();
    protected abstract MemoryContentsDialogFragment getMemoryContentsDialogFragment(
            AssemblyMemoryData data, ComputeCore core, MemoryContentsDialogFragment.ListUpdater updater);

    protected static final String DATA_MNEUMONIC = "DATA";
    private static final String PREFERENCES_FILE = "lastUsedFiles";
    private static final String INSTR_DIALOG_TAG = "instructionFragment";
    private static final String FILE_DIALOG_TAG = "filenameFragment";
    private static final String LOADFILE_DIALOG_TAG = "loadFileFragment";
    private static final String PROGRAM_ADAPTER_DATA = "programMetaData"; // key for instruction UI metadata
    private static final String DATA_ADAPTER_DATA = "dataMetaData"; // key for data memory UI metadata
    private static final int HEX_RADIX = 16;
    private static final String INS_SEPERATOR = ",";
    private static final String INS_DATA_SEPERATOR = ";";

    private ComputeCore mainCore;
    private ArrayList<AssemblyMemoryData> adapterData;
    private ListView memoryContentsListView;
    private Button saveButton, loadButton, newButton;
    private String currentFile;

    public MemFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_memory, container, false);
        memoryContentsListView = (ListView) rootView.findViewById(R.id.listView);
        saveButton = (Button) rootView.findViewById(R.id.saveButton);
        loadButton = (Button) rootView.findViewById(R.id.loadButton);
        newButton = (Button) rootView.findViewById(R.id.newButton);

        TextView titleTextView = (TextView) rootView.findViewById(R.id.titleText);
        titleTextView.setText(getString(titleStringResource()));

        return rootView;
    }

    @Override
    public void restorePageData(Bundle bundle) {
        adapterData = bundle.getParcelableArrayList(getAdapterDataKey());
        initialiseViews(bundle, adapterData == null);
    }

    @Override
    public void savePageData(Bundle bundle) {
        String key = getAdapterDataKey();
        bundle.putParcelableArrayList(key, adapterData);

        ArrayList<Integer> memoryContents = new ArrayList<Integer>();
        for(AssemblyMemoryData data: adapterData){
            String formattedValue = data.getNumericValue();
            memoryContents.add( parseHex( formattedValue.substring(formattedValue.indexOf('x') + 1) ) );
        }

        switch (memoryType()){
            case INSTRUCTION:
                key = InspectActivity.PROGRAM_MEMORY;
                break;
            case DATA:
                key = InspectActivity.DATA_MEMORY;
                break;
        }
        bundle.putIntegerArrayList(key, memoryContents);
    }

    @Override
    public void updatePage(Bundle bundle) {
        initialiseViews(bundle, true); // TODO: get better logic for boolean
    }

    private String getAdapterDataKey(){
        String key = null;
        switch (memoryType()){
            case INSTRUCTION:
                key = PROGRAM_ADAPTER_DATA;
                break;
            case DATA:
                key = DATA_ADAPTER_DATA;
                break;
        }
        return key;
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
                adapterData = new ArrayList<AssemblyMemoryData>();
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

                String zeroString = null;
                switch (memoryType()){
                    case INSTRUCTION:
                        zeroString = mainCore.instrValueString(0);
                        break;
                    case DATA:
                        zeroString = mainCore.dataValueString(0);
                        break;
                }

                if(saveListToFile(adapterData, targetFile, zeroString, context)) {
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
                    fragment.show(MemFragment.this.getActivity().getSupportFragmentManager(),
                            FILE_DIALOG_TAG);
                }
            }
        });

        /** Configure Load button **/
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** load a file into the current list (limited by memoryContents address width) **/
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
                fragment.show(MemFragment.this.getActivity().getSupportFragmentManager(),
                        LOADFILE_DIALOG_TAG);
            }
        });

        /** Configure New button **/
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentFile = null;
                adapterData = new ArrayList<AssemblyMemoryData>();
                populateListView(context);
            }
        });
    }

    private void loadListViewData(String filename, Context context, Bundle dataBundle){
        currentFile = filename;

        File coreRootDir = getComputeCorePath(dataBundle, context);
        int maxWidth = memoryType() == AssemblyMemoryData.MemoryType.DATA ?
                mainCore.dAddrWidth() : mainCore.iAddrWidth();
        adapterData = loadListFromFile(new File(coreRootDir, currentFile),
                (int) Math.pow(2, maxWidth), context);

        // store last loaded file for the appropriate core
        SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(coreRootDir.getPath(), currentFile);
        editor.apply();
    }

    private void populateListView(Context context){
        final AssemblyCodeAdapter adapter = new AssemblyCodeAdapter(context, R.layout.listitem_instruction,
                mainCore, adapterData, memoryType());

        final MemoryContentsDialogFragment.ListUpdater updater = new MemoryContentsDialogFragment.ListUpdater(){
            @Override
            public void notifyUpdate() {
                adapter.notifyDataSetChanged();
            }
        };

        memoryContentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Get current item data and pass it to new custom DialogFragment
                DialogFragment fragment = getMemoryContentsDialogFragment(
                        (AssemblyMemoryData) adapterView.getItemAtPosition(position),
                        mainCore, updater);

                // show the DialogFragment!
                fragment.show(MemFragment.this.getActivity().getSupportFragmentManager(), INSTR_DIALOG_TAG);
            }
        });
        memoryContentsListView.setAdapter(adapter);
    }

    private ArrayList<AssemblyMemoryData> loadListFromFile(File file, int maxLines, Context context){
        ArrayList<AssemblyMemoryData> result = new ArrayList<AssemblyMemoryData>();

        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            int lineCount = 0;
            String line;
            while ((line = buf.readLine()) != null){
                // File line Format:  MNEUMONIC[,OP1,OP2];label;comment
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
                result.add(new AssemblyMemoryData(mainCore, memoryType(),
                        mneumonic, operands, label, comment));

                // limit list size to maxLines
                ++lineCount;
                if(lineCount > maxLines){
                    Toast.makeText(context, context.getString(R.string.file_too_long_message),
                            Toast.LENGTH_SHORT).show();
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
    private boolean saveListToFile(List<AssemblyMemoryData> dataList, File file,
                                   String zeroString, Context context) {
        int lastIndex = dataList.size() - 1;
        if(memoryType() == AssemblyMemoryData.MemoryType.INSTRUCTION) {
            // Reduce file size by only saving data from start until
            // last address with memoryContents/data (non-zero)
            for ( ; lastIndex >= 0; --lastIndex) {
                AssemblyMemoryData data = dataList.get(lastIndex);

                if (!zeroString.equals(data.getNumericValue())) {
                    break;
                }
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
                AssemblyMemoryData data = dataList.get(x);

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
                fw.write(data.getMneumonic() + operandsString + INS_DATA_SEPERATOR +
                        data.getLabel() + INS_DATA_SEPERATOR + data.getComment() + "\n");
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

    private File getComputeCorePath(Bundle dataBundle, Context context){
        String coreName = dataBundle.getString(InspectActivity.CORE_NAME);
        int coreDataWidth = dataBundle.getInt(InspectActivity.CORE_DATA_WIDTH);

        String directoryName = coreName + "_" + coreDataWidth + "_" + memoryTypeString();
        return context.getDir(directoryName, Context.MODE_PRIVATE);
    }

    private static int parseHex(String text){
        return Integer.parseInt(text, HEX_RADIX);
    }

    public static class MemoryContentsDialogFragment extends DialogFragment {
        public interface ListUpdater {
            void notifyUpdate();
        }

        protected ComputeCore mainCore;
        protected AssemblyMemoryData memoryData;
        protected ListUpdater mUpdater;

        public void init(AssemblyMemoryData data, ComputeCore core, ListUpdater updater){
            memoryData = data;
            mainCore = core;
            mUpdater = updater;
        }

        protected int getSafeInt(EditText editText){
            String currentText = editText.getText().toString();
            return currentText.equals("") ? 0 : parseHex(currentText);
        }
    }

    private static class AssemblyCodeAdapter extends ArrayAdapter<AssemblyMemoryData> {
        private ComputeCore mainCore;
        private AssemblyMemoryData.MemoryType memoryType;

        public AssemblyCodeAdapter(Context context, int resource, ComputeCore core,
                                   List<AssemblyMemoryData> objects, AssemblyMemoryData.MemoryType type) {
            super(context, resource);
            mainCore = core;
            memoryType = type;

            int memoryWidth = 0;
            switch (memoryType){
                case INSTRUCTION:
                    memoryWidth = mainCore.iAddrWidth();
                    break;
                case DATA:
                    memoryWidth = mainCore.dAddrWidth();
                    break;
            }

            int objectCount = objects.size();
            int listSize = (int) Math.pow(2, memoryWidth);
            for(int x=0; x < listSize; ++x){
                if(x < objectCount){
                    add(objects.get(x));
                }
                else{
                    // add newly created data element to list and adapter
                    // (for easier reference when saving page data)
                    AssemblyMemoryData data = new AssemblyMemoryData(core, memoryType);
                    objects.add(data);
                    add(data);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_memory_contents, parent, false);
            }

            AssemblyMemoryData data = getItem(position);

            TextView indexTextView = (TextView) convertView.findViewById(R.id.index);
            String addressValue = null;
            switch (memoryType){
                case INSTRUCTION:
                    addressValue = mainCore.instrAddrValueString(position);
                    break;
                case DATA:
                    addressValue = mainCore.dataAddrValueString(position);
                    break;
            }
            indexTextView.setText(addressValue);

            TextView labelTextView = (TextView) convertView.findViewById(R.id.label);
            String labelText = data.getLabel();
            labelTextView.setText(labelText);
            labelTextView.setVisibility(labelText.equals("") ? View.GONE : View.VISIBLE);

            TextView memoryContentsTextView = (TextView) convertView.findViewById(R.id.memoryContents);
            memoryContentsTextView.setText(data.getMemoryContents());

            TextView valueTextView = (TextView) convertView.findViewById(R.id.value);
            valueTextView.setText(data.getNumericValue());

            TextView commentTextView = (TextView) convertView.findViewById(R.id.comment);
            String commentText = data.getComment();
            commentTextView.setText(commentText);
            commentTextView.setVisibility(commentText.equals("") ? View.GONE : View.VISIBLE);

            return convertView;
        }
    }

    protected static class AssemblyMemoryData implements Parcelable {
        public enum MemoryType {
            INSTRUCTION, DATA
        }

        private String label, memoryContents, numericValue, comment, mneumonic;
        private int [] operands;
        private MemoryType memType;

        public AssemblyMemoryData(ComputeCore core, MemoryType type){
            mneumonic = DATA_MNEUMONIC;
            operands = new int[1];
            operands[0] = 0;
            label = "";
            comment = "";
            memType = type;

            switch (memType){
                case INSTRUCTION:
                    initAsPureData(core.instrValueString(operands[0]));
                    break;
                case DATA:
                    initAsPureData(core.dataValueString(operands[0]));
                    break;
            }
        }

        public AssemblyMemoryData(ComputeCore core, MemoryType type,
                                  String m, int []ops, String l, String c){
            mneumonic = m;
            operands = ops;
            label = l;
            comment = c;
            memType = type;

            switch (memType){
                case INSTRUCTION:
                    if(mneumonic.equals(DATA_MNEUMONIC)){
                        initAsPureData(core.instrValueString(operands[0]));
                    }
                    else{
                        int instr = core.encodeInstruction(mneumonic, operands);
                        memoryContents = core.instrString(instr);
                        numericValue = core.instrValueString(instr);
                    }
                    break;
                case DATA:
                    initAsPureData(core.dataValueString(operands[0]));
                    break;
            }
        }

        protected AssemblyMemoryData(Parcel in) {
            label = in.readString();
            memoryContents = in.readString();
            numericValue = in.readString();
            comment = in.readString();
            mneumonic = in.readString();
            operands = in.createIntArray();
        }

        public static final Creator<AssemblyMemoryData> CREATOR = new Creator<AssemblyMemoryData>() {
            @Override
            public AssemblyMemoryData createFromParcel(Parcel in) {
                return new AssemblyMemoryData(in);
            }

            @Override
            public AssemblyMemoryData[] newArray(int size) {
                return new AssemblyMemoryData[size];
            }
        };

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(label);
            parcel.writeString(memoryContents);
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

        public String getMemoryContents(){
            return memoryContents;
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

        public void setMemoryContents(String contents) {
            memoryContents = contents;
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
            setNumericValue(value);
            setMneumonic(DATA_MNEUMONIC);
            setMemoryContents(DATA_MNEUMONIC + " " + numericValue);
        }
    }
}
