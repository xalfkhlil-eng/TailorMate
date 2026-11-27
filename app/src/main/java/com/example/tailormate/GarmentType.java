package com.example.tailormate;

import androidx.annotation.NonNull;

/** Model: shirt, trousers, jacket, dress, skirt... */
public class GarmentType {
    private long _id;
    private String _name;

    public GarmentType() { }
    public GarmentType(long id, String name) { this._id = id; this._name = name; }

    public long getID() { return _id; }
    public void setID(long id) { this._id = id; }
    public String getName() { return _name; }
    public void setName(String name) { this._name = name; }

    @NonNull
    @Override
    public String toString() { return _name; }
}
