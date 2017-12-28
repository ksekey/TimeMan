package com.ksekey.timeman;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ksekey.timeman.adapters.ListAdapter;
import com.ksekey.timeman.database.DatabaseHelper;
import com.ksekey.timeman.models.Task;
import com.ksekey.timeman.models.TimeEntry;
import com.ksekey.timeman.network.NetworkHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Response;

public class SearchActivity extends AppCompatActivity implements ListAdapter.OnClickItem {

    private RecyclerView list;
    private NetworkHelper networkHelper;
    private ListAdapter listAdapter;
    private Button startButton;
    private Button finishButton;
    private EditText searchWord;

    private Date start = new Date();
    private Date finish = new Date();
    private String word = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_search);
        networkHelper = NetworkHelper.getInstance();

        list = findViewById(R.id.list);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        list.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        listAdapter = new ListAdapter();
        listAdapter.setOnClickItem(this);
        list.setAdapter(listAdapter);

        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchListTask searchListTask = new SearchListTask();
                searchListTask.execute();
            }
        });

        startButton = findViewById(R.id.search_date_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(start);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMounth = calendar.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(SearchActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(start);
                        calendar.set(year, month, dayOfMonth);
                        start = calendar.getTime();
                        refreshStart();
                    }
                }, year, month, dayOfMounth).show();
            }
        });

        finishButton = findViewById(R.id.search_date_finish);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(finish);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMounth = calendar.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(SearchActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(finish);
                        calendar.set(year, month, dayOfMonth);
                        finish = calendar.getTime();
                        refreshFinish();
                    }
                }, year, month, dayOfMounth).show();
            }
        });

        refreshStart();
        refreshFinish();

        searchWord = findViewById(R.id.search_word);
    }

    private void refreshStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMounth = calendar.get(Calendar.DAY_OF_MONTH);
        startButton.setText(dayOfMounth + "." + month + "." + year);
    }

    private void refreshFinish() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(finish);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMounth = calendar.get(Calendar.DAY_OF_MONTH);
        finishButton.setText(dayOfMounth + "." + month + "." + year);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SearchListTask searchListTask = new SearchListTask();
        searchListTask.execute();
    }

    @Override
    public void onClick(TimeEntry timeEntry) {
        Intent intent = new Intent(this, EditItemActivity.class);
        intent.putExtra("id",timeEntry.getId());
        startActivity(intent);
    }

    public class SearchListTask extends AsyncTask<Void, Void, List<TimeEntry>> {
        private static final String TAG = "LoadEntryListTask";

        @Override
        protected List<TimeEntry> doInBackground(Void... params) {
            //пробуем получить данные с сервера
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            try {

                QueryBuilder<Task, String> taskQueryBuilder = dbHelper.getTaskDao().queryBuilder();
                taskQueryBuilder.where().like("name", "%" + searchWord.getText().toString()+"%");

                QueryBuilder<TimeEntry, String> queryBuilder = dbHelper.getTimeEntryDao().queryBuilder();

                List<TimeEntry> list1 = queryBuilder
                        .where()
                        .like("description","%" + searchWord.getText().toString()+"%")
                        .and()
                        .between("date",start, finish)
                        .query();



               return list1;
            } catch (SQLException e) {
                Log.w(TAG, "doInBackground: ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final List<TimeEntry> list) {
            if (list == null) {
                Toast.makeText(SearchActivity.this, "Error", Toast.LENGTH_SHORT).show();
            } else {
                listAdapter.setDataset(list);
            }
        }

        @Override
        protected void onCancelled() {

        }


    }
}
