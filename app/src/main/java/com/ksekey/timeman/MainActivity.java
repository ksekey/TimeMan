package com.ksekey.timeman;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.j256.ormlite.field.DatabaseField;
import com.ksekey.timeman.adapters.ListAdapter;
import com.ksekey.timeman.database.DatabaseHelper;
import com.ksekey.timeman.models.TimeEntry;
import com.ksekey.timeman.models.Token;
import com.ksekey.timeman.network.NetworkHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ListAdapter.OnClickItem {
    private RecyclerView list;
    private ListAdapter listAdapter;
    private NetworkHelper networkHelper;
    private LoadEntryListTask loadEntryListTask;
    private FloatingActionButton addItemButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);
        networkHelper = NetworkHelper.getInstance();
        addItemButton = findViewById(R.id.addItem);

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
                startActivity(intent);
            }
        });
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

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search:
                 Intent intent = new Intent(this, SearchActivity.class);
                 startActivity(intent);
                 break;
            case R.id.logout:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (loadEntryListTask == null) {
            loadEntryListTask = new LoadEntryListTask();

            loadEntryListTask.execute();
        }
    }

    public void showProgress(Boolean progress) {
    }

    @Override
    public void onClick(TimeEntry timeEntry) {
        Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
        intent.putExtra("id",timeEntry.getId());
        startActivity(intent);
    }

    /**
     * Загрузка записей в фоне
     */
    public class LoadEntryListTask extends AsyncTask<Void, Void, List<TimeEntry>> {
        private static final String TAG = "LoadEntryListTask";

        @Override
        protected List<TimeEntry> doInBackground(Void... params) {
            //пробуем получить данные с сервера
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            try {
                TokenHelper tokenHelper = new TokenHelper(MainActivity.this);
                Response<List<TimeEntry>> response = networkHelper.getApi().listTimeEntry("Bearer " + tokenHelper.loadToken().getAccess_token()).execute();
                if (response.isSuccessful()) {
                    List<TimeEntry> timeEntries = response.body();
                    for (int i = 0; i < timeEntries.size(); i++) {
                        dbHelper.getTimeEntryDao().createOrUpdate(timeEntries.get(i));
                    }
                    return timeEntries;
                }
            } catch (IOException | SQLException e) {
                Log.w(TAG, "doInBackground: ", e);
            }
            //если дошли до этого момента, значит что-то не так с интернетом, загружаем данные из бд
            try {
                return dbHelper.getTimeEntryDao().queryForAll();
            } catch (SQLException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final List<TimeEntry> list) {
            loadEntryListTask = null;
            showProgress(false);
            listAdapter.setDataset(list);
        }

        @Override
        protected void onCancelled() {
            loadEntryListTask = null;
            showProgress(false);
        }


    }

}
