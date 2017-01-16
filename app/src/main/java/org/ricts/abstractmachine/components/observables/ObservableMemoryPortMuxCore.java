package org.ricts.abstractmachine.components.observables;

import org.ricts.abstractmachine.components.interfaces.MemoryPortMuxCore;

/**
 * Created by Jevon on 14/01/2017.
 */

public class ObservableMemoryPortMuxCore extends ObservableType<MemoryPortMuxCore> implements MemoryPortMuxCore {
    public static class WriteParams extends Params{
        protected enum Args{
            INDEX, ADDRESS, DATA
        }

        public WriteParams(Object... a){
            super(a);
        }

        public int getPortId(){
            return (Integer) params[Args.INDEX.ordinal()];
        }

        public int getAddress(){
            return (Integer) params[Args.ADDRESS.ordinal()];
        }

        public int getData(){
            return (Integer) params[Args.DATA.ordinal()];
        }
    }

    public ObservableMemoryPortMuxCore(MemoryPortMuxCore type) {
        super(type);
    }

    @Override
    public int read(int portId, int address) {
        setChanged();
        notifyObservers(new WriteParams(portId, address));
        return observable_data.read(portId, address);
    }

    @Override
    public void write(int portId, int address, int data) {
        observable_data.write(portId, address, data);
        setChanged();
        notifyObservers(new WriteParams(portId, address, data));
    }
}
