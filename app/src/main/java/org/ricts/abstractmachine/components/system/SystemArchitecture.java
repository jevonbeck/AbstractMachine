package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.compute.cores.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.observables.ObservableComputeCore;

public abstract class SystemArchitecture {
	protected ThreadProcessingUnit tpu;
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
		int result = tpu.nextActionTransitionTime();
		tpu.triggerNextAction();
		sysClock += result; 
	}

    public void reset(){
        tpu.reset();
        mainCore.reset();
        sysClock = 0;
    }

    public ObservableComputeCore getComputeCore(){
        return mainCore;
    }
}
