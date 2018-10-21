package org.ricts.abstractmachine.components.storage;

import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.interfaces.Bit;

public class RegisterBit extends Device implements Bit {
    private int index;
    private RegisterImpl register;

    public RegisterBit(RegisterImpl reg, int i) {
        register = reg;
        index = i;
    }

    @Override
    public boolean read() {
        return getBitAtIndex(index, register.read());
    }

    @Override
    public void write(boolean value) {
        register.write(setBitValueAtIndex(index, register.read(), value));
    }

    @Override
    public void set() {
        write(true);
    }

    @Override
    public void clear() {
        write(false);
    }
}
