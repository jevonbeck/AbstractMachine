package org.ricts.abstractmachine.components.storage;

import org.ricts.abstractmachine.components.interfaces.MemoryPort;

public class RAM extends ROM implements MemoryPort{

    public RAM(int dWidth, int aWidth, int access){
        super(dWidth, aWidth, access);
    }

    public void write(int address, int data){
        dataArray[address & addressBitMask] = data & dataBitMask;
    }

    public String dataString(int data){
        return formatNumberInHex(data, dataWidth);
    }
}
