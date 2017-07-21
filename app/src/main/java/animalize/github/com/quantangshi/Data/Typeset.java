package animalize.github.com.quantangshi.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;

import animalize.github.com.quantangshi.MyApplication;
import animalize.github.com.quantangshi.SpinnerAdapter;

/**
 * Created by anima on 17-3-10.
 */

public class Typeset {
    private static final String TAG_TITLE_LINES = "title_lines";
    private static final String TAG_TITLE_SIZE = "title_size";
    private static final String TAG_TEXT_SIZE = "text_size";
    private static final String TAG_LINE_SPACE = "line_space";
    private static final String TAG_LINE_BREAK = "line_break";
    private static final String TAG_BG_IMG = "bg_img";
    private static Typeset singleTong;
    private int titleLines;
    private int titleSize;
    private int textSize;
    private int lineSpace;
    private int lineBreak;
    private int bgImg;
    private Bitmap bmp;
    private BitmapDrawable poemBMP, studyBMP;

    private Typeset() {
        loadConfig();
    }

    public static Typeset getInstance() {
        if (singleTong == null) {
            singleTong = new Typeset();
        }
        return singleTong;
    }

    public void loadConfig() {
        Context c = MyApplication.getContext();
        SharedPreferences sp = c.getSharedPreferences(
                "typeset",
                Context.MODE_PRIVATE);

        titleLines = sp.getInt(TAG_TITLE_LINES, 2);
        titleSize = sp.getInt(TAG_TITLE_SIZE, 26);
        textSize = sp.getInt(TAG_TEXT_SIZE, 26);
        lineSpace = sp.getInt(TAG_LINE_SPACE, 8);
        lineBreak = sp.getInt(TAG_LINE_BREAK, 5);
        bgImg = sp.getInt(TAG_BG_IMG, 0);
    }

    private void saveOne(String name, int value) {
        Context c = MyApplication.getContext();
        SharedPreferences.Editor editor = c.getSharedPreferences(
                "typeset",
                Context.MODE_PRIVATE).edit();
        editor.putInt(name, value);
        editor.apply();
    }

    public BitmapDrawable getPoemBGDrawable() {
        if (poemBMP == null) {
            if (bmp == null) {
                bmp = BitmapFactory.decodeResource(
                        MyApplication.getContext().getResources(),
                        SpinnerAdapter.getResID(bgImg));
            }

            poemBMP = new BitmapDrawable(
                    MyApplication.getContext().getResources(),
                    bmp);
            poemBMP.setTileModeXY(
                    Shader.TileMode.REPEAT,
                    Shader.TileMode.REPEAT);
        }
        return poemBMP;
    }

    public BitmapDrawable getStudyBGDrawable() {
        if (studyBMP == null) {
            if (bmp == null) {
                bmp = BitmapFactory.decodeResource(
                        MyApplication.getContext().getResources(),
                        SpinnerAdapter.getResID(bgImg));
            }

            studyBMP = new BitmapDrawable(
                    MyApplication.getContext().getResources(),
                    bmp);
            studyBMP.setTileModeXY(
                    Shader.TileMode.REPEAT,
                    Shader.TileMode.REPEAT);
        }
        return studyBMP;
    }

    public int getTitleLines() {
        return titleLines;
    }

    public void setTitleLines(int titleLines) {
        if (this.titleLines != titleLines) {
            this.titleLines = titleLines;
            saveOne(TAG_TITLE_LINES, titleLines);
        }
    }

    public int getTitleSize() {
        return titleSize;
    }

    public void setTitleSize(int titleSize) {
        if (this.titleSize != titleSize) {
            this.titleSize = titleSize;
            saveOne(TAG_TITLE_SIZE, titleSize);
        }
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            saveOne(TAG_TEXT_SIZE, textSize);
        }
    }

    public int getLineBreak() {
        return lineBreak;
    }

    public void setLineBreak(int lineBreak) {
        if (this.lineBreak != lineBreak) {
            this.lineBreak = lineBreak;
            saveOne(TAG_LINE_BREAK, lineBreak);
        }
    }

    public int getLineSpace() {
        return lineSpace;
    }

    public void setLineSpace(int lineSpace) {
        if (this.lineSpace != lineSpace) {
            this.lineSpace = lineSpace;
            saveOne(TAG_LINE_SPACE, lineSpace);
        }
    }

    public int getBgImg() {
        return bgImg;
    }

    public void setBgImg(int bgImg) {
        if (this.bgImg != bgImg) {
            this.bgImg = bgImg;
            saveOne(TAG_BG_IMG, bgImg);
        }

        bmp = null;
        poemBMP = studyBMP = null;
    }
}
