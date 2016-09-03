package org.ricts.abstractmachine.ui.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Jevon on 01/11/14.
 */
public class CustomDimenRecyclerView extends RecyclerView {
    public CustomDimenRecyclerView(Context context) {
        this(context, null);
    }

    public CustomDimenRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomDimenRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init(){

    }

    protected int findWidth(int widthMeasureSpec){
        return MeasureSpec.getSize(widthMeasureSpec);
    }

    protected int findHeight(int heightMeasureSpec){
        return MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int finalWidth = getPaddingLeft() + findWidth(widthMeasureSpec) + getPaddingRight();
        int newWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(finalWidth,
                View.MeasureSpec.getMode(widthMeasureSpec));

        int finalHeight = getPaddingTop() + findHeight(heightMeasureSpec) + getPaddingBottom();
        int newHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(finalHeight,
                View.MeasureSpec.getMode(heightMeasureSpec));

        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
    }
}
