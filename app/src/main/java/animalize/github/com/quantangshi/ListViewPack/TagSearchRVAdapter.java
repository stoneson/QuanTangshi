package animalize.github.com.quantangshi.ListViewPack;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import animalize.github.com.quantangshi.Data.InfoItem;
import animalize.github.com.quantangshi.R;

public abstract class TagSearchRVAdapter
        extends RecyclerView.Adapter<MyHolder> {

    private static final int PAGEITEMS = 50;

    private List<InfoItem> mList;
    // 最后一页的页号
    private int mLastPage;
    // 当前页，1起始
    private int mPage;

    private String[] mForSpinner;

    public abstract void onItemClick(int pid);

    public void setArrayList(List<InfoItem> al) {
        mList = al;

        mLastPage = mList.size() / PAGEITEMS + (mList.size() % PAGEITEMS != 0 ? 1 : 0);

        mForSpinner = new String[mLastPage];
        for (int i = 0; i < mLastPage; i++) {
            if (i != mLastPage - 1) {
                mForSpinner[i] = "" + (i * PAGEITEMS + 1) + "-" +
                        (i + 1) * PAGEITEMS;
            } else {
                mForSpinner[i] = "" + (i * PAGEITEMS + 1) + "-" +
                        (i * PAGEITEMS + mList.size() % PAGEITEMS);
            }
        }
    }

    public void setPage(int page) {
        mPage = page > mLastPage ? mLastPage : page;
        notifyDataSetChanged();
    }

    public void clear() {
        mList = null;
        mForSpinner = null;
        notifyDataSetChanged();
    }

    public int getCurrentPage() {
        return mPage;
    }

    public int getLastPage() {
        return mLastPage;
    }

    public boolean hasPrev() {
        return mPage > 1;
    }

    public boolean hasNext() {
        return mPage < mLastPage;
    }

    public String[] getForSpinner() {
        return mForSpinner;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recent_list_item, parent, false);
        final MyHolder holder = new MyHolder(v);

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int posi = holder.getAdapterPosition();
                posi = (mPage - 1) * PAGEITEMS + posi;
                InfoItem ri = mList.get(posi);

                onItemClick(ri.getId());
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        InfoItem ri = mList.get((mPage - 1) * PAGEITEMS + position);

        if (position % 2 == 0) {
            holder.root.setBackgroundColor(Color.rgb(0xff, 0xcc, 0xcc));
        } else {
            holder.root.setBackgroundColor(Color.rgb(0xcc, 0xcc, 0xff));
        }

        holder.order.setText(String.valueOf((mPage - 1) * PAGEITEMS + position + 1));
        holder.title.setText(ri.getTitle());
        holder.author.setText(ri.getAuthor());
        holder.id.setText("" + ri.getId());
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }

        if (mPage == mLastPage) {
            return mList.size() % PAGEITEMS;
        } else {
            return PAGEITEMS;
        }
    }
}