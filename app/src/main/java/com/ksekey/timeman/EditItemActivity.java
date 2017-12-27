package com.ksekey.timeman;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.ksekey.timeman.database.DatabaseHelper;
import com.ksekey.timeman.models.Task;
import com.ksekey.timeman.models.TimeEntry;
import com.ksekey.timeman.network.NetworkHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import retrofit2.Response;

public class EditItemActivity extends AppCompatActivity {
    private Spinner editTask;
    private EditText description;
    private NumberPicker timeInMinutes;
    private Button saveButton;

    private NetworkHelper networkHelper;
    private SaveTimeEntryOnServerTask saveTimeEntryOnServerTask;
    private LoadTasksListfromServerTask loadTaskfromServerTask;

    ArrayAdapter<Task> adapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_edit_item);
        networkHelper = NetworkHelper.getInstance();
        editTask = findViewById(R.id.edit_task);
        description = findViewById(R.id.edit_description);
        timeInMinutes = findViewById(R.id.time_in_minutes);
        saveButton = findViewById(R.id.save_button);

        // адаптер
        adapter = new ArrayAdapter<Task>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.edit_task);
        spinner.setAdapter(adapter);
        // заголовок
        spinner.setPrompt("Select task");

        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // показываем позиция нажатого элемента
                Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                if (saveTimeEntryOnServerTask == null) {
                    TimeEntry timeEntry = new TimeEntry();
                    String db_description = description.getText().toString();
                    timeEntry.setDescription(db_description);
                    Integer db_timeInMinutes = timeInMinutes.getValue();
                    timeEntry.setTimeInMinutes(db_timeInMinutes);
                    saveTimeEntryOnServerTask = new SaveTimeEntryOnServerTask(timeEntry);
                    saveTimeEntryOnServerTask.execute();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loadTaskfromServerTask == null) {
            loadTaskfromServerTask = new LoadTasksListfromServerTask();
            loadTaskfromServerTask.execute();
        }
    }

    /**
     *
     */
    public class SaveTimeEntryOnServerTask extends AsyncTask<Void, Void, TimeEntry> {
        private static final String TAG = "LoadEntryListTask";
        private final TimeEntry timeEntry;

        public SaveTimeEntryOnServerTask(TimeEntry timeEntry) {
            this.timeEntry = timeEntry;
        }

        @Override
        protected TimeEntry doInBackground(Void... params) {
            //пробуем получить данные с сервера
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            try {
                TokenHelper tokenHelper = new TokenHelper(EditItemActivity.this);
                Response<TimeEntry> response = networkHelper.getApi().createItem("Bearer " + tokenHelper.loadToken().getAccess_token(), timeEntry).execute();
                if (response.isSuccessful()) {
                    TimeEntry timeEntry = response.body();
                    dbHelper.getTimeEntryDao().createOrUpdate(timeEntry);
                    return timeEntry;
                }
            } catch (IOException | SQLException e) {
                Log.w(TAG, "doInBackground: ", e);
            }
            //если дошли до этого момента, значит что-то не так с интернетом, загружаем данные из бд
            return null;
        }

        @Override
        protected void onPostExecute(TimeEntry timeEntry) {
            saveTimeEntryOnServerTask = null;
            if (timeEntry != null) {
                finish();
            } else {
                Toast.makeText(EditItemActivity.this, "Произошла ошибка!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            saveTimeEntryOnServerTask = null;
        }
    }

    public class LoadTasksListfromServerTask extends AsyncTask<Void, Void, List<Task>> {
        private static final String TAG = "LoadTaskfromServerTask";

        @Override
        protected List<Task> doInBackground(Void... params) {
            //пробуем получить данные с сервера
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            try {
                TokenHelper tokenHelper = new TokenHelper(EditItemActivity.this);
                Response<List<Task>> response = networkHelper.getApi().takeTaskNameList("Bearer " + tokenHelper.loadToken().getAccess_token()).execute();
                if (response.isSuccessful()) {
                    List<Task> tasks = response.body();
                    for (int i = 0; i < tasks.size(); i++) {
                        dbHelper.getTaskDao().createOrUpdate(tasks.get(i));
                    }
                    return tasks;
                }
            } catch (IOException | SQLException e) {
                Log.w(TAG, "doInBackground: ", e);
            }
            //если дошли до этого момента, значит что-то не так с интернетом, загружаем данные из бд
            try {
                return dbHelper.getTaskDao().queryForAll();
            } catch (SQLException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Task> taskList) {
            loadTaskfromServerTask = null;
            if (taskList != null) {
                adapter.clear();
                adapter.addAll(taskList);
            } else {
                Toast.makeText(EditItemActivity.this, "Произошла ошибка!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            loadTaskfromServerTask = null;
        }
    }
}
