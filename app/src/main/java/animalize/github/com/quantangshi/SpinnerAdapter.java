package animalize.github.com.quantangshi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class SpinnerAdapter extends ArrayAdapter<String> {

    private static final int[] IDs = {
            R.drawable.b1, R.drawable.b2,
            R.drawable.b3, R.drawable.b4,
            R.drawable.b5, R.drawable.b6,
            R.drawable.b7, R.drawable.b8,
            R.drawable.b9, R.drawable.b10,
    };
    private Context ctx;

    public SpinnerAdapter(Context context) {
        super(context, R.layout.bg_spinner_item);
        this.ctx = context;
    }

    public static int getResID(int posi) {
        return IDs[posi];
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.bg_spinner_item, parent, false);
        }

        Bitmap bmp = BitmapFactory.decodeResource(ctx.getResources(), IDs[position]);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(ctx.getResources(), bmp);
        bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            convertView.setBackground(bitmapDrawable);
        } else {
            convertView.setBackgroundDrawable(bitmapDrawable);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return IDs.length;
    }
}