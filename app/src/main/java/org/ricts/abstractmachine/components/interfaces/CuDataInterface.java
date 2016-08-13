package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 13/08/2016.
 */
public interface CuDataInterface extends ControlUnitInterface {
    boolean isInHaltState();
    boolean isInSleepState();

    String getPCDataString();
    String getIRDataString();
    String getCurrentStateString();
}
