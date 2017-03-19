package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.cu.ControlUnitState.GenericCUState;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.Register;

/**
 * Created by Jevon on 09/07/2016.
 */
public class PipelinedControlUnit extends ControlUnitCore {
    private boolean branched;
    private GenericCUState currentState;

    private ComputeCoreInterface mainCore;
    private ControlUnitFSM fsm1;
    private ControlUnitFSM fsm2;

    private Register branchPC;
    private Register branchIR;

    public PipelinedControlUnit(ComputeCoreInterface core, ReadPort instructionCache){
        super(instructionCache, core.iAddrWidth(), core.instrWidth());
        mainCore = core;

        int iAddrWidth = mainCore.iAddrWidth();
        int instrWidth = mainCore.instrWidth();

        branchPC = new Register(iAddrWidth);
        branchIR = new Register(instrWidth);

        /* N.B. : Both FSMs are connected to the same instructionCache and dataMemory!
           During normal operation, one performs a fetch while the other executes... ALWAYS! */

        // FSM 1 - initial state = 'fetch'
        fsm1 = new ControlUnitFSM(regCore, core);

        // FSM 2 - initial state = 'execute'
        fsm2 = new ControlUnitFSM(regCore, core);

        // initialise FSMs
        reset();
    }

    @Override
    public void setNextFetch(int instructionAddress) {
        // This method is only implemented by non-pipelined control unit
    }

    @Override
    public void setNextFetchAndExecute(int instructionAddress, int nopInstruction) {
        if(isNormalExecution()) { // if normal execution ...
            // ... set branch registers
            branched = true;
            setBranchPC(instructionAddress);
            setBranchIR(nopInstruction);
        }
        else { // ... we need to explicitly set each FSM state
            currentState = GenericCUState.ACTIVE;

            regCore.setPcAndIr(instructionAddress, nopInstruction);
            fsm1.setToFetchState();
            fsm2.setToExecuteState();
        }
    }

    @Override
    public void setStartExecFrom(int currentPC) {
        currentState = GenericCUState.ACTIVE;
        branched = false;

        regCore.setPcAndIr(currentPC, mainCore.getNopInstruction());
        fsm1.setToFetchState();
        fsm2.setToExecuteState();
    }

    @Override
    public boolean isPipelined() {
        return true;
    }

    @Override
    public void setNextStateToHalt() {
        // fsm1 and fsm2 do nothing
        fsm1.setNextStateToHalt();
        fsm2.setNextStateToHalt();
        currentState = GenericCUState.HALT;
    }

    @Override
    public void setNextStateToSleep() {
        // fsm1 does nothing while fsm2 executes sleep
        fsm1.setNextStateToHalt();
        fsm2.setNextStateToSleep();
        currentState = GenericCUState.SLEEP;
    }

    @Override
    public boolean isInHaltState() {
        return currentState.equals(GenericCUState.HALT);
    }

    @Override
    public boolean isInSleepState() {
        return currentState.equals(GenericCUState.SLEEP);
    }

    @Override
    public void performNextAction() {
        // advance both FSMs
        fsm1.triggerStateChange();
        fsm2.triggerStateChange();

        if(isNormalExecution()){
            /*
             * N.B: Only after both FSMs have 'executed' can a proper assessment of final PC and IR state be made.
             * If one FSM executes while the other fetches, then for non-branching instruction, the PC should be one more than the previous value.
             * This is true since the fetch stage increments the PC, while the execute stage only modifies PC if the instruction is branching.
             * If a branch is detected, the next instruction to execute should be a NOP.
             * */

            if(branched){ // if branch has occurred ...
                // ... don't execute instruction that was just fetched!
                regCore.setPcAndIr(getBranchPC(), getBranchIR());
                branched = false;
            }
            else {
                regCore.updatePcWithExpectedValues();
            }
        }
    }

    @Override
    public int nextActionDuration() {
        return Math.max(fsm1.nextActionDuration(), fsm2.nextActionDuration());
    }

    @Override
    public String getPCDataString() {
        return regCore.getPCDataString();
    }

    @Override
    public String getIRDataString() {
        return regCore.getIRDataString();
    }

    @Override
    public String getCurrentStateString(){
        return currentState.name();
    }

    @Override
    protected CuRegCore createRegCore(ReadPort instructionCache, int pcWidth, int irWidth) {
        return new CuRegCore(instructionCache, pcWidth, irWidth, true);
    }

    public ControlUnitFSM getFsm1(){
        return fsm1;
    }

    public ControlUnitFSM getFsm2(){
        return fsm2;
    }

    private boolean isNormalExecution(){
        return currentState.equals(GenericCUState.ACTIVE);
    }

    private int getBranchPC(){
        return branchPC.read();
    }

    private void setBranchPC(int currentPC){
        branchPC.write(currentPC);
    }

    private int getBranchIR(){
        return branchIR.read();
    }

    private void setBranchIR(int currentIR){
        branchIR.write(currentIR);
    }
}
