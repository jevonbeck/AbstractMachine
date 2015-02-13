package org.ricts.abstractmachine.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Jevon on 01/11/14.
 */
public class CustomDimenRecyclerView extends RecyclerView {
    public CustomDimenRecyclerView(Context context) {
        super(context);
        init();
    }

    public CustomDimenRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
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
        int newWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(findWidth(widthMeasureSpec),
                View.MeasureSpec.getMode(widthMeasureSpec));

        int newHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(findHeight(heightMeasureSpec),
                View.MeasureSpec.getMode(heightMeasureSpec));

        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
    }
}
