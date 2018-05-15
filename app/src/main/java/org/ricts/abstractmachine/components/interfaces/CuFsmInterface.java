package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 27/03/2017.
 */

public interface CuFsmInterface extends FsmInterface {
    boolean isInHaltState();
    boolean isInSleepState();
    int nextActionDuration();
    int parallelStageCount();
}
