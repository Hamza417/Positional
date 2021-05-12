package app.simple.positional.decorations.ripple;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.google.android.material.animation.ArgbEvaluatorCompat;

import java.util.Arrays;

import androidx.core.content.ContextCompat;
import app.simple.positional.R;
import app.simple.positional.preference.MainPreferences;

public class Utils {
    
    static final int alpha = 70;
    
    static void animateBackground(int endColor, View view) {
        view.clearAnimation();
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluatorCompat(),
                view.getBackgroundTintList().getDefaultColor(),
                endColor);
        valueAnimator.setDuration(300L);
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        valueAnimator.addUpdateListener(animation -> view.setBackgroundTintList(ColorStateList.valueOf((int) animation.getAnimatedValue())));
        valueAnimator.start();
    }
    
    public static void animateBackground(int endColor, ViewGroup view) {
        view.clearAnimation();
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluatorCompat(),
                view.getBackgroundTintList().getDefaultColor(),
                endColor);
        valueAnimator.setDuration(300L);
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        valueAnimator.addUpdateListener(animation -> view.setBackgroundTintList(ColorStateList.valueOf((int) animation.getAnimatedValue())));
        valueAnimator.start();
    }
    
    static RippleDrawable getRippleDrawable(Context context, Drawable backgroundDrawable) {
        float[] outerRadii = new float[8];
        float[] innerRadii = new float[8];
        Arrays.fill(outerRadii, MainPreferences.INSTANCE.getCornerRadius());
        Arrays.fill(innerRadii, MainPreferences.INSTANCE.getCornerRadius());
        
        RoundRectShape shape = new RoundRectShape(outerRadii, null, innerRadii);
        ShapeDrawable mask = new ShapeDrawable(shape);
        
        ColorStateList stateList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.rippleColor));
        
        RippleDrawable rippleDrawable = new RippleDrawable(stateList, backgroundDrawable, mask);
        rippleDrawable.setAlpha(alpha);
        
        return rippleDrawable;
    }
    
    static RippleDrawable getRippleDrawable(Context context, Drawable backgroundDrawable, float divisiveFactor) {
        float[] outerRadii = new float[8];
        float[] innerRadii = new float[8];
        Arrays.fill(outerRadii, MainPreferences.INSTANCE.getCornerRadius() / divisiveFactor);
        Arrays.fill(innerRadii, MainPreferences.INSTANCE.getCornerRadius() / divisiveFactor);
        
        RoundRectShape shape = new RoundRectShape(outerRadii, null, innerRadii);
        ShapeDrawable mask = new ShapeDrawable(shape);
        
        ColorStateList stateList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.rippleColor));
        
        RippleDrawable rippleDrawable = new RippleDrawable(stateList, backgroundDrawable, mask);
        rippleDrawable.setAlpha(alpha);
        
        return rippleDrawable;
    }
}
