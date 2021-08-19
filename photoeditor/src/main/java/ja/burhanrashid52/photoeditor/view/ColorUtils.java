package ja.burhanrashid52.photoeditor.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class ColorUtils {
    public static int getColorFromResource(Context context, int colorId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return context.getResources().getColor(colorId, context.getTheme());
            } else {
                return ContextCompat.getColor(context, colorId);
            }
        } catch (Exception e) {
            return Color.BLACK;
        }

    }
}
