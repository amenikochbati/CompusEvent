package com.example.compusevent;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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
     * L'en-tête reste fixe pour garder Retour/Accueil accessibles, mais un geste commencé
     * sur son titre doit faire défiler le contenu de la page.
     */
    public static void forwardScrollFromHeader(Activity activity) {
        View headerContent = activity.findViewById(R.id.app_bar_content);
        View scrollTarget = activity.findViewById(R.id.scroll_target);
        if (headerContent == null || scrollTarget == null) {
            return;
        }

        final float[] lastY = {0};
        headerContent.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastY[0] = event.getRawY();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                int deltaY = (int) (lastY[0] - event.getRawY());
                scrollTarget.scrollBy(0, deltaY);
                lastY[0] = event.getRawY();
                return true;
            }
            return true;
        });
    }

    public static void goHome(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.finish();
    }
}
