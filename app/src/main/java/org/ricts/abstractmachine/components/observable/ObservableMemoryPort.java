package org.ricts.abstractmachine.components.observable;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;

/**
 * Created by Jevon on 14/02/2017.
 */
public class ObservableMemoryPort extends ObservableReadPort<MemoryPort> implements MemoryPort{

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

    public ObservableMemoryPort(MemoryPort port){
        super(port);
    }

    @Override
    public void write(int address, int data) {
        observable_data.write(address, data);
        setChanged();
        notifyObservers(new WriteParams(address, data));
    }

    @Override
    public String dataString(int data) {
        return observable_data.dataString(data);
    }
}
