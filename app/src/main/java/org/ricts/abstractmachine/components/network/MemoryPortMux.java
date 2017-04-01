package org.ricts.abstractmachine.components.network;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;
import org.ricts.abstractmachine.components.interfaces.MultiMemoryPort;
import org.ricts.abstractmachine.components.interfaces.Multiplexer;

/**
 * Created by Jevon on 14/01/2017.
 */

public class MemoryPortMux extends MemoryPortSerializer implements Multiplexer {
    private int currentSel;

    public MemoryPortMux(MemoryPort port, int portCount) {
        super(port, portCount);
        setSelection(0);
    }

    @Override
    public int getSelection() {
        return currentSel;
    }

    @Override
    public void setSelection(int sel){
        currentSel = sel & portIndexMask;
    }

    @Override
    public String getSelectionText(){
        return formatNumberInHex(currentSel, portIndexMask);
    }

    @Override
    protected MultiMemoryPort createMultiPortInterface(MemoryPort targetPort) {
        return new SimpleMultiMemoryPort(targetPort){
            @Override
            public int read(int portId, int address) {
                return ((portId & portIndexMask) == currentSel) ?
                        targetMemoryPort.read(address) : -1;
            }

            @Override
            public void write(int portId, int address, int data) {
                if((portId & portIndexMask) == currentSel) {
                    targetMemoryPort.write(address, data);
                }
            }
        };
    }
}
