package org.ricts.abstractmachine.components.compute.interrupt;

/**
 * Created by Jevon on 04/06/2017.
 */

public interface InterruptTarget {
    boolean isEnabled(String sourceName);
    void raiseInterrupt(String sourceName);

    int [] getRegData(String sourceName);
    void setRegData(String sourceName, int [] data);
}
