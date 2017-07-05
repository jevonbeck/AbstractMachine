package org.ricts.abstractmachine.components.compute.interrupt;

import org.ricts.abstractmachine.components.devicetype.Device;

/**
 * Created by Jevon on 04/06/2017.
 */

public abstract class InterruptSource extends Device {
    protected abstract void performMainOperation(InterruptTarget target);

    protected String sourceName;

    private InterruptTarget target;
    private boolean interruptTriggered;

    public InterruptSource(String name, InterruptTarget t) {
        sourceName = name;
        target = t;
        interruptTriggered = false;
    }

    public void updateTarget(){
        if(target.isEnabled(sourceName)) {
            performMainOperation(target);

            if(interruptTriggered) {
                target.raiseInterrupt(sourceName);
                interruptTriggered = false;
            }
        }
    }

    protected void setInterruptTriggered(){
        interruptTriggered = true;
    }
}
