package org.ricts.abstractmachine.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.ui.utils.wizard.WizardFragment;

import java.util.List;

/**
 * Created by Jevon on 18/05/2016.
 */
public class InstrMemFragment extends WizardFragment {

    private ListView listView;
    private ListAdapter adapter;

    public InstrMemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // TODO: Create array adapter with custom layout and instruction data from Activity/file-system
        Bundle dataBundle = dataSource.getWizardData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_instruction_memory, container, false);

        listView = (ListView) rootView.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // TODO: Get current item data and pass it to new custom DialogFragment
                // TODO: Call show() on DialogFragment!
            }
        });
        listView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void restorePageData(Bundle bundle) {

    }

    @Override
    public void savePageData(Bundle bundle) {

    }

    public static class AssemblyCodeDialogFragment extends DialogFragment {

        public AssemblyCodeDialogFragment(){
            // Required empty public constructor
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            // TODO: create custom dialog by calling appropriate functions on builder
            return builder.create();
        }
    }

    public static class AssemblyCodeAdapter extends ArrayAdapter<AssemblyCodeData> {
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
                    add(new AssemblyCodeData(instWidth));
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

            TextView instructionTextView = (TextView) convertView.findViewById(R.id.instruction);
            instructionTextView.setText(data.getInstruction());

            TextView valueTextView = (TextView) convertView.findViewById(R.id.value);
            valueTextView.setText(data.getNumericValue());

            TextView labelTextView = (TextView) convertView.findViewById(R.id.label);
            String labelText = data.getLabel();
            labelTextView.setText(labelText);
            labelTextView.setVisibility(labelText.equals("") ? View.GONE : View.VISIBLE);

            return convertView;
        }
    }

    private static class AssemblyCodeData {
        private String label, instruction, numericValue;

        public AssemblyCodeData(int instructionWidth){
            label = "";
            instruction = "0";
            numericValue = Device.formatNumberInHex(0, instructionWidth);
        }

        public AssemblyCodeData(String i, String v){
            label = "";
            instruction = i;
            numericValue = v;
        }

        public AssemblyCodeData(String i, String v, String l){
            label = l;
            instruction = i;
            numericValue = v;
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

        public void setLabel(String l) {
            label = l;
        }

        public void setInstruction(String inst) {
            instruction = inst;
        }

        public void setNumericValue(String value) {
            numericValue = value;
        }
    }
}
