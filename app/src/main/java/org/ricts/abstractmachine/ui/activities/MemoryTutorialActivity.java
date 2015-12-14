package org.ricts.abstractmachine.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.storage.RamView;

public class MemoryTutorialActivity extends Activity {
	private RamView memory;
	private EditText addressEdit;
	private EditText dataEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memory_tutorial);
				
		memory = (RamView) findViewById(R.id.memory);
		memory.initMemory(8, 3, 10);
		
		addressEdit = (EditText) findViewById(R.id.addressEdit); 
		dataEdit = (EditText) findViewById(R.id.dataEdit); 
		
		Button readButton = (Button) findViewById(R.id.readButton);
		readButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				memory.read(getAddress());
			}
		});
		
		
		Button writeButton = (Button) findViewById(R.id.writeButton);
		writeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				memory.write(getAddress(), getData()); 
			}
		});
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
