package app.simple.positional.decorations.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * Class PhysicalRotationImageView extends Android ImageView to perform cool, real-life animation of objects
 * such compass needle in magnetic field. Rotation is performed relative to the center of image.
 * <p>
 * It uses angular motion equation of magnetic dipole in magnetic field to implement such animation.
 * To vary behaviour (damping, oscillation, responsiveness and so on) set various physical properties.
 * <p>
 * Use `setPhysical()` to vary physical properties.
 * Use `rotationUpdate()` to change angle of "magnetic field" at which image should rotate.
 */
public class PhysicalRotationImageView extends androidx.appcompat.widget.AppCompatImageView {

    static final public float TIME_DELTA_THRESHOLD = 0.25f; // maximum time difference between iterations, s
    static final public float ANGLE_DELTA_THRESHOLD = 0.1f; // minimum rotation change to be redrawn, deg

    // default physical properties
    static final public float INERTIA_MOMENT_DEFAULT = 0.5f;
    static final public float ALPHA_DEFAULT = 10f;
    static final public float MB_DEFAULT = 5000f;

    // timestamps of previous iterations--used in numerical integration
    long time1;
    long time2;

    // angles of previous iterations
    double angle1;
    double angle2;
    double angle0;


    // last drawn angular position
    double angleLastDrawn;

    // if animation should be performed
    boolean animationOn = false;

    float inertiaMoment = INERTIA_MOMENT_DEFAULT;   // moment of inertia
    float alpha = ALPHA_DEFAULT;    // damping coefficient
    float mB = MB_DEFAULT;  // magnetic field coefficient

    /**
     * Constructor inherited from ImageView
     *
     * @param context context
     */
    public PhysicalRotationImageView(Context context) {
        super(context);
    }

    /**
     * Constructor inherited from ImageView
     *
     * @param context context
     * @param attrs   attributes
     */
    public PhysicalRotationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor inherited from ImageView
     *
     * @param context  context
     * @param attrs    attributes
     * @param defStyle defaultStyle
     */
    public PhysicalRotationImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * onDraw override.
     * If animation is "on", view is invalidated after each redraw,
     * to perform recalculation on every loop of UI redraw
     */
    @Override
    public void onDraw(Canvas canvas) {
        if (animationOn) {
            if (angleRecalculate(System.currentTimeMillis())) {
                this.setRotation(angle1 > 0 ? (float) (angle1 % 360) : (float) (360 + angle1 % 360));
            }
        } else {
            this.setRotation(angle1 > 0 ? (float) (angle1 % 360) : (float) (360 + angle1 % 360));
        }

        super.onDraw(canvas);

        if (animationOn) {
            this.invalidate();
        }
    }

    /**
     * Use this to set physical properties.
     * Negative values will be replaced by default values
     *
     * @param inertiaMoment Moment of inertia (default 0.1)
     * @param alpha         Damping coefficient (default 10)
     * @param mB            Magnetic field coefficient (default 1000)
     */
    public void setPhysical(float inertiaMoment, float alpha, float mB) {
        this.inertiaMoment = inertiaMoment >= 0 ? inertiaMoment : INERTIA_MOMENT_DEFAULT;
        this.alpha = alpha >= 0 ? alpha : ALPHA_DEFAULT;
        this.mB = mB >= 0 ? mB : MB_DEFAULT;
    }


    /**
     * Use this to set new "magnetic field" angle at which image should rotate
     *
     * @param angleNew new magnetic field angle, deg., relative to vertical axis.
     * @param animate  true, if image should rotate using animation, false to set new rotation instantly
     */
    public void rotationUpdate(final float angleNew, final boolean animate) {
        if (animate) {
            if (Math.abs(angle0 - angleNew) > ANGLE_DELTA_THRESHOLD) {
                angle0 = angleNew;
                this.invalidate();
            }
            animationOn = true;
        } else {
            angle1 = angleNew;
            angle2 = angleNew;
            angle0 = angleNew;
            angleLastDrawn = angleNew;
            this.invalidate();
            animationOn = false;
        }
    }

    /**
     * Recalculate angles using equation of dipole circular motion
     *
     * @param timeNew timestamp of method invoke
     * @return if there is a need to redraw rotation
     */
    protected boolean angleRecalculate(final long timeNew) {
        // recalculate angle using simple numerical integration of motion equation
        float deltaT1 = (timeNew - time1) / 1000f;
        if (deltaT1 > TIME_DELTA_THRESHOLD) {
            deltaT1 = TIME_DELTA_THRESHOLD;
            time1 = timeNew + Math.round(TIME_DELTA_THRESHOLD * 1000);
        }

        float deltaT2 = (time1 - time2) / 1000f;
        if (deltaT2 > TIME_DELTA_THRESHOLD) {
            deltaT2 = TIME_DELTA_THRESHOLD;
        }

        // circular acceleration coefficient
        double coefI = inertiaMoment / deltaT1 / deltaT2;

        // circular velocity coefficient
        double coefAlpha = alpha / deltaT1;

        // angular momentum coefficient
        double coefk = mB * (Math.sin(Math.toRadians(angle0)) * Math.cos(Math.toRadians(angle1)) -
                (Math.sin(Math.toRadians(angle1)) * Math.cos(Math.toRadians(angle0))));

        double angleNew = (coefI * (angle1 * 2f - angle2) + coefAlpha * angle1 + coefk) / (coefI + coefAlpha);

        // reassign previous iteration variables
        angle2 = angle1;
        angle1 = angleNew;
        time2 = time1;
        time1 = timeNew;

        // if angles changed less then threshold, return false - no need to redraw the view
        if (Math.abs(angleLastDrawn - angle1) < ANGLE_DELTA_THRESHOLD) {
            return false;
        } else {
            angleLastDrawn = angle1;
            return true;
        }
    }

    /**
     * Set animationOn to false as well
     * to free up view from animating
     * if the animation cancellation is
     * required
     */
    @Override
    public void clearAnimation() {
        time1 = System.currentTimeMillis();
        rotationUpdate(getRotation(), false);
        super.clearAnimation();
    }
}
