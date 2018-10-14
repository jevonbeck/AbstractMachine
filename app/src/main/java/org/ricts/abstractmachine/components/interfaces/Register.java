package org.ricts.abstractmachine.components.interfaces;

import org.ricts.abstractmachine.components.devicetype.DataDevice;

public interface Register extends DataDevice {
    int read();
    void write(int data);

    Bit getBitAt(int index);
    Register getSubRegister(int width, int offset);

    String dataString();
}
