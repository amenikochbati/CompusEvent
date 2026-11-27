package com.example.compusevent;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.OverScroller;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

/** Navigation simple et commune : retour arrière, retour à l'accueil et défilement depuis l'en-tête. */
public final class NavigationHelper {

    private NavigationHelper() {
    }

    public static void setup(Activity activity, String title, String subtitle) {
        TextView tvTitle = activity.findViewById(R.id.tv_app_bar_title);
        TextView tvSubtitle = activity.findViewById(R.id.tv_app_bar_subtitle);
        ImageButton btnBack = activity.findViewById(R.id.btn_back);
        ImageButton btnHome = activity.findViewById(R.id.btn_home);

        if (tvTitle != null) {
            tvTitle.setText(title);
        }
        if (tvSubtitle != null && subtitle != null) {
            tvSubtitle.setText(subtitle);
        }
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> activity.finish());
        }
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> goHome(activity));
        }

        forwardScrollFromHeader(activity);
    }

    /**
     * L'en-tête reste fixe pour garder Retour/Accueil accessibles, mais tout geste vertical
     * commencé dessus (sauf sur un bouton) fait défiler la page, avec une inertie naturelle.
     */
    public static void forwardScrollFromHeader(Activity activity) {
        View header = activity.findViewById(R.id.app_bar);
        View scrollTarget = activity.findViewById(R.id.scroll_target);
        if (header == null || scrollTarget == null) {
            View fallback = activity.findViewById(R.id.app_bar_content);
            if (fallback == null || scrollTarget == null) {
                return;
            }
            header = fallback;
        }

        final View touchHeader = header;
        final float[] lastY = {0f};
        final VelocityTracker[] tracker = {VelocityTracker.obtain()};
        final OverScroller scroller = new OverScroller(activity);
        final Runnable flingRunnable = new Runnable() {
            @Override
            public void run() {
                if (scroller.computeScrollOffset()) {
                    scrollTarget.scrollTo(scroller.getCurrX(), scroller.getCurrY());
                    ViewCompat.postOnAnimation(scrollTarget, this);
                }
            }
        };

        header.setOnTouchListener((v, event) -> {
            if (isOverClickableChild(touchHeader, event.getX(), event.getY())) {
                return false;
            }

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    scroller.abortAnimation();
                    tracker[0].clear();
                    tracker[0].addMovement(event);
                    lastY[0] = event.getY();
                    return true;

                case MotionEvent.ACTION_MOVE: {
                    tracker[0].addMovement(event);
                    int deltaY = (int) (lastY[0] - event.getY());
                    if (deltaY != 0) {
                        scrollTarget.scrollBy(0, deltaY);
                        lastY[0] = event.getY();
                    }
                    return true;
                }

                case MotionEvent.ACTION_UP:
                    tracker[0].addMovement(event);
                    tracker[0].computeCurrentVelocity(1000);
                    int velocityY = (int) tracker[0].getYVelocity();
                    scroller.fling(
                            scrollTarget.getScrollX(),
                            scrollTarget.getScrollY(),
                            0,
                            -velocityY,
                            0,
                            0,
                            0,
                            Integer.MAX_VALUE);
                    ViewCompat.postOnAnimation(scrollTarget, flingRunnable);
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    tracker[0].clear();
                    return true;

                default:
                    return true;
            }
        });
    }

    /** Laisse les boutons Retour, Accueil et Connexion parfaitement cliquables. */
    private static boolean isOverClickableChild(View header, float x, float y) {
        if (!(header instanceof ViewGroup)) {
            return false;
        }
        ViewGroup group = (ViewGroup) header;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.isClickable()
                    && x >= child.getLeft() && x <= child.getRight()
                    && y >= child.getTop() && y <= child.getBottom()) {
                return true;
            }
        }
        return false;
    }

    public static void goHome(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.finish();
    }
}
