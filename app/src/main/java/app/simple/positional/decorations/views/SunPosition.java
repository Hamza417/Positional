package app.simple.positional.decorations.views;

import android.content.Context;
import android.graphics.BlurMaskFilter;
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
import app.simple.positional.util.MoonAngle;

@SuppressWarnings("FieldCanBeLocal")
public class SunPosition extends View {

    private final String TAG = "SunPosition";

    private final Paint circlePaint = new Paint();
    private final Paint nightPaint = new Paint();
    private final Paint pointPaint = new Paint();
    private final Paint dashedLinePaint = new Paint();
    private final Paint linePaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint sunElevationPaint = new Paint();
    private final Paint moonElevationPaint = new Paint();
    private final Paint earthElevationPaint = new Paint();

    private final RectF rectF = new RectF();
    private final Rect sunRect = new Rect();
    private final Rect moonRect = new Rect();
    private final Rect earthRect = new Rect();

    private Drawable sunDrawable;
    private Drawable moonDrawable;
    private Drawable earthDrawable;

    private float x;
    private float y;
    private double sunAzimuth = 270.0;
    private double moonAzimuth = 0.0;

    private int radius = 50;
    private int moonRadius = 125;
    private final int sunSize = 42;
    private final int moonSize = 14;
    private final int earthSize = 24;

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
        dashedLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.textPrimary));
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

        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(lineColor);
        linePaint.setAlpha(100);
        linePaint.setStrokeWidth(1);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        sunElevationPaint.setAntiAlias(true);
        sunElevationPaint.setAlpha(50);
        sunElevationPaint.setShadowLayer(50, 0, 0, Color.parseColor("#50f5b041"));
        sunElevationPaint.setMaskFilter(new BlurMaskFilter(150, BlurMaskFilter.Blur.NORMAL));

        moonElevationPaint.setAntiAlias(true);
        moonElevationPaint.setAlpha(50);
        moonElevationPaint.setShadowLayer(50, 0, 0, Color.parseColor("#FFFFFF")); // White color for the moon
        moonElevationPaint.setMaskFilter(new BlurMaskFilter(150, BlurMaskFilter.Blur.NORMAL));

        earthElevationPaint.setAntiAlias(true);
        earthElevationPaint.setAlpha(20);
        earthElevationPaint.setShadowLayer(50, 0, 0, Color.parseColor("#702980b9"));
        earthElevationPaint.setMaskFilter(new BlurMaskFilter(100, BlurMaskFilter.Blur.NORMAL));

        post(() -> {
            x = getWidth() / 2f;
            y = getHeight() / 2f;
            invalidate();
        });

        sunDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_sunrise);
        moonDrawable = ContextCompat.getDrawable(getContext(), R.drawable.moon10);
        earthDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_earth);
        // calculateCoordinatesFromAzimuth(82, 2.6);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the circle
        x = getWidth() / 2f;
        y = getHeight() / 2f;
        radius = Math.min(getWidth(), getHeight()) / 2;

        // Moon radius should be 25% of the sun radius
        moonRadius = (int) (radius * 0.25);

        rectF.set(x - radius, y - radius, x + radius, y + radius);
        canvas.drawArc(rectF, 0, 360, false, dashedLinePaint);

        // draw a dashed line in the middle horizontally
        canvas.drawLine(0, y, getWidth(), y, dashedLinePaint);

        // Write directions
        canvas.drawText(getContext().getString(R.string.north_N), x - 10, y - radius + 40, textPaint);
        canvas.drawText(getContext().getString(R.string.south_S), x - 10, y + radius - 30, textPaint);
        canvas.drawText(getContext().getString(R.string.east_E), x + radius - 40, y + 10, textPaint);
        canvas.drawText(getContext().getString(R.string.west_W), x - radius + 20, y + 10, textPaint);

        // Position the sun on the arc
        canvas.save();
        canvas.rotate(Math.abs((float) (sunAzimuth)), getWidth() / 2F, getHeight() / 2F);
        canvas.translate(radius * -1, 0);

        // Draw the line from the center to the sun
        canvas.drawLine(x, y, x + radius, y, linePaint);

        // Position the sun on the arc
        sunRect.set((int) (x - sunSize), (int) (y - sunSize), (int) (x + sunSize), (int) (y + sunSize));

        // Draw the shadow under the sun
        canvas.drawRect(sunRect, sunElevationPaint);

        // Draw the sun
        sunDrawable.setBounds(sunRect);
        sunDrawable.draw(canvas);
        canvas.restore();

        // Draw the shadow under the earth
        canvas.drawRect(earthRect, earthElevationPaint);

        // Draw arc for the moon
        rectF.set(x - moonRadius, y - moonRadius, x + moonRadius, y + moonRadius);
        canvas.drawArc(rectF, 0, 360, false, dashedLinePaint);

        // Draw the moon
        Log.d(TAG, "onDraw: Moon Azimuth " + moonAzimuth);
        canvas.save();
        canvas.rotate(Math.abs((float) (Math.abs(moonAzimuth))), getWidth() / 2F, getHeight() / 2F);
        canvas.translate(moonRadius * -1, 0);
        canvas.drawLine(x, y, x + moonRadius, y, linePaint);
        moonRect.set((int) (x - moonSize), (int) (y - moonSize), (int) (x + moonSize), (int) (y + moonSize));
        moonDrawable.setBounds(moonRect);
        moonDrawable.draw(canvas);

        // Draw the shadow under the moon
        canvas.drawRect(moonRect, moonElevationPaint);

        canvas.restore();

        // Draw the earth at the center
        canvas.save();
        canvas.translate(0, 0);
        // canvas.rotate(Math.abs((float) (-sunAzimuth)), getWidth() / 2F, getHeight() / 2F);
        earthRect.set((int) (x - earthSize), (int) (y - earthSize), (int) (x + earthSize), (int) (y + earthSize));
        earthDrawable.setBounds(earthRect);
        earthDrawable.draw(canvas);
        canvas.restore();

        super.onDraw(canvas);
    }

    public void setSunAzimuth(double sunAzimuth) {
        // Compensate for the rotation of the canvas
        this.sunAzimuth = sunAzimuth + 90;
    }

    public void setSunDrawable(Drawable sunDrawable) {
        this.sunDrawable = sunDrawable;
    }

    public void setMoonDrawable(double azimuth, double moonPhase) {
        this.moonAzimuth = azimuth + 90;
        this.moonDrawable = ContextCompat.getDrawable(getContext(), MoonAngle.INSTANCE.getMoonPhaseGraphics(moonPhase));
    }
}
