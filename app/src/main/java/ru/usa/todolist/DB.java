package ru.usa.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DB extends SQLiteOpenHelper {

    private static final String DB_NAME = "todolist";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "b_task_list";
    private static final String COL_ID = "ID";
    private static final String COL_NAME = "task_name";

    public DB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL);", TABLE_NAME, COL_ID, COL_NAME );
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*String query = String.format(
                "DELETE TABLE IF EXISTS %s", TABLE_NAME);
        db.execSQL(query);
        onCreate(db);*/
    }

    public void insertRow(String task){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(COL_NAME, task);
        db.insertWithOnConflict(
                TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
    }

    //Удаление записей
    public void deleteRow(String idTask){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{idTask}); // Удаляем записи по ID
        db.close();
    }

    //Получение всех записей
    public ArrayList<String[]> getTasks(){
        ArrayList<String[]> allTasks = new ArrayList<>(); // Расширяемый массив для хранения всех записей
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()){
            int idIndex = cursor.getColumnIndex(COL_ID);
            int nameIndex = cursor.getColumnIndex(COL_NAME);
            String[] itemTasks = new String[2]; // Отдельный массив для хранения ID и названия задачи
            itemTasks[0] = cursor.getString(idIndex);
            itemTasks[1] = cursor.getString(nameIndex);
            allTasks.add(itemTasks);
        }

        cursor.close();
        db.close();
        return allTasks;
    }
}