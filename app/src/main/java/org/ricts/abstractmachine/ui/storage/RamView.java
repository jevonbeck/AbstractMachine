package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.util.AttributeSet;

import org.ricts.abstractmachine.components.observables.ObservableRAM;
import org.ricts.abstractmachine.components.storage.ROM;

import java.util.Observable;

public class RamView extends RomView {

    public RamView(Context context) {
        this(context, null);
	}

	public RamView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
	}

	public RamView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

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

    @Override
    public void update(Observable observable, Object o) {
        if(o instanceof Boolean) { // update from a reset
            updateRomUI();
        }
        else {
            if(updatePins)
                memoryPins.update(observable, o); // initialise animation
            else if(o instanceof ObservableRAM.WriteParams)
                updateRomUI(); // immediately update ROM UI
        }
    }

    public void setWriteResponder(final MemoryPortView.WriteResponder responder){
        memoryPins.setWriteResponder(new MemoryPortView.WriteResponder() {
            @Override
            public void onWriteFinished() {
                updateRomUI();
                responder.onWriteFinished();
            }

            @Override
            public void onWriteStart() {
                responder.onWriteStart();
            }
        });
    }
}
