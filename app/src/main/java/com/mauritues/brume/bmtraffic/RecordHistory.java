package com.mauritues.brume.bmtraffic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mauritues.brume.bmtraffic.serviceproviders.LocationRecordingServiceProvider;

public class RecordHistory extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        String locale = Utils.getLang(newBase);
        Context ctx = Utils.changeLocale(newBase, locale);
        super.attachBaseContext(ctx);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_history);
        try {
            if (getSupportActionBar() != null)
                getSupportActionBar().hide();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        LocationRecordingServiceProvider provider = new LocationRecordingServiceProvider(FirebaseFirestore.getInstance());
        provider.retrieveAllRecordings();
        findViewById(R.id.menuButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(RecordHistory.this, v);
                menu.getMenuInflater().inflate(R.menu.main_map, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.logOut) {
                            SharedPreferences prefs = getSharedPreferences("traffic", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putBoolean("login", false);
                            edit.apply();
                            startActivity(new Intent(RecordHistory.this, MainActivity.class));
                            finish();
                            return true;
                        } else if (item.getItemId() == R.id.nav_home) {
                            onBackPressed();
                            finish();
                            return true;
                        } else
                            return false;
                    }
                });
                menu.show();
            }
        });



        CustomListAdapter adapter = new CustomListAdapter(SessionVariables.storedRecordings);
        RecyclerView listView = findViewById(R.id.listView);
        listView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        listView.setHasFixedSize(true);
        listView.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setOverScrollMode(RecyclerView.OVER_SCROLL_IF_CONTENT_SCROLLS);
        listView.setAdapter(adapter);
    }

//    private List<LocationRecording> generateLocationRecording() {
//        LocationRecording locationRecording1 = new LocationRecording(
//                40.0,
//                2.0,
//                "35," +
//                        " Authority Avenue Ikotun Nigeria",
//                47.0,
//                2.4,
//                "77, Ikeja Lagos Nigeria",
//                "fun"
//        );
//
//        LocationRecording locationRecording2 = new LocationRecording(
//                42.0,
//                2.0,
//                "15, Obafemi Awolowo Way VI",
//                45.0,
//                2.4,
//                "43, Admirality Plaza Lekki",
//                "fun"
//        );
//
//        LocationRecording locationRecording3 = new LocationRecording(
//                42.0,
//                2.0,
//                "15, Obafemi Awolowo Way VI",
//                45.0,
//                2.4,
//                "43, Admirality Plaza Lekki",
//                "fun"
//        );
//
//        LocationRecording locationRecording4 = new LocationRecording(
//                40.0,
//                2.0,
//                "35, Authority Avenue Ikotun Nigeria",
//                47.0,
//                2.4,
//                "77, Ikeja Lagos Nigeria",
//                "fun"
//        );
//
//        List<LocationRecording> locations = new ArrayList<>();
//        locations.add(locationRecording1);
//        locations.add(locationRecording2);
//        locations.add(locationRecording3);
//        locations.add(locationRecording4);
//
//        return locations;
//    }
}
