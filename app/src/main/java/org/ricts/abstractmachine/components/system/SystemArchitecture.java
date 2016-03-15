package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;

public abstract class SystemArchitecture {
	protected ThreadProcessingUnit processorCore;
    protected ObservableComputeCore mainCore;

    private int sysClock; // system clock
  	
	public SystemArchitecture(ComputeCore core){
        mainCore = new ObservableComputeCore<ComputeCore>(core);

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

    public ObservableComputeCore getComputeCore(){
        return mainCore;
    }
}
