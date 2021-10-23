package app.simple.positional.decorations.searchview;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import app.simple.positional.R;
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout;
import app.simple.positional.decorations.ripple.DynamicRippleImageButton;
import app.simple.positional.preferences.SearchPreferences;
import app.simple.positional.util.ViewUtils;

public class SearchView extends DynamicCornerLinearLayout {
    
    private EditText editText;
    private SearchViewEventListener searchViewEventListener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    public SearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
        setProperties();
    }
    
    private void setProperties() {
        setElevation(30F);
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.mainBackground)));
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutTransition(new LayoutTransition());
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        ViewUtils.INSTANCE.addShadow(this);
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    private void initViews() {
        
        View view = LayoutInflater.from(getContext()).inflate(R.layout.search_view, this, true);
        
        editText = view.findViewById(R.id.search_view_text_input_layout);
        DynamicRippleImageButton clear = view.findViewById(R.id.search_clear);
        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /* no-op */
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchViewEventListener != null) {
                    searchViewEventListener.onSearchTextChanged(s.toString(), count);
                }
    
                if (count > 0) {
                    clear.animate().alpha(1F)
                            .scaleX(1F)
                            .scaleY(1F)
                            .setInterpolator(new LinearOutSlowInInterpolator())
                            .setDuration(getResources().getInteger(R.integer.animation_duration))
                            .start();
        
                    clear.setClickable(true);
                }
                else {
                    clear.animate().alpha(0F)
                            .scaleX(0F)
                            .scaleY(0F)
                            .setInterpolator(new AccelerateInterpolator())
                            .setDuration(getResources().getInteger(R.integer.animation_duration))
                            .start();
        
                    clear.setClickable(false);
                }
    
                SearchPreferences.INSTANCE.setLastSearchKeyword(s.toString());
            }
    
            @Override
            public void afterTextChanged(Editable s) {
                /* no-op */
            }
        });
    
        clear.setOnClickListener(v -> {
            //handler.post(clearText);
            editText.getText().clear();
        });
    }
    
    public void setKeywords(String string) {
        editText.setText(string);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        editText.clearAnimation();
    }
    
    private final Runnable clearText = new Runnable() {
        @Override
        public void run() {
            if (editText.getText().length() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(editText.getText());
                stringBuilder.deleteCharAt(editText.getText().length() - 1);
                editText.setText(stringBuilder);
                handler.postDelayed(this, 25L);
            }
            else {
                editText.getText().clear();
                handler.removeCallbacks(this);
            }
        }
    };
    
    public void animateElevation(float elevation) {
        if (getElevation() == elevation) {
            return;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(getElevation(), elevation);
        valueAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        valueAnimator.setDuration(getResources().getInteger(R.integer.animation_duration));
        valueAnimator.addUpdateListener(animation -> setElevation((float) animation.getAnimatedValue()));
        valueAnimator.start();
    }
    
    public void setSearchViewEventListener(SearchViewEventListener searchViewEventListener) {
        this.searchViewEventListener = searchViewEventListener;
    }
}
