package org.ricts.abstractmachine.ui.storage;


import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import org.ricts.abstractmachine.components.devicetype.Device;
import org.ricts.abstractmachine.components.storage.Register;
import org.ricts.abstractmachine.components.observables.ObservableRegister;
import org.ricts.abstractmachine.ui.utils.DelayedUpdateTextView;

import java.util.Observable;

public class RegDataView extends DelayedUpdateTextView {
	public RegDataView(Context context) {
        this(context, null);
    }

    public RegDataView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

	public RegDataView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

        mainTextView.setTypeface(Typeface.MONOSPACE);
        mainTextView.setText(Device.formatNumberInHex(0, 1));
        mainTextView.setTextColor(context.getResources().getColor(android.R.color.white));
	}

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof ObservableRegister) {
            Register dataReg = ((ObservableRegister) observable).getType();

            setUpdateText(Device.formatNumberInHex(dataReg.read(), dataReg.dataWidth()));
            attemptImmediateTextUpdate();
        }
    }
}
