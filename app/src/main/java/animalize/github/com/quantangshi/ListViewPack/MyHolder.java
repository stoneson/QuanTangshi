package animalize.github.com.quantangshi.ListViewPack;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import animalize.github.com.quantangshi.R;

public class MyHolder extends RecyclerView.ViewHolder {
    public LinearLayout root;
    public TextView order;
    public TextView title;
    public TextView author;
    public TextView id;

    public MyHolder(View itemView) {
        super(itemView);

        root = (LinearLayout) itemView.findViewById(R.id.recent_item);
        order = (TextView) itemView.findViewById(R.id.recent_item_order);
        title = (TextView) itemView.findViewById(R.id.recent_item_title);
        author = (TextView) itemView.findViewById(R.id.recent_item_author);
        id = (TextView) itemView.findViewById(R.id.recent_item_id);
    }
}

