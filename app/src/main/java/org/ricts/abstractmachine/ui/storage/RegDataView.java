package org.ricts.abstractmachine.ui.storage;


import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import org.ricts.abstractmachine.components.Device;
import org.ricts.abstractmachine.components.interfaces.RegisterPort;
import org.ricts.abstractmachine.components.storage.Register;
import org.ricts.abstractmachine.ui.device.ReadPortView;

public class RegDataView extends TextView implements RegisterPort, ReadPortView.ReadResponder {
	private Register dataReg;
	private boolean isDelayed;

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

        isDelayed = false;
	}
	
	public void setDataWidth(int dataWidth){
		dataReg = new Register(dataWidth);
        updateTextView();
	}

    public void setRegister(Register r){
        dataReg = r;
        updateTextView();
    }
	
	@Override
	public int read() {
		return dataReg.read();
	}

	@Override
	public void write(int data) {
		dataReg.write(data);
		
		if(!isDelayed){
            updateTextView();
		}
	}

	@Override
	public void onReadFinished() {
		updateTextView();
	}

    @Override
    public void onReadStart() {

    }

    public void setDelayEnable(boolean enable){
		isDelayed = enable;
	}

    private void updateTextView(){
        setText(Device.formatNumberInHex(dataReg.read(), dataReg.dataWidth()));
    }


}
