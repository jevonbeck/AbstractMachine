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
    private int fillPosition, diagonal, pinOrientation;
    private Paint trianglePaint, pinPaint;

    private Path trianglePath, pinPath;
    private PointF triStart, triMiddle, triEnd;

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
        pinOrientation = a.getInt(R.styleable.RightAngleTriangleView_pinOrientation,
                context.getResources().getInteger(
                        R.integer.RightAngleTriangleView_pinOrientation_none));
        a.recycle();

        /*** setup drawing related variables ***/
        trianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        trianglePaint.setColor(fillColour);

        trianglePath = new Path();
        trianglePath.setFillType(Path.FillType.EVEN_ODD);

        triStart = new PointF();
        triMiddle = new PointF();
        triEnd = new PointF();

        pinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pinPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pinPaint.setColor(context.getResources().getColor(android.R.color.darker_gray));

        pinPath = new Path();
        pinPath.setFillType(Path.FillType.EVEN_ODD);
    }

    @Override
    protected void onDraw(Canvas canvas){
        float pinLengthDiff = 0, pinWidth = 10;

        // setup for pin drawing if applicable
        if(pinOrientation != getContext().getResources().getInteger(
                R.integer.RightAngleTriangleView_pinOrientation_none)){

            if(pinOrientation == getContext().getResources().getInteger(
                    R.integer.RightAngleTriangleView_pinOrientation_horizontal)){
                pinLengthDiff = (pinWidth * getWidth()) / getHeight();
            }
            else{
                pinLengthDiff = (pinWidth * getHeight()) / getWidth();
            }

            pinPath.reset(); // remove any previously drawn paths
            pinPath.moveTo(getWidth() / 2, getHeight() / 2);
        }


        if(diagonal == getContext().getResources().getInteger(
                R.integer.RightAngleTriangleView_diagonal_topRightToBottomLeft)){
            // draw diagonal
            triStart.set(getWidth(), 0);
            triMiddle.set(0, getHeight());

            // determine triEnd based on fill position
            // draw pin if shown
            if(fillPosition == getContext().getResources().getInteger(
                    R.integer.RightAngleTriangleView_fillPosition_right)){
                triEnd.set(getWidth(), getHeight());

                if(pinLengthDiff != 0){
                    if(pinOrientation == getContext().getResources().getInteger(
                            R.integer.RightAngleTriangleView_pinOrientation_horizontal)){
                        drawHorizontalPinLeft_DiagonalTR2BL(pinLengthDiff, pinWidth);
                    }
                    else{
                        drawVerticalPinUp_DiagonalTR2BL(pinLengthDiff, pinWidth);
                    }
                    pinPath.close();
                    canvas.drawPath(pinPath, pinPaint);
                }
            }
            else{
                triEnd.set(0, 0);

                if(pinLengthDiff != 0){
                    if(pinOrientation == getContext().getResources().getInteger(
                            R.integer.RightAngleTriangleView_pinOrientation_horizontal)){
                        drawHorizontalPinRight_DiagonalTR2BL(pinLengthDiff, pinWidth);
                    }
                    else{
                        drawVerticalPinDown_DiagonalTR2BL(pinLengthDiff, pinWidth);
                    }
                    pinPath.close();
                    canvas.drawPath(pinPath, pinPaint);
                }
            }
        }
        else{
            // draw diagonal
            triStart.set(0, 0);
            triMiddle.set(getWidth(), getHeight());

            // determine triEnd based on fill position
            // draw pin if shown
            if(fillPosition == getContext().getResources().getInteger(
                    R.integer.RightAngleTriangleView_fillPosition_right)){
                triEnd.set(getWidth(), 0);

                if(pinLengthDiff != 0){
                    if(pinOrientation == getContext().getResources().getInteger(
                            R.integer.RightAngleTriangleView_pinOrientation_horizontal)){
                        drawHorizontalPinLeft_DiagonalTL2BR(pinLengthDiff, pinWidth);
                    }
                    else{
                        drawVerticalPinDown_DiagonalTL2BR(pinLengthDiff, pinWidth);
                    }
                    pinPath.close();
                    canvas.drawPath(pinPath, pinPaint);
                }
            }
            else{
                triEnd.set(0, getHeight());

                if(pinLengthDiff != 0){
                    if(pinOrientation == getContext().getResources().getInteger(
                            R.integer.RightAngleTriangleView_pinOrientation_horizontal)){
                        drawHorizontalPinRight_DiagonalTL2BR(pinLengthDiff, pinWidth);
                    }
                    else{
                        drawVerticalPinUp_DiagonalTL2BR(pinLengthDiff, pinWidth);
                    }
                    pinPath.close();
                    canvas.drawPath(pinPath, pinPaint);
                }
            }
        }

        trianglePath.reset(); // remove any previously drawn paths
        trianglePath.moveTo(triStart.x, triStart.y);
        trianglePath.lineTo(triMiddle.x, triMiddle.y);
        trianglePath.lineTo(triEnd.x, triEnd.y);
        trianglePath.close(); // automatically draw third side

        canvas.drawPath(trianglePath, trianglePaint);
    }

    private void drawHorizontalPinLeft_DiagonalTL2BR(float pinLengthDiff, float pinWidth){
        pinPath.rMoveTo(-pinLengthDiff / 2, -pinWidth / 2);
        pinPath.rLineTo((pinLengthDiff - getWidth()) / 2, 0);
        pinPath.rLineTo(0, pinWidth);
        pinPath.rLineTo((getWidth() + pinLengthDiff) / 2, 0);
    }

    private void drawHorizontalPinLeft_DiagonalTR2BL(float pinLengthDiff, float pinWidth){
        pinPath.rMoveTo(pinLengthDiff / 2, -pinWidth / 2);
        pinPath.rLineTo(-(getWidth() + pinLengthDiff) / 2, 0);
        pinPath.rLineTo(0, pinWidth);
        pinPath.rLineTo((getWidth() - pinLengthDiff) / 2, 0);
    }

    private void drawHorizontalPinRight_DiagonalTL2BR(float pinLengthDiff, float pinWidth){
        pinPath.rMoveTo(-pinLengthDiff / 2, -pinWidth / 2);
        pinPath.rLineTo((getWidth() + pinLengthDiff) / 2, 0);
        pinPath.rLineTo(0, pinWidth);
        pinPath.rLineTo((pinLengthDiff - getWidth()) / 2, 0);
    }

    private void drawHorizontalPinRight_DiagonalTR2BL(float pinLengthDiff, float pinWidth){
        pinPath.rMoveTo(pinLengthDiff / 2, -pinWidth / 2);
        pinPath.rLineTo((getWidth() - pinLengthDiff) / 2, 0);
        pinPath.rLineTo(0, pinWidth);
        pinPath.rLineTo(-(getWidth() + pinLengthDiff) / 2, 0);
    }

    private void drawVerticalPinUp_DiagonalTL2BR(float pinLengthDiff, float pinWidth){
        pinPath.rMoveTo(-pinWidth / 2, -pinLengthDiff / 2);
        pinPath.rLineTo(0, (pinLengthDiff - getHeight()) / 2);
        pinPath.rLineTo(pinWidth, 0);
        pinPath.rLineTo(0, (getHeight() + pinLengthDiff) / 2);
    }

    private void drawVerticalPinUp_DiagonalTR2BL(float pinLengthDiff, float pinWidth){
        pinPath.rMoveTo(pinWidth / 2, -pinLengthDiff / 2);
        pinPath.rLineTo(0, (pinLengthDiff - getHeight()) / 2);
        pinPath.rLineTo(-pinWidth, 0);
        pinPath.rLineTo(0, (getHeight() + pinLengthDiff) / 2);
    }

    private void drawVerticalPinDown_DiagonalTL2BR(float pinLengthDiff, float pinWidth){
        pinPath.rMoveTo(-pinWidth / 2, -pinLengthDiff / 2);
        pinPath.rLineTo(0, (pinLengthDiff + getHeight()) / 2);
        pinPath.rLineTo(pinWidth, 0);
        pinPath.rLineTo(0, (pinLengthDiff - getHeight()) / 2);
    }

    private void drawVerticalPinDown_DiagonalTR2BL(float pinLengthDiff, float pinWidth){
        pinPath.rMoveTo(pinWidth / 2, -pinLengthDiff / 2);
        pinPath.rLineTo(0, (pinLengthDiff + getHeight()) / 2);
        pinPath.rLineTo(-pinWidth, 0);
        pinPath.rLineTo(0, (pinLengthDiff - getHeight()) / 2);
    }
}
