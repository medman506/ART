package at.fhj.mad.art.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import at.fhj.mad.art.model.Task;

/**
 * Class to create the SQLite Database and have access to it.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    //Change if Changes to db structure are made
    //Updates background db structure
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_NAME = "tasks";
    private static final String KEY_UNIQUE_ID = "id";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_LINK = "link";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_YEAR = "year";
    private static final String KEY_MONTH = "month";
    private static final String KEY_DAY = "day";
    private static final String KEY_HOUR = "hour";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_SECOND = "second";
    private static final String KEY_TOPICS = "topics";
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" + KEY_UNIQUE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_MESSAGE + " TEXT, " + KEY_ADDRESS + " TEXT, " + KEY_TOPICS + " TEXT, " + KEY_LINK + " TEXT, " +
                    KEY_YEAR + " INTEGER, " + KEY_MONTH + " INTEGER, " + KEY_DAY + " INTEGER, " +
                    KEY_HOUR + " INTEGER, " + KEY_MINUTE + " INTEGER, " + KEY_SECOND + " INTEGER "
                    + ");";
    private static SQLiteHelper mInstance = null;

    private SQLiteHelper(Context context) {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
    }

    public static SQLiteHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new SQLiteHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        onCreate(db);
    }

    public long addTask(Task t) {
       /* String sql = "INSERT INTO " + TABLE_NAME + " (" + KEY_MESSAGE + "," + KEY_ADDRESS + "," + KEY_TOPICS + "," +
                KEY_LINK + "," + KEY_YEAR + "," + KEY_MONTH + "," + KEY_DAY + "," + KEY_HOUR + "," + KEY_MINUTE +
                "," + KEY_SECOND + ") VALUES ('" + t.getMessage() + "','" + t.getAddress() + "','" + t.getTopic() + "','" +
                t.getLink() + "'," + t.getYear() + "," + t.getMonth() + "," + t.getDay() + "," + t.getHour() + "," +
                t.getMinute() + "," + t.getSeconds() + ");";
        */
        ContentValues cv = new ContentValues();
        cv.put(KEY_MESSAGE, t.getMessage());
        cv.put(KEY_ADDRESS, t.getAddress());
        cv.put(KEY_TOPICS, t.getTopic());
        cv.put(KEY_LINK, t.getLink());
        cv.put(KEY_YEAR, t.getYear());
        cv.put(KEY_MONTH, t.getMonth());
        cv.put(KEY_DAY, t.getDay());
        cv.put(KEY_HOUR, t.getHour());
        cv.put(KEY_MINUTE, t.getMinute());
        cv.put(KEY_SECOND, t.getSeconds());

        return getWritableDatabase().insert(TABLE_NAME, null, cv);
    }

    public ArrayList<Task> readAllTasks() {
        String sql = "SELECT * FROM " + TABLE_NAME + ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        ArrayList<Task> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(cursor.getInt(0));
                task.setMessage(cursor.getString(1));
                task.setAddress(cursor.getString(2));
                task.setTopic(cursor.getString(3));
                task.setLink(cursor.getString(4));
                task.setYear(cursor.getInt(5));
                task.setMonth(cursor.getInt(6));
                task.setDay(cursor.getInt(7));
                task.setHour(cursor.getInt(8));
                task.setMinute(cursor.getInt(9));
                task.setSeconds(cursor.getInt(10));
                list.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean deleteTask(Task t) {
        boolean result = false;
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id=" + t.getId() + ";";
        getWritableDatabase().execSQL(sql);
        if (readIdAvailable(t.getId())) {
            result = true;
        }
        return result;
    }

    private boolean readIdAvailable(long id) {
        boolean result = false;
        String sql = "SELECT id FROM " + TABLE_NAME + " WHERE id=" + id + ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if (!cursor.moveToFirst()) {
            result = true;
        }
        cursor.close();
        return result;
    }

    public Task readId(long id) {

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id=" + id + ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToFirst();
        Task task = new Task(cursor.getInt(0));
        task.setMessage(cursor.getString(1));
        task.setAddress(cursor.getString(2));
        task.setTopic(cursor.getString(3));
        task.setLink(cursor.getString(4));
        task.setYear(cursor.getInt(5));
        task.setMonth(cursor.getInt(6));
        task.setDay(cursor.getInt(7));
        task.setHour(cursor.getInt(8));
        task.setMinute(cursor.getInt(9));
        task.setSeconds(cursor.getInt(10));
        cursor.close();

        return task;
    }

}
