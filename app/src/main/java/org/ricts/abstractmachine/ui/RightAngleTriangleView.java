package org.ricts.abstractmachine.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import org.ricts.abstractmachine.R;

/**
 * Created by Jevon on 09/05/2015.
 */
public class RightAngleTriangleView extends View {
    private int fillPosition, diagonal;
    private Paint trianglePaint;

    private Path trianglePath;
    private PointF start, middle, end;

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
        int fillColour = a.getColor(R.styleable.RightAngleTriangleView_fillColour,
                context.getResources().getColor(android.R.color.darker_gray));
        fillPosition = a.getInt(R.styleable.RightAngleTriangleView_fillPosition,
                context.getResources().getInteger(
                        R.integer.RightAngleTriangleView_fillPosition_left));
        diagonal = a.getInt(R.styleable.RightAngleTriangleView_diagonal,
                context.getResources().getInteger(
                        R.integer.RightAngleTriangleView_diagonal_topLeftToBottomRight));

        a.recycle();

        /*** setup Paint and related variables ***/
        trianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trianglePaint.setAntiAlias(true);
        trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        trianglePaint.setColor(fillColour);

        trianglePath = new Path();
        trianglePath.setFillType(Path.FillType.EVEN_ODD);

        start = new PointF();
        middle = new PointF();
        end = new PointF();
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(diagonal == getContext().getResources().getInteger(
                R.integer.RightAngleTriangleView_diagonal_topRightToBottomLeft)){
            // draw diagonal
            start.set(getWidth(), 0);
            middle.set(0, getHeight());

            // determine triangle end based on fill position
            if(fillPosition == getContext().getResources().getInteger(
                    R.integer.RightAngleTriangleView_fillPosition_right)){
                end.set(getWidth(), getHeight());
            }
            else{
                end.set(0, 0);
            }
        }
        else{
            // draw diagonal
            start.set(0, 0);
            middle.set(getWidth(), getHeight());

            // determine triangle end based on fill position
            if(fillPosition == getContext().getResources().getInteger(
                    R.integer.RightAngleTriangleView_fillPosition_right)){
                end.set(getWidth(), 0);
            }
            else{
                end.set(0, getHeight());
            }
        }

        trianglePath.reset(); // remove any previously drawn paths
        trianglePath.moveTo(start.x, start.y);
        trianglePath.lineTo(middle.x, middle.y);
        trianglePath.lineTo(end.x, end.y);
        trianglePath.close(); // automatically draw third side

        canvas.drawPath(trianglePath, trianglePaint);
    }
}
