package animalize.github.com.quantangshi;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;

/**
 * Created by anima on 17-3-25.
 */

public class Utils {
    public static Spanned getFromHtml(String html) {
        Spanned s;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            s = Html.fromHtml(html,
                    Html.FROM_HTML_MODE_LEGACY);
        } else {
            s = Html.fromHtml(html);
        }
        return s;
    }

    public static void openInSysBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }
}
