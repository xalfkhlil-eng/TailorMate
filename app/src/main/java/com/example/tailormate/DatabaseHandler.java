package com.example.tailormate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the TailorMate SQLite database: schema, seed data and CRUD.
 * Same pattern as the lecture example: SQLiteOpenHelper + ContentValues + Cursor.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // ---------- Database configuration ----------
    // Increase DATABASE_VERSION when the schema changes, then add a case in onUpgrade.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "tailormate.db";

    // ---------- Table names ----------
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String TABLE_GARMENT_TYPES = "garment_types";
    private static final String TABLE_FIELDS = "measurement_fields";
    private static final String TABLE_SETS = "measurement_sets";
    private static final String TABLE_VALUES = "measurement_values";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_SETTINGS = "settings";

    // ---------- Common column names ----------
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_NOTES = "notes";

    // customers
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_PHOTO = "photo_path";
    private static final String KEY_CREATED_AT = "created_at";

    // measurement_fields / sets / values
    private static final String KEY_GARMENT_ID = "garment_type_id";
    private static final String KEY_SORT = "sort_order";
    private static final String KEY_CUSTOMER_ID = "customer_id";
    private static final String KEY_MEASURED_AT = "measured_at";
    private static final String KEY_SET_ID = "set_id";
    private static final String KEY_FIELD_ID = "field_id";
    private static final String KEY_VALUE = "value";

    // orders
    private static final String KEY_MEASUREMENT_SET_ID = "measurement_set_id";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PRICE = "price";
    private static final String KEY_PAID = "paid";
    private static final String KEY_ORDER_DATE = "order_date";
    private static final String KEY_DELIVERY_DATE = "delivery_date";

    // settings
    private static final String KEY_SETTING_KEY = "key";
    private static final String KEY_SETTING_VALUE = "value";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // SQLite ignores FOREIGN KEY constraints unless they are switched on for every connection.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called once, the first time the database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CUSTOMERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT NOT NULL,"
                + KEY_PHONE + " TEXT,"
                + KEY_ADDRESS + " TEXT,"
                + KEY_NOTES + " TEXT,"
                + KEY_PHOTO + " TEXT,"
                + KEY_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_GARMENT_TYPES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT NOT NULL UNIQUE"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_FIELDS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_GARMENT_ID + " INTEGER NOT NULL,"
                + KEY_NAME + " TEXT NOT NULL,"
                + KEY_SORT + " INTEGER NOT NULL DEFAULT 0,"
                + "FOREIGN KEY(" + KEY_GARMENT_ID + ") REFERENCES "
                + TABLE_GARMENT_TYPES + "(" + KEY_ID + ") ON DELETE CASCADE"
                + ")");

        // Deleting a customer also deletes his measurement sets and orders
        db.execSQL("CREATE TABLE " + TABLE_SETS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_CUSTOMER_ID + " INTEGER NOT NULL,"
                + KEY_GARMENT_ID + " INTEGER NOT NULL,"
                + KEY_MEASURED_AT + " TEXT NOT NULL,"
                + KEY_NOTES + " TEXT,"
                + "FOREIGN KEY(" + KEY_CUSTOMER_ID + ") REFERENCES "
                + TABLE_CUSTOMERS + "(" + KEY_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + KEY_GARMENT_ID + ") REFERENCES "
                + TABLE_GARMENT_TYPES + "(" + KEY_ID + ")"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_VALUES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SET_ID + " INTEGER NOT NULL,"
                + KEY_FIELD_ID + " INTEGER NOT NULL,"
                + KEY_VALUE + " REAL NOT NULL,"
                + "UNIQUE(" + KEY_SET_ID + "," + KEY_FIELD_ID + "),"
                + "FOREIGN KEY(" + KEY_SET_ID + ") REFERENCES "
                + TABLE_SETS + "(" + KEY_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + KEY_FIELD_ID + ") REFERENCES "
                + TABLE_FIELDS + "(" + KEY_ID + ")"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_ORDERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_CUSTOMER_ID + " INTEGER NOT NULL,"
                + KEY_GARMENT_ID + " INTEGER NOT NULL,"
                + KEY_MEASUREMENT_SET_ID + " INTEGER,"
                + KEY_STATUS + " TEXT NOT NULL DEFAULT 'NEW',"
                + KEY_PRICE + " REAL NOT NULL DEFAULT 0,"
                + KEY_PAID + " REAL NOT NULL DEFAULT 0,"
                + KEY_ORDER_DATE + " TEXT NOT NULL,"
                + KEY_DELIVERY_DATE + " TEXT,"
                + KEY_NOTES + " TEXT,"
                + "FOREIGN KEY(" + KEY_CUSTOMER_ID + ") REFERENCES "
                + TABLE_CUSTOMERS + "(" + KEY_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + KEY_GARMENT_ID + ") REFERENCES "
                + TABLE_GARMENT_TYPES + "(" + KEY_ID + "),"
                + "FOREIGN KEY(" + KEY_MEASUREMENT_SET_ID + ") REFERENCES "
                + TABLE_SETS + "(" + KEY_ID + ") ON DELETE SET NULL"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + "("
                + KEY_SETTING_KEY + " TEXT PRIMARY KEY,"
                + KEY_SETTING_VALUE + " TEXT"
                + ")");

        // Indexes for the screens that search / filter a lot
        db.execSQL("CREATE INDEX idx_customers_name ON " + TABLE_CUSTOMERS + "(" + KEY_NAME + ")");
        db.execSQL("CREATE INDEX idx_sets_customer ON " + TABLE_SETS + "(" + KEY_CUSTOMER_ID + ")");
        db.execSQL("CREATE INDEX idx_orders_customer ON " + TABLE_ORDERS + "(" + KEY_CUSTOMER_ID + ")");
        db.execSQL("CREATE INDEX idx_orders_status ON " + TABLE_ORDERS + "(" + KEY_STATUS + ")");

        seedData(db);
    }

    // Called when DATABASE_VERSION is increased.
    // IMPORTANT: never DROP the tables here, that would delete the customers' data.
    // Add one "case" per version, and let it fall through to the next ones.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                // Example for version 2:
                // db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN email TEXT");
                // (fall through)
            default:
                break;
        }
    }

    // ---------- Seed data (garment types, their fields, default settings) ----------
    // NOTE: the field lists are based on the prototype; adjust them to the final design.
    private void seedData(SQLiteDatabase db) {
        seedGarment(db, "Shirt / Sweater", new String[]{
                "Shoulder Width", "Chest Circumference", "Waist Circumference", "Sleeve Length",
                "Upper Arm", "Cuff", "Neck", "Shirt Length"});
        seedGarment(db, "Trousers / Pants", new String[]{
                "Waist Circumference", "Hip Circumference", "Thigh Circumference", "Knee Circumference",
                "Calf Circumference", "Leg Opening", "Inseam", "Outseam", "Crotch Rise"});
        seedGarment(db, "Jacket / Blazer", new String[]{
                "Shoulder Width", "Chest Circumference", "Waist Circumference", "Sleeve Length",
                "Upper Arm", "Back Length", "Jacket Length", "Cuff"});
        seedGarment(db, "Tailored Dress", new String[]{
                "Bust", "Waist", "Hip", "Shoulder Width", "Sleeve Length", "Dress Length", "Back Length"});
        seedGarment(db, "Skirt", new String[]{
                "Waist", "Hip", "Skirt Length", "Hem Width", "Waist to Hip"});

        setSettingInternal(db, "shop_name", "My Atelier");
        setSettingInternal(db, "license_id", "");
        setSettingInternal(db, "unit_system", "cm");   // cm or in
        setSettingInternal(db, "currency", "USD");
    }

    private void seedGarment(SQLiteDatabase db, String garmentName, String[] fields) {
        ContentValues g = new ContentValues();
        g.put(KEY_NAME, garmentName);
        long garmentId = db.insert(TABLE_GARMENT_TYPES, null, g);

        for (int i = 0; i < fields.length; i++) {
            ContentValues f = new ContentValues();
            f.put(KEY_GARMENT_ID, garmentId);
            f.put(KEY_NAME, fields[i]);
            f.put(KEY_SORT, i);
            db.insert(TABLE_FIELDS, null, f);
        }
    }

    // =====================================================================
    //                              CUSTOMERS
    // =====================================================================

    // CREATE: returns the new customer id (or -1 on failure)
    public long addCustomer(Customer c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, c.getName());
        values.put(KEY_PHONE, c.getPhone());
        values.put(KEY_ADDRESS, c.getAddress());
        values.put(KEY_NOTES, c.getNotes());
        values.put(KEY_PHOTO, c.getPhotoPath());
        return db.insert(TABLE_CUSTOMERS, null, values);
    }

    // READ one
    public Customer getCustomer(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMERS, null, KEY_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Customer c = null;
        if (cursor.moveToFirst()) {
            c = cursorToCustomer(cursor);
        }
        cursor.close();
        return c;
    }

    // READ all (newest first)
    public List<Customer> getAllCustomers() {
        return searchCustomers(null);
    }

    // SEARCH by name or phone. Pass null/empty to get everybody.
    public List<Customer> searchCustomers(String text) {
        List<Customer> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if (text == null || text.trim().isEmpty()) {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_CUSTOMERS
                    + " ORDER BY " + KEY_NAME + " COLLATE NOCASE", null);
        } else {
            String like = "%" + text.trim() + "%";
            cursor = db.rawQuery("SELECT * FROM " + TABLE_CUSTOMERS
                    + " WHERE " + KEY_NAME + " LIKE ? OR " + KEY_PHONE + " LIKE ?"
                    + " ORDER BY " + KEY_NAME + " COLLATE NOCASE", new String[]{like, like});
        }
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToCustomer(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // UPDATE: returns number of rows changed
    public int updateCustomer(Customer c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, c.getName());
        values.put(KEY_PHONE, c.getPhone());
        values.put(KEY_ADDRESS, c.getAddress());
        values.put(KEY_NOTES, c.getNotes());
        values.put(KEY_PHOTO, c.getPhotoPath());
        return db.update(TABLE_CUSTOMERS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(c.getID())});
    }

    // DELETE: his measurements and orders are deleted too (ON DELETE CASCADE)
    public void deleteCustomer(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CUSTOMERS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int getCustomerCount() {
        return countQuery("SELECT COUNT(*) FROM " + TABLE_CUSTOMERS, null);
    }

    private Customer cursorToCustomer(Cursor cursor) {
        Customer c = new Customer();
        c.setID(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
        c.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
        c.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE)));
        c.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADDRESS)));
        c.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTES)));
        c.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHOTO)));
        c.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT)));
        return c;
    }

    // =====================================================================
    //                    GARMENT TYPES & MEASUREMENT FIELDS
    // =====================================================================

    // For the "Select Garment" screen
    public List<GarmentType> getAllGarmentTypes() {
        List<GarmentType> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + KEY_ID + ", " + KEY_NAME
                + " FROM " + TABLE_GARMENT_TYPES + " ORDER BY " + KEY_ID, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new GarmentType(cursor.getLong(0), cursor.getString(1)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // For the "Measurements" screen: the fields to show for one garment (e.g. "9 Specs")
    public List<MeasurementField> getFieldsForGarment(long garmentTypeId) {
        List<MeasurementField> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + KEY_ID + ", " + KEY_GARMENT_ID + ", "
                + KEY_NAME + ", " + KEY_SORT + " FROM " + TABLE_FIELDS
                + " WHERE " + KEY_GARMENT_ID + " = ? ORDER BY " + KEY_SORT,
                new String[]{String.valueOf(garmentTypeId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(new MeasurementField(cursor.getLong(0), cursor.getLong(1),
                        cursor.getString(2), cursor.getInt(3)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // =====================================================================
    //                          MEASUREMENT SETS
    // =====================================================================

    // CREATE a set together with all its values, in ONE transaction
    // (either everything is saved or nothing). Returns the new set id.
    public long addMeasurementSet(MeasurementSet set) {
        SQLiteDatabase db = this.getWritableDatabase();
        long setId = -1;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_CUSTOMER_ID, set.getCustomerId());
            values.put(KEY_GARMENT_ID, set.getGarmentTypeId());
            values.put(KEY_MEASURED_AT, set.getMeasuredAt());
            values.put(KEY_NOTES, set.getNotes());
            setId = db.insertOrThrow(TABLE_SETS, null, values);
            insertValues(db, setId, set.getValues());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return setId;
    }

    // UPDATE: replaces the date/notes and all the values of an existing set
    public void updateMeasurementSet(MeasurementSet set) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_MEASURED_AT, set.getMeasuredAt());
            values.put(KEY_NOTES, set.getNotes());
            db.update(TABLE_SETS, values, KEY_ID + " = ?", new String[]{String.valueOf(set.getID())});

            db.delete(TABLE_VALUES, KEY_SET_ID + " = ?", new String[]{String.valueOf(set.getID())});
            insertValues(db, set.getID(), set.getValues());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void insertValues(SQLiteDatabase db, long setId, List<MeasurementSet.MeasurementValue> list) {
        for (MeasurementSet.MeasurementValue v : list) {
            ContentValues cv = new ContentValues();
            cv.put(KEY_SET_ID, setId);
            cv.put(KEY_FIELD_ID, v.fieldId);
            cv.put(KEY_VALUE, v.value);
            db.insertOrThrow(TABLE_VALUES, null, cv);
        }
    }

    // DELETE (its values are deleted too)
    public void deleteMeasurementSet(long setId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SETS, KEY_ID + " = ?", new String[]{String.valueOf(setId)});
    }

    // For the "Measurement History" screen: newest first.
    // "Latest" badge = first item of each garment type; the rest are "Archived".
    // So no is_latest column is needed (it can never get out of sync).
    public List<MeasurementSet> getMeasurementSetsForCustomer(long customerId) {
        List<MeasurementSet> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT s." + KEY_ID + ", s." + KEY_CUSTOMER_ID + ", s."
                + KEY_GARMENT_ID + ", g." + KEY_NAME + ", s." + KEY_MEASURED_AT + ", s." + KEY_NOTES
                + " FROM " + TABLE_SETS + " s JOIN " + TABLE_GARMENT_TYPES + " g ON g." + KEY_ID
                + " = s." + KEY_GARMENT_ID
                + " WHERE s." + KEY_CUSTOMER_ID + " = ?"
                + " ORDER BY s." + KEY_MEASURED_AT + " DESC, s." + KEY_ID + " DESC",
                new String[]{String.valueOf(customerId)});
        if (cursor.moveToFirst()) {
            do {
                MeasurementSet s = new MeasurementSet();
                s.setID(cursor.getLong(0));
                s.setCustomerId(cursor.getLong(1));
                s.setGarmentTypeId(cursor.getLong(2));
                s.setGarmentName(cursor.getString(3));
                s.setMeasuredAt(cursor.getString(4));
                s.setNotes(cursor.getString(5));
                s.setValues(getValuesForSet(db, s.getID()));
                list.add(s);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // The most recent set of one customer for one garment (to pre-fill the form), or null
    public MeasurementSet getLatestSet(long customerId, long garmentTypeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT s." + KEY_ID + ", s." + KEY_MEASURED_AT + ", s." + KEY_NOTES
                + " FROM " + TABLE_SETS + " s WHERE s." + KEY_CUSTOMER_ID + " = ? AND s."
                + KEY_GARMENT_ID + " = ? ORDER BY s." + KEY_MEASURED_AT + " DESC, s." + KEY_ID
                + " DESC LIMIT 1",
                new String[]{String.valueOf(customerId), String.valueOf(garmentTypeId)});
        MeasurementSet s = null;
        if (cursor.moveToFirst()) {
            s = new MeasurementSet();
            s.setID(cursor.getLong(0));
            s.setCustomerId(customerId);
            s.setGarmentTypeId(garmentTypeId);
            s.setMeasuredAt(cursor.getString(1));
            s.setNotes(cursor.getString(2));
            s.setValues(getValuesForSet(db, s.getID()));
        }
        cursor.close();
        return s;
    }

    private List<MeasurementSet.MeasurementValue> getValuesForSet(SQLiteDatabase db, long setId) {
        List<MeasurementSet.MeasurementValue> values = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT v." + KEY_FIELD_ID + ", f." + KEY_NAME + ", v." + KEY_VALUE
                + " FROM " + TABLE_VALUES + " v JOIN " + TABLE_FIELDS + " f ON f." + KEY_ID
                + " = v." + KEY_FIELD_ID + " WHERE v." + KEY_SET_ID + " = ? ORDER BY f." + KEY_SORT,
                new String[]{String.valueOf(setId)});
        if (cursor.moveToFirst()) {
            do {
                values.add(new MeasurementSet.MeasurementValue(
                        cursor.getLong(0), cursor.getString(1), cursor.getDouble(2)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return values;
    }

    // =====================================================================
    //                               ORDERS
    // =====================================================================

    // CREATE
    public long addOrder(Order o) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_ORDERS, null, orderToValues(o));
    }

    // READ all orders (status == null means every status). Soonest delivery first.
    public List<Order> getOrders(String status) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT o.*, c." + KEY_NAME + " AS customer_name FROM " + TABLE_ORDERS
                + " o JOIN " + TABLE_CUSTOMERS + " c ON c." + KEY_ID + " = o." + KEY_CUSTOMER_ID;
        String[] args = null;
        if (status != null) {
            sql += " WHERE o." + KEY_STATUS + " = ?";
            args = new String[]{status};
        }
        sql += " ORDER BY o." + KEY_DELIVERY_DATE + " ASC, o." + KEY_ID + " DESC";
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // READ the orders of one customer
    public List<Order> getOrdersForCustomer(long customerId) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT o.*, c." + KEY_NAME + " AS customer_name FROM "
                + TABLE_ORDERS + " o JOIN " + TABLE_CUSTOMERS + " c ON c." + KEY_ID + " = o."
                + KEY_CUSTOMER_ID + " WHERE o." + KEY_CUSTOMER_ID + " = ? ORDER BY o." + KEY_ID + " DESC",
                new String[]{String.valueOf(customerId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // UPDATE (whole order)
    public int updateOrder(Order o) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_ORDERS, orderToValues(o), KEY_ID + " = ?",
                new String[]{String.valueOf(o.getID())});
    }

    // UPDATE only the status (used by the "move to next stage" button)
    public int updateOrderStatus(long orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_STATUS, status);
        return db.update(TABLE_ORDERS, values, KEY_ID + " = ?", new String[]{String.valueOf(orderId)});
    }

    // DELETE
    public void deleteOrder(long orderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ORDERS, KEY_ID + " = ?", new String[]{String.valueOf(orderId)});
    }

    // For the dashboard cards ("Pending Orders", "Ready for Trial")
    public int countOrdersByStatus(String status) {
        return countQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE " + KEY_STATUS + " = ?",
                new String[]{status});
    }

    // Pending = everything not yet delivered
    public int countPendingOrders() {
        return countQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE " + KEY_STATUS + " != ?",
                new String[]{Order.STATUS_DELIVERED});
    }

    private ContentValues orderToValues(Order o) {
        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_ID, o.getCustomerId());
        values.put(KEY_GARMENT_ID, o.getGarmentTypeId());
        if (o.getMeasurementSetId() == null) {
            values.putNull(KEY_MEASUREMENT_SET_ID);
        } else {
            values.put(KEY_MEASUREMENT_SET_ID, o.getMeasurementSetId());
        }
        values.put(KEY_STATUS, o.getStatus());
        values.put(KEY_PRICE, o.getPrice());
        values.put(KEY_PAID, o.getPaid());
        values.put(KEY_ORDER_DATE, o.getOrderDate());
        values.put(KEY_DELIVERY_DATE, o.getDeliveryDate());
        values.put(KEY_NOTES, o.getNotes());
        return values;
    }

    private Order cursorToOrder(Cursor cursor) {
        Order o = new Order();
        o.setID(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
        o.setCustomerId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CUSTOMER_ID)));
        o.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow("customer_name")));
        o.setGarmentTypeId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_GARMENT_ID)));
        int setIdx = cursor.getColumnIndexOrThrow(KEY_MEASUREMENT_SET_ID);
        o.setMeasurementSetId(cursor.isNull(setIdx) ? null : cursor.getLong(setIdx));
        o.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_STATUS)));
        o.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE)));
        o.setPaid(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PAID)));
        o.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_DATE)));
        o.setDeliveryDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DELIVERY_DATE)));
        o.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTES)));
        return o;
    }

    // =====================================================================
    //                              SETTINGS
    // =====================================================================

    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SETTINGS, new String[]{KEY_SETTING_VALUE},
                KEY_SETTING_KEY + " = ?", new String[]{key}, null, null, null);
        String result = defaultValue;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }

    public void setSetting(String key, String value) {
        setSettingInternal(this.getWritableDatabase(), key, value);
    }

    private void setSettingInternal(SQLiteDatabase db, String key, String value) {
        ContentValues values = new ContentValues();
        values.put(KEY_SETTING_KEY, key);
        values.put(KEY_SETTING_VALUE, value);
        db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // =====================================================================
    //                              HELPERS
    // =====================================================================

    private int countQuery(String sql, String[] args) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
