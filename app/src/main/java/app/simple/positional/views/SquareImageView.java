package app.simple.positional.views;

import android.content.Context;
import android.util.AttributeSet;

@SuppressWarnings ("ALL")
public class SquareImageView extends androidx.appcompat.widget.AppCompatImageView {
    
    public SquareImageView(Context context) {
        super(context);
    }
    
    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        // Optimization so we don't measure twice unless we need to
        if (width != getMeasuredHeight()) {
            setMeasuredDimension(width, width);
        }
    }
}
