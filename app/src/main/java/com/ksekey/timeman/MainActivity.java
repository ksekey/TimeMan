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
import android.view.View;
import android.widget.Toast;

import com.ksekey.timeman.adapters.ListAdapter;
import com.ksekey.timeman.models.TimeEntry;
import com.ksekey.timeman.models.Token;
import com.ksekey.timeman.network.NetworkHelper;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private RecyclerView list;
    private ListAdapter listAdapter;
    private NetworkHelper networkHelper;
    private LoadEntryListTask loadEntryListTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);
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
    protected void onResume() {
        super.onResume();
        if (loadEntryListTask == null){
            loadEntryListTask = new LoadEntryListTask();

            loadEntryListTask.execute();
        }
    }

    public void showProgress(Boolean progress) {
    }

    /**
     * Загрузка записей в фоне
     */
    public class LoadEntryListTask extends AsyncTask<Void, Void, List<TimeEntry>> {

        @Override
        protected List<TimeEntry> doInBackground(Void... params) {
            //пробуем залогиниться
            try {
                TokenHelper tokenHelper = new TokenHelper(MainActivity.this);
                Response<List<TimeEntry>> response = networkHelper.getApi().listTimeEntry("Bearer " + tokenHelper.loadToken().getAccess_token()).execute();
                if (response.isSuccessful()) {
                    return response.body();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return null;
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
