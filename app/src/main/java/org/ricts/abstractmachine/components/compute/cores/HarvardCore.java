package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;

public class HarvardCore implements ThreadProcessingUnit{
	private PipelinedControlUnit pipelinedCU;
  
	public HarvardCore(ComputeCoreInterface core, ReadPort instructionCache, MemoryPort dataMemory){
        pipelinedCU = new PipelinedControlUnit(core, instructionCache, dataMemory);
	}
	
	@Override
	public void setStartExecFrom(int currentPC){
        pipelinedCU.setStartExecFrom(currentPC);
	}  
	
	@Override
	public int nextActionTransitionTime() {
		return pipelinedCU.nextActionDuration();
	}

	@Override
	public void triggerNextAction() {
        pipelinedCU.performNextAction();
	}

    @Override
    public void reset() {
        pipelinedCU.reset();
    }

    public ObservableControlUnit getCu1(){
        return pipelinedCU.getCu1();
    }

    public ObservableControlUnit getCu2(){
        return pipelinedCU.getCu2();
    }
}
