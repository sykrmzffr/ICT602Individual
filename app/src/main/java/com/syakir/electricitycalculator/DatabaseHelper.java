package com.syakir.electricitycalculator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database helper for managing the bills database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "electribill.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    public static final String TABLE_BILLS = "bills";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_UNITS = "units";
    public static final String COLUMN_TOTAL_CHARGES = "total_charges";
    public static final String COLUMN_REBATE_PERCENT = "rebate_percent";
    public static final String COLUMN_FINAL_COST = "final_cost";

    private static final String CREATE_TABLE_BILLS =
            "CREATE TABLE " + TABLE_BILLS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MONTH + " TEXT NOT NULL, " +
                    COLUMN_UNITS + " REAL NOT NULL, " +
                    COLUMN_TOTAL_CHARGES + " REAL NOT NULL, " +
                    COLUMN_REBATE_PERCENT + " REAL NOT NULL, " +
                    COLUMN_FINAL_COST + " REAL NOT NULL" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BILLS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }
}
