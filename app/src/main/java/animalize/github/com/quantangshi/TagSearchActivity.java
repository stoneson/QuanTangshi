package animalize.github.com.quantangshi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import animalize.github.com.quantangshi.Data.InfoItem;
import animalize.github.com.quantangshi.Data.TagInfo;
import animalize.github.com.quantangshi.Database.MyDatabaseHelper;
import animalize.github.com.quantangshi.Database.TagAgent;
import animalize.github.com.quantangshi.ListViewPack.RVAdapter;
import animalize.github.com.quantangshi.UIPoem.OnePoemActivity;
import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

public class TagSearchActivity extends AppCompatActivity {

    private boolean inResult = false;
    private int currentCount = 0;

    private List<TagInfo> mAllTagList;
    private TagContainerLayout searchTags;
    private TagContainerLayout allTags;

    private Toolbar tb;
    private LinearLayout layoutAll;
    private LinearLayout layoutResult;

    private Button searchButton;

    private RVAdapter resultAdapter;
    private RecyclerView rvResult;

    public static void actionStart(Context context) {
        Intent i = new Intent(context, TagSearchActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_search);

        // toolbar
        tb = (Toolbar) findViewById(R.id.tag_search_toolbar);
        tb.setTitle("标签搜索");
        setSupportActionBar(tb);

        // 要在setSupportActionBar之后
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchTags = (TagContainerLayout) findViewById(R.id.search_tags);
        searchTags.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {

            }

            @Override
            public void onTagLongClick(int position, String text) {

            }

            @Override
            public void onTagCrossClick(int position) {
                searchTags.removeTag(position);

                currentCount -= 1;
                setSearchButton();
            }
        });

        // 所有tags 数组
        mAllTagList = TagAgent.getAllTagInfos();
        if (mAllTagList.isEmpty()) {
            Toast.makeText(this, "尚未添加标签，请在添加后使用本功能。", Toast.LENGTH_LONG).show();
        }

        // 所有tags
        allTags = (TagContainerLayout) findViewById(R.id.all_tags);
        allTags.setIsTagViewClickable(true);
        allTags.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {
                TagInfo info = mAllTagList.get(position);
                TagSearchActivity.this.clickOneAllTag(info);
            }

            @Override
            public void onTagLongClick(int position, String text) {
            }

            @Override
            public void onTagCrossClick(int position) {

            }
        });
        allTags.setTags(TagAgent.getAllTagsHasCount());

        // 开始搜索
        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch();
            }
        });

        // 退回
        Button bt = (Button) findViewById(R.id.back_button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultToSearch();
            }
        });

        layoutAll = (LinearLayout) findViewById(R.id.layout_search);
        layoutResult = (LinearLayout) findViewById(R.id.layout_result);

        rvResult = (RecyclerView) findViewById(R.id.rv_result);

        // 布局管理
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rvResult.setLayoutManager(lm);

        // adapter
        resultAdapter = new RVAdapter() {
            @Override
            public void onItemClick(int pid) {
                OnePoemActivity.actionStart(TagSearchActivity.this, pid);
            }
        };
        rvResult.setAdapter(resultAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        List<String> tags = searchTags.getTags();
        outState.putStringArrayList("search_tags", (ArrayList<String>) tags);

        outState.putBoolean("in_result", inResult);
        outState.putInt("count", currentCount);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ArrayList<String> tags = savedInstanceState.getStringArrayList("search_tags");
        searchTags.setTags(tags);

        inResult = savedInstanceState.getBoolean("in_result", false);
        currentCount = savedInstanceState.getInt("count", 0);

        if (inResult) {
            doSearch();
        }
        setSearchButton();
    }

    private void setSearchButton() {
        searchButton.setEnabled(currentCount > 0);
    }

    public void clickOneAllTag(TagInfo info) {
        if (searchTags.getTags().contains(info.getName())) {
            return;
        }

        searchTags.addTag(info.getName());

        currentCount += 1;
        setSearchButton();
    }

    private void doSearch() {
        List<String> list = searchTags.getTags();
        if (list.isEmpty()) {
            return;
        }

        ArrayList<InfoItem> l = MyDatabaseHelper.queryByTags(list);

        tb.setTitle("标签搜索 - 找到" + l.size() + "首");

        resultAdapter.setArrayList(l);

        layoutAll.setVisibility(View.INVISIBLE);
        layoutResult.setVisibility(View.VISIBLE);

        List<String> tags = searchTags.getTags();
        searchTags.setEnableCross(false);
        searchTags.setTags(tags);

        inResult = true;
    }

    private void resultToSearch() {
        // 所有标签
        mAllTagList = TagAgent.getAllTagInfos();
        allTags.setTags(TagAgent.getAllTagsHasCount());

        // 可能被删除的搜索标签
        List<String> tmp = searchTags.getTags();
        if (!tmp.isEmpty()) {
            for (int posi = tmp.size() - 1; posi >= 0; posi--) {
                boolean pass = true;
                final String s = tmp.get(posi);

                for (TagInfo info : mAllTagList) {
                    if (s.equals(info.getName())) {
                        pass = false;
                        break;
                    }
                }
                if (pass) {
                    searchTags.removeTag(posi);
                }
            }
        }

        tb.setTitle("标签搜索");

        // 可见、不可见
        layoutResult.setVisibility(View.INVISIBLE);
        layoutAll.setVisibility(View.VISIBLE);

        // tag上的叉
        List<String> tags = searchTags.getTags();
        searchTags.setEnableCross(true);
        searchTags.setTags(tags);

        inResult = false;
    }

    @Override
    public void onBackPressed() {
        if (inResult) {
            resultToSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
