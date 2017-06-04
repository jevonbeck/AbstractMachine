package org.ricts.abstractmachine.components.network;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableMultiMemoryPort;
import org.ricts.abstractmachine.components.observable.ObservableType;

import java.util.List;

/**
 * Created by Jevon on 14/01/2017.
 */

public class MemoryPortSerializer extends MultiPortSerializer<MemoryPort, MultiMemoryPort> {

    public MemoryPortSerializer(MemoryPort port, int portCount) {
        super(port, portCount);
    }

    @Override
    protected MultiMemoryPort createMultiPortInterface(MemoryPort targetPort) {
        return new SimpleMultiMemoryPort(targetPort);
    }

    @Override
    protected MemoryPort[] createInputs(final MultiMemoryPort multiMemoryPort, int inputCount) {
        MemoryPort[] array = new MemoryPort[inputCount];
        for(int x=0; x < array.length; ++x){
            final int index = x;
            array[x] = new MemoryPort() {
                @Override
                public void write(int address, int data) {
                    multiMemoryPort.write(index, address, data);
                }

                @Override
                public String dataString(int data) {
                    return multiMemoryPort.dataString(data);
                }

                @Override
                public int read(int address) {
                    return multiMemoryPort.read(index, address);
                }

                @Override
                public void setData(List<Integer> data) {
                    // do nothing
                }

                @Override
                public int accessTime() {
                    return multiMemoryPort.accessTime();
                }

                @Override
                public String addressString(int address) {
                    return multiMemoryPort.addressString(address);
                }

                @Override
                public String dataAtAddressString(int address) {
                    return multiMemoryPort.dataAtAddressString(address);
                }
            };
        }
        return array;
    }

    @Override
    protected ObservableType<MultiMemoryPort> createObservable(MultiMemoryPort multiMemoryPort) {
        return new ObservableMultiMemoryPort(multiMemoryPort);
    }

    protected static class SimpleMultiMemoryPort implements MultiMemoryPort {
        protected MemoryPort targetMemoryPort;

        public SimpleMultiMemoryPort(MemoryPort memoryPort){
            targetMemoryPort = memoryPort;
        }

        @Override
        public int read(int portId, int address) {
            return targetMemoryPort.read(address);
        }

        @Override
        public void write(int portId, int address, int data) {
            targetMemoryPort.write(address, data);
        }

        @Override
        public int accessTime() {
            return targetMemoryPort.accessTime();
        }

        @Override
        public String addressString(int address) {
            return targetMemoryPort.addressString(address);
        }

        @Override
        public String dataAtAddressString(int address) {
            return targetMemoryPort.dataString(address);
        }

        @Override
        public String dataString(int data) {
            return targetMemoryPort.dataString(data);
        }
    }
}
