package animalize.github.com.quantangshi;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import animalize.github.com.quantangshi.Database.MyAssetsDatabaseHelper;
import animalize.github.com.quantangshi.T2sMap.T2SData;

public class AboutActivity extends AppCompatActivity {

    private boolean isChecking = false;
    private Button checkButton;
    private TextView versionInfo;

    public static void actionStart(Context context) {
        Intent i = new Intent(context, AboutActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // toolbar
        Toolbar tb = findViewById(R.id.about_toolbar);
        setSupportActionBar(tb);

        // 要在setSupportActionBar之后
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 版本
        TextView tv = findViewById(R.id.version);

        String versionName = "程序版本：" + BuildConfig.VERSION_NAME +
                "\n全唐诗数据修订：" + MyAssetsDatabaseHelper.DATABASE_VERSION +
                "\n繁简转换表修订：" + T2SData.DATA_VERSION +
                "\n";

        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        DateFormat df = new SimpleDateFormat("编译于：yyyy-MM-dd E HH:mm", Locale.getDefault());
        tv.setText(versionName + df.format(buildDate));

        // mail
        tv = findViewById(R.id.about_mail);
        StringBuilder sb = new StringBuilder();
        sb.append("反馈意见、勘误：<br><a href=mailto:mal");
        sb.append("incn");
        sb.append("s@163");
        sb.append(".com>m");
        sb.append("alincn");
        sb.append("s@163.");
        sb.append("com</a>");
        Spanned s = Utils.getFromHtml(sb.toString());
        tv.setText(s);

        // html
        tv = findViewById(R.id.about_text);
        s = Utils.getFromHtml(getString(R.string.about));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(s);

        // 检查更新
        checkButton = findViewById(R.id.check_update);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isChecking) {
                    return;
                }

                v.setEnabled(false);
                new CheckTask(AboutActivity.this).execute();
            }
        });

        versionInfo = findViewById(R.id.ver_info);
    }

    public void updateUI(String s) {
        checkButton.setEnabled(true);

        if (s == null) {
            versionInfo.setText("检查失败");
        } else {
            versionInfo.setText(s);
        }

        versionInfo.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private static class CheckTask extends AsyncTask<Void, Void, String> {
        private static final String verURL = "https://raw.githubusercontent.com/animalize/QuanTangshi/master/app/build.gradle";
        private WeakReference<AboutActivity> ref;

        public CheckTask(AboutActivity about) {
            ref = new WeakReference<>(about);
        }

        @Override
        protected String doInBackground(Void... params) {
            String html;

            try {
                URL url = new URL(verURL);
                URLConnection con = url.openConnection();
                con.setConnectTimeout(10 * 1000);
                con.setReadTimeout(10 * 1000);
                InputStream in = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                html = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    html += line;
                }

                reader.close();
            } catch (Exception e) {
                return null;
            }

            String p = "versionName\\s*\"(.*?)\"" +
                    ".*?dataRev\\s*\"(.*?)\"" +
                    ".*?T2SRev\\s*\"(.*?)\"";

            Pattern pattern = Pattern.compile(p, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(html);
            if (!matcher.find()) {
                return null;
            }

            return "GitHub上最新版本：" + matcher.group(1) +
                    "\n最新全唐诗数据修订：" + matcher.group(2) +
                    "\n最新繁简转换表修订：" + matcher.group(3);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s == null) {
                s = "获取信息失败";
            }

            AboutActivity about = ref.get();
            if (about == null) {
                return;
            }

            about.updateUI(s);
        }
    }
}
