package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.cores.UniMemoryCpuCore;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.Multiplexer;
import org.ricts.abstractmachine.components.interfaces.ReadPort;

public class ControlUnit extends ControlUnitCore {
    private static final int DATA_MEM_ID = UniMemoryCpuCore.SerializerInputId.DATA_MEM.ordinal();
    private static final int INS_MEM_ID = UniMemoryCpuCore.SerializerInputId.INSTRUCTION_MEM.ordinal();

    private Multiplexer mux;
    private ControlUnitFSM fsm;

    public ControlUnit(ComputeCoreInterface core, ReadPort instructionCache, Multiplexer muxInterface){
        super(instructionCache, core.iAddrWidth(), core.instrWidth());

        fsm = new ControlUnitFSM(regCore, core);
        mux = muxInterface;

        // initialise
        reset();
    }

    @Override
    public void setNextStateToHalt() {
        fsm.setNextStateToHalt();
    }

    @Override
    public void setNextStateToSleep() {
        fsm.setNextStateToSleep();
    }

    @Override
    public boolean isInHaltState(){
        return fsm.isInHaltState();
    }

    @Override
    public boolean isInSleepState(){
        return fsm.isInSleepState();
    }

    @Override
    public void performNextAction(){
        int selection = isInExecuteState() ? DATA_MEM_ID : INS_MEM_ID;
        mux.setSelection(selection);

        fsm.triggerStateChange();
    }

    @Override
    public int nextActionDuration(){ // in clock cycles
        return fsm.nextActionDuration();
    }

    @Override
    public void setNextFetch(int instructionAddress){
        regCore.setPC(instructionAddress);
        fsm.setNextStateToFetch();
    }

    @Override
    public void setNextFetchAndExecute(int instructionAddress, int nopInstruction) {
        // This method is only implemented by pipelined control unit
    }

    @Override
    public void setStartExecFrom(int currentPC) {
        regCore.setPcAndIr(currentPC, 0);
        setToFetchState();
    }

    @Override
    public boolean isPipelined() {
        return false;
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
        return fsm.getCurrentStateString();
    }

    @Override
    protected CuRegCore createRegCore(ReadPort instructionCache, int pcWidth, int irWidth) {
        return new CuRegCore(instructionCache, pcWidth, irWidth);
    }

    public boolean isInFetchState() {
        return fsm.isInFetchState();
    }

    public boolean isInExecuteState(){
        return fsm.isInExecuteState();
    }

    private void setToFetchState(){
        fsm.setToFetchState();
    }
}