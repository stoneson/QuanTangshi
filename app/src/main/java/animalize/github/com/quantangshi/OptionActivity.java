package animalize.github.com.quantangshi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import animalize.github.com.quantangshi.Data.RawPoem;
import animalize.github.com.quantangshi.Data.Typeset;
import animalize.github.com.quantangshi.UIPoem.OptionPoemView;

public class OptionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // 编号一定要大于诗的总数
    private RawPoem samplePoem = new RawPoem(
            66666,
            "在学习界面可以看到完整的诗标题，很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长",
            "诗的作者",
            "座隅泉出洞，竹上云起岭。\n五岳寻仙不辞远，一生好入名山游。\n头上何所有，翠微盍叶垂鬓唇。\n朝避猛虎，夕避长蛇。"
    );
    private OptionPoemView poemView;

    private TextView titleLinesTextView;
    private TextView titleSizeTextView;
    private TextView textSizeTextView;
    private TextView lineSpaceTextView;
    private TextView lineBreakTextView;
    private TextView bgTextView;

    public static void actionStart(Context context) {
        Intent i = new Intent(context, OptionActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        poemView = (OptionPoemView) findViewById(R.id.poem_view);
        poemView.setPoem(samplePoem, true);

        // 启动后跳转
        CheckBox cb = (CheckBox) findViewById(R.id.jump_to_read);
        Context c = MyApplication.getContext();
        SharedPreferences sp = c.getSharedPreferences(
                "global",
                Context.MODE_PRIVATE);
        cb.setChecked(sp.getBoolean("jump", false));

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Context c = MyApplication.getContext();
                SharedPreferences.Editor editor = c.getSharedPreferences(
                        "global",
                        Context.MODE_PRIVATE).edit();

                editor.putBoolean("jump", isChecked);
                editor.apply();
            }
        });

        // 屏幕常亮
        boolean screenOn = poemView.getTypeset().isScreenOn();
        cb = (CheckBox) findViewById(R.id.keep_screen_on);
        cb.setChecked(screenOn);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Typeset typeset = poemView.getTypeset();
                typeset.setScreenOn(isChecked);
            }
        });

        // 标题行数
        titleLinesTextView = (TextView) findViewById(R.id.title_lines_text);
        SeekBar titleLinesSeekbar = (SeekBar) findViewById(R.id.title_lines_seekbar);
        titleLinesSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                titleLinesTextView.setText("标题最大行数: " + progress);

                Typeset typeset = poemView.getTypeset();
                typeset.setTitleLines(progress);

                poemView.updateTypeset();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        titleLinesSeekbar.setProgress(poemView.getTypeset().getTitleLines());

        // 标题字体
        titleSizeTextView = (TextView) findViewById(R.id.title_size_text);
        SeekBar titleSizeSeekbar = (SeekBar) findViewById(R.id.title_size_seekbar);
        titleSizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                titleSizeTextView.setText("标题字体: " + progress);

                Typeset typeset = poemView.getTypeset();
                typeset.setTitleSize(progress);

                poemView.updateTypeset();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        titleSizeSeekbar.setProgress(poemView.getTypeset().getTitleSize());

        // 诗文字体
        textSizeTextView = (TextView) findViewById(R.id.text_size_text);
        SeekBar textSizeSeekbar = (SeekBar) findViewById(R.id.text_size_seekbar);
        textSizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textSizeTextView.setText("诗文字体: " + progress);

                Typeset typeset = poemView.getTypeset();
                typeset.setTextSize(progress);

                poemView.updateTypeset();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        textSizeSeekbar.setProgress(poemView.getTypeset().getTextSize());

        // 行间距
        lineSpaceTextView = (TextView) findViewById(R.id.line_space_text);
        SeekBar lineSpaceSeekbar = (SeekBar) findViewById(R.id.line_space_seekbar);
        lineSpaceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lineSpaceTextView.setText("行间距: " + progress);

                Typeset typeset = poemView.getTypeset();
                typeset.setLineSpace(progress);

                poemView.updateTypeset();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        lineSpaceSeekbar.setProgress(poemView.getTypeset().getLineSpace());

        // 换行
        lineBreakTextView = (TextView) findViewById(R.id.line_break_text);
        SeekBar lineBreakSeekbar = (SeekBar) findViewById(R.id.line_break_seekbar);
        lineBreakSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lineBreakTextView.setText("换行字数: " + progress);

                Typeset typeset = poemView.getTypeset();
                typeset.setLineBreak(progress);

                poemView.updateTypeset();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        lineBreakSeekbar.setProgress(poemView.getTypeset().getLineBreak());

        // 背景图
        bgTextView = (TextView) findViewById(R.id.bg_img);
        Spinner bgSpinner = (Spinner) findViewById(R.id.bg_spinner);
        bgSpinner.setOnItemSelectedListener(this);

        SpinnerAdapter sa = new SpinnerAdapter(this);
        bgSpinner.setAdapter(sa);
        Typeset typeset = poemView.getTypeset();
        bgSpinner.setSelection(typeset.getBgImg());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Typeset typeset = poemView.getTypeset();
        typeset.setBgImg(position);

        bgTextView.setText("背景图: " + (position + 1));
        poemView.setBackgroundIMG();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
