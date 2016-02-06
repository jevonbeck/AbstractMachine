package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.storage.RAM;

/**
 * Created by Jevon on 16/01/2016.
 */
public class ObservableRAM extends ObservableROM<RAM> implements MemoryPort{

    public static class WriteParams extends ReadParams{
        private final int enumOffset;

        public WriteParams(Object... objects){
            super(objects);
            enumOffset = Args.values().length;
        }

        public int getData(){
            return (Integer) params[enumOffset];
        }
    }

    public ObservableRAM(RAM ram){
        super(ram);
    }

    @Override
    public void write(int address, int data) {
        observable_data.write(address, data);
        setChanged();
        notifyObservers(new WriteParams(address, data));
    }
}
