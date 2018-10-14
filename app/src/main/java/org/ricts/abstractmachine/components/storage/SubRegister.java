package org.ricts.abstractmachine.components.storage;

import org.ricts.abstractmachine.components.interfaces.Bit;
import org.ricts.abstractmachine.components.interfaces.Register;

public class SubRegister extends AbstractRegister {
    private Register dataReg;
    private int regOffset;

    public SubRegister(Register register, int dWidth, int offset) {
        super(dWidth);
        dataReg = register;
        regOffset = offset;
    }

    @Override
    public int read() {
        return getWordFrom(dataReg.read(), dataWidth, regOffset);
    }

    @Override
    public void write(int data) {
        dataReg.write(setWordIn(dataReg.read(), data, dataWidth, regOffset));
    }

    @Override
    protected Bit getBitFromDataSource(int index) {
        return dataReg.getBitAt(index + regOffset);
    }

    @Override
    protected Register createSubRegister(Register register, int width, int offset) {
        return new SubRegister(register, width, offset);
    }
}
