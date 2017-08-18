package animalize.github.com.quantangshi.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import animalize.github.com.quantangshi.Data.InfoItem;
import animalize.github.com.quantangshi.Data.RawPoem;
import animalize.github.com.quantangshi.Data.TagInfo;
import animalize.github.com.quantangshi.MyApplication;


public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "MyDatabaseHelper";

    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 4;

    private static final String ENCODING = "UTF-16LE";

    private static final String NAME_300 = "300首";

    // 静态变量
    private static MyDatabaseHelper mHelper;
    private static SQLiteDatabase mDb;
    private static int mPoemCount = -1;
    private static RawPoem mCachePoem;

    private MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static void init() {
        if (mHelper == null) {
            Context context = MyApplication.getContext();
            mHelper = new MyDatabaseHelper(context);

            // create or update
            String mQuantangshi = MyAssetsDatabaseHelper.getDBPath(context);

            // attach
            mDb = mHelper.getWritableDatabase();
            mDb.execSQL("ATTACH DATABASE '" +
                    mQuantangshi + "' AS 'tangshi';");
        }
    }

    // 得到一个必然的整数结果
    private static int getOneInt(String sql, String[] selectionArgs) {
        Cursor c = mDb.rawQuery(sql, selectionArgs);
        c.moveToFirst();
        int ret = c.getInt(0);
        c.close();

        return ret;
    }

    // ================== 诗 公有 ==================

    // 总共有多少首诗
    public static synchronized int getPoemCount() {
        if (mPoemCount != -1) {
            return mPoemCount;
        }

        init();

        String sql = "SELECT COUNT(*) FROM tangshi.poem";
        mPoemCount = getOneInt(sql, null);

        return mPoemCount;
    }

    // 随机一首
    public static synchronized RawPoem randomPoem() {
        init();

        int poemCount = MyDatabaseHelper.getPoemCount();
        Random rand = new Random();

        RawPoem p;
        // 数据库中192首诗的诗文没有内容，跳过这些诗
        do {
            int id = rand.nextInt(poemCount) + 1;
            p = MyDatabaseHelper.getPoemById(id);
        } while (p.getText().length() == 0);

        return p;
    }

    // 得到指定id的诗
    public static synchronized RawPoem getPoemById(int id) {
        if (mCachePoem != null && mCachePoem.getId() == id) {
            return mCachePoem;
        }

        init();

        String sql = "SELECT title,author,txt FROM tangshi.poem WHERE id=?";
        Cursor c = mDb.rawQuery(sql, new String[]{String.valueOf(id)});
        c.moveToFirst();
        RawPoem p;
        try {
            p = new RawPoem(
                    id,
                    new String(c.getBlob(0), ENCODING),
                    new String(c.getBlob(1), ENCODING),
                    new String(c.getBlob(2), ENCODING)
            );
        } catch (UnsupportedEncodingException e) {
            p = null;
        }
        c.close();

        mCachePoem = p;
        return p;
    }

    // ================== TAG 公有 ==================

    // 得到所有的tag list
    public static synchronized List<TagInfo> getAllTags() {
        init();

        String sql = "SELECT id, name, count " +
                "FROM tag " +
                "ORDER BY count DESC, id ASC";
        Cursor c = mDb.rawQuery(sql, null);

        List<TagInfo> l = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                TagInfo ti = new TagInfo(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2)
                );
                l.add(ti);
            } while (c.moveToNext());
        }
        c.close();

        return l;
    }

    // 得到一首诗的tag list
    public static synchronized List<TagInfo> getTagsByPoem(int pid) {
        init();

        String sql = "SELECT tag.id, tag.name, tag.count " +
                "FROM tag, tag_map " +
                "WHERE tag_map.pid=? AND tag_map.tid=tag.id";
        Cursor c = mDb.rawQuery(sql, new String[]{String.valueOf(pid)});

        List<TagInfo> l = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                TagInfo ti = new TagInfo(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2)
                );
                l.add(ti);
            } while (c.moveToNext());
        }
        c.close();

        return l;
    }

    // 给一首诗添加一个tag
    public static synchronized boolean addTagToPoem(String tag, int pid) {
        init();

        int tid = getTagID(tag);

        if (tid != -1) {
            // 已在tag表
            if (poemHasTagID(pid, tid)) {
                // 诗已存在此tag
                return false;
            } else {
                mDb.execSQL("BEGIN");

                // 添加到tag_map
                addToTagMap(pid, tid);
                // 更新tag表计数
                updateTagCount(tid);

                mDb.execSQL("COMMIT");
            }
        } else {
            // 没在tag表
            mDb.execSQL("BEGIN");

            tid = MyDatabaseHelper.addTag(tag);
            MyDatabaseHelper.addToTagMap(pid, tid);

            mDb.execSQL("COMMIT");
        }

        return true;
    }

    // 从一首诗删除一个tag
    public static synchronized boolean delTagFromPoem(int pid, TagInfo info) {
        init();

        if (!poemHasTagID(pid, info.getId())) {
            // 没有
            return false;
        }

        mDb.execSQL("BEGIN");

        // 从tag_map表删除
        delFromTagMap(pid, info.getId());
        // 更新tag表计数
        updateTagCount(info.getId());
        delZeroCountTag();

        mDb.execSQL("COMMIT");

        return true;
    }

    // 用tag列表搜索
    public static synchronized ArrayList<InfoItem> queryByTags(List<String> tags) {
        init();

        ArrayList<InfoItem> l = new ArrayList<>();

        int max = tags.size() - 1;
        if (max == -1) {
            return l;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; ; i++) {
            sb.append('\'');
            sb.append(tags.get(i));
            sb.append('\'');
            if (i == max) {
                break;
            }
            sb.append(',');
        }

        String sql = "SELECT p.id, p.title, p.author " +
                "FROM tangshi.poem p " +
                "INNER JOIN tag_map tm " +
                "ON p.id = tm.pid " +
                "INNER JOIN tag t " +
                "ON tm.tid = t.id " +
                "WHERE t.name in (" + sb + ") " +
                "GROUP BY p.id " +
                "HAVING COUNT(*) = " + tags.size() + " " +
                "ORDER BY tm.id";

        Cursor c = mDb.rawQuery(sql, null);
        try {
            if (c.moveToFirst()) {
                do {
                    InfoItem ri = new InfoItem(
                            c.getInt(0),
                            new String(c.getBlob(1), ENCODING),
                            new String(c.getBlob(2), ENCODING)
                    );
                    l.add(ri);
                } while (c.moveToNext());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        c.close();

        return l;
    }

    // 是否存在tag
    public static synchronized boolean hasTag(String tag) {
        return getTagID(tag) != -1;
    }

    // 整体，改名/合并标签
    public static void renameTag(String o, String n) {
        int ntid, otid;
        String sql;

        // 得到新tag id
        ntid = getTagID(n);

        if (ntid == -1) { // 新标签不存在，仅改名
            sql = "UPDATE tag SET name=? WHERE name=?";
            mDb.execSQL(sql, new String[]{n, o});
        } else { // 新标签存在，合并
            mDb.execSQL("BEGIN");

            // 得到旧tag id
            otid = getTagID(o);

            // 得到所有旧的pid list
            sql = "SELECT pid FROM tag_map WHERE tid=?";
            Cursor c = mDb.rawQuery(sql, new String[]{String.valueOf(otid)});

            ArrayList<Integer> l = new ArrayList<>();
            if (c.moveToFirst()) {
                do {
                    l.add(c.getInt(0));
                } while (c.moveToNext());
            }
            c.close();

            // 添加到新的
            sql = "UPDATE tag_map SET tid=? WHERE " +
                    "(pid=? AND tid=? AND NOT EXISTS(" +
                    "SELECT * FROM tag_map WHERE pid=? AND tid=?" +
                    "))";
            for (int pid : l) {
                mDb.execSQL(sql, new String[]{String.valueOf(ntid),
                        String.valueOf(pid), String.valueOf(otid),
                        String.valueOf(pid), String.valueOf(ntid)});
            }

            // 删除旧的tag
            sql = "DELETE FROM tag_map WHERE tid=?";
            mDb.execSQL(sql, new String[]{String.valueOf(otid)});

            sql = "DELETE FROM tag WHERE id=?";
            mDb.execSQL(sql, new String[]{String.valueOf(otid)});

            // 更新count
            updateTagCount(ntid);

            mDb.execSQL("COMMIT");
        }
    }

    // 整体，删除一个标签
    public static void delTag(String tag) {
        init();

        mDb.execSQL("BEGIN");

        // 从tag map删除
        String sql = "DELETE FROM tag_map " +
                "WHERE tid = (SELECT id " +
                "FROM tag " +
                "WHERE name=?)";
        mDb.execSQL(sql, new String[]{tag});

        // 从tag删除
        sql = "DELETE FROM tag WHERE name=?";
        mDb.execSQL(sql, new String[]{tag});

        mDb.execSQL("COMMIT");
    }

    // ================== TAG 私有 ==================

    // 返回tag id，-1为没有
    private static int getTagID(String tag) {
        String sql = "SELECT id FROM tag WHERE name=?";
        Cursor c = mDb.rawQuery(sql, new String[]{tag});

        int ret;
        if (c.moveToFirst()) {
            ret = c.getInt(0);
        } else {
            ret = -1;
        }
        c.close();

        return ret;
    }

    // 诗是否有tag id
    private static boolean poemHasTagID(int pid, int tid) {
        String sql = "SELECT * FROM tag_map WHERE pid=? AND tid=?";
        Cursor c = mDb.rawQuery(sql,
                new String[]{String.valueOf(pid), String.valueOf(tid)}
        );
        if (!c.moveToFirst()) {
            c.close();
            return false;
        }

        c.close();
        return true;
    }

    // 添加到tag表，设count初始值为1，返回tag id
    private static int addTag(String tag) {
        ContentValues cv = new ContentValues();
        cv.put("name", tag);
        cv.put("count", 1);

        return (int) mDb.insert("tag", null, cv);
    }

    // 添加到tag_map
    private static int addToTagMap(int pid, int tid) {
        ContentValues cv = new ContentValues();
        cv.put("pid", pid);
        cv.put("tid", tid);

        return (int) mDb.insert("tag_map", null, cv);
    }

    // 从tag_map删除
    private static void delFromTagMap(int pid, int tid) {
        mDb.delete("tag_map",
                "pid=? AND tid=?",
                new String[]{String.valueOf(pid), String.valueOf(tid)});
    }

    // 更新tag count
    private static void updateTagCount(int tid) {
        String temp = String.valueOf(tid);

        // 重新计数
        String sql = "UPDATE tag SET count=(" +
                "SELECT COUNT(*) FROM tag_map WHERE tid=?) " +
                "WHERE id=?";
        mDb.execSQL(sql, new String[]{temp, temp});
    }

    // 删除count为0的tag
    private static void delZeroCountTag() {
        String sql = "DELETE FROM tag WHERE count<=0";
        mDb.execSQL(sql);
    }

    // ================== recent 公有 ==================

    // 得到最近列表
    public static synchronized ArrayList<InfoItem> getRecentList() {
        init();

        String sql = "SELECT pid, title, author " +
                "FROM recent " +
                "ORDER BY id";
        Cursor c = mDb.rawQuery(sql, null);

        ArrayList<InfoItem> l = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                InfoItem ri = new InfoItem(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2)
                );
                l.add(ri);
            } while (c.moveToNext());
        }
        c.close();

        return l;
    }

    // 添加到最近列表
    public static synchronized void addToRecentList(InfoItem info, int limit) {
        init();

        mDb.execSQL("BEGIN");

        // 已有的话，先删
        mDb.delete("recent", "pid=?", new String[]{String.valueOf(info.getId())});

        // add
        ContentValues cv = new ContentValues();
        cv.put("pid", info.getId());
        cv.put("title", info.getTitle());
        cv.put("author", info.getAuthor());
        cv.put("time", (int) (System.currentTimeMillis() / 1000));
        mDb.insert("recent", null, cv);

        // del old
        String sql = "DELETE FROM recent " +
                "WHERE id IN (" +
                "SELECT id FROM recent ORDER BY id DESC LIMIT ? OFFSET ?)";
        String temp = String.valueOf(limit);
        mDb.execSQL(sql, new String[]{temp, temp});

        mDb.execSQL("COMMIT");
    }

    // 得到邻近的
    public static synchronized ArrayList<InfoItem> getNeighbourList(int id,
                                                                    int window) {
        init();

        window = window / 2;
        final int left = id - window;
        final int right = id + window;

        String sql = "SELECT id,title,author FROM tangshi.poem " +
                "WHERE ? <= id AND id <= ? " +
                "ORDER BY id";
        Cursor c = mDb.rawQuery(sql, new String[]{
                String.valueOf(left),
                String.valueOf(right)});

        ArrayList<InfoItem> l = new ArrayList<>();
        if (c.moveToFirst()) do {
            InfoItem ri;
            try {
                ri = new InfoItem(
                        c.getInt(0),
                        new String(c.getBlob(1), ENCODING),
                        new String(c.getBlob(2), ENCODING)
                );
                l.add(ri);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } while (c.moveToNext());
        c.close();

        return l;
    }

    // vacuum
    public static synchronized void vacuum() {
        init();
        mDb.execSQL("VACUUM");
    }

    // 备份数据库
    public static synchronized void backup(File target) {
        init();

        // VACUUM
        String sql = "VACUUM";
        mDb.execSQL(sql);

        // 关闭
        mHelper.close();
        mHelper = null;

        // 复制文件
        File dbFile = MyApplication
                .getContext()
                .getDatabasePath(DATABASE_NAME);

        copyFile(dbFile, target);

        // 重新打开
        init();
    }

    // 还原数据库
    public static synchronized void restore(File source) {
        init();

        // 关闭
        mHelper.close();
        mHelper = null;

        // 复制文件
        File dbFile = MyApplication
                .getContext()
                .getDatabasePath(DATABASE_NAME);

        copyFile(source, dbFile);

        // 重新打开
        init();
    }

    // 生成唐诗300首tag
    private static synchronized void tangshi300(boolean clean) {

        class TaskRunnable implements Runnable {
            boolean clean;

            TaskRunnable(boolean clean) {
                this.clean = clean;
            }

            @Override
            public void run() {
                init();

                mDb.execSQL("BEGIN");

                if (clean) {
                    // 从tag map删除
                    String sql = "DELETE FROM tag_map " +
                            "WHERE tid = (SELECT id " +
                            "FROM tag " +
                            "WHERE name=?)";
                    mDb.execSQL(sql, new String[]{NAME_300});
                }

                // 得到tid
                int tid = getTagID(NAME_300);
                if (tid == -1) {
                    tid = addTag(NAME_300);
                }

                // 添加关系
                for (int id : TagData.tangshi300) {
                    if (!poemHasTagID(id, tid)) {
                        addToTagMap(id, tid);
                    }
                }

                // 更新计数
                updateTagCount(tid);

                mDb.execSQL("COMMIT");
            }
        }

        Thread t = new Thread(new TaskRunnable(clean));
        t.start();
    }

    public static synchronized void installTags(boolean clean) {
        tangshi300(clean);
    }

    public static synchronized int getDBSize() {
        File dbFile = MyApplication
                .getContext()
                .getDatabasePath(DATABASE_NAME);

        try {
            int size;
            FileInputStream fis = new FileInputStream(dbFile);
            size = fis.available();
            fis.close();
            return size;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static void copyFile(File src, File dst) {
        InputStream in;
        try {
            if (!dst.exists()) {
                dst.createNewFile();
            }

            in = new FileInputStream(src);

            OutputStream out = new FileOutputStream(dst);

            byte[] buf = new byte[2048];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // tag表
        String sql = "CREATE TABLE tag (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "count INTEGER);";
        db.execSQL(sql);

        sql = "CREATE INDEX tname_idx ON tag(name);";
        db.execSQL(sql);

        sql = "CREATE INDEX tcount_idx ON tag(count);";
        db.execSQL(sql);

        // tag_map表
        sql = "CREATE TABLE tag_map (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "pid INTEGER," +
                "tid INTEGER);";
        db.execSQL(sql);

        sql = "CREATE INDEX pid_idx ON tag_map(pid);";
        db.execSQL(sql);

        sql = "CREATE INDEX tid_idx ON tag_map(tid);";
        db.execSQL(sql);

        // recent表, add in db ver 2
        sql = "CREATE TABLE recent (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pid INTEGER, " +
                "title TEXT, " +
                "author TEXT, " +
                "time INTEGER);";
        db.execSQL(sql);

        sql = "CREATE INDEX recent_pid_idx ON recent(pid);";
        db.execSQL(sql);

        mDb = db;
        // 唐诗300首
        tangshi300(false);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql;

        if (oldVersion < 2) {
            // recent表
            sql = "CREATE TABLE recent (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "pid INTEGER, " +
                    "title TEXT, " +
                    "author TEXT, " +
                    "time INTEGER);";
            db.execSQL(sql);
        }

        if (oldVersion < 3) {
            sql = "CREATE INDEX recent_pid_idx ON recent(pid);";
            db.execSQL(sql);
        }

        mDb = db;
        if (oldVersion < 4) {
            // 唐诗300首
            tangshi300(false);
        }
    }
}
