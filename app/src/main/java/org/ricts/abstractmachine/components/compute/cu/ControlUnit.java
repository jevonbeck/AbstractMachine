package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.observables.ObservableRegister;
import org.ricts.abstractmachine.components.storage.Register;

public class ControlUnit extends FiniteStateMachine implements ControlUnitInterface {
    private ObservableRegister pc; // Program Counter
    private ObservableRegister ir; // Instruction Register

    private ControlUnitState fetch, execute, halt;
    private State nextState = null;

    public ControlUnit(ComputeCoreInterface core, ReadPort instructionCache,
                       MemoryPort dataMemory){
        // ObservableRegister is used to be able to view independent internal changes in state
        pc = new ObservableRegister(new Register(core.iAddrWidth()));
        ir = new ObservableRegister(new Register(core.instrWidth()));

        // setup instruction cycle
        fetch = new ControlUnitFetchState(pc, ir, instructionCache);
        execute = new ControlUnitExecuteState(core, dataMemory, this);
        halt = new ControlUnitHaltState();

        // initialise
        setPC(0);
        setCurrentState(fetch);
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
    public boolean isAboutToExecute(){
        return currentState() == execute;
    }

    @Override
    public void setToFetchState(){
        nextState = fetch;
    }

    @Override
    public void setToExecuteState(){
        nextState = execute;
    }

    @Override
    public void setToHaltState() {
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
    public int getPC(){
        return pc.read();
    }

    @Override
    public int getIR() {
        return ir.read();
    }

    @Override
    public void setPC(int currentPC){
        pc.write(currentPC);
    }

    @Override
    public void setIR(int currentIR){
        ir.write(currentIR);
    }

    @Override
    public void setStartExecFrom(int currentPC){
        pc.write(currentPC);
        setToFetchState();
    }

    public ObservableRegister getPcReg(){
        return pc;
    }

    public ObservableRegister getIrReg(){
        return ir;
    }
}