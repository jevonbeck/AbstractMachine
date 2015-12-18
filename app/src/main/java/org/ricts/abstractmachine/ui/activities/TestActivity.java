package org.ricts.abstractmachine.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;
import org.ricts.abstractmachine.ui.network.MemoryPortMultiplexerView;
import org.ricts.abstractmachine.ui.storage.RamView;

public class TestActivity extends Activity {
    private EditText addressEdit, dataEdit, selectEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // TODO: Make MemoryPortMultiplexerView tutorial?!
        RamView memory = (RamView) findViewById(R.id.memory);
        memory.initMemory(8, 3, 10);

        final MemoryPortMultiplexerView mux = (MemoryPortMultiplexerView) findViewById(R.id.mux);
        mux.initMux(1, 8, 3);
        mux.setOutputSource(memory);

        final MemoryPortView inputs[] =  mux.getInputs();

        addressEdit = (EditText) findViewById(R.id.addressEdit);
        dataEdit = (EditText) findViewById(R.id.dataEdit);
        selectEdit = (EditText) findViewById(R.id.selectEdit);

        Button readButton = (Button) findViewById(R.id.readButton);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mux.setSelection(getSelect());
                inputs[mux.getSelection()].read(getAddress());
            }
        });

        Button writeButton = (Button) findViewById(R.id.writeButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mux.setSelection(getSelect());
                inputs[mux.getSelection()].write(getAddress(), getData());
            }
        });
    }

    private int getAddress(){
        return getEditValue(addressEdit);
    }

    private int getData(){
        return getEditValue(dataEdit);
    }

    private int getSelect(){
        return getEditValue(selectEdit);
    }

    private int getEditValue(EditText editText){
        int data;
        String dataText = editText.getText().toString();
        if(dataText.equals("")){
            data = 0;
        }
        else{
            data = Integer.parseInt(dataText);
        }

        return data;
    }
}
