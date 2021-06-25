package app.simple.positional.decorations.fastscroll;

import androidx.annotation.NonNull;

public interface ViewHelperProvider {
    
    @NonNull
    FastScroller.ViewHelper getViewHelper();
}
