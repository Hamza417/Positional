package app.simple.positional.decorations.searchview;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import app.simple.positional.R;
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout;
import app.simple.positional.util.ViewUtils;

public class SearchView extends DynamicCornerLinearLayout {
    
    private EditText editText;
    private SearchViewEventListener searchViewEventListener;
    
    public SearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
        setProperties();
    }
    
    private void setProperties() {
        setElevation(20F);
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.mainBackground)));
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutTransition(new LayoutTransition());
        ViewUtils.INSTANCE.addShadow(this);
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    private void initViews() {
        
        View view = LayoutInflater.from(getContext()).inflate(R.layout.search_view, this, true);
        
        editText = view.findViewById(R.id.search_view_text_input_layout);
        
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
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                /* no-op */
            }
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
    
    public void setSearchViewEventListener(SearchViewEventListener searchViewEventListener) {
        this.searchViewEventListener = searchViewEventListener;
    }
}
