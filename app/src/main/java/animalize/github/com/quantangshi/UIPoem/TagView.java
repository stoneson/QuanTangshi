package animalize.github.com.quantangshi.UIPoem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import animalize.github.com.quantangshi.Data.TagInfo;
import animalize.github.com.quantangshi.Database.TagAgent;
import animalize.github.com.quantangshi.MyApplication;
import animalize.github.com.quantangshi.R;
import co.lujun.androidtagview.TagContainerLayout;


public class TagView extends LinearLayout implements View.OnClickListener, co.lujun.androidtagview.TagView.OnTagClickListener {
    InputMethodManager imm;
    private int mPid;

    private PoemController mController;
    private List<TagInfo> mTagList;
    private List<TagInfo> mAllTagList;
    private TagContainerLayout mPoemTags;
    private TagContainerLayout mAllTags;
    private EditText mEdit;

    public TagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_tag, this);

        imm = (InputMethodManager) MyApplication
                .getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        mEdit = findViewById(R.id.tag_edit);

        Button mAddTag = findViewById(R.id.tag_add);
        mAddTag.setOnClickListener(this);

        // tag
        mPoemTags = findViewById(R.id.poem_tags);
        mPoemTags.setOnTagClickListener(this);

        mAllTags = findViewById(R.id.all_tags);
        mAllTags.setIsTagViewClickable(true);
        mAllTags.setOnTagClickListener(this);
    }

    private void addTag(String tag) {
        // queryByTags需要单引号，因此不允许标签有单引号
        if (tag.contains("'")) {
            Toast.makeText(this.getContext(), "标签不允许有单引号", Toast.LENGTH_SHORT).show();
            return;
        }

        TagAgent.addTagToPoem(tag, mPid);
        setPoemId(mPid);
    }

    private void removeTag(TagInfo info) {
        TagAgent.delTagFromPoem(mPid, info);

        Toast.makeText(getContext(),
                "删除: " + info.getName(),
                Toast.LENGTH_SHORT).show();

        setPoemId(mPid);
    }

    public void setPoemId(int pid) {
        mPid = pid;

        mTagList = TagAgent.getTagsInfo(pid);
        mController.setHasTag(!mTagList.isEmpty());

        List<String> tags = TagAgent.getTagsNoCount(mTagList);
        mPoemTags.setTags(tags);

        setAllTags();

        hideSoftInput();
    }

    private void hideSoftInput() {
        mEdit.clearFocus();
        imm.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
    }

    private void setAllTags() {
        mAllTagList = TagAgent.getAllTagInfos();
        mAllTags.setTags(TagAgent.getAllTagsHasCount());
    }

    @Override
    public void onClick(View v) {
        // 添加标签 按钮
        String tag = mEdit.getText().toString().trim();
        if ("".equals(tag)) {
            return;
        }
        addTag(tag);
        mEdit.setText("");

        // 关闭输入法
        hideSoftInput();
    }

    @Override
    public void onTagClick(int position, String text) {
        // 注意，两个tag窗口共用一个接口
        // 这是下面的，所有标签
        String tag = mAllTagList.get(position).getName();
        addTag(tag);
    }

    @Override
    public void onTagLongClick(int position, String text) {

    }

    @Override
    public void onSelectedTagDrag(int i, String s) {

    }

    @Override
    public void onTagCrossClick(int position) {
        // 注意，两个tag窗口共用一个接口
        // 这是上面的，当前标签
        final TagInfo info = mTagList.get(position);

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getContext());
        builder.setTitle("确认删除: " + info.getName() + "?");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TagView.this.removeTag(info);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    public void setPoemController(PoemController controller) {
        mController = controller;
    }
}
