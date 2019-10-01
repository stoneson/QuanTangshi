package animalize.github.com.quantangshi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import animalize.github.com.quantangshi.Data.InfoItem;
import animalize.github.com.quantangshi.Data.TagInfo;
import animalize.github.com.quantangshi.Database.MyDatabaseHelper;
import animalize.github.com.quantangshi.Database.TagAgent;
import animalize.github.com.quantangshi.ListViewPack.TagSearchRVAdapter;
import animalize.github.com.quantangshi.UIPoem.OnePoemActivity;
import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

public class TagSearchActivity extends AppCompatActivity implements View.OnClickListener, TagView.OnTagClickListener, AdapterView.OnItemSelectedListener {

    private boolean inResult = false;

    private List<TagInfo> mAllTagList;
    private TagContainerLayout searchTags;
    private TagContainerLayout allTags;

    private Toolbar tb;
    private LinearLayout layoutAll;
    private LinearLayout layoutResult;

    private Button searchButton;
    private Button prev, next;
    private Spinner spinner;

    private TagSearchRVAdapter resultAdapter;
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
        tb = findViewById(R.id.tag_search_toolbar);
        setSupportActionBar(tb);

        // 要在setSupportActionBar之后
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchTags = findViewById(R.id.search_tags);
        searchTags.setOnTagClickListener(this);

        // 所有tags 数组
        mAllTagList = TagAgent.getAllTagInfos();
        if (mAllTagList.isEmpty()) {
            Toast.makeText(this, "尚未添加标签，请在添加后使用本功能。", Toast.LENGTH_LONG).show();
        }

        // 所有tags
        allTags = findViewById(R.id.all_tags);
        allTags.setIsTagViewClickable(true);
        allTags.setOnTagClickListener(this);
        allTags.setTags(TagAgent.getAllTagsHasCount());

        // 开始搜索
        searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);

        // 退回
        Button bt = findViewById(R.id.back_button);
        bt.setOnClickListener(this);

        // 前一个，后一个，spinner
        prev = findViewById(R.id.prev);
        prev.setOnClickListener(this);
        next = findViewById(R.id.next);
        next.setOnClickListener(this);

        // spinner
        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        layoutAll = findViewById(R.id.layout_search);
        layoutResult = findViewById(R.id.layout_result);

        rvResult = findViewById(R.id.rv_result);

        // 布局管理
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rvResult.setLayoutManager(lm);

        // adapter
        resultAdapter = new TagSearchRVAdapter() {
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

        List<String> t = searchTags.getTags();
        outState.putStringArray("search_tags", t.toArray(new String[t.size()]));

        outState.putBoolean("in_result", inResult);

        if (inResult) {
            outState.putInt("current_page", resultAdapter.getCurrentPage());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String[] tags = savedInstanceState.getStringArray("search_tags");
        searchTags.setTags(tags);

        inResult = savedInstanceState.getBoolean("in_result", false);

        searchButton.setEnabled(tags.length > 0);
        if (inResult) {
            doSearch();

            int current = savedInstanceState.getInt("current_page");
            resultAdapter.setPage(current);

            int last = resultAdapter.getLastPage();
            spinner.setSelection((current > last ? last : current) - 1);
        }
    }

    public void clickOneAllTag(TagInfo info) {
        if (searchTags.getTags().contains(info.getName())) {
            return;
        }

        searchTags.addTag(info.getName());
        searchButton.setEnabled(true);
    }

    private void doSearch() {
        List<String> list = searchTags.getTags();
        if (list.isEmpty()) {
            return;
        }

        // 查询
        ArrayList<InfoItem> searchResultList = MyDatabaseHelper.queryByTags(list);
        tb.setTitle("标签搜索 - 找到" + searchResultList.size() + "首");

        // adapter
        resultAdapter.setArrayList(searchResultList);
        resultAdapter.setPage(1);

        // 前后页按钮
        prev.setEnabled(resultAdapter.hasPrev());
        next.setEnabled(resultAdapter.hasNext());

        // 切换界面
        layoutAll.setVisibility(View.INVISIBLE);
        layoutResult.setVisibility(View.VISIBLE);

        // spinner
        String[] forSpinner = resultAdapter.getForSpinner();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                forSpinner
        );
        spinner.setAdapter(spinnerAdapter);

        // 去掉tag的叉
        List<String> tags = searchTags.getTags();
        searchTags.setEnableCross(false);
        searchTags.setTags(tags);

        // 当前状态
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
        if (searchTags.getTags().isEmpty()) {
            searchButton.setEnabled(false);
        }

        tb.setTitle("标签搜索");

        // 切换界面
        layoutResult.setVisibility(View.INVISIBLE);
        layoutAll.setVisibility(View.VISIBLE);

        // tag上的叉
        List<String> tags = searchTags.getTags();
        searchTags.setEnableCross(true);
        searchTags.setTags(tags);

        // 清空adapter
        resultAdapter.clear();

        // 切换状态
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_button:
                doSearch();
                break;

            case R.id.back_button:
                resultToSearch();
                break;

            case R.id.prev:
                spinner.setSelection(spinner.getSelectedItemPosition() - 1);
                break;

            case R.id.next:
                spinner.setSelection(spinner.getSelectedItemPosition() + 1);
                break;
        }
    }

    @Override
    public void onTagClick(int position, String text) {
        // 注意，两个TagContainerLayout共用一个listener
        // 所有标签的逻辑
        TagInfo info = mAllTagList.get(position);
        clickOneAllTag(info);
    }

    @Override
    public void onTagLongClick(int position, String text) {

    }

    @Override
    public void onSelectedTagDrag(int i, String s) {

    }

    @Override
    public void onTagCrossClick(int position) {
        // 注意，两个TagContainerLayout共用一个listener
        // 当前标签的逻辑
        searchTags.removeTag(position);

        if (searchTags.getTags().isEmpty()) {
            searchButton.setEnabled(false);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // adapter
        resultAdapter.setPage(position + 1);
        rvResult.scrollToPosition(0);

        // 前后页按钮
        prev.setEnabled(resultAdapter.hasPrev());
        next.setEnabled(resultAdapter.hasNext());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
