package org.ricts.abstractmachine.components.observable;

import java.util.Observable;

/**
 * Created by Jevon on 22/01/2016.
 */
public class ObservableType<T> extends Observable {
    protected T observable_data;

    public static class Params {
        protected Object [] params;

        public Params(Object... o){
            params= o;
        }
    }

    public ObservableType(T type){
        observable_data = type;
    }

    public T getType(){
        return observable_data;
    }
}
