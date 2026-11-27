package com.example.tailormate;

import java.util.ArrayList;
import java.util.List;

/**
 * Model: one measuring session of one customer for one garment type.
 * Values are always stored in centimetres; the unit field is only the unit
 * the user typed them in (the UI converts cm <-> in).
 */
public class MeasurementSet {

    /** One value inside a set: field + number. */
    public static class MeasurementValue {
        public long fieldId;
        public String fieldName;   // filled only when reading
        public double value;

        public MeasurementValue(long fieldId, double value) {
            this.fieldId = fieldId;
            this.value = value;
        }

        public MeasurementValue(long fieldId, String fieldName, double value) {
            this(fieldId, value);
            this.fieldName = fieldName;
        }
    }

    private long _id;
    private long _customerId;
    private long _garmentTypeId;
    private String _garmentName;   // filled only when reading (JOIN)
    private String _measuredAt;    // yyyy-MM-dd
    private String _notes;
    private List<MeasurementValue> _values = new ArrayList<>();

    public MeasurementSet() { }

    public long getID() { return _id; }
    public void setID(long id) { this._id = id; }
    public long getCustomerId() { return _customerId; }
    public void setCustomerId(long customerId) { this._customerId = customerId; }
    public long getGarmentTypeId() { return _garmentTypeId; }
    public void setGarmentTypeId(long garmentTypeId) { this._garmentTypeId = garmentTypeId; }
    public String getGarmentName() { return _garmentName; }
    public void setGarmentName(String garmentName) { this._garmentName = garmentName; }
    public String getMeasuredAt() { return _measuredAt; }
    public void setMeasuredAt(String measuredAt) { this._measuredAt = measuredAt; }
    public String getNotes() { return _notes; }
    public void setNotes(String notes) { this._notes = notes; }
    public List<MeasurementValue> getValues() { return _values; }
    public void setValues(List<MeasurementValue> values) { this._values = values; }
}
