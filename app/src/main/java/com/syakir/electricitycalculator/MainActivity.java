package com.syakir.electricitycalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * Main calculator screen for estimating electricity bills.
 * Users select a month, enter kWh units, adjust rebate, and calculate.
 */
public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText etUnits;
    private SeekBar seekBarRebate;
    private TextView tvRebateValue;
    private TextView tvTotalCharges;
    private TextView tvFinalCost;
    private LinearLayout layoutResults;
    private BillDao billDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set custom title
        setTitle(R.string.title_calculator);

        // Initialize database
        billDao = new BillDao(this);

        // Find views
        spinnerMonth = findViewById(R.id.spinnerMonth);
        etUnits = findViewById(R.id.etUnits);
        seekBarRebate = findViewById(R.id.seekBarRebate);
        tvRebateValue = findViewById(R.id.tvRebateValue);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost = findViewById(R.id.tvFinalCost);
        layoutResults = findViewById(R.id.layoutResults);
        Button btnCalculate = findViewById(R.id.btnCalculate);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnAbout = findViewById(R.id.btnAbout);

        // Setup month spinner
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                this, R.array.months, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Setup rebate SeekBar listener
        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRebateValue.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Calculate button
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBill();
            }
        });

        // Navigation buttons
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });
    }

    /**
     * Validates inputs, calculates the electricity bill using tiered rates,
     * saves the record to the database, and displays the results.
     */
    private void calculateBill() {
        // Validate month selection
        int monthPosition = spinnerMonth.getSelectedItemPosition();
        if (monthPosition == 0) {
            Toast.makeText(this, R.string.error_no_month, Toast.LENGTH_SHORT).show();
            return;
        }
        String month = spinnerMonth.getSelectedItem().toString();

        // Validate units input
        String unitsStr = etUnits.getText().toString().trim();
        if (unitsStr.isEmpty()) {
            etUnits.setError(getString(R.string.error_empty_units));
            etUnits.requestFocus();
            return;
        }

        double units;
        try {
            units = Double.parseDouble(unitsStr);
        } catch (NumberFormatException e) {
            etUnits.setError(getString(R.string.error_invalid_units));
            etUnits.requestFocus();
            return;
        }

        if (units < 1 || units > 1000) {
            etUnits.setError(getString(R.string.error_invalid_units));
            etUnits.requestFocus();
            return;
        }

        // Get rebate percentage
        int rebatePercent = seekBarRebate.getProgress();

        // Calculate total charges using tiered rates
        double totalCharges = calculateTieredCharges(units);

        // Apply rebate
        double finalCost = totalCharges - (totalCharges * rebatePercent / 100.0);

        // Save to database
        BillRecord record = new BillRecord(month, units, totalCharges, rebatePercent, finalCost);
        billDao.insertBill(record);

        // Display results
        tvTotalCharges.setText(String.format(Locale.getDefault(), "RM %.2f", totalCharges));
        tvFinalCost.setText(String.format(Locale.getDefault(), "RM %.2f", finalCost));
        layoutResults.setVisibility(View.VISIBLE);

        Toast.makeText(this, R.string.calculation_saved, Toast.LENGTH_SHORT).show();
    }

    /**
     * Calculates electricity charges using Malaysian tiered rates:
     * - 1–200 kWh: 21.8 sen/kWh
     * - 201–300 kWh: 33.4 sen/kWh
     * - 301–600 kWh: 51.6 sen/kWh
     * - 601–1000 kWh: 54.6 sen/kWh
     *
     * @param units Total kWh consumed
     * @return Total charges in RM
     */
    public static double calculateTieredCharges(double units) {
        double total = 0;
        double remaining = units;

        // Tier 1: 1-200 kWh at 21.8 sen/kWh
        if (remaining > 0) {
            double tierUnits = Math.min(remaining, 200);
            total += tierUnits * 0.218;
            remaining -= tierUnits;
        }

        // Tier 2: 201-300 kWh at 33.4 sen/kWh
        if (remaining > 0) {
            double tierUnits = Math.min(remaining, 100);
            total += tierUnits * 0.334;
            remaining -= tierUnits;
        }

        // Tier 3: 301-600 kWh at 51.6 sen/kWh
        if (remaining > 0) {
            double tierUnits = Math.min(remaining, 300);
            total += tierUnits * 0.516;
            remaining -= tierUnits;
        }

        // Tier 4: 601-1000 kWh at 54.6 sen/kWh
        if (remaining > 0) {
            double tierUnits = Math.min(remaining, 400);
            total += tierUnits * 0.546;
            remaining -= tierUnits;
        }

        return total;
    }
}
