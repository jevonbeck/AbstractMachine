package org.ricts.abstractmachine.components.devices;

public interface ComputeDevice extends DataDevice{
    int dAddrWidth();
    int instrWidth();
    int iAddrWidth();
}
