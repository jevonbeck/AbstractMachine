package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

public class VerticalPinDataView extends PinDataView {

	public VerticalPinDataView(Context context) {
		super(context);
	}
	
	public VerticalPinDataView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VerticalPinDataView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    @Override
	protected void init(){
        setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));
	}

    @Override
	protected int findWidth(int widthMeasureSpec){
        return getPinLengthDimension(widthMeasureSpec);
	}

    @Override
    protected int findHeight(int heightMeasureSpec){
        return getPinHeightDimension(heightMeasureSpec);
    }
}
