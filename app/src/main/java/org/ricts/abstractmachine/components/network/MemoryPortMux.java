package org.ricts.abstractmachine.components.network;

import org.ricts.abstractmachine.components.interfaces.MultiplexerCore;
import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.observables.ObservableMultiplexerCore;
import org.ricts.abstractmachine.components.observables.ObservableType;

/**
 * Created by Jevon on 14/01/2017.
 */

public class MemoryPortMux extends MultiPortSerializer<MemoryPort, MultiplexerCore> {
    private int currentSel;

    public MemoryPortMux(MemoryPort port, int portCount) {
        super(port, portCount);
        setSelection(0);
    }

    public int getSelection(){
        return currentSel;
    }

    public void setSelection(int sel){
        currentSel = sel & portIndexMask;
    }

    public String getSelectionText(){
        return formatNumberInHex(currentSel, portIndexMask);
    }

    @Override
    protected MultiplexerCore createSerializerCore(final MemoryPort targetPort) {
        return new MultiplexerCore(){
            @Override
            public int read(int portId, int address) {
                setSelection(portId);
                return targetPort.read(address);
            }

            @Override
            public void write(int portId, int address, int data) {
                setSelection(portId);
                targetPort.write(address, data);
            }
        };
    }

    @Override
    protected MemoryPort[] createInputs(final MultiplexerCore serializerCore, int inputCount) {
        MemoryPort[] array = new MemoryPort[inputCount];
        for(int x=0; x < array.length; ++x){
            final int index = x;
            array[x] = new MemoryPort() {
                @Override
                public void write(int address, int data) {
                    serializerCore.write(index, address, data);
                }

                @Override
                public int read(int address) {
                    return serializerCore.read(index, address);
                }

                @Override
                public int accessTime() {
                    return 0;
                }
            };
        }
        return array;
    }

    @Override
    protected ObservableType<MultiplexerCore> createObservable(MultiplexerCore serializerCore) {
        return new ObservableMultiplexerCore(serializerCore);
    }
}
