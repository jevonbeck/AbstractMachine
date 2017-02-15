package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.cores.UniMemoryCpuCore;
import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.Multiplexer;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.Register;

public class ControlUnit implements CuDataInterface {
    private Register pc; // Program Counter
    private Register ir; // Instruction Register

    private Multiplexer mux;
    private ControlUnitFSM fsm;

    public ControlUnit(ComputeCoreInterface core, ReadPort instructionCache,
                       MemoryPort dataMemory, Multiplexer muxInterface){
        pc = new Register(core.iAddrWidth());
        ir = new Register(core.instrWidth());
        fsm = new ControlUnitFSM(this, core, instructionCache, dataMemory);
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
        if(isInExecuteState()){
            mux.setSelection(UniMemoryCpuCore.PortIdentifier.DATA_MEM.ordinal());
        }
        else {
            mux.setSelection(UniMemoryCpuCore.PortIdentifier.INSTRUCTION_MEM.ordinal());
        }

        fsm.triggerStateChange();
    }

    @Override
    public int nextActionDuration(){ // in clock cycles
        return fsm.nextActionDuration();
    }

    @Override
    public void setNextFetch(int instructionAddress){
        setPC(instructionAddress);
        fsm.setNextStateToFetch();
    }

    @Override
    public void setNextFetchAndExecute(int instructionAddress, int nopInstruction) {
        // This method is only implemented by pipelined control unit
    }

    @Override
    public void reset() {
        setStartExecFrom(0);
    }

    @Override
    public void setStartExecFrom(int currentPC) {
        setPC(currentPC);
        setIR(0);
        setToFetchState();
    }

    @Override
    public void fetchInstruction(ReadPort instructionCache){
        int pcValue = getPC();
        setIR(instructionCache.read(pcValue)); // IR = iCache[PC]
        setPC(pcValue + 1); // PC += 1
    }

    @Override
    public boolean isPipelined() {
        return false;
    }

    @Override
    public String getPCDataString() {
        return pc.dataString();
    }

    @Override
    public String getIRDataString() {
        return ir.dataString();
    }

    @Override
    public String getCurrentStateString(){
        return fsm.getCurrentStateString();
    }

    @Override
    public int getPC(){
        return pc.read();
    }

    @Override
    public int getIR() {
        return ir.read();
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

    private void setPC(int currentPC){
        pc.write(currentPC);
    }

    private void setIR(int currentIR){
        ir.write(currentIR);
    }
}