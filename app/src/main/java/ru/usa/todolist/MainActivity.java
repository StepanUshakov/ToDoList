package ru.usa.todolist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView allTasksList; // Список всех задач
    EditText listTitle;
    SharedPreferences pref;
    String listTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        allTasksList = (ListView) findViewById(R.id.taskList);
        listTitle = (EditText) findViewById(R.id.listTitle);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        listTitleText = pref.getString("listTitleText", "");
        listTitle.setText(listTitleText);

        listTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SharedPreferences.Editor editPref = pref.edit();
                editPref.putString("listTitleText", String.valueOf(listTitle.getText()));
                editPref.apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        DBAsyncTask dbAsyncTask = new DBAsyncTask(null, null);
        dbAsyncTask.execute(); // Получение сформированного списка задач из отдельного потока
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        Drawable icon = menu.getItem(0).getIcon();
        icon.mutate();
        icon.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        if(menuItem.getItemId() == R.id.new_task){
            final EditText userTaskTitle = new EditText(this);
            userTaskTitle.setHint(R.string.alert_dialog_user_task_title);
            AlertDialog alert = new AlertDialog.Builder(this)
                    .setTitle(R.string.alert_dialog_title)
                    .setMessage(R.string.alert_dialog_message)
                    .setView(userTaskTitle)
                    .setPositiveButton(R.string.alert_dialog_positive_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String task = String.valueOf(userTaskTitle.getText());
                            DBAsyncTask dbAsyncTask = new DBAsyncTask(task, null); // Создание потока с передачей данных на запись
                            dbAsyncTask.execute(); // Получение сформированного списка задач из отдельного потока
                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_negative_button, null)
                    .create();
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void deleteTask(View view){
        ImageButton thisButton = (ImageButton)view;
        String idThisTask = String.valueOf(thisButton.getTag());
        DBAsyncTask dbAsyncTask = new DBAsyncTask(null, idThisTask); // Создание потока с передачей данных на удаление
        dbAsyncTask.execute(); // Передача данные на удаление из БД и получение сформированного списка задач из отдельного потока
    }

    class DBAsyncTask extends AsyncTask<String, Void, View> {
        DB db = new DB(MainActivity.this);
        ArrayAdapter<String> lists;
        ArrayList<AllTasks> taskListRows;
        private String dataInsert; // Данные для записи
        private String dataDelete; // Данные для удаления

        DBAsyncTask(String dataInsert, String dataDelete){
            this.dataInsert = dataInsert;
            this.dataDelete = dataDelete;
        }

        // класс для хранения отдельной задачи
        class AllTasks {
            public final String id;
            public final String title;

            public AllTasks(String id, String title) {
                this.id = id;
                this.title = title;
            }
        }

        // Адаптер для построения собственного списка ListView
        class AllTasksAdapter extends ArrayAdapter<AllTasks> {
            public AllTasksAdapter(Context context) {
                super(context, R.layout.list, taskListRows);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                AllTasks allTasks = getItem(position);

                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.list, null);
                }
                ((ImageButton) convertView.findViewById(R.id.btn_delete))
                        .setTag(allTasks.id); // Добавление тега к кнопке с id записи
                ((TextView) convertView.findViewById(R.id.task_title))
                        .setText(allTasks.title);
                return convertView;
            }
        }

        // Формирование списка всех записей для вывода
        private void getAllTasks(){
            ArrayList<String[]> taskList = db.getTasks();
            taskListRows = new ArrayList<>();

            for (int i = 0; i < taskList.size(); i++) {
                taskListRows.add(new AllTasks(taskList.get(i)[0], taskList.get(i)[1]));
            }
        }

        @Override
        protected View doInBackground(String... params) {
            if(dataInsert != null){
                db.insertRow(dataInsert);
                dataInsert = null;
            }

            if(dataDelete != null){
                db.deleteRow(dataDelete);
                dataDelete = null;
            }

            getAllTasks();
            return null;
        }

        @Override
        protected void onPostExecute(View view) {
            super.onPostExecute(view);
            if(lists == null) {
                // Вывод сформированного списка в MainActivity
                ArrayAdapter<AllTasks> adapter = new AllTasksAdapter(MainActivity.this);
                allTasksList.setAdapter(adapter);
            }
        }
    }
}