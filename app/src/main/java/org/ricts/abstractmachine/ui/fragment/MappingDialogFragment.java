package org.ricts.abstractmachine.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.components.devicetype.Device;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jevon on 23/10/2016.
 */

public class MappingDialogFragment extends DialogFragment {
    private MneumonicTypeMapping dataMapping, dataRegMapping;
    private MappingAdapter dataAdapter, dataRegAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Context context = getContext();

        /*** create children ***/
        LinearLayout mainView = new LinearLayout(context);
        mainView.setOrientation(LinearLayout.VERTICAL);
        final ListView listView = new ListView(context);
        RadioGroup radioGroup = new RadioGroup(context);

        String dataRegMappingText = context.getString(R.string.data_reg_mapping_button_text);
        String dataMappingText = context.getString(R.string.data_mapping_button_text);

        dataAdapter = new MappingAdapter(context, dataMapping);
        dataRegAdapter = new MappingAdapter(context, dataRegMapping, true);

        RadioButton dataRegMappingButton = new RadioButton(context);
        dataRegMappingButton.setId(R.id.MappingDialogFragment_dataRegMappingButton);
        dataRegMappingButton.setText(dataRegMappingText);
        RadioButton dataMappingButton = new RadioButton(context);
        dataMappingButton.setId(R.id.MappingDialogFragment_dataMappingButton);
        dataMappingButton.setText(dataMappingText);

        /*** determine children layouts and positions ***/
        LayoutParams lpViewParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LayoutParams lpListViewParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        radioGroup.addView(dataRegMappingButton, -1, lpViewParams);
        radioGroup.addView(dataMappingButton, -1, lpViewParams);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.MappingDialogFragment_dataRegMappingButton:
                        listView.setAdapter(dataRegAdapter);
                        break;
                    case R.id.MappingDialogFragment_dataMappingButton:
                        listView.setAdapter(dataAdapter);
                        break;
                }
            }
        });

        mainView.addView(radioGroup, -1, lpViewParams);
        mainView.addView(listView, -1, lpListViewParams);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(mainView)
                .setTitle(R.string.mapping_dialog_title);
        return builder.create();
    }

    public static MappingDialogFragment newInstance(MneumonicTypeMapping dataMapping,
                                                    MneumonicTypeMapping dataRegMapping) {
        MappingDialogFragment fragment = new MappingDialogFragment();
        fragment.init(dataMapping, dataRegMapping);
        return fragment;
    }

    public void init(MneumonicTypeMapping dMapping, MneumonicTypeMapping dRegMapping){
        dataMapping = dMapping;
        dataRegMapping = dRegMapping;
    }

    public static class MneumonicTypeMapping {
        private OperandInfo operandInfo;

        public MneumonicTypeMapping(OperandInfo opInfo) {
            operandInfo = opInfo;
        }

        public Map<String, Integer> getAllMappings() {
            return operandInfo.getMapping();
        }

        public void addMapping(String mneumonic, int value) {
            operandInfo.addMapping(mneumonic, value);
        }

        public void addMappingWithoutReplacement(String mneumonic, int value) {
            operandInfo.addMappingWithoutReplacement(mneumonic, value);
        }

        public void removeMapping(String mneumonic) {
            operandInfo.removeMapping(mneumonic);
        }

        public Map.Entry<String, Integer> getEntryForKey(String key) {
            Map<String, Integer> map = getAllMappings();
            for(Map.Entry<String, Integer> entry : map.entrySet()) {
                if(entry.getKey().equals(key)) {
                    return entry;
                }
            }
            return null;
        }
    }

    private static class MappingAdapter extends ArrayAdapter<Map.Entry<String, Integer>> {
        private String emptyString = "";
        private MneumonicTypeMapping dataMap;
        private boolean addWithoutReplacement;

        public MappingAdapter(Context context, MneumonicTypeMapping dMap) {
            this(context, dMap, false);
        }

        public MappingAdapter(Context context, MneumonicTypeMapping dMap, boolean withoutReplacement) {
            super(context, 0);
            dataMap = dMap;
            addWithoutReplacement = withoutReplacement;

            Map<String, Integer> dataCopy = new HashMap<>();
            dataCopy.putAll(dataMap.getAllMappings());
            dataCopy.put(emptyString, 0);

            Map.Entry<String, Integer> emptyEntry = null;
            for(Map.Entry<String, Integer> entry : dataCopy.entrySet()) {
                if(!entry.getKey().equals(emptyString)) {
                    add(entry);
                }
                else {
                    emptyEntry = entry;
                }
            }
            add(emptyEntry);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_mneumonic_map, parent, false);
            }

            Map.Entry<String, Integer> data = getItem(position);
            String key = data.getKey();
            String value = "0x" + Integer.toHexString(data.getValue());

            Button addRemoveButton = (Button) convertView.findViewById(R.id.addRemoveButton);
            EditText keyEditText = (EditText) convertView.findViewById(R.id.keyEditText);
            EditText valueEditText = (EditText) convertView.findViewById(R.id.valueEditText);

            if(!key.equals(emptyString)){
                setupAsRemoveButton(addRemoveButton, data);
                keyEditText.setText(key);
                valueEditText.setText(value);
            }
            else {
                setupAsAddButton(addRemoveButton, keyEditText.getText().toString(),
                        valueEditText.getText().toString());
                keyEditText.setText(emptyString);
                valueEditText.setText(emptyString);
            }

            return convertView;
        }

        private void setupAsAddButton(Button button, final String keyText, final String valueText) {
            button.setText(R.string.add_button_text);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button thisButton = (Button) view;

                    int value = Device.parseHex(valueText);
                    if(addWithoutReplacement) {
                        dataMap.addMappingWithoutReplacement(keyText, value);
                    }
                    else {
                        dataMap.addMapping(keyText, value);
                    }

                    Map.Entry<String, Integer> entry = dataMap.getEntryForKey(keyText);
                    setupAsRemoveButton(thisButton, entry);
                    MappingAdapter.this.insert(entry, MappingAdapter.this.getCount() - 1);
                    MappingAdapter.this.notifyDataSetChanged();
                }
            });
        }

        private void setupAsRemoveButton(Button button, final Map.Entry<String, Integer> data) {
            button.setText(R.string.remove_button_text);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dataMap.removeMapping(data.getKey());
                    MappingAdapter.this.remove(data);
                    MappingAdapter.this.notifyDataSetChanged();
                }
            });
        }
    }
}
