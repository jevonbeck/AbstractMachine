package org.ricts.abstractmachine.components.storage;

import org.ricts.abstractmachine.components.devicetype.DataDevice;
import org.ricts.abstractmachine.components.devicetype.Device;

public class Register extends Device implements DataDevice {
    private int dataWord;
    private int dataBitMask;
    private int dataWidth;

    public Register(int dWidth){
        super();
        dataWidth = dWidth;
        dataBitMask = bitMaskOfWidth(dWidth);
    }

    public int read(){
        return dataWord;
    }

    public void write(int data){
        dataWord = data & dataBitMask;
    }

    public int dataWidth(){
        return dataWidth;
    }

    public String dataString(){
        return formatNumberInHex(dataWord, dataWidth);
    }
}