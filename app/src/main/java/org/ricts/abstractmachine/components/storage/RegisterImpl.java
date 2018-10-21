package org.ricts.abstractmachine.components.storage;

import org.ricts.abstractmachine.components.interfaces.Bit;
import org.ricts.abstractmachine.components.interfaces.Register;

public class RegisterImpl extends AbstractRegister {
    private int dataWord;
    private int dataBitMask;
    private RegisterBit [] bitArray;

    public RegisterImpl(int dWidth){
        super(dWidth);
        dataBitMask = bitMaskOfWidth(dWidth);

        bitArray = new RegisterBit[dWidth];
        for(int x=0; x < dWidth; ++x) {
            bitArray[x] = new RegisterBit(this, x);
        }
    }

    @Override
    public int read(){
        return dataWord;
    }

    @Override
    public void write(int data){
        dataWord = data & dataBitMask;
    }

    @Override
    protected Bit getBitFromDataSource(int index) {
        return bitArray[index];
    }

    @Override
    protected Register createSubRegister(Register register, int width, int offset) {
        return new SubRegister(register, width, offset);
    }
}