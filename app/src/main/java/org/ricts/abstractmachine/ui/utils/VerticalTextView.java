package org.ricts.abstractmachine.ui.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import org.ricts.abstractmachine.R;

/**
 * Created by Jevon on 22/12/14.
 */
public class VerticalTextView extends TextView {
    private boolean topDown;

    public VerticalTextView(Context context) {
        this(context, null);
    }

    public VerticalTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public VerticalTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        /*** extract XML attributes ***/
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalTextView);
        topDown = a.getBoolean(R.styleable.VerticalTextView_topDown, false);
        a.recycle();
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}
	
	@Override
    protected void onDraw(Canvas canvas){
		canvas.save();
		
		if(topDown){
			canvas.translate(getWidth(), 0);
			canvas.rotate(90);
		}else {
			canvas.translate(0, getHeight());
			canvas.rotate(-90);
		}
		
		canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());
        Layout layout = getLayout();
        if(layout != null){
            layout.draw(canvas);
        }
        canvas.restore();
	}
}
