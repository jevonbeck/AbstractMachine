package org.ricts.abstractmachine.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.observable.ObservableMemoryPort;
import org.ricts.abstractmachine.components.storage.RAM;
import org.ricts.abstractmachine.ui.storage.RamView;

public class MemoryTutorialActivity extends Activity {
	private EditText addressEdit;
	private EditText dataEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memory_tutorial);

        final ObservableMemoryPort ram = new ObservableMemoryPort(new RAM(8, 3, 10));

        RamView memory = (RamView) findViewById(R.id.memory);
		memory.setDataSource((RAM) ram.getType());
		
		addressEdit = (EditText) findViewById(R.id.addressEdit); 
		dataEdit = (EditText) findViewById(R.id.dataEdit); 
		
		Button readButton = (Button) findViewById(R.id.readButton);
		readButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ram.read(getAddress());
			}
		});

		Button writeButton = (Button) findViewById(R.id.writeButton);
		writeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ram.write(getAddress(), getData());
            }
		});

        ram.addObserver(memory);
	}
	
	private int getAddress(){
		int address;
		String addressText = addressEdit.getText().toString();
		if(addressText.equals("")){
			address = 0;
		}
		else{
			address = Integer.parseInt(addressText);
		}
		
		return address;
	}
	
	private int getData(){
		int data;
		String dataText = dataEdit.getText().toString();
		if(dataText.equals("")){
			data = 0;
		}
		else{
			data = Integer.parseInt(dataText);
		}
		
		return data;
	}
}
