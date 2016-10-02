package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.Register;

public class ControlUnit implements CuDataInterface {
    private Register pc; // Program Counter
    private Register ir; // Instruction Register

    private ControlUnitEngine engine;

    public ControlUnit(ComputeCoreInterface core, ReadPort instructionCache,
                       MemoryPort dataMemory){
        pc = new Register(core.iAddrWidth());
        ir = new Register(core.instrWidth());
        engine = new ControlUnitEngine(this, core, instructionCache, dataMemory);

        // initialise
        reset();
    }

    @Override
    public void setNextStateToHalt() {
        engine.setNextStateToHalt();
    }

    @Override
    public void setNextStateToSleep() {
        engine.setNextStateToSleep();
    }

    @Override
    public boolean isInHaltState(){
        return engine.isInHaltState();
    }

    @Override
    public boolean isInSleepState(){
        return engine.isInSleepState();
    }

    @Override
    public void performNextAction(){
        engine.triggerStateChange();
    }

    @Override
    public int nextActionDuration(){ // in clock cycles
        return engine.nextActionDuration();
    }

    @Override
    public void setNextFetch(int instructionAddress){
        setPC(instructionAddress);
        engine.setNextStateToFetch();
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
        return engine.getCurrentStateString();
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
        return engine.isInFetchState();
    }

    public boolean isInExecuteState(){
        return engine.isInExecuteState();
    }

    private void setToFetchState(){
        engine.setToFetchState();
    }

    private void setPC(int currentPC){
        pc.write(currentPC);
    }

    private void setIR(int currentIR){
        ir.write(currentIR);
    }
}