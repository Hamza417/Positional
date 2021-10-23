package app.simple.positional.decorations.transformers;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class DepthTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(@NonNull View page, float position) {
        if (position < -1) {
            page.setAlpha(0);
        } else if (position <= 0) {
            page.setAlpha(1);
            page.setTranslationZ(0);
            page.setScaleX(1);
            page.setScaleY(1);
        } else if (position <= 1) {
            page.setTranslationZ(-position * page.getHeight());
            page.setAlpha(1 - Math.abs(position));
            page.setScaleX(1 - Math.abs(position));
            page.setScaleY(1 - Math.abs(position));
        } else {
            page.setAlpha(0);
        }
    }
}