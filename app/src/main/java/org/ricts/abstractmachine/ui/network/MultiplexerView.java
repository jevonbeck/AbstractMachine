package org.ricts.abstractmachine.ui.network;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.components.interfaces.Multiplexer;
import org.ricts.abstractmachine.components.observable.ObservableMultiplexer;
import org.ricts.abstractmachine.ui.device.DevicePin;
import org.ricts.abstractmachine.ui.device.TrapeziumView;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jevon on 07/06/2015.
 */
public abstract class MultiplexerView extends ManyToOnePortView implements Observer {
    private TrapeziumView trapeziumView;
    private int currentSelection = 0;

    /** Standard Constructors **/
    public MultiplexerView(Context context) {
        this(context, null);
    }

    public MultiplexerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiplexerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        trapeziumView = (TrapeziumView) mainView;
    }

    @Override
    protected View createMainBodyView(Context context, AttributeSet attrs) {
        final String pinName = context.getResources().getString(R.string.pin_name_select);
        return new TrapeziumView(context, attrs) {
            @Override
            protected void initEdgePin(DevicePin pin) {
                pin.name = pinName;
            }
        };
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservableMultiplexer) {
            Multiplexer mux = ((ObservableMultiplexer) observable).getType();
            currentSelection = mux.getSelection();
            trapeziumView.setEdgePinText(mux.getSelectionText());
        }
    }

    public void setSelectWidth(int selW){
        createInputPins(1 << selW);
    }

    protected int getSelection(){
        return currentSelection;
    }

    protected void animateSelectPin(){
        trapeziumView.animateEdgePin();
    }

}
