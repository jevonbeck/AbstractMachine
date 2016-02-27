package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.util.AttributeSet;

import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.storage.ROM;

import java.util.Observable;

public class RamView extends RomView {

    public RamView(Context context) {
		super(context);
	}

	public RamView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RamView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    @Override
    public void update(Observable observable, Object o) {
        if(updatePins)
            memoryPins.update(observable, o); // initialise animation
        else if(o instanceof ObservableRAM.WriteParams)
            updateRomUI(); // immediately update ROM UI
    }

    @Override
    public void setDataSource(ROM r){
        super.setDataSource(r);

        memoryPins.setWriteResponder(new MemoryPortView.WriteResponder() {
            @Override
            public void onWriteFinished() {
                updateRomUI();
            }

            @Override
            public void onWriteStart() {

            }
        });
    }

    private void updateRomUI(){
        dataAdapter.notifyDataSetChanged();
    }
}
