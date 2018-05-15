package org.ricts.abstractmachine.ui.device;

/**
 * Created by Jevon on 20/01/2018.
 */

public enum PinBodyAlignment {
    START, CENTER, END;

    public static PinBodyAlignment getAlignmentFromInt(int value) {
        // These mappings should be consistent with the
        // DeviceView_positionAlignment attribute
        // defined in attrs.xml
        PinBodyAlignment alignment;
        switch(value) {
            case 0:
                alignment = START;
                break;
            case 1:
                alignment = CENTER;
                break;
            case 2:
                alignment = END;
                break;
            default:
                alignment = null;
        }
        return alignment;
    }
}
