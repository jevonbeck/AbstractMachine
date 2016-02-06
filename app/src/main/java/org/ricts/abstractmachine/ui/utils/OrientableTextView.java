package org.ricts.abstractmachine.ui.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ricts.abstractmachine.R;

/**
 * Created by Jevon on 03/01/2016.
 */
public class OrientableTextView extends RelativeLayout {
    protected TextView mainTextView;

    /** Standard Constructors **/
    public OrientableTextView(Context context) {
        this(context, null);
    }

    public OrientableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OrientableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /*** extract XML attributes ***/
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OrientableTextView);
        int orientation = a.getInt(R.styleable.OrientableTextView_android_orientation, 0);
        boolean isHorizontal = orientation == 0;
        a.recycle();

        /*** create children and determine layouts & positions based on attributes ***/
        if(isHorizontal){
            mainTextView = new TextView(context);
        }
        else {
            mainTextView = new VerticalTextView(context);
        }

        LayoutParams lpMainView = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        addView(mainTextView, lpMainView);
    }
}
