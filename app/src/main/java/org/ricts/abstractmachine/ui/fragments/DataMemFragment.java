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

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.compute.cores.ComputeCore;

/**
 * Created by Jevon on 02/07/2016.
 */
public class DataMemFragment extends MemFragment {
    @Override
    protected String memoryTypeString() {
        return "data";
    }

    @Override
    protected AssemblyMemoryData.MemoryType memoryType() {
        return AssemblyMemoryData.MemoryType.DATA;
    }

    @Override
    protected MemoryContentsDialogFragment getMemoryContentsDialogFragment(
            AssemblyMemoryData data, ComputeCore core, MemoryContentsDialogFragment.ListUpdater updater) {
        return DataMemoryDialogFragment.newInstance(data, core, updater);
    }

    public static class DataMemoryDialogFragment extends MemoryContentsDialogFragment {

        public DataMemoryDialogFragment(){
            // Required empty public constructor
        }

        public static DataMemoryDialogFragment newInstance(AssemblyMemoryData data, ComputeCore core,
                                                               ListUpdater updater){
            DataMemoryDialogFragment fragment = new DataMemoryDialogFragment();
            fragment.init(data, core, updater);
            return fragment;
        }
        
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Pass null as the parent view because its going in the dialog layout
            // TODO: create mainView from scratch
            final View mainView = inflater.inflate(R.layout.fragment_data_mem_dialog, null);

            /** Setup UI interactions and initialise UI **/
            // TODO: find a way to do hex-only input (soft keyboard?!)
            final EditText operandOneEditText = (EditText) mainView.findViewById(R.id.operandOneEditText);

            final EditText labelEditText = (EditText) mainView.findViewById(R.id.labelEditText);
            labelEditText.setText(memoryData.getLabel());

            final EditText commentEditText = (EditText) mainView.findViewById(R.id.commentEditText);
            commentEditText.setText(memoryData.getComment());

            Context context = getContext();

            /** Actually create the dialog **/
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(mainView)
                    // Add action buttons
                    .setPositiveButton(R.string.positive_button_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            /** save memoryContents data for location in adapter **/
                            int dataValue = getSafeInt(operandOneEditText);
                            String dataValueText = mainCore.dataValueString(dataValue);
                            String dataText = DATA_MNEUMONIC + " " + dataValueText;
                            int [] operands = new int[1];
                            operands[0] = dataValue;

                            // update memoryContents data in list
                            memoryData.setMneumonic(DATA_MNEUMONIC);
                            memoryData.setOperands(operands);
                            memoryData.setMemoryContents(dataText);
                            memoryData.setNumericValue(dataValueText);
                            memoryData.setLabel(labelEditText.getText().toString());
                            memoryData.setComment(commentEditText.getText().toString());
                            mUpdater.notifyUpdate();
                        }
                    })
                    .setNegativeButton(R.string.negative_button_text, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DataMemoryDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }
}
