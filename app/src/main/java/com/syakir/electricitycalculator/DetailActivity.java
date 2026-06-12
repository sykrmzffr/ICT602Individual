package com.syakir.electricitycalculator;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * Detail screen for viewing, editing, and deleting a bill record.
 * Receives a bill_id via Intent extra and loads the record from the database.
 */
public class DetailActivity extends AppCompatActivity {

    private TextView tvDetailMonth, tvDetailUnits, tvDetailTotal, tvDetailRebate, tvDetailFinalCost;
    private LinearLayout layoutDisplay, layoutEdit;
    private Spinner spinnerEditMonth;
    private EditText etEditUnits;
    private SeekBar seekBarEditRebate;
    private TextView tvEditRebateValue;
    private Button btnEdit, btnSave, btnDelete, btnBack;

    private BillDao billDao;
    private BillRecord currentRecord;
    private int billId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setTitle(R.string.title_detail);

        billDao = new BillDao(this);

        // Get bill ID from intent
        billId = getIntent().getIntExtra("bill_id", -1);
        if (billId == -1) {
            Toast.makeText(this, "Error: Record not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Find views - Display section
        layoutDisplay = findViewById(R.id.layoutDisplay);
        tvDetailMonth = findViewById(R.id.tvDetailMonth);
        tvDetailUnits = findViewById(R.id.tvDetailUnits);
        tvDetailTotal = findViewById(R.id.tvDetailTotal);
        tvDetailRebate = findViewById(R.id.tvDetailRebate);
        tvDetailFinalCost = findViewById(R.id.tvDetailFinalCost);

        // Find views - Edit section
        layoutEdit = findViewById(R.id.layoutEdit);
        spinnerEditMonth = findViewById(R.id.spinnerEditMonth);
        etEditUnits = findViewById(R.id.etEditUnits);
        seekBarEditRebate = findViewById(R.id.seekBarEditRebate);
        tvEditRebateValue = findViewById(R.id.tvEditRebateValue);
        btnSave = findViewById(R.id.btnSave);

        // Find buttons
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBackDetail);

        // Setup month spinner for edit mode
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                this, R.array.months, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditMonth.setAdapter(monthAdapter);

        // Setup rebate SeekBar listener for edit mode
        seekBarEditRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvEditRebateValue.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Load and display record
        loadRecord();

        // Edit button toggles edit mode
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditMode();
            }
        });

        // Save button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecord();
            }
        });

        // Delete button with confirmation dialog
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmation();
            }
        });

        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Loads the bill record from the database and displays it.
     */
    private void loadRecord() {
        currentRecord = billDao.getBillById(billId);
        if (currentRecord == null) {
            Toast.makeText(this, "Error: Record not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvDetailMonth.setText(currentRecord.getMonth());
        tvDetailUnits.setText(String.format(Locale.getDefault(), "%.1f kWh", currentRecord.getUnits()));
        tvDetailTotal.setText(String.format(Locale.getDefault(), "RM %.2f", currentRecord.getTotalCharges()));
        tvDetailRebate.setText(String.format(Locale.getDefault(), "%.0f%%", currentRecord.getRebatePercent()));
        tvDetailFinalCost.setText(String.format(Locale.getDefault(), "RM %.2f", currentRecord.getFinalCost()));
    }

    /**
     * Toggles between view mode and edit mode.
     */
    private void toggleEditMode() {
        isEditMode = !isEditMode;

        if (isEditMode) {
            layoutEdit.setVisibility(View.VISIBLE);
            btnEdit.setText("Cancel");

            // Pre-fill edit fields with current values
            // Find the month position in the spinner
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerEditMonth.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(currentRecord.getMonth())) {
                    spinnerEditMonth.setSelection(i);
                    break;
                }
            }

            etEditUnits.setText(String.valueOf((int) currentRecord.getUnits()));
            seekBarEditRebate.setProgress((int) currentRecord.getRebatePercent());
            tvEditRebateValue.setText((int) currentRecord.getRebatePercent() + "%");
        } else {
            layoutEdit.setVisibility(View.GONE);
            btnEdit.setText(R.string.btn_edit);
        }
    }

    /**
     * Validates edited inputs, recalculates, updates the database, and refreshes the display.
     */
    private void saveRecord() {
        // Validate month
        int monthPosition = spinnerEditMonth.getSelectedItemPosition();
        if (monthPosition == 0) {
            Toast.makeText(this, R.string.error_no_month, Toast.LENGTH_SHORT).show();
            return;
        }
        String month = spinnerEditMonth.getSelectedItem().toString();

        // Validate units
        String unitsStr = etEditUnits.getText().toString().trim();
        if (unitsStr.isEmpty()) {
            etEditUnits.setError(getString(R.string.error_empty_units));
            etEditUnits.requestFocus();
            return;
        }

        double units;
        try {
            units = Double.parseDouble(unitsStr);
        } catch (NumberFormatException e) {
            etEditUnits.setError(getString(R.string.error_invalid_units));
            etEditUnits.requestFocus();
            return;
        }

        if (units < 1 || units > 1000) {
            etEditUnits.setError(getString(R.string.error_invalid_units));
            etEditUnits.requestFocus();
            return;
        }

        // Get rebate
        int rebatePercent = seekBarEditRebate.getProgress();

        // Recalculate
        double totalCharges = MainActivity.calculateTieredCharges(units);
        double finalCost = totalCharges - (totalCharges * rebatePercent / 100.0);

        // Update record
        currentRecord.setMonth(month);
        currentRecord.setUnits(units);
        currentRecord.setTotalCharges(totalCharges);
        currentRecord.setRebatePercent(rebatePercent);
        currentRecord.setFinalCost(finalCost);

        billDao.updateBill(currentRecord);

        // Refresh display and exit edit mode
        isEditMode = true; // Will be toggled to false
        toggleEditMode();
        loadRecord();

        Toast.makeText(this, R.string.record_updated, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows a confirmation AlertDialog before deleting the record.
     */
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                    billDao.deleteBill(billId);
                    Toast.makeText(DetailActivity.this, R.string.record_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }
}
