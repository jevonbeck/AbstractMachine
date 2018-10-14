package org.ricts.abstractmachine.components.storage;

import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.interfaces.Bit;
import org.ricts.abstractmachine.components.interfaces.Register;

public abstract class AbstractRegister extends Device implements Register {
    protected int dataWidth;

    protected abstract Bit getBitFromDataSource(int index);
    protected abstract Register createSubRegister(Register register, int width, int offset);

    public AbstractRegister(int dWidth) {
        dataWidth = dWidth;
    }

    @Override
    public Bit getBitAt(int index) {
        return (0 <= index && index < dataWidth) ? getBitFromDataSource(index) : null;
    }

    @Override
    public Register getSubRegister(int width, int offset) {
        return  (offset >= 0 && width > 0 && width + offset <= dataWidth) ?
                (offset == 0 && width == dataWidth) ? this : createSubRegister(this, width, offset)
                : null;
    }

    @Override
    public String dataString() {
        return formatNumberInHex(read(), dataWidth);
    }

    @Override
    public int dataWidth() {
        return dataWidth;
    }
}
