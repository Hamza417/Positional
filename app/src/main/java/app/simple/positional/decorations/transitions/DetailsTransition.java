package app.simple.positional.decorations.transitions;

import android.content.Context;
import android.util.AttributeSet;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.ChangeTransform;
import androidx.transition.TransitionSet;

/**
 * Transition that performs almost exactly like {@link android.transition.AutoTransition}, but has an
 * added {@link ChangeImageTransform} to support properly scaling up our gorgeous kittens {@link android.widget.ImageView}.
 */
public class DetailsTransition extends TransitionSet {
    
    public DetailsTransition() {
        init();
    }
    
    /**
     * This constructor allows us to use this transition in XML
     */
    public DetailsTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        /*
         * Makes sure things go smoothly
         */
        setOrdering(ORDERING_TOGETHER);
        
        /*
         * Setting duration to 750L will force the
         * transition to finish last and not interfere
         * with the other transitions that are going on
         * like RecyclerView's layout transition
         * and AndroidX fragment transitions
         */
        addTransition(new ChangeBounds())
                .addTransition(new ChangeTransform())
                .addTransition(new ChangeTransform())
                .addTransition(new ChangeImageTransform())
                .setDuration(500)
                .setInterpolator(new LinearOutSlowInInterpolator());
    }
}
