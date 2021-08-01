package app.simple.positional.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import org.jetbrains.annotations.NotNull;

import app.simple.positional.decorations.utils.LayoutBackground;

public class DynamicCornerEditText extends AppCompatEditText {
    public DynamicCornerEditText(@NonNull @NotNull Context context) {
        super(context);
        LayoutBackground.setBackground(context, this, null);
    }

    public DynamicCornerEditText(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutBackground.setBackground(context, this, attrs);
    }

    public DynamicCornerEditText(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutBackground.setBackground(context, this, attrs);
    }
}
