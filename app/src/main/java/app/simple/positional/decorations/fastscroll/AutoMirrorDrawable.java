package app.simple.positional.decorations.fastscroll;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.graphics.drawable.DrawableWrapper;
import androidx.core.graphics.drawable.DrawableCompat;

@SuppressLint ("RestrictedApi")
class AutoMirrorDrawable extends DrawableWrapper {

    public AutoMirrorDrawable(@NonNull Drawable drawable) {
        super(drawable);
    }
    
    @Override
    public void draw(@NonNull Canvas canvas) {
        if (needMirroring()) {
            float centerX = getBounds().exactCenterX();
            canvas.scale(-1, 1, centerX, 0);
            super.draw(canvas);
            canvas.scale(-1, 1, centerX, 0);
        }
        else {
            super.draw(canvas);
        }
    }
    
    @Override
    public boolean onLayoutDirectionChanged(int layoutDirection) {
        super.onLayoutDirectionChanged(layoutDirection);
        return true;
    }
    
    @Override
    public boolean isAutoMirrored() {
        return true;
    }
    
    private boolean needMirroring() {
        return DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_RTL;
    }
    
    @Override
    public boolean getPadding(@NonNull Rect padding) {
        boolean hasPadding = super.getPadding(padding);
        if (needMirroring()) {
            int paddingStart = padding.left;
            padding.left = padding.right;
            padding.right = paddingStart;
        }
        return hasPadding;
    }
}
