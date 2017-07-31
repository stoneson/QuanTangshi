package animalize.github.com.quantangshi;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudyResultActivity
        extends AppCompatActivity
        implements Toolbar.OnMenuItemClickListener,
        View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        DialogInterface.OnClickListener {

    private static final String[] engines =
            {
                    "百度", "汉语", "百科搜索", "百科词条", "图片"
            };

    private static final String PREFIX = "缩放百分比：";

    private String word;
    private WebView webView;

    private LinearLayout ratioPanel;
    private Button ratioOK, ratioCancel;
    private TextView ratioText;
    private SeekBar ratioBar;
    private int ratio;

    // 跳转通假字用
    private static Pattern hanyu_url;
    private boolean isHanyu = false;
    private String html;
    private String[] array;
    private MenuItem extractTongjiazi;

    public static void actionStart(Context context, String word, String url) {
        Intent i = new Intent(context, StudyResultActivity.class);
        i.putExtra("word", word);
        i.putExtra("url", url);
        context.startActivity(i);
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // intent
        Intent intent = getIntent();
        word = intent.getStringExtra("word");
        String url = intent.getStringExtra("url");

        setContentView(R.layout.activity_study_result);

        // toolbar
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        // 要在setSupportActionBar之后
        tb.setOnMenuItemClickListener(this);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // 标题
        TextView barTitle = (TextView) findViewById(R.id.title_name);
        barTitle.setText(word);
        barTitle.setOnClickListener(this);

        // 缩放比例
        ratio = loadRatio();

        // webview
        webView = (WebView) findViewById(R.id.webView);

        if (savedInstanceState == null) {
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setTextZoom(ratio);
            settings.setDomStorageEnabled(true);

            webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    isHanyu = hanyuURL(url);
                    if (isHanyu) {
                        webView.loadUrl("javascript:HTMLOUT.processHTML(document.documentElement.outerHTML);");
                    }
                }
            });

            webView.setWebChromeClient(new WebChromeClient());
            webView.loadUrl(url);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    private void initWidgets() {
        if (ratioPanel != null) {
            return;
        }

        ratioPanel = (LinearLayout) findViewById(R.id.ratio_panel);

        ratioOK = (Button) findViewById(R.id.ratio_ok);
        ratioOK.setOnClickListener(this);

        ratioCancel = (Button) findViewById(R.id.ratio_cancel);
        ratioCancel.setOnClickListener(this);

        ratioText = (TextView) findViewById(R.id.ratio_text);
        ratioText.setText(PREFIX + ratio);

        ratioBar = (SeekBar) findViewById(R.id.ratio_bar);
        ratioBar.setProgress(ratio);
        ratioBar.setOnSeekBarChangeListener(this);
    }

    private int loadRatio() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        return pref.getInt("ratio", 100);
    }

    private void saveRatio(int ratio) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putInt("ratio", ratio);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.studyresult_menu, menu);
        extractTongjiazi = menu.findItem(R.id.extract_tongjiazi);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        extractTongjiazi.setEnabled(isHanyu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.extract_tongjiazi:
                tongJiaZi();
                break;

            case R.id.copy_url:
                String url = webView.getUrl();

                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, url));

                Toast.makeText(
                        this,
                        "已复制本页链接",
                        Toast.LENGTH_SHORT).show();
                break;

            case R.id.open_it:
                url = webView.getUrl();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                break;

            case R.id.set_font_ratio:
                initWidgets();
                ratioPanel.setVisibility(View.VISIBLE);
                break;

            case R.id.set_clear_caches:
                webView.clearCache(true);
                String s = "已清除本应用的WebView缓存。\n通常不必执行此操作。";
                Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                break;

            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ratio_cancel:
                ratioPanel.setVisibility(View.GONE);

                // 还原界面
                webView.getSettings().setTextZoom(ratio);
                ratioText.setText(PREFIX + ratio);
                ratioBar.setProgress(ratio);
                break;

            case R.id.ratio_ok:
                ratioPanel.setVisibility(View.GONE);

                // 保存设置
                ratio = webView.getSettings().getTextZoom();
                saveRatio(ratio);
                break;

            case R.id.title_name:
                new AlertDialog.Builder(this)
                        .setTitle("切换搜索引擎")
                        .setItems(engines, this)
                        .setNegativeButton("取消", null)
                        .show();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progress = progress / 5;
        progress = progress * 5;
        ratioText.setText(PREFIX + progress);

        webView.getSettings().setTextZoom(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        String url;
        switch (which) {
            case 0:
                url = "http://www.baidu.com/s?wd=" + word;
                break;
            case 1:
                url = "http://hanyu.baidu.com/zici/s?wd=" + word;
                break;
            case 2:
                url = "http://baike.baidu.com/search?word=" + word;
                break;
            case 3:
                url = "http://baike.baidu.com/item/" + word;
                break;
            case 4:
                url = "http://image.baidu.com/search/wiseala?tn=wiseala&word=" + word;
                break;
            default:
                url = "http://www.baidu.com";
        }
        webView.loadUrl(url);
    }

    private boolean hanyuURL(String url) {
        if (hanyu_url == null) {
            hanyu_url = Pattern.compile(
                    "hanyu\\.baidu\\.com",
                    Pattern.CASE_INSENSITIVE);
        }

        return hanyu_url.matcher(url).find();
    }

    private void tongJiaZi() {
        // 判断当前链接
        if (!hanyuURL(webView.getUrl()) || html == null) {
            Toast.makeText(
                    this,
                    "当前页不是百度汉语，无法提取通假字",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 提取通假字
        String p = "(?:同|见|通|作)\\s{0,3}" +
                "[“\"”]\\s{0,3}" +
                "([^\\s\\d”\"“a-zA-Z]{1,10})(?:\\d{1,2})?\\s{0,3}[”\"“]";
        Pattern tongjiazi = Pattern.compile(p);
        Matcher matcher = tongjiazi.matcher(html);

        ArrayList<String> list = new ArrayList<>();
        while (matcher.find()) {
            String temp = matcher.group(1);
            if (!list.contains(temp)) {
                list.add(temp);
            }
        }

        if (list.isEmpty()) {
            Toast.makeText(
                    this,
                    "在当前页，没有找到通假字",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 对话框
        array = list.toArray(new String[list.size()]);

        new AlertDialog.Builder(this)
                .setTitle("跳转通假字")
                .setItems(array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = "http://hanyu.baidu.com/zici/s?wd=" + array[which];
                        webView.loadUrl(url);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            StudyResultActivity.this.html = html;
        }
    }
}
