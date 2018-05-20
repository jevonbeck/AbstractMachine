package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.interfaces.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.observable.ObservableComputeCore;

public abstract class SystemArchitecture<T extends ComputeCore> {
    protected abstract ObservableComputeCore<T> createObservableComputeCore(T core);

	protected ThreadProcessingUnit tpu;
    protected ObservableComputeCore<T> mainCore;
    protected DecoderUnit decoderUnit;

    private int sysClock; // system clock

	public SystemArchitecture(T core){
        mainCore = createObservableComputeCore(core);
        decoderUnit = mainCore.getDecoderUnit();
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
        decoderUnit.reset();
        mainCore.reset();
        sysClock = 0;
    }

    public ObservableComputeCore<T> getComputeCore(){
        return mainCore;
    }
}
