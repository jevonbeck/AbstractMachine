package org.ricts.abstractmachine.components.compute;

import org.ricts.abstractmachine.components.compute.cu.ControlUnit;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ThreadProcessingUnit;
import org.ricts.abstractmachine.components.storage.Register;

public class PipelinedCpuCore implements ThreadProcessingUnit{
	private int nopInstruction; 
  
	private Register pc1; // Program Counter for TU 1 
	private Register ir1; // Instruction Register for TU 1
	private ControlUnit cu1; // Control Unit for TU 1
  
	private Register pc2; // Program Counter for TU 2
	private Register ir2; // Instruction Register for TU 2
	private ControlUnit cu2; // Control Unit for TU 2
  
	public PipelinedCpuCore(ComputeCore core, ReadPort instructionCache, MemoryPort dataMemory){
		nopInstruction = core.nopInstruction();
				
    /* N.B. : Both thread units are connected to the same instructionCache and dataMemory! 
       One performs a fetch while the other executes... ALWAYS! */
    
		// thread unit 1 (TU 1) - initial state = 'fetch'
		pc1 = new Register(core.iAddrWidth());
	    ir1 = new Register(core.instrWidth());
	    cu1 = new ControlUnit(pc1, ir1, core, instructionCache, dataMemory);
	        
	    // thread unit 2 (TU 2) - initial state = 'execute'
	    pc2 = new Register(core.iAddrWidth());
	    ir2 = new Register(core.instrWidth());
	    cu2 = new ControlUnit(pc2, ir2, core, instructionCache, dataMemory); 
	    
	    // initialise thread units
	    setStartExecFrom(0);	
	}
	
	@Override
	public void setStartExecFrom(int currentPC){
		pc1.write(currentPC);
	  	cu1.setToFetchState();
	  	
	  	pc2.write(currentPC + 1); 
	  	ir2.write(nopInstruction);
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
		int val1 = pc1.read();
		int val2 = pc2.read();
		
		// newIR is obtained from thread unit that just fetched an instruction
		// newPC is obtained from thread unit that just executed an instruction
		// thread unit that fetched an instruction is loaded with newPC + 1 (always assumes no branching)
		if(cu1.isAboutToExecute()){ // cu1 just finished fetch!
			if(val1 != val2){ // if branch has occurred ...				
				ir1.write(nopInstruction); // ... don't execute instruction that was just fetched!
			}
			
			//ir.write(ir1.read());
			//pc.write(val2);
						
			pc1.write(val2 + 1);
		}
		else{ // cu2 just finished fetch!
			if(val1 != val2){ // if branch has occurred ...
				ir2.write(nopInstruction); // ... don't execute instruction that was just fetched!
			}
			
			//ir.write(ir2.read());
			//pc.write(val1);
			
			pc2.write(val1 + 1); 
		}	
	}
}
