package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;

public abstract class SystemArchitecture {
	protected ThreadProcessingUnit processorCore;
	private int sysClock; // system clock
  	
	public SystemArchitecture(){
		sysClock = 0;
	}
	
	public int timeElapsed(){
		return sysClock;
	}
	
	public void advanceTime(){
		int result = processorCore.nextActionTransitionTime();
		processorCore.triggerNextAction();
		sysClock += result; 
	}
}
