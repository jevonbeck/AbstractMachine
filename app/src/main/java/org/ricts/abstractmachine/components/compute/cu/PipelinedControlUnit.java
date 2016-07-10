package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.observables.ObservableControlUnit;

/**
 * Created by Jevon on 09/07/2016.
 */
public class PipelinedControlUnit implements ControlUnitInterface {
    private int currentPCVal, currentIRVal;

    private int nopInstruction;
    private ObservableControlUnit cu1; // Control Unit for TU 1
    private ObservableControlUnit cu2; // Control Unit for TU 2

    public PipelinedControlUnit(ComputeCoreInterface core, ReadPort instructionCache, MemoryPort dataMemory){
        nopInstruction = core.nopInstruction();

        /* N.B. : Both thread units are connected to the same instructionCache and dataMemory!
           One performs a fetch while the other executes... ALWAYS! */

        // thread unit 1 (TU 1) - initial state = 'fetch'
        cu1 = new ObservableControlUnit(new ControlUnit(core, instructionCache, dataMemory));

        // thread unit 2 (TU 2) - initial state = 'execute'
        cu2 = new ObservableControlUnit(new ControlUnit(core, instructionCache, dataMemory));

        // initialise thread units
        reset();
    }

    @Override
    public void setPC(int currentPC) {
        currentPCVal = currentPC;
        currentIRVal = nopInstruction;

        cu1.setStartExecFrom(currentPCVal);

        cu2.setPC(currentPCVal + 1);
        cu2.setIR(currentIRVal);
        cu2.setToExecuteState(); // delay by 1 instruction cycle stage to facilitate pipeline
    }

    @Override
    public void setStartExecFrom(int currentPC) {
        setPC(currentPC);
    }

    @Override
    public void reset() {
        setStartExecFrom(0);
    }

    @Override
    public void setNextStateToHalt() {
        // cu1 executes halt while cu2 does nothing
        cu1.setNextStateToHalt();
        cu2.setIR(nopInstruction);
        cu2.setToExecuteState();
    }

    @Override
    public void performNextAction() {
        // advance both thread units
        cu1.performNextAction();
        cu2.performNextAction();

		/*
		 * N.B: Only after both thread units have executed can a proper assessment of thread unit synchronisation be made.
		 * If one unit executes while the other fetches, then for non-branching instruction, the PC should be the same for each unit.
		 * This is true since the fetch stage increments the PC, while the execute stage only modifies PC if the instruction is branching.
		 * If a branch is detected, the next instruction to execute should be a NOP.
		 * */
        ControlUnit controlUnit1 = cu1.getType();
        int val1 = controlUnit1.getPC();
        int val2 = cu2.getType().getPC();

        // newIR is obtained from thread unit that just fetched an instruction
        // newPC is obtained from thread unit that just executed an instruction
        // thread unit that fetched an instruction is loaded with newPC + 1 (always assumes no branching)
        ObservableControlUnit nextExecutingCU;
        if(controlUnit1.isAboutToExecute()){ // cu1 just finished fetch!
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

        currentIRVal = nextExecutingCU.getType().getIR();
        nextExecutingCU.setPC(currentPCVal + 1); // fetch this instruction after executing
    }

    @Override
    public int nextActionDuration() {
        return Math.max(cu1.nextActionDuration(), cu2.nextActionDuration());
    }

    public int getPC() {
        return currentPCVal;
    }

    public int getIR() {
        return currentIRVal;
    }

    public ObservableControlUnit getCu1(){
        return cu1;
    }

    public ObservableControlUnit getCu2(){
        return cu2;
    }
}
