package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.compute.ComputeCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;

public class ControlUnit {
    private RegisterPort pc; // Program Counter
    private RegisterPort ir; // Instruction Register
  
    private FiniteStateMachine fsm;
    private ControlUnitFetchState fetch;
    private ControlUnitExecuteState execute;

    public ControlUnit(RegisterPort instrAddr, RegisterPort instruction, ComputeCore core,
                       ReadPort instructionCache, MemoryPort dataMemory){
        pc = instrAddr;
        ir = instruction;
        fsm = new FiniteStateMachine();

        // setup instruction cycle
        fetch = new ControlUnitFetchState(core.clockFrequency(), pc, instructionCache, ir);
        execute = new ControlUnitExecuteState(ir, core, dataMemory, pc);

        fetch.setNextState(execute);
        execute.setNextState(fetch);

        setToFetchState();
    }

    public void setToFetchState(){
        fsm.setCurrentState(fetch);
    }

    public void setToExecuteState(){
        fsm.setCurrentState(execute);
    }

    public boolean isAboutToExecute(){
        return fsm.currentState() == execute;
    }

    private boolean terminatingCondition(){
        return ir.read() == 0 && isAboutToExecute();
    }

    public void performNextAction(){
        if(!terminatingCondition()){ // only execute instruction if non-zero. When IR == 0, execution ends
            fsm.doCurrentStateAction();
            fsm.goToNextState();
        }
    }

    public int nextActionDuration(){ // in clock cycles
        if(!terminatingCondition()){
            if(fsm.currentState() == fetch){
                return fetch.actionDuration();
            }
            else{
                return execute.actionDuration();
            }
        }
        else{
            return 1;
        }
    }
}