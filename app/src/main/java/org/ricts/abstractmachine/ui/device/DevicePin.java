package org.ricts.abstractmachine.ui.device;

import android.view.animation.Animation.AnimationListener;

public class DevicePin {
	public enum PinDirection{
		LEFT, RIGHT, LEFTRIGHT, UP, DOWN, UPDOWN
	}
	
	public enum PinAction{
		MOVING, STATIONARY
	}
	
	public enum AnimStartBehaviour{
		IMMEDIATE, DELAY
	}
	
	public String data = "";
	public String name;
	public int dataWidth;
	public PinDirection direction = PinDirection.LEFTRIGHT;
	public PinAction action = PinAction.STATIONARY;
	public AnimStartBehaviour startBehaviour = AnimStartBehaviour.IMMEDIATE;
	public AnimationListener animListener = null;
	public int animationDelay = -1;
}
