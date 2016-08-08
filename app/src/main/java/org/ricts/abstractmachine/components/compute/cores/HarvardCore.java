package org.ricts.abstractmachine.components.compute.cores;

import org.ricts.abstractmachine.components.compute.cu.PipelinedControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;

public class HarvardCore implements ThreadProcessingUnit{
	private PipelinedControlUnit pipelinedCU;
  
	public HarvardCore(ComputeCoreInterface core, ReadPort instructionCache, MemoryPort dataMemory){
        pipelinedCU = new PipelinedControlUnit(core, instructionCache, dataMemory);
	}
	
	@Override
	public void setStartExecFrom(int currentPC){
        pipelinedCU.setNextFetch(currentPC);
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

}
