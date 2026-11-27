package com.example.tailormate;

import androidx.annotation.NonNull;

/** Model: one tailoring order. */
public class Order {
    // Allowed values for the status column
    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_READY_FOR_TRIAL = "READY_FOR_TRIAL";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_DELIVERED = "DELIVERED";

    private long _id;
    private long _customerId;
    private String _customerName;      // filled only when reading (JOIN)
    private long _garmentTypeId;
    private Long _measurementSetId;    // may be null
    private String _status = STATUS_NEW;
    private double _price;
    private double _paid;
    private String _orderDate;         // yyyy-MM-dd
    private String _deliveryDate;      // yyyy-MM-dd
    private String _notes;

    public Order() { }

    public long getID() { return _id; }
    public void setID(long id) { this._id = id; }
    public long getCustomerId() { return _customerId; }
    public void setCustomerId(long customerId) { this._customerId = customerId; }
    public String getCustomerName() { return _customerName; }
    public void setCustomerName(String customerName) { this._customerName = customerName; }
    public long getGarmentTypeId() { return _garmentTypeId; }
    public void setGarmentTypeId(long garmentTypeId) { this._garmentTypeId = garmentTypeId; }
    public Long getMeasurementSetId() { return _measurementSetId; }
    public void setMeasurementSetId(Long measurementSetId) { this._measurementSetId = measurementSetId; }
    public String getStatus() { return _status; }
    public void setStatus(String status) { this._status = status; }
    public double getPrice() { return _price; }
    public void setPrice(double price) { this._price = price; }
    public double getPaid() { return _paid; }
    public void setPaid(double paid) { this._paid = paid; }
    public double getRemaining() { return _price - _paid; }
    public String getOrderDate() { return _orderDate; }
    public void setOrderDate(String orderDate) { this._orderDate = orderDate; }
    public String getDeliveryDate() { return _deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this._deliveryDate = deliveryDate; }
    public String getNotes() { return _notes; }
    public void setNotes(String notes) { this._notes = notes; }

    @NonNull
    @Override
    public String toString() {
        return "#" + _id + " | " + _customerName + " | " + _status;
    }
}
