package app.simple.positional.decorations.scrollers;

import android.content.Context;

public class SnapperSmoothScroller extends FlexiSmoothScroller {
    public SnapperSmoothScroller(Context context) {
        super(context);
        setMillisecondsPerInchFoundTarget(50f);
    }
}
