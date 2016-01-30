package org.ricts.abstractmachine.components.devicetype;

public interface ComputeDevice extends DataDevice{
    int dAddrWidth();
    int instrWidth();
    int iAddrWidth();
}
