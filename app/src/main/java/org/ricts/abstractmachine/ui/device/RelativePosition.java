package org.ricts.abstractmachine.ui.device;

/**
 * Created by Jevon on 08/01/2017.
 */

public enum RelativePosition {
    LEFT, RIGHT, TOP, BOTTOM;

    public static RelativePosition getPositionFromInt(int value) {
        // These mappings should be consistent with the
        // DeviceView_pinPosition and ManyToOnePortView_outPosition attributes
        // defined in attrs.xml
        RelativePosition position;
        switch(value) {
            case 0:
                position = LEFT;
                break;
            case 1:
                position = RIGHT;
                break;
            case 2:
                position = TOP;
                break;
            case 3:
                position = BOTTOM;
                break;
            default:
                position = null;
        }
        return position;
    }

    public static RelativePosition getOppositePosition(RelativePosition position) {
        switch (position) {
            case TOP:
                return BOTTOM;
            case BOTTOM:
                return TOP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
        }
        return null;
    }
}
