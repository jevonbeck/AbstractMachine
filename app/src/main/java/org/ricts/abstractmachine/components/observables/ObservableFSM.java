package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.compute.cu.FiniteStateMachine;
import org.ricts.abstractmachine.components.compute.cu.State;
import org.ricts.abstractmachine.components.interfaces.FSMInterface;


/**
 * Created by Jevon on 23/01/2016.
 */
public class ObservableFSM<T extends FiniteStateMachine> extends ObservableType<T> implements FSMInterface {
    public ObservableFSM(T fsm) {
        super(fsm);
    }

    @Override
    public State currentState() {
        return observable_data.currentState();
    }

    @Override
    public void setCurrentState(State state) {
        observable_data.setCurrentState(state);
        setChanged();
        notifyObservers();
    }

    @Override
    public void triggerStateChange() {
        observable_data.triggerStateChange();
        setChanged();
        notifyObservers();
    }
}
