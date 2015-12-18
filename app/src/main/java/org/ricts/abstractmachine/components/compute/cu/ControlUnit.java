package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;

public class ControlUnit implements ControlUnitInterface {
    private RegisterPort pc; // Program Counter
    private RegisterPort ir; // Instruction Register
    private ComputeCoreInterface mainCore;
    private FiniteStateMachine fsm;
    private ControlUnitState fetch, execute, halt;

    public ControlUnit(RegisterPort instrPtr, RegisterPort instruction, ComputeCoreInterface core,
                       ReadPort instructionCache, MemoryPort dataMemory){
        pc = instrPtr;
        ir = instruction;
        mainCore = core;
        fsm = new FiniteStateMachine();

        // setup instruction cycle
        fetch = new ControlUnitFetchState(pc, instructionCache, ir);
        execute = new ControlUnitExecuteState(ir, mainCore, dataMemory, pc);
        halt = new ControlUnitHaltState();

        fetch.setNextState(execute);
        execute.setNextState(fetch);

        setToFetchState();
    }

    @Override
    public boolean isAboutToExecute(){
        return fsm.currentState() == execute;
    }

    @Override
    public void setToFetchState(){
        fsm.setCurrentState(fetch);
    }

    @Override
    public void setToExecuteState(){
        fsm.setCurrentState(execute);
    }

    @Override
    public void performNextAction(){
        fsm.doCurrentStateAction();
        fsm.goToNextState();

        if(terminatingCondition()){
            fsm.setCurrentState(halt);
        }
    }

    @Override
    public int nextActionDuration(){ // in clock cycles
        return ((ControlUnitState) fsm.currentState()).actionDuration();
    }

    @Override
    public int getPC(){
        return pc.read();
    }

    @Override
    public void setPC(int currentPC){
        pc.write(currentPC);
    }

    @Override
    public void setIR(int currentIR){
        ir.write(currentIR);
    }

    public String getCurrentState(){
        return fsm.currentState().getName();
    }

    private boolean terminatingCondition(){
        // if last executed instruction can cause ComputeCore to halt
        return mainCore.isHaltInstruction(ir.read()) && fsm.currentState() == fetch;
    }
}