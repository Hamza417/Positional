package app.simple.positional.corners;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import androidx.appcompat.widget.AppCompatButton;
import app.simple.positional.R;
import app.simple.positional.preference.MainPreferences;

class LayoutBackground {
    static void setBackground(Context context, ViewGroup viewGroup, AttributeSet attrs) {
        TypedArray theme = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DynamicCornerLayout, 0, 0);
        
        boolean roundTopCorners = theme.getBoolean(R.styleable.DynamicCornerLayout_roundTopCorners, false);
        boolean roundBottomCorners = theme.getBoolean(R.styleable.DynamicCornerLayout_roundBottomCorners, false);
        
        ShapeAppearanceModel shapeAppearanceModel;
        
        if (roundBottomCorners && roundTopCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .build();
        }
        else if (roundTopCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .setTopRightCorner(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .build();
        }
        else if (roundBottomCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setBottomLeftCorner(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .setBottomRightCorner(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .build();
        }
        else {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .build();
        }
    
        viewGroup.setBackground(new MaterialShapeDrawable(shapeAppearanceModel));
    }
    
    static void setBackground(Context context, AppCompatButton appCompatButton, AttributeSet attrs) {
        TypedArray theme = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DynamicCornerLayout, 0, 0);
        
        boolean roundTopCorners = theme.getBoolean(R.styleable.DynamicCornerLayout_roundTopCorners, false);
        boolean roundBottomCorners = theme.getBoolean(R.styleable.DynamicCornerLayout_roundBottomCorners, false);
        
        ShapeAppearanceModel shapeAppearanceModel;
        
        if (roundBottomCorners && roundTopCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .build();
        }
        else if (roundTopCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .setTopRightCorner(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .build();
        }
        else if (roundBottomCorners) {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setBottomLeftCorner(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .setBottomRightCorner(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .build();
        }
        else {
            shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, MainPreferences.INSTANCE.getCornerRadius())
                    .build();
        }
        
        appCompatButton.setBackground(new MaterialShapeDrawable(shapeAppearanceModel));
    }
}
