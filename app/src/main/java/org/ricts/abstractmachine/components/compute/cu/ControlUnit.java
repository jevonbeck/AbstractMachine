package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.storage.Register;

public class ControlUnit extends FiniteStateMachine implements ControlUnitInterface {
    private Register pc; // Program Counter
    private Register ir; // Instruction Register

    private ControlUnitState fetch, execute, halt;
    private State nextState = null;

    public ControlUnit(ComputeCoreInterface core, ReadPort instructionCache,
                       MemoryPort dataMemory){
        pc = new Register(core.iAddrWidth());
        ir = new Register(core.instrWidth());

        // setup instruction cycle
        fetch = new ControlUnitFetchState(this, instructionCache);
        execute = new ControlUnitExecuteState(core, dataMemory, this);
        halt = new ControlUnitHaltState();

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

        return halt;
    }

    @Override
    public void setNextStateToHalt() {
        nextState = halt;
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
    public void setPC(int currentPC){
        pc.write(currentPC);
    }

    @Override
    public void setStartExecFrom(int currentPC){
        setPC(currentPC);
        setCurrentState(fetch);
    }

    @Override
    public void reset() {
        setStartExecFrom(0);
    }

    public int getPC(){
        return pc.read();
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

    public void setToExecuteState(){
        setCurrentState(execute);
    }

    public boolean isAboutToFetch() {
        return currentState() == fetch;
    }

    public boolean isAboutToExecute(){
        return currentState() == execute;
    }

    public boolean isAboutToHalt(){
        return currentState() == halt;
    }

    public Register getPcReg(){
        return pc;
    }

    public Register getIrReg(){
        return ir;
    }
}