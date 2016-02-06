package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.interfaces.RegisterPort;
import org.ricts.abstractmachine.components.storage.Register;

/**
 * Created by Jevon on 16/01/2016.
 */
public class ObservableRegister extends ObservableType<Register> implements RegisterPort {

    public ObservableRegister(Register reg){
        super(reg);
    }

    @Override
    public int read() {
        return observable_data.read();
    }

    @Override
    public void write(int data) {
        observable_data.write(data);
        setChanged();
        notifyObservers();
    }
}
