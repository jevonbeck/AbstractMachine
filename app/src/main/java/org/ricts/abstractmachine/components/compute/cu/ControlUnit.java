package org.ricts.abstractmachine.components.compute.cu;

import org.ricts.abstractmachine.components.interfaces.ComputeCoreInterface;
import org.ricts.abstractmachine.components.interfaces.ControlUnitInterface;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.ReadPort;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;

public class ControlUnit implements ControlUnitInterface {
    private FiniteStateMachine fsm;
    private StateEngine engine;

    public ControlUnit(){
        fsm = new FiniteStateMachine();
    }

    public ControlUnit(RegisterPort instrPtr, RegisterPort instruction,
                       ComputeCoreInterface core, ReadPort instructionCache,
                       MemoryPort dataMemory){
        this();
        setStateEngine(new StateEngine(instrPtr, instruction, core,
                instructionCache, dataMemory, this));
    }

    @Override
    public boolean isAboutToExecute(){
        return fsm.currentState() == engine.getExecuteState();
    }

    @Override
    public void setToFetchState(){
        fsm.setCurrentState(engine.getFetchState());
    }

    @Override
    public void setToExecuteState(){
        fsm.setCurrentState(engine.getExecuteState());
    }

    @Override
    public void setToHaltState() {
        fsm.setCurrentState(engine.getHaltState());
    }

    @Override
    public void performNextAction(){
        fsm.doCurrentStateAction();
        fsm.goToNextState();
    }

    @Override
    public int nextActionDuration(){ // in clock cycles
        return ((ControlUnitState) fsm.currentState()).actionDuration();
    }

    @Override
    public int getPC(){
        return engine.pcRead();
    }

    @Override
    public void setPC(int currentPC){
        engine.pcWrite(currentPC);
    }

    @Override
    public void setIR(int currentIR){
        engine.irWrite(currentIR);
    }

    public String getCurrentState(){
        return fsm.currentState().getName();
    }

    public void setStateEngine(StateEngine e){
        engine = e;
        setToFetchState();
    }

    public static class StateEngine{
        private RegisterPort pc; // Program Counter
        private RegisterPort ir; // Instruction Register

        private ControlUnitState fetch, execute, halt;

        public StateEngine(RegisterPort instrPtr, RegisterPort instruction, ComputeCoreInterface core,
                           ReadPort instructionCache, MemoryPort dataMemory, ControlUnitInterface cu){
            pc = instrPtr;
            ir = instruction;

            // setup instruction cycle
            fetch = new ControlUnitFetchState(pc, instructionCache, ir);
            execute = new ControlUnitExecuteState(ir, core, dataMemory, cu);
            halt = new ControlUnitHaltState();

            fetch.setNextState(execute);
            execute.setNextState(fetch);
        }

        public int pcRead(){
            return pc.read();
        }

        public void pcWrite(int currentPC){
            pc.write(currentPC);
        }

        public void irWrite(int currentIR){
            ir.write(currentIR);
        }

        public ControlUnitState getFetchState(){
            return fetch;
        }

        public ControlUnitState getExecuteState(){
            return execute;
        }

        public ControlUnitState getHaltState(){
            return halt;
        }
    }
}