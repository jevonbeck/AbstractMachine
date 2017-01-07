package org.ricts.abstractmachine.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.ui.fragments.MemFragment;

/**
 * Created by Jevon on 05/01/2017.
 */

public class MemoryContentsDialogActivity extends AppCompatActivity {
    protected static final String DATA_MNEUMONIC = "DATA";

    public static final String MEM_DATA_KEY = "memoryData";
    public static final String MEM_ADDR_KEY = "memoryAddress";
    public static final String MEM_TYPE_KEY = "memoryType";

    protected int getSafeInt(EditText editText){
        return convertTextToHex(editText.getText().toString());
    }

    protected int getSafeInt(EditText editText, OperandInfo operandInfo){
        String currentText = editText.getText().toString();
        if(operandInfo.hasMneumonic(currentText)) {
            return operandInfo.decodeMneumonic(currentText);
        }

        return convertTextToHex(currentText);
    }

    protected void sendMemoryData(Context context, int memoryAddress,
                                  MemFragment.AssemblyMemoryData memoryData,
                                  String memoryType) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(MemFragment.UPDATE_MEM_LOCATION_ACTION);
        intent.putExtra(MEM_ADDR_KEY, memoryAddress);
        intent.putExtra(MEM_DATA_KEY, memoryData);
        intent.putExtra(MEM_TYPE_KEY, memoryType);
        broadcastManager.sendBroadcastSync(intent);
    }

    private int convertTextToHex(String text){
        return text.equals("") ? 0 : Device.parseHex(text);
    }
}
