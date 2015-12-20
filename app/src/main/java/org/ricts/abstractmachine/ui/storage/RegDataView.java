package org.ricts.abstractmachine.ui.storage;


import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import org.ricts.abstractmachine.components.devices.Device;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;
import org.ricts.abstractmachine.components.storage.Register;

public class RegDataView extends TextView implements RegisterPort{
	private Register dataReg;
	private boolean updateImmediately;

    public RegDataView(Context context) {
        this(context, null);
    }

    public RegDataView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

	public RegDataView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

        setTypeface(Typeface.MONOSPACE);
        setText(Device.formatNumberInHex(0, 1));

        updateImmediately = true;
	}
	
	public void setDataWidth(int dataWidth){
        setRegister(new Register(dataWidth));
	}

    public void setRegister(Register r){
        dataReg = r;
        updateDisplayText();
    }
	
	@Override
	public int read() {
		return dataReg.read();
	}

	@Override
	public void write(int data) {
		dataReg.write(data);
		
		if(updateImmediately){
            updateDisplayText();
		}
	}

    public void setUpdateImmediately(boolean immediately){
		updateImmediately = immediately;
	}

    public void updateDisplayText(){
        setText(Device.formatNumberInHex(dataReg.read(), dataReg.dataWidth()));
    }
}
