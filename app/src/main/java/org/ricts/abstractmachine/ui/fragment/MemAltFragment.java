package org.ricts.abstractmachine.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.core.ComputeCore;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.ui.activity.InspectActivity;
import org.ricts.abstractmachine.ui.activity.InspectAltActivity;
import org.ricts.abstractmachine.ui.activity.MemoryContentsDialogActivity;
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
public abstract class MemAltFragment extends WizardFragment {
    private static final String TAG = "MemFragment";

    protected abstract int titleStringResource();
    protected abstract MemoryType memoryType();
    protected abstract Intent getDialogActivityIntent();

    public static final String UPDATE_MEM_LOCATION_ACTION = "updateAdapter";
    protected static final String DATA_MNEUMONIC = "DATA";
    private static final String PREFERENCES_FILE = "lastUsedFiles";
    private static final String FILE_DIALOG_TAG = "filenameFragment";
    private static final String LOADFILE_DIALOG_TAG = "loadFileFragment";
    private static final String MAPPING_DIALOG_TAG = "mappingFragment";
    private static final String PROGRAM_ADAPTER_DATA = "programMetaData"; // key for instruction UI metadata
    private static final String DATA_ADAPTER_DATA = "dataMetaData"; // key for data memory UI metadata
    private static final String INS_SEPERATOR = ",";
    private static final String INS_DATA_SEPERATOR = ";";

    private String mainCoreName;
    private DecoderUnit decoderUnit;
    private ArrayList<AssemblyMemoryData> adapterData;
    private ListView memoryContentsListView;
    private Button saveButton, loadButton, newButton;
    private String currentFile;

    private BroadcastReceiver receiver;
    private LocalBroadcastManager broadcastManager;
    private IntentFilter filter;

    public enum MemoryType {
        INSTRUCTION("instr"), DATA("data");

        private String shortName;

        MemoryType(String name) {
            shortName = name;
        }

        public String getShortName() {
            return shortName;
        }
    }

