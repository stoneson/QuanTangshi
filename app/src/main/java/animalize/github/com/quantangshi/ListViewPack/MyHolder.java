package animalize.github.com.quantangshi.ListViewPack;


import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import animalize.github.com.quantangshi.R;

public class MyHolder extends RecyclerView.ViewHolder {
    public LinearLayout root;
    public TextView order;
    public TextView title;
    public TextView author;
    public TextView id;

    public MyHolder(View itemView) {
        super(itemView);

        root = itemView.findViewById(R.id.recent_item);
        order = itemView.findViewById(R.id.recent_item_order);
        title = itemView.findViewById(R.id.recent_item_title);
        author = itemView.findViewById(R.id.recent_item_author);
        id = itemView.findViewById(R.id.recent_item_id);
    }
}

