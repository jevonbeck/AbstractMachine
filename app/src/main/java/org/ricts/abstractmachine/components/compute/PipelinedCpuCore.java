package org.ricts.abstractmachine.components.compute;

import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.storage.Register;

public class PipelinedCpuCore implements ThreadProcessingUnit{
	private int nopInstruction; 
  
	private ControlUnitInterface cu1; // Control Unit for TU 1
	private ControlUnitInterface cu2; // Control Unit for TU 2
  
	public PipelinedCpuCore(ComputeCoreInterface core, ReadPort instructionCache, MemoryPort dataMemory){
		nopInstruction = core.nopInstruction();
				
    /* N.B. : Both thread units are connected to the same instructionCache and dataMemory! 
       One performs a fetch while the other executes... ALWAYS! */
    
		// thread unit 1 (TU 1) - initial state = 'fetch'
        Register pc1 = new Register(core.iAddrWidth()); // Program Counter for TU 1
        Register ir1 = new Register(core.instrWidth()); // Instruction Register for TU 1
	    cu1 = new ControlUnit(pc1, ir1, core, instructionCache, dataMemory);
	        
	    // thread unit 2 (TU 2) - initial state = 'execute'
        Register pc2 = new Register(core.iAddrWidth()); // Program Counter for TU 2
        Register ir2 = new Register(core.instrWidth()); // Instruction Register for TU 2
	    cu2 = new ControlUnit(pc2, ir2, core, instructionCache, dataMemory); 
	    
	    // initialise thread units
	    setStartExecFrom(0);	
	}
	
	@Override
	public void setStartExecFrom(int currentPC){
		cu1.setPC(currentPC);
        cu1.setToFetchState();

        cu2.setPC(currentPC + 1);
        cu2.setIR(nopInstruction);
        cu2.setToExecuteState(); // delay by 1 instruction cycle stage to facilitate pipeline
	}  
	
	@Override
	public int nextActionTransitionTime() {
		return Math.max(cu1.nextActionDuration(), cu2.nextActionDuration());
	}

	@Override
	public void triggerNextAction() {
		// advance both thread units
		cu1.performNextAction();
		cu2.performNextAction();
		
		/*
		 * N.B: Only after both thread units have executed can a proper assessment of thread unit synchronisation be made.
		 * If one unit executes while the other fetches, then for non-branching instruction, the PC should be the same for each unit.
		 * This is true since the fetch stage increments the PC, while the execute stage only modifies PC if the instruction is branching.
		 * If a branch is detected, the next instruction to execute should be a NOP.
		 * */	
		int val1 = cu1.getPC();
		int val2 = cu2.getPC();
		
		// newIR is obtained from thread unit that just fetched an instruction
		// newPC is obtained from thread unit that just executed an instruction
		// thread unit that fetched an instruction is loaded with newPC + 1 (always assumes no branching)
        int currentPCVal;
        ControlUnitInterface nextExecutingCU;
		if(cu1.isAboutToExecute()){ // cu1 just finished fetch!
            nextExecutingCU = cu1;
            currentPCVal = val2;
		}
		else{ // cu2 just finished fetch!
            nextExecutingCU = cu2;
            currentPCVal = val1;
		}

        if(val1 != val2){ // if branch has occurred ...
            nextExecutingCU.setIR(nopInstruction); // ... don't execute instruction that was just fetched!
        }
        nextExecutingCU.setPC(currentPCVal + 1); // fetch this instruction after executing
	}
}
