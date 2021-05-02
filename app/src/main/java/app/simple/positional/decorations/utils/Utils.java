package app.simple.positional.decorations.utils;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RadioButton;

import com.google.android.material.animation.ArgbEvaluatorCompat;

public class Utils {
    public static void animateBackground(int endColor, View view) {
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
    
    public static void animateTint(int endColor, RadioButton view) {
        view.clearAnimation();
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluatorCompat(),
                view.getButtonTintList().getDefaultColor(),
                endColor);
        valueAnimator.setDuration(300L);
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        valueAnimator.addUpdateListener(animation -> view.setButtonTintList(ColorStateList.valueOf((int) animation.getAnimatedValue())));
        valueAnimator.start();
    }
    
    public static void animateElevation(Float elevation, RadioButton button) {
        button.clearAnimation();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(button.getElevation(), elevation);
        valueAnimator.setDuration(300L);
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        valueAnimator.addUpdateListener(animation -> button.setElevation((Float) animation.getAnimatedValue()));
        valueAnimator.start();
    }
}
