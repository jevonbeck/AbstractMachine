package org.ricts.abstractmachine.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.ricts.abstractmachine.R;

/**
 * Created by Jevon on 09/05/2015.
 */
public class RightAngleTriangleView extends View {
    private int solidFill, fillColour, diagonal;
    private Paint mPaint;

    public RightAngleTriangleView(Context context) {
        this(context, null);
    }

    public RightAngleTriangleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightAngleTriangleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        /*** extract XML attributes ***/
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RightAngleTriangleView);
        solidFill = a.getInt(R.styleable.RightAngleTriangleView_solidFill,
                R.integer.RightAngleTriangleView_solidFill_left);
        fillColour = a.getColor(R.styleable.RightAngleTriangleView_fillColour,
                android.R.color.darker_gray);
        diagonal = a.getInt(R.styleable.RightAngleTriangleView_diagonal,
                R.integer.RightAngleTriangleView_diagonal_topLeftToBottomRight);

        a.recycle();

        /*** setup Paint ***/
    }

    @Override
    protected void onDraw(Canvas canvas){
        float startX, startY, endX, endY;
        switch (diagonal){
            case R.integer.RightAngleTriangleView_diagonal_topRightToBottomLeft:
                startX = getWidth();
                startY = 0;
                endX = 0;
                endY = getHeight();
                break;
            case R.integer.RightAngleTriangleView_diagonal_topLeftToBottomRight:
            default:
                startX = 0;
                startY = 0;
                endX = getWidth();
                endY = getHeight();
                break;
        }

        canvas.drawLine(startX, startY, endX, endY, mPaint);

    }
}
