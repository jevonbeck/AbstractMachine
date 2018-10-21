package org.ricts.abstractmachine.ui.device;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

public class HorizontalPinDataView extends PinDataView {

    public HorizontalPinDataView(Context context) {
        super(context);
    }

    public HorizontalPinDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalPinDataView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(){
        setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    protected int findWidth(int widthMeasureSpec){
        return getPinHeightDimension(widthMeasureSpec);
    }

    @Override
    protected int findHeight(int heightMeasureSpec){
        return getPinLengthDimension(heightMeasureSpec);
    }
}
