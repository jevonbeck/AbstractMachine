package org.ricts.abstractmachine.components.system;

import org.ricts.abstractmachine.components.interfaces.CompCore;
import org.ricts.abstractmachine.components.interfaces.DecoderUnit;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.observable.ObservableComputeAltCore;

public abstract class SystemAltArchitecture<T extends CompCore> {
    protected abstract ObservableComputeAltCore<T> createObservableComputeCore(T core);

	protected ThreadProcessingUnit tpu;
    protected ObservableComputeAltCore<T> mainCore;
    protected DecoderUnit decoderUnit;

    private int sysClock; // system clock

	public SystemAltArchitecture(T core){
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

    public ObservableComputeAltCore<T> getComputeCore(){
        return mainCore;
    }
}
