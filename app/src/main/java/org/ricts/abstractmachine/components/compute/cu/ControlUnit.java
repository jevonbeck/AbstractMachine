package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.ControlUnitPort;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;

public class ControlUnit implements ControlUnitPort {
    private RegisterPort ir; // Instruction Register
    private ComputeCore mainCore;
    private FiniteStateMachine fsm;
    private ControlUnitState fetch, execute, halt;

    public ControlUnit(RegisterPort pc, RegisterPort instruction, ComputeCore core,
                       ReadPort instructionCache, MemoryPort dataMemory){
        ir = instruction;
        mainCore = core;
        fsm = new FiniteStateMachine();

        // setup instruction cycle
        fetch = new ControlUnitFetchState(pc, instructionCache, ir);
        execute = new ControlUnitExecuteState(ir, core, dataMemory, pc);
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

    public int nextActionDuration(){ // in clock cycles
        return ((ControlUnitState) fsm.currentState()).actionDuration();
    }

    public String getCurrentState(){
        return fsm.currentState().getName();
    }

    private boolean terminatingCondition(){
        // if last executed instruction can cause ComputeCore to halt
        return mainCore.isHaltInstruction(ir.read()) && fsm.currentState() == fetch;
    }
}