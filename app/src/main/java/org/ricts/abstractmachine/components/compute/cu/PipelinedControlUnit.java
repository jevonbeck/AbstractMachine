package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.cu.fsm.ControlUnitFSM;
import org.ricts.abstractmachine.components.compute.cu.fsm.PipelinedControlUnitFSM;
import org.ricts.abstractmachine.components.interfaces.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.CuFsmInterface;
import org.ricts.abstractmachine.components.interfaces.FetchCore;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.Register;
import org.ricts.abstractmachine.components.storage.RegisterImpl;

/**
 * Created by Jevon on 09/07/2016.
 */
public class PipelinedControlUnit extends ControlUnitCore {
    private boolean branched;

    private Register branchPC, branchIR;
    private PipelinedControlUnitFSM pipelinedCuFSM;

    public PipelinedControlUnit(ComputeCore core, ReadPort instructionCache){
        super(core, instructionCache);

        int iAddrWidth = decoderUnit.iAddrWidth();
        int instrWidth = decoderUnit.instrWidth();

        branchPC = new RegisterImpl(iAddrWidth);
        branchIR = new RegisterImpl(instrWidth);

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
        else { // ... it's a reset, so we need to explicitly set each FSM state
            regCore.setPcAndIr(instructionAddress, nopInstruction);
            decoderUnit.invalidateValues();
            mainFSM.setNextState(ACTIVE_STATE);
        }
    }

    @Override
    public boolean isPipelined() {
        return true;
    }

    @Override
    public void performNextAction() {
        // advance FSMs
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
                decoderUnit.invalidateValues();
                branched = false;
            }
            else {
                regCore.updatePcWithExpectedValues();
                decoderUnit.updateValues();
            }
        }
    }

    @Override
    public int nextActionDuration() {
        return pipelinedCuFSM.nextActionDuration();
    }

    @Override
    protected FetchUnit createRegCore(ReadPort instructionCache, int pcWidth, int irWidth) {
        return new FetchUnit(instructionCache, pcWidth, irWidth, true);
    }

    @Override
    protected CuFsmInterface createMainFSM(FetchCore regCore, ComputeCore core) {
        pipelinedCuFSM = new PipelinedControlUnitFSM(regCore, core);
        return pipelinedCuFSM;
    }

    @Override
    protected DefaultValueSource createDefaultValueSource() {
        return new DefaultValueSource() {
            @Override
            public int defaultValue() {
                return decoderUnit.getNopInstruction();
            }
        };
    }

    @Override
    protected void resetInternal() {
        branched = false;
    }

    public ControlUnitFSM getFsm1(){
        return pipelinedCuFSM.getFsm1();
    }

    public ControlUnitFSM getFsm2(){
        return pipelinedCuFSM.getFsm2();
    }

    public ControlUnitFSM getFsm3(){
        return pipelinedCuFSM.getFsm3();
    }

    private boolean isNormalExecution(){
        return pipelinedCuFSM.isInActiveState();
    }
}
