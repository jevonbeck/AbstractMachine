package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

/**
 * Created by Jevon on 09/07/2016.
 */
public class PipelinedControlUnit extends Device implements CuDataInterface {
    private int currentPCVal, currentIRVal;
    private int pcWidth, irWidth;
    private String currentState, activeString, haltString, sleepString;

    private ComputeCoreInterface mainCore;
    private ControlUnit cu1; // Control Unit for TU 1
    private ControlUnit cu2; // Control Unit for TU 2

    public PipelinedControlUnit(ComputeCoreInterface core, ReadPort instructionCache, MemoryPort dataMemory){
        mainCore = core;
        pcWidth = mainCore.iAddrWidth();
        irWidth = mainCore.instrWidth();
        activeString = "active";
        haltString = "halt";
        sleepString = "sleep";

        /* N.B. : Both thread units are connected to the same instructionCache and dataMemory!
           During normal operation, one performs a fetch while the other executes... ALWAYS! */

        // thread unit 1 (TU 1) - initial state = 'fetch'
        cu1 = new ControlUnit(core, instructionCache, dataMemory);

        // thread unit 2 (TU 2) - initial state = 'execute'
        cu2 = new ControlUnit(core, instructionCache, dataMemory);

        // initialise thread units
        reset();
    }

    @Override
    public void setNextFetch(int instructionAddress) {
        // This method is only implemented by non-pipelined control unit
    }

    @Override
    public void setNextFetchAndExecute(int instructionAddress, int nopInstruction) {
        currentPCVal = instructionAddress;
        currentIRVal = nopInstruction;

        if(isNormalExecution()){ // ensure that we are in normal execution ...
            // N.B: This function is only called by the currently executing CU.
            // As a result, only the currently executing CU should be updated.
            ControlUnit executingCU = cu1.isInExecuteState() ? cu1 : cu2;
            executingCU.setNextFetch(currentPCVal);
        }
        else { // ... otherwise we need to explicitly set each CU
            currentState = activeString;

            cu1.setPC(currentPCVal);
            cu1.setToFetchState();

            cu2.setPC(currentPCVal + 1);
            cu2.setIR(currentIRVal);
            cu2.setToExecuteState(); // delay by 1 instruction cycle stage to facilitate pipeline
        }
    }

    @Override
    public void reset() {
        currentPCVal = 0;
        currentIRVal = mainCore.getNopInstruction();
        currentState = activeString;

        cu1.reset();

        cu2.setPC(currentPCVal + 1);
        cu2.setIR(currentIRVal);
        cu2.setToExecuteState(); // delay by 1 instruction cycle stage to facilitate pipeline
    }

    @Override
    public boolean isPipelined() {
        return true;
    }

    @Override
    public void setNextStateToHalt() {
        // cu1 and cu2 do nothing
        cu1.setNextStateToHalt();
        cu2.setNextStateToHalt();
        currentState = haltString;
    }

    @Override
    public void setNextStateToSleep() {
        // cu1 does nothing while cu2 executes sleep
        cu1.setNextStateToHalt();
        cu2.setNextStateToSleep();
        currentState = sleepString;
    }

    @Override
    public boolean isInHaltState() {
        return currentState.equals(haltString);
    }

    @Override
    public boolean isInSleepState() {
        return currentState.equals(sleepString);
    }

    @Override
    public void performNextAction() {
        // advance both thread units
        cu1.performNextAction();
        cu2.performNextAction();

        if(isNormalExecution()){
            /*
             * N.B: Only after both thread units have 'executed' can a proper assessment of thread unit synchronisation be made.
             * If one unit executes while the other fetches, then for non-branching instruction, the PC should be the same for each unit.
             * This is true since the fetch stage increments the PC, while the execute stage only modifies PC if the instruction is branching.
             * If a branch is detected, the next instruction to execute should be a NOP.
             * */
            int val1 = cu1.getPC();
            int val2 = cu2.getPC();

            // newIR is obtained from thread unit that just fetched an instruction
            // newPC is obtained from thread unit that just executed an instruction
            // thread unit that fetched an instruction is loaded with newPC + 1 (always assumes no branching)

            ControlUnit nextExecutingCU = cu1.isInExecuteState() ? cu1 : cu2;
            if(val1 != val2){ // if branch has occurred ...
                nextExecutingCU.setIR(currentIRVal); // ... don't execute instruction that was just fetched!
                // currentPCVal and currentIRVal will already have been set
            }
            else {
                currentPCVal = val1; // it doesn't matter which value is used to set currentPCVal
                currentIRVal = nextExecutingCU.getIR();
            }

            nextExecutingCU.setPC(currentPCVal + 1); // fetch this instruction after executing
        }
    }

    @Override
    public int nextActionDuration() {
        return Math.max(cu1.nextActionDuration(), cu2.nextActionDuration());
    }

    @Override
    public String getPCDataString() {
        return formatNumberInHex(currentPCVal, pcWidth);
    }

    @Override
    public String getIRDataString() {
        return formatNumberInHex(currentIRVal, irWidth);
    }

    @Override
    public String getCurrentStateString(){
        return currentState;
    }

    private boolean isNormalExecution(){
        return currentState.equals(activeString);
    }

    public ControlUnit getCu1(){
        return cu1;
    }

    public ControlUnit getCu2(){
        return cu2;
    }
}
