package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitRegCore;
import org.ricts.abstractmachine.components.interfaces.CuFsmInterface;
import org.ricts.abstractmachine.components.interfaces.DefaultValueSource;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.Register;

/**
 * Created by Jevon on 09/07/2016.
 */
public class PipelinedControlUnit extends ControlUnitCore {
    private boolean branched;

    private ComputeCoreInterface mainCore;
    private PipelinedControlUnitFSM pipelinedCuFSM;

    private Register branchPC, branchIR;

    public PipelinedControlUnit(ComputeCoreInterface core, ReadPort instructionCache){
        super(core, instructionCache);
        mainCore = core;

        int iAddrWidth = mainCore.iAddrWidth();
        int instrWidth = mainCore.instrWidth();

        branchPC = new Register(iAddrWidth);
        branchIR = new Register(instrWidth);

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
            branchPC.write(instructionAddress);
            branchIR.write(nopInstruction);
        }
        else { // ... we need to explicitly set each FSM state
            regCore.setPcAndIr(instructionAddress, nopInstruction);
            mainFSM.setNextState(ACTIVE_STATE);
        }
    }

    @Override
    public void setStartExecFrom(int currentPC) {
        super.setStartExecFrom(currentPC);
        branched = false;
    }

    @Override
    public boolean isPipelined() {
        return true;
    }

    @Override
    public void performNextAction() {
        // advance both FSMs
        mainFSM.triggerStateChange();

        if(isNormalExecution()){
            /*
             * N.B: Only after both FSMs have 'executed' can a proper assessment of final PC and IR state be made.
             * If one FSM executes while the other fetches, then for non-branching instruction, the PC should be one more than the previous value.
             * This is true since the fetch stage increments the PC, while the execute stage only modifies PC if the instruction is branching.
             * If a branch is detected, the next instruction to execute should be a NOP.
             * */
            if(branched){ // if branch has occurred ...
                // ... don't execute instruction that was just fetched!
                regCore.setPcAndIr(branchPC.read(), branchIR.read());
                branched = false;
            }
            else {
                regCore.updatePcWithExpectedValues();
            }
        }
    }

    @Override
    public int nextActionDuration() {
        return pipelinedCuFSM.nextActionDuration();
    }

    @Override
    protected CuRegCore createRegCore(ReadPort instructionCache, int pcWidth, int irWidth) {
        return new CuRegCore(instructionCache, pcWidth, irWidth, true);
    }

    @Override
    protected CuFsmInterface createMainFSM(ControlUnitRegCore regCore, ComputeCoreInterface core) {
        pipelinedCuFSM = new PipelinedControlUnitFSM(regCore, core);
        return pipelinedCuFSM;
    }

    @Override
    protected DefaultValueSource createDefaultValueSource() {
        return new DefaultValueSource() {
            @Override
            public int defaultValue() {
                return mainCore.getNopInstruction();
            }
        };
    }

    public ControlUnitFSM getFsm1(){
        return pipelinedCuFSM.getFsm1();
    }

    public ControlUnitFSM getFsm2(){
        return pipelinedCuFSM.getFsm2();
    }

    private boolean isNormalExecution(){
        return pipelinedCuFSM.isInActiveState();
    }
}
