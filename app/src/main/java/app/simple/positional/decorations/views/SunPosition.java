package app.simple.positional.decorations.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import app.simple.positional.R;

public class SunPosition extends View {

    private final Paint circlePaint = new Paint();
    private final Paint pointPaint = new Paint();
    private final Paint dashedLinePaint = new Paint();
    private final Paint textPaint = new Paint();
    private final RectF rectF = new RectF();
    private final Rect sunRect = new Rect();

    private Drawable sunDrawable;

    private float x;
    private float y;
    private float sunX;
    private float sunY;
    private double azimuth = 0.0;

    private int radius = 50;
    private int width = 100;
    private int height = 100;
    private int sunSize = 42;

    private final int lineColor = Color.parseColor("#2c3e50");

    public SunPosition(Context context) {
        super(context);
        init();
    }

    public SunPosition(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SunPosition(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SunPosition(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(lineColor);
        circlePaint.setAlpha(100);
        circlePaint.setStrokeWidth(3);
        circlePaint.setStrokeCap(Paint.Cap.ROUND);

        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(lineColor);
        pointPaint.setAlpha(100);
        pointPaint.setStrokeWidth(35);
        pointPaint.setStrokeCap(Paint.Cap.ROUND);
        pointPaint.setStrokeJoin(Paint.Join.ROUND);

        dashedLinePaint.setAntiAlias(true);
        dashedLinePaint.setStyle(Paint.Style.STROKE);
        dashedLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.dividerColor));
        dashedLinePaint.setAlpha(100);
        dashedLinePaint.setStrokeWidth(2);
        dashedLinePaint.setStrokeCap(Paint.Cap.ROUND);
        dashedLinePaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.textPrimary));
        textPaint.setTextSize(32);
        textPaint.setAlpha(100);
        textPaint.setStrokeWidth(35);
        textPaint.setStrokeCap(Paint.Cap.ROUND);

        post(() -> {
            x = getWidth() / 2f;
            y = getHeight() / 2f;
            invalidate();
        });

        sunDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_sunrise);
        // calculateCoordinatesFromAzimuth(82, 2.6);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the circle
        x = getWidth() / 2f;
        y = getHeight() / 2f;
        radius = Math.min(getWidth(), getHeight()) / 2;

        rectF.set(x - radius, y - radius, x + radius, y + radius);
        canvas.drawArc(rectF, 0, 360, false, circlePaint);

        // draw a dashed line in the middle horizontally
        canvas.drawLine(0, y, getWidth(), y, dashedLinePaint);

        // Write directions
        canvas.drawText(getContext().getString(R.string.north_N), x - 10, y - radius + 40, textPaint);
        canvas.drawText(getContext().getString(R.string.south_S), x - 10, y + radius - 30, textPaint);
        canvas.drawText(getContext().getString(R.string.east_E), x + radius - 40, y + 10, textPaint);
        canvas.drawText(getContext().getString(R.string.west_W), x - radius + 20, y + 10, textPaint);

        // Position the sun on the arc
        Log.d("SunPosition", "onDraw: " + sunX + " " + sunY);
        canvas.rotate(Math.abs((float) (azimuth)), getWidth() / 2F, getHeight() / 2F);
        canvas.translate(radius * -1, 0);

        // Rotate the drawable
        Log.d("SunPosition", "onDraw: " + (x) + " " + (y));

        // Position the sun on the arc
        sunRect.set((int) (x - sunSize), (int) (y - sunSize), (int) (x + sunSize), (int) (y + sunSize));
        sunDrawable.setBounds(sunRect);
        sunDrawable.draw(canvas);

        super.onDraw(canvas);
    }

    public void setAzimuth(double azimuth) {
        // Compensate for the rotation of the canvas
        this.azimuth = azimuth + 90;
        invalidate();
    }

    public void setSunDrawable(Drawable sunDrawable) {
        this.sunDrawable = sunDrawable;
        invalidate();
    }
}
