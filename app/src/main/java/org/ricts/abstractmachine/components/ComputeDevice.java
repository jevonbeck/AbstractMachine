package org.ricts.abstractmachine.components;

public interface ComputeDevice extends DataDevice{
    int dAddrWidth();
    int instrWidth();
    int iAddrWidth();
}
