package animalize.github.com.quantangshi.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;

import animalize.github.com.quantangshi.MyApplication;
import animalize.github.com.quantangshi.UIPoem.SpinnerAdapter;

/**
 * Created by anima on 17-3-10.
 */

public class Typeset {
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

        titleLines = sp.getInt("title_lines", 2);
        titleSize = sp.getInt("title_size", 26);
        textSize = sp.getInt("text_size", 26);
        lineSpace = sp.getInt("line_space", 8);
        lineBreak = sp.getInt("line_break", 5);
        bgImg = sp.getInt("bg_img", 0);
    }

    public void saveConfig() {
        Context c = MyApplication.getContext();
        SharedPreferences.Editor editor = c.getSharedPreferences(
                "typeset",
                Context.MODE_PRIVATE).edit();

        editor.putInt("title_lines", titleLines);
        editor.putInt("title_size", titleSize);
        editor.putInt("text_size", textSize);
        editor.putInt("line_space", lineSpace);
        editor.putInt("line_break", lineBreak);
        editor.putInt("bg_img", bgImg);
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
        this.titleLines = titleLines;
    }

    public int getTitleSize() {
        return titleSize;
    }

    public void setTitleSize(int titleSize) {
        this.titleSize = titleSize;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getLineBreak() {
        return lineBreak;
    }

    public void setLineBreak(int lineBreak) {
        this.lineBreak = lineBreak;
    }

    public int getLineSpace() {
        return lineSpace;
    }

    public void setLineSpace(int lineSpace) {
        this.lineSpace = lineSpace;
    }

    public int getBgImg() {
        return bgImg;
    }

    public void setBgImg(int bgImg) {
        this.bgImg = bgImg;

        bmp = null;
        poemBMP = studyBMP = null;
    }
}
