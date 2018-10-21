package org.ricts.abstractmachine.components.compute.cu.fsm;


import org.ricts.abstractmachine.components.fsm.State;

public abstract class ControlUnitState extends State {
    public enum GenericCUState {
        FETCH, DECODE, EXECUTE, SLEEP, HALT, ACTIVE
    }

    public abstract int actionDuration();

    public ControlUnitState(GenericCUState sName){
        super(sName.name());
    }
}
