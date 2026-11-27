package com.example.tailormate;

/** Model: one measurement field of a garment (e.g. Chest, Waist). */
public class MeasurementField {
    private long _id;
    private long _garmentTypeId;
    private String _name;
    private int _sortOrder;

    public MeasurementField() { }
    public MeasurementField(long id, long garmentTypeId, String name, int sortOrder) {
        this._id = id;
        this._garmentTypeId = garmentTypeId;
        this._name = name;
        this._sortOrder = sortOrder;
    }

    public long getID() { return _id; }
    public long getGarmentTypeId() { return _garmentTypeId; }
    public String getName() { return _name; }
    public int getSortOrder() { return _sortOrder; }
}
