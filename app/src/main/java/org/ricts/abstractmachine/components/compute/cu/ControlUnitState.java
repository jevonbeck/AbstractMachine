package org.ricts.abstractmachine.components.compute.cu;


public abstract class ControlUnitState extends State {
    public enum GenericCUState {
        FETCH, EXECUTE, SLEEP, HALT, ACTIVE
    }

    public abstract int actionDuration();

    public ControlUnitState(GenericCUState sName){
        super(sName.name());
    }
}
