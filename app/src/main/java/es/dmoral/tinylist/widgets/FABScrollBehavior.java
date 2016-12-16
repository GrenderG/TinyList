package es.dmoral.tinylist.widgets;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by grend on 16/01/2016.
 * <p>
 * This class is used to give a custom behaviour to the Floating Action Button.
 * (Hide when scrolling down and show when scrolling up)
 */
public class FABScrollBehavior extends FloatingActionButton.Behavior {

    private static boolean canHideChild = true;

    public FABScrollBehavior(Context context, AttributeSet attributeSet) {
        super();
    }

    public static void setCanHideChild(boolean canHideChild) {
        FABScrollBehavior.canHideChild = canHideChild;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if ((dyConsumed > 0 || dyUnconsumed > 0) && child.getVisibility() == View.VISIBLE) {
            if (FABScrollBehavior.canHideChild)
                child.hide();
        } else if ((dyConsumed < 0 || dyUnconsumed < 0) && child.getVisibility() == View.GONE) {
            child.show();
        }
    }
}
