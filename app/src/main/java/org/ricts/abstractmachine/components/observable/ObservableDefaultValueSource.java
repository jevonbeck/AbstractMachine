package org.ricts.abstractmachine.components.observable;

import org.ricts.abstractmachine.components.compute.cu.DefaultValueSource;

/**
 * Created by Jevon on 08/04/2017.
 */

public class ObservableDefaultValueSource extends ObservableType<DefaultValueSource> implements DefaultValueSource {
    public ObservableDefaultValueSource(DefaultValueSource type) {
        super(type);
    }

    @Override
    public int defaultValue() {
        setChanged();
        notifyObservers();
        return observable_data.defaultValue();
    }
}
