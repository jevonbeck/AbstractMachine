package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.util.AttributeSet;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.storage.RAM;

public class RamView extends RomView {
	private RAM ram;

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
	public void initMemory(int dWidth, int aWidth, int accessTime){
		dataWidth = dWidth;
		addressWidth = aWidth;

		ram = new RAM(dataWidth, addressWidth, accessTime);

		init();
	}

	@Override
	protected void init(){
		rom = ram;
        super.init();
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

    public void setDataSource(RAM r){
        ram = r;
        super.setDataSource(r);
        init();
    }
}
