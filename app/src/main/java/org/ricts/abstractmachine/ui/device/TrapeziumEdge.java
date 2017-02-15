package org.ricts.abstractmachine.ui.device;

/**
 * Created by Jevon on 08/01/2017.
 */

public enum TrapeziumEdge {
    FIRST_EDGE, LAST_EDGE, NONE;

    public static TrapeziumEdge getEdgeFromInt(int value) {
        // This mapping should be consistent with the
        // MultiplexerView_selectPosition attribute
        // defined in attrs.xml
        switch (value) {
            case 0:
                return TrapeziumEdge.FIRST_EDGE;
            case 1:
                return TrapeziumEdge.LAST_EDGE;
            case 2:
            default:
                return TrapeziumEdge.NONE;
        }
    }
}
