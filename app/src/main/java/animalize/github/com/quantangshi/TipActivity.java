package animalize.github.com.quantangshi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class TipActivity extends AppCompatActivity {

    public static void actionStart(Context context) {
        Intent i = new Intent(context, TipActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip);

        // toolbar
        Toolbar tb = (Toolbar) findViewById(R.id.tip_toolbar);
        setSupportActionBar(tb);

        // 要在setSupportActionBar之后
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // html
        TextView tv = (TextView) findViewById(R.id.tip_text);
        Spanned s = Utils.getFromHtml(getString(R.string.tip));

        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(s);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
