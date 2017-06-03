package org.ricts.abstractmachine.components.observable;

import org.ricts.abstractmachine.components.interfaces.Multiplexer;

/**
 * Created by Jevon on 30/01/2017.
 */

public class ObservableMultiplexer extends ObservableType<Multiplexer> implements Multiplexer {
    public ObservableMultiplexer(Multiplexer type) {
        super(type);
    }

    @Override
    public int getSelection() {
        return observable_data.getSelection();
    }

    @Override
    public void setSelection(int sel) {
        observable_data.setSelection(sel);
        setChanged();
        notifyObservers();
    }

    @Override
    public String getSelectionText() {
        return observable_data.getSelectionText();
    }
}
