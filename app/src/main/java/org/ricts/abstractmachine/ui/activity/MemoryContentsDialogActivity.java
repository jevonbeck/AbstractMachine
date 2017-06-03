package org.ricts.abstractmachine.ui.activity;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.SearchView;

import org.ricts.abstractmachine.components.compute.isa.OperandInfo;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.ui.fragment.MemFragment;
import org.ricts.abstractmachine.ui.contentprovider.OperandContentProvider;

/**
 * Created by Jevon on 05/01/2017.
 */

public class MemoryContentsDialogActivity extends AppCompatActivity {
    protected static final String DATA_MNEUMONIC = "DATA";
    protected static final String SEARCH_AUTHORITY = "org.ricts.abstractmachine.provider";

    public static final String MEM_DATA_KEY = "memoryData";
    public static final String MEM_ADDR_KEY = "memoryAddress";
    public static final String MEM_TYPE_KEY = "memoryType";

    public static final String SUGGESTION_ACTION = "org.ricts.abstractmachine.action.OPERAND_SUGGESTION";


    private ContentProviderClient providerClient;
    protected OperandContentProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getContentResolver();
        providerClient = resolver.acquireContentProviderClient(SEARCH_AUTHORITY);
        provider = (OperandContentProvider) providerClient.getLocalContentProvider();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        providerClient.release();
    }

    protected int getSafeInt(SearchView searchView, OperandInfo operandInfo){
        String currentText = searchView.getQuery().toString();
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
