package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.CuDataInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.Register;

public class ControlUnit extends FiniteStateMachine implements CuDataInterface {
    private Register pc; // Program Counter
    private Register ir; // Instruction Register

    private ControlUnitState fetch, execute, halt, sleep;
    private State nextState = null;

    public ControlUnit(ComputeCoreInterface core, ReadPort instructionCache,
                       MemoryPort dataMemory){
        pc = new Register(core.iAddrWidth());
        ir = new Register(core.instrWidth());

        // setup instruction cycle
        fetch = new ControlUnitFetchState(this, instructionCache);
        execute = new ControlUnitExecuteState(core, dataMemory, this);
        halt = new ControlUnitHaltState();
        sleep = new ControlUnitSleepState(core, this);

        // initialise
        reset();
    }

    @Override
    protected State getNextState(State currentState) {
        if(nextState != null){ // If normal cycle interrupted ...
            // ... next state determined by interrupting state
            currentState = nextState;
            nextState = null;
            return currentState;
        }

        // Otherwise next state determined by current state
        if(currentState == fetch)
            return execute;
        else if(currentState == execute)
            return fetch;
        else if(currentState == sleep)
            return sleep;

        return halt;
    }

    @Override
    public void setNextStateToHalt() {
        nextState = halt;
    }

    @Override
    public void setNextStateToSleep() {
        nextState = sleep;
    }

    @Override
    public boolean isInHaltState(){
        return currentState() == halt;
    }

    @Override
    public boolean isInSleepState(){
        return currentState() == sleep;
    }

    @Override
    public void performNextAction(){
        triggerStateChange();
    }

    @Override
    public int nextActionDuration(){ // in clock cycles
        return ((ControlUnitState) currentState()).actionDuration();
    }

    @Override
    public void setNextFetch(int instructionAddress){
        setPC(instructionAddress);
        nextState = fetch;
    }

    @Override
    public void setNextFetchAndExecute(int instructionAddress, int nopInstruction) {
        // This method is only implemented by pipelined control unit
    }

    @Override
    public void reset() {
        setPC(0);
        setIR(0);
        setToFetchState();
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
        return currentState().getName();
    }

    public int getPC(){
        return pc.read();
    }

    public void setPC(int currentPC){
        pc.write(currentPC);
    }

    public int getIR() {
        return ir.read();
    }

    public void setIR(int currentIR){
        ir.write(currentIR);
    }

    public void fetchInstruction(ReadPort instructionCache){
        setIR(instructionCache.read(pc.read())); // IR = iCache[PC]
        setPC(pc.read() + 1); // PC += 1
    }

    public void setToFetchState(){
        setCurrentState(fetch);
    }

    public void setToExecuteState(){
        setCurrentState(execute);
    }

    public boolean isInFetchState() {
        return currentState() == fetch;
    }

    public boolean isInExecuteState(){
        return currentState() == execute;
    }
}