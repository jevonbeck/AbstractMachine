package org.ricts.abstractmachine.ui.storage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import org.ricts.abstractmachine.components.Device;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;

public class RamView extends RomView implements MemoryPort {

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
	public void write(int address, int data) {
		// Setup correct data in pin UI
		DevicePin pin = pinArray[PinNames.ADDRESS.ordinal()];
		pin.data = Device.formatNumberInHex(address, addressWidth);
		pin.direction = inDirection;
		pin.action = DevicePin.PinAction.MOVING;
		
		pin = pinArray[PinNames.DATA.ordinal()];
		pin.data = Device.formatNumberInHex(data, dataWidth);
		pin.direction = inDirection;
		pin.action = DevicePin.PinAction.MOVING;
		pin.startBehaviour = DevicePin.AnimStartBehaviour.IMMEDIATE;
		pin.animListener = null;
		
		pin = pinArray[PinNames.COMMAND.ordinal()];
		pin.data = "write";
		pin.direction = inDirection;
		pin.action = DevicePin.PinAction.MOVING;
		
		final int a = address;
		final int d = data;
		pin.animListener = new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation){
				memory.write(a,d);
				//scrollToPosition(a); // ensure that address is visible
				dataAdapter.notifyDataSetChanged(); // Animate memory UI
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
				
			}

			@Override
			public void onAnimationStart(Animation arg0) {
				
			}
		};
		
		pinAdapter.notifyDataSetChanged(); // Animate pin UI
	}
}