    public MemAltFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);
        filter = new IntentFilter(UPDATE_MEM_LOCATION_ACTION);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
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

        ArrayList<Integer> memoryContents = new ArrayList<>();
        for(AssemblyMemoryData data: adapterData){
            memoryContents.add(Device.parseHex(data.getNumericValue()));
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

        // Create appropriate DecoderUnit
        mainCoreName = dataBundle.getString(InspectAltActivity.CORE_NAME);
        decoderUnit = InspectAltActivity.getDecoderUnit(getResources(), dataBundle);

        /** Configure ListView **/
        if(initFromFile){
            // Search for assembly language programs that target the specified core
            File coreRootDir = getComputeCorePath(dataBundle, context);
            String [] availableFiles = coreRootDir.list();
            if(availableFiles.length == 0){ // no files available
                currentFile = null;
                adapterData = new ArrayList<>();
            }
            else{
                SharedPreferences preferences =
                        context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
                // load most recently used file OR 1st (and only file)
                loadListViewData(preferences.getString(coreRootDir.getPath(), availableFiles[0]), dataBundle);
            }
        } // else 'adapterData' has already been initialised

        populateListView();

        /** Configure Save button **/
        final FilenameDialogFragment.FileSaver saver = new FilenameDialogFragment.FileSaver(){
            @Override
            public void saveFile(String filename) {
                File targetFile = new File(getComputeCorePath(dataBundle, context), filename);

                String zeroString = null;
                switch (memoryType()){
                    case INSTRUCTION:
                        zeroString = decoderUnit.instrValueString(0);
                        break;
                    case DATA:
                        zeroString = decoderUnit.dataValueString(0);
                        break;
                }

                if(saveListToFile(adapterData, targetFile, zeroString, context)) {
                    currentFile = filename; // last saved file is now current file
                }
            }
        };

        /*
        mappingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment fragment = MappingDialogFragment.newInstance(
                        new MappingDialogFragment.MneumonicTypeMapping(decoderUnit.getDataOperandInfo()),
                        new MappingDialogFragment.MneumonicTypeMapping(mainCore.getDataRegOperandInfo()));
                fragment.show(MemAltFragment.this.getActivity().getSupportFragmentManager(),
                        MAPPING_DIALOG_TAG);
            }
        });
        */

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentFile != null){ // if file was previously loaded ...
                    // ... update current file
                    saver.saveFile(currentFile);
                }
                else { // prompt user for filename... and then save file
                    DialogFragment fragment = FilenameDialogFragment.newInstance(saver);
                    fragment.show(MemAltFragment.this.getActivity().getSupportFragmentManager(),
                            FILE_DIALOG_TAG);
                }
            }
        });

        /** Configure Load button **/
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** load a file into the current list (limited by memoryContents address width) **/
                String [] fileList = getComputeCorePath(dataBundle, context).list();

                // prompt user to select a file from list of available file names and
                // initialise ListView
                DialogFragment fragment =
                        LoadFileDialogFragment.newInstance(MemAltFragment.this, fileList, dataBundle);
                fragment.show(MemAltFragment.this.getActivity().getSupportFragmentManager(),
                        LOADFILE_DIALOG_TAG);
            }
        });

        /** Configure New button **/
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentFile = null;
                adapterData = new ArrayList<>();
                populateListView();
            }
        });
    }

    public void loadListViewData(String filename, Bundle dataBundle){
        Context context = getContext();
        currentFile = filename;

        File coreRootDir = getComputeCorePath(dataBundle, context);
        int maxWidth = memoryType() == MemoryType.DATA ?
                decoderUnit.dAddrWidth() : decoderUnit.iAddrWidth();
        adapterData = loadListFromFile(new File(coreRootDir, currentFile),
                1 << maxWidth, context); // max lines = 2^maxWidth

        // store last loaded file for the appropriate core
        SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(coreRootDir.getPath(), currentFile);
        editor.apply();
    }

    public void populateListView(){
        final AssemblyCodeAdapter adapter = new AssemblyCodeAdapter(getContext(), R.layout.listitem_instruction,
                decoderUnit, adapterData, memoryType());

        unregisterReceiver();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(UPDATE_MEM_LOCATION_ACTION)) {
                    String memType = intent.getStringExtra(MemoryContentsDialogActivity.MEM_TYPE_KEY);

                    // update the appropriate MemFragment (identified by type)
                    if(memType.equals(memoryType().name())) {
                        int position = intent.getIntExtra(MemoryContentsDialogActivity.MEM_ADDR_KEY, -1);
                        AssemblyMemoryData memoryData =
                                intent.getParcelableExtra(MemoryContentsDialogActivity.MEM_DATA_KEY);

                        // update the appropriate memory location
                        AssemblyMemoryData currentData = adapter.getItem(position);
                        currentData.setMneumonic(memoryData.getMneumonic());
                        currentData.setOperands(memoryData.getOperands());
                        currentData.setMemoryContents(memoryData.getMemoryContents());
                        currentData.setNumericValue(memoryData.getNumericValue());
                        currentData.setLabel(memoryData.getLabel());
                        currentData.setComment(memoryData.getComment());

                        adapter.notifyDataSetChanged();
                    }
                }
            }
        };
        broadcastManager.registerReceiver(receiver, filter);

        memoryContentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Get current item data and pass it to new custom DialogActivity
                Intent intent = getDialogActivityIntent();
                populateActivityIntent(intent, position,
                        (AssemblyMemoryData) adapterView.getItemAtPosition(position),
                        decoderUnit, mainCoreName);
                startActivity(intent);
            }
        });
        memoryContentsListView.setAdapter(adapter);
    }

    protected void populateActivityIntent(Intent intent, int position,
                                          AssemblyMemoryData data, DecoderUnit decoderUnit, String coreName){
        intent.putExtra(MemoryContentsDialogActivity.MEM_TYPE_KEY, memoryType().name());
        intent.putExtra(MemoryContentsDialogActivity.MEM_ADDR_KEY, position);
        intent.putExtra(MemoryContentsDialogActivity.MEM_DATA_KEY, data);

        intent.putExtra(InspectActivity.CORE_NAME, coreName);

        intent.putExtra(InspectActivity.CORE_DATA_WIDTH, decoderUnit.dataWidth());
        intent.putExtra(InspectActivity.INSTR_ADDR_WIDTH, decoderUnit.iAddrWidth());
        intent.putExtra(InspectActivity.DATA_ADDR_WIDTH, decoderUnit.dAddrWidth());
    }

    private ArrayList<AssemblyMemoryData> loadListFromFile(File file, int maxLines, Context context){
        ArrayList<AssemblyMemoryData> result = new ArrayList<>();

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
                result.add(new AssemblyMemoryData(decoderUnit, memoryType(),
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
        if(memoryType() == MemoryType.INSTRUCTION) {
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

        String directoryName = coreName + "_" + coreDataWidth + "_" + memoryType().getShortName();
        return context.getDir(directoryName, Context.MODE_PRIVATE);
    }

    private void unregisterReceiver(){
        if(receiver != null){
            broadcastManager.unregisterReceiver(receiver);
        }
    }

    public static class LoadFileDialogFragment extends DialogFragment {
        private String [] fileList;
        private MemAltFragment parentFragment;
        private Bundle dataBundle;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Context context = getContext();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.file_list_dialog_title)
                    .setItems(fileList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            parentFragment.loadListViewData(fileList[which], dataBundle);
                            parentFragment.populateListView();
                        }
                    })
                    .setNegativeButton(R.string.negative_button_text, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            LoadFileDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }

        public static LoadFileDialogFragment newInstance(MemAltFragment memFragment,
                                                         String [] files, Bundle configData){
            LoadFileDialogFragment fragment = new LoadFileDialogFragment();
            fragment.init(memFragment, files, configData);
            return fragment;
        }

        public void init(MemAltFragment memFragment, String [] files, Bundle configData){
            parentFragment = memFragment;
            fileList = files;
            dataBundle = configData;
        }
    }

    private static class AssemblyCodeAdapter extends ArrayAdapter<AssemblyMemoryData> {
        private DecoderUnit decoderUnit;
        private MemoryType memoryType;

        public AssemblyCodeAdapter(Context context, int resource, DecoderUnit decoder,
                                   List<AssemblyMemoryData> objects, MemoryType type) {
            super(context, resource);
            decoderUnit = decoder;
            memoryType = type;

            int memoryWidth = 0;
            switch (memoryType){
                case INSTRUCTION:
                    memoryWidth = decoderUnit.iAddrWidth();
                    break;
                case DATA:
                    memoryWidth = decoderUnit.dAddrWidth();
                    break;
            }

            int objectCount = objects.size();
            int listSize = 1 << memoryWidth; // 2^memoryWidth
            for(int x=0; x < listSize; ++x){
                if(x < objectCount){
                    add(objects.get(x));
                }
                else{
                    // add newly created data element to list and adapter
                    // (for easier reference when saving page data)
                    AssemblyMemoryData data = new AssemblyMemoryData(decoderUnit, memoryType);
                    objects.add(data);
                    add(data);
                }
            }
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent){
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_memory_contents, parent, false);
            }

            AssemblyMemoryData data = getItem(position);

            TextView indexTextView = (TextView) convertView.findViewById(R.id.index);
            String addressValue = null;
            switch (memoryType){
                case INSTRUCTION:
                    addressValue = decoderUnit.instrAddrValueString(position);
                    break;
                case DATA:
                    addressValue = decoderUnit.dataAddrValueString(position);
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

    public static class AssemblyMemoryData implements Parcelable {
        private String label, memoryContents, numericValue, comment, mneumonic;
        private int [] operands;
        private MemoryType memType;

        public AssemblyMemoryData(DecoderUnit decoderUnit, MemoryType type){
            mneumonic = DATA_MNEUMONIC;
            operands = new int[1];
            operands[0] = 0;
            label = "";
            comment = "";
            memType = type;

            switch (memType){
                case INSTRUCTION:
                    initAsPureData(decoderUnit.instrValueString(operands[0]));
                    break;
                case DATA:
                    initAsPureData(decoderUnit.dataValueString(operands[0]));
                    break;
            }
        }

        public AssemblyMemoryData(DecoderUnit decoderUnit, MemoryType type,
                                  String m, int []ops, String l, String c){
            mneumonic = m;
            operands = ops;
            label = l;
            comment = c;
            memType = type;

            switch (memType){
                case INSTRUCTION:
                    if(mneumonic.equals(DATA_MNEUMONIC)){
                        initAsPureData(decoderUnit.instrValueString(operands[0]));
                    }
                    else{
                        int instr = decoderUnit.encodeInstruction(mneumonic, operands);
                        decoderUnit.decode(0, instr);
                        if(decoderUnit.hasTempStorage()) {
                            decoderUnit.updateValues();
                        }
                        memoryContents = decoderUnit.instrString();
                        numericValue = decoderUnit.instrValueString(instr);
                    }
                    break;
                case DATA:
                    initAsPureData(decoderUnit.dataValueString(operands[0]));
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
