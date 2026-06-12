package com.syakir.electricitycalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for bill records. Provides full CRUD operations.
 */
public class BillDao {

    private final DatabaseHelper dbHelper;

    public BillDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Insert a new bill record into the database.
     * @return the row ID of the newly inserted record, or -1 if an error occurred
     */
    public long insertBill(BillRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_MONTH, record.getMonth());
        values.put(DatabaseHelper.COLUMN_UNITS, record.getUnits());
        values.put(DatabaseHelper.COLUMN_TOTAL_CHARGES, record.getTotalCharges());
        values.put(DatabaseHelper.COLUMN_REBATE_PERCENT, record.getRebatePercent());
        values.put(DatabaseHelper.COLUMN_FINAL_COST, record.getFinalCost());

        long id = db.insert(DatabaseHelper.TABLE_BILLS, null, values);
        db.close();
        return id;
    }

    /**
     * Get all bill records from the database, ordered by ID descending (newest first).
     */
    public List<BillRecord> getAllBills() {
        List<BillRecord> bills = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BILLS,
                null, null, null, null, null,
                DatabaseHelper.COLUMN_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                BillRecord record = cursorToRecord(cursor);
                bills.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return bills;
    }

    /**
     * Get a single bill record by its ID.
     * @return the BillRecord, or null if not found
     */
    public BillRecord getBillById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BILLS,
                null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        BillRecord record = null;
        if (cursor.moveToFirst()) {
            record = cursorToRecord(cursor);
        }
        cursor.close();
        db.close();
        return record;
    }

    /**
     * Update an existing bill record.
     * @return the number of rows affected
     */
    public int updateBill(BillRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_MONTH, record.getMonth());
        values.put(DatabaseHelper.COLUMN_UNITS, record.getUnits());
        values.put(DatabaseHelper.COLUMN_TOTAL_CHARGES, record.getTotalCharges());
        values.put(DatabaseHelper.COLUMN_REBATE_PERCENT, record.getRebatePercent());
        values.put(DatabaseHelper.COLUMN_FINAL_COST, record.getFinalCost());

        int rows = db.update(
                DatabaseHelper.TABLE_BILLS,
                values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(record.getId())}
        );
        db.close();
        return rows;
    }

    /**
     * Delete a bill record by its ID.
     * @return the number of rows affected
     */
    public int deleteBill(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_BILLS,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        db.close();
        return rows;
    }

    /**
     * Helper method to convert a Cursor row into a BillRecord object.
     */
    private BillRecord cursorToRecord(Cursor cursor) {
        BillRecord record = new BillRecord();
        record.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        record.setMonth(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTH)));
        record.setUnits(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNITS)));
        record.setTotalCharges(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL_CHARGES)));
        record.setRebatePercent(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REBATE_PERCENT)));
        record.setFinalCost(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FINAL_COST)));
        return record;
    }
}
