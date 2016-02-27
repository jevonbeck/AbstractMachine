package org.ricts.abstractmachine.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.ui.network.MemoryPortMultiplexerView;
import org.ricts.abstractmachine.ui.storage.MemoryPortView;
import org.ricts.abstractmachine.ui.storage.RamView;

public class MemoryPortMultiplexerTutorialActivity extends AppCompatActivity {
    private EditText addressEdit, dataEdit, selectEdit;

    // FIXME!!!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_port_multiplexer_tutorial);

        final ObservableRAM dataSource = new ObservableRAM(new RAM(8, 3, 10));

        RamView memory = (RamView) findViewById(R.id.memory);
        memory.setDataSource(dataSource.getType());

        final MemoryPortMultiplexerView mux = (MemoryPortMultiplexerView) findViewById(R.id.mux);
        mux.setSelectWidth(1);

        /*
        View [] temp = mux.getInputs();
        final MemoryPortView inputs[] =  new MemoryPortView[temp.length];
        for(int x=0; x != inputs.length; ++x){
            inputs[x] = (MemoryPortView) temp[x];
        }
        */

        addressEdit = (EditText) findViewById(R.id.addressEdit);
        dataEdit = (EditText) findViewById(R.id.dataEdit);
        selectEdit = (EditText) findViewById(R.id.selectEdit);

        Button readButton = (Button) findViewById(R.id.readButton);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mux.setSelection(getSelect());
                dataSource.read(getAddress());
                //inputs[mux.getSelection()].read(getAddress());
            }
        });

        Button writeButton = (Button) findViewById(R.id.writeButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mux.setSelection(getSelect());
                dataSource.write(getAddress(), getData());
                //inputs[mux.getSelection()].write(getAddress(), getData());
            }
        });

        /** Add observers to observables **/
        dataSource.addObserver(memory);
        dataSource.addObserver(mux);
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
