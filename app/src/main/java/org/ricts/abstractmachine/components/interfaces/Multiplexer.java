package org.ricts.abstractmachine.components.interfaces;

/**
 * Created by Jevon on 29/01/2017.
 */

public interface Multiplexer {
    int getSelection();
    void setSelection(int sel);
    String getSelectionText();
}
