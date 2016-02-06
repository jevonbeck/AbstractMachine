package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.util.AttributeSet;

import org.ricts.abstractmachine.components.storage.ROM;

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
    public void setDataSource(ROM r){
        super.setDataSource(r);

        memoryPins.setWriteResponder(new MemoryPortView.WriteResponder() {
            @Override
            public void onWriteFinished() {
                dataAdapter.notifyDataSetChanged(); // Animate rom UI
            }

            @Override
            public void onWriteStart() {

            }
        });
    }
}
