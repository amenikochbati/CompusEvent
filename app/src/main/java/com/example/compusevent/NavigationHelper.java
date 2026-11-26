package com.example.compusevent;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;

/** Navigation simple et commune : retour arrière et retour à l'accueil. */
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
    }

    public static void goHome(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.finish();
    }
}
