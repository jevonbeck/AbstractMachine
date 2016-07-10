package org.ricts.abstractmachine.ui.device;

import android.view.animation.Animation.AnimationListener;

public class DevicePin {
	public enum PinDirection{
		LEFT, RIGHT, UP, DOWN
	}
	
	public enum PinAction{
		MOVING, STATIONARY
	}
	
	public enum AnimStartBehaviour{
		IMMEDIATE, DELAY
	}

	public DevicePin(){

	}

	public DevicePin(DevicePin pin){
		data = pin.data;
		name = pin.name;
		direction = pin.direction;
		action = pin.action;
		startBehaviour = pin.startBehaviour;
		animListener = pin.animListener;
		animationDelay = pin.animationDelay;
	}

	public String data = "";
	public String name = "";
	public PinDirection direction = PinDirection.LEFT;
	public PinAction action = PinAction.STATIONARY;
	public AnimStartBehaviour startBehaviour = AnimStartBehaviour.IMMEDIATE;
	public AnimationListener animListener = null;
	public int animationDelay = -1;
}
