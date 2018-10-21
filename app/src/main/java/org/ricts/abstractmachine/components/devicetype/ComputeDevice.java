package org.ricts.abstractmachine.components.devicetype;

public interface ComputeDevice extends DataDevice, InstructionDevice, InstructionAddressDevice {
    int dAddrWidth();
}
