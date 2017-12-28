package com.ksekey.timeman;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.ksekey.timeman.database.DatabaseHelper;
import com.ksekey.timeman.models.Task;
import com.ksekey.timeman.models.TimeEntry;
import com.ksekey.timeman.models.User;
import com.ksekey.timeman.network.NetworkHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Response;

public class EditItemActivity extends AppCompatActivity {
    private EditText description;
    private NumberPicker timeInMinutes;
    private NumberPicker timeInHours;
    private Spinner spinner;
    private Button saveButton;
    private Button dateButton;

    private NetworkHelper networkHelper;
    private SaveTimeEntryOnServerTask saveTimeEntryOnServerTask;
    private LoadTasksListfromServerTask loadTaskfromServerTask;

    private Date date = new Date();
    private int timeMin;
    private int timeHour;

    private Task task;
    private String id;

    ArrayAdapter<Task> adapter;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_edit_item);
        networkHelper = NetworkHelper.getInstance();
        description = findViewById(R.id.edit_description);
        timeInMinutes = findViewById(R.id.time_in_minutes);
        timeInMinutes.setMinValue(0);
        timeInMinutes.setMaxValue(60);

        timeInHours = findViewById(R.id.time_in_hours);
        timeInHours.setMinValue(0);
        timeInHours.setMaxValue(24);

        timeInMinutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                timeMin = newVal;
            }
        });

        timeInHours.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                timeHour = newVal;
            }
        });

        saveButton = findViewById(R.id.save_button);
        dateButton = findViewById(R.id.date_button);

        // адаптер
        adapter = new ArrayAdapter<Task>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner = (Spinner) findViewById(R.id.edit_task);
        spinner.setAdapter(adapter);
        // заголовок
        spinner.setPrompt("Select task");

        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // показываем позиция нажатого элемента
                task = adapter.getItem(position);
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
                    timeEntry.setTimeInMinutes(timeHour * 60 + timeMin);
                    timeEntry.setDate(date);
                    timeEntry.setTask(task);
                    saveTimeEntryOnServerTask = new SaveTimeEntryOnServerTask(timeEntry);
                    saveTimeEntryOnServerTask.execute();
                }
            }
        });

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMounth = calendar.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(EditItemActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        calendar.set(year, month, dayOfMonth);
                        date = calendar.getTime();
                        refreshDateButton();
                    }
                }, year, month, dayOfMounth).show();
            }
        });

        id = getIntent().getStringExtra("id");


        loadTaskfromServerTask = new LoadTasksListfromServerTask();
        loadTaskfromServerTask.execute();

    }

    private void refreshDateButton() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMounth = calendar.get(Calendar.DAY_OF_MONTH);
        dateButton.setText(dayOfMounth + "." + month + "." + year);
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
                Response<User> userResponse = networkHelper.getApi().takeIdUser("Bearer " + tokenHelper.loadToken().getAccess_token()).execute();
                if (userResponse.isSuccessful()) {
                    User user = userResponse.body();
                    timeEntry.setUser(user);
                    if (id == null) {
                        Response<TimeEntry> response = networkHelper.getApi().createItem("Bearer " + tokenHelper.loadToken().getAccess_token(), timeEntry).execute();
                        if (response.isSuccessful()) {
                            TimeEntry timeEntry = response.body();
                            dbHelper.getTimeEntryDao().createOrUpdate(timeEntry);
                            return timeEntry;
                        }
                    }else {
                        Response<TimeEntry> response = networkHelper.getApi().editItem("Bearer " + tokenHelper.loadToken().getAccess_token(), id, timeEntry).execute();
                        if (response.isSuccessful()) {
                            TimeEntry timeEntry = response.body();
                            dbHelper.getTimeEntryDao().createOrUpdate(timeEntry);
                            return timeEntry;
                        }

                    }

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
        private TimeEntry timeEntry;

        @Override
        protected List<Task> doInBackground(Void... params) {
            //пробуем получить данные с сервера
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            try {
                if (id != null) {
                    timeEntry = dbHelper.getTimeEntryDao().queryForId(id);
                }

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

                if (timeEntry != null) {
                    description.setText(timeEntry.getDescription());
                    spinner.setSelection(adapter.getPosition(timeEntry.getTask()));
                    timeInMinutes.setValue(timeEntry.getTimeInMinutes()%60);
                    timeInHours.setValue(timeEntry.getTimeInMinutes()/60);
                    date = timeEntry.getDate();
                    refreshDateButton();
                }
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
