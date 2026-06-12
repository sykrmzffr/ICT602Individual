package com.syakir.electricitycalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * History screen displaying all saved bill records in a ListView.
 * Each item shows the month and final cost. Clicking an item opens the detail screen.
 */
public class HistoryActivity extends AppCompatActivity {

    private ListView listViewHistory;
    private TextView tvEmptyHistory;
    private BillDao billDao;
    private List<BillRecord> billList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        setTitle(R.string.title_history);

        billDao = new BillDao(this);
        listViewHistory = findViewById(R.id.listViewHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        Button btnBack = findViewById(R.id.btnBackHistory);

        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Item click opens detail screen
        listViewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BillRecord record = billList.get(position);
                Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
                intent.putExtra("bill_id", record.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBills();
    }

    /**
     * Loads all bill records from the database and populates the ListView.
     */
    private void loadBills() {
        billList = billDao.getAllBills();

        if (billList.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            listViewHistory.setVisibility(View.GONE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            listViewHistory.setVisibility(View.VISIBLE);

            // Create display strings for each record
            List<String> displayList = new ArrayList<>();
            for (BillRecord record : billList) {
                String display = record.getMonth() + "  —  " +
                        String.format(Locale.getDefault(), "RM %.2f", record.getFinalCost());
                displayList.add(display);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    displayList
            );
            listViewHistory.setAdapter(adapter);
        }
    }
}
