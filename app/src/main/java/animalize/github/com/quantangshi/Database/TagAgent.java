package animalize.github.com.quantangshi.Database;

import java.util.ArrayList;
import java.util.List;

import animalize.github.com.quantangshi.Data.TagInfo;

/**
 * Created by anima on 17-3-17.
 */

public class TagAgent {

    private static List<TagInfo> allTags;
    private static List<String> allHasCount;

    // 得到所有tags
    public static synchronized List<TagInfo> getAllTagInfos() {
        if (allTags == null) {
            allTags = MyDatabaseHelper.getAllTags();
        }

        return allTags;
    }

    // 无效所有tags
    public static synchronized void invalideTags() {
        allTags = null;
        allHasCount = null;
    }

    // 所有，有计数字符串
    public static List<String> getAllTagsHasCount() {
        if (allHasCount == null) {
            List<TagInfo> list = getAllTagInfos();

            allHasCount = new ArrayList<>();
            for (TagInfo info : list) {
                String s = info.getName();
                if (info.getCount() != 1) {
                    s += "(" + info.getCount() + ")";
                }
                allHasCount.add(s);
            }
        }

        return allHasCount;
    }

    // 得到 无计数字符串
    public static List<String> getTagsNoCount(List<TagInfo> list) {
        List<String> tags = new ArrayList<>();
        for (TagInfo info : list) {
            String s = info.getName();
            tags.add(s);
        }
        return tags;
    }

    // 得到一首诗的tags
    public static synchronized List<TagInfo> getTagsInfo(int pid) {
        return MyDatabaseHelper.getTagsByPoem(pid);
    }

    // 给诗添加tag
    public static synchronized boolean addTagToPoem(String tag, int pid) {
        boolean r = MyDatabaseHelper.addTagToPoem(tag, pid);
        if (r) {
            invalideTags();
        }

        return r;
    }

    // 从诗删tag
    public static synchronized boolean delTagFromPoem(int pid, TagInfo info) {
        boolean r = MyDatabaseHelper.delTagFromPoem(pid, info);
        if (r) {
            invalideTags();
        }

        return r;
    }

    // 是否存在tag
    public static synchronized boolean hasTag(String tag) {
        return MyDatabaseHelper.hasTag(tag);
    }

    // 整体，改名、合并
    // 即使old不存在了，目前的逻辑也无妨
    public static synchronized boolean renameTag(String o, String n) {
        if (o.equals(n)) {
            return false;
        }

        MyDatabaseHelper.renameTag(o, n);
        invalideTags();

        return true;
    }

    // 整体，删
    public static synchronized void delTag(String tag) {
        MyDatabaseHelper.delTag(tag);
        invalideTags();
    }

    // 生成预置标签
    public static synchronized void installTags(boolean clean) {
        // 此函数有线程，会在内部执行 TagAgent.invalideTags();
        MyDatabaseHelper.installTags(clean);
    }
}
