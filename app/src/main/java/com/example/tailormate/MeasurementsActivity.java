package com.example.tailormate;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Dynamic measurement form for the selected garment and customer. */
public class MeasurementsActivity extends AppCompatActivity {

    private DatabaseHandler db;
    private Spinner spCustomers;
    private LinearLayout fieldsContainer;
    private EditText etNotes;
    private final List<Customer> customers = new ArrayList<>();
    private final List<MeasurementField> fields = new ArrayList<>();
    private final List<EditText> inputs = new ArrayList<>();
    private long garmentId;
    private String garmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurements);

        db = new DatabaseHandler(this);
        garmentId = getIntent().getLongExtra("GARMENT_ID", -1);
        garmentName = getIntent().getStringExtra("GARMENT_NAME");

        if (garmentId == -1 || garmentName == null) {
            Toast.makeText(this, "Garment information is missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextView tvGarmentName = findViewById(R.id.tvGarmentName);
        spCustomers = findViewById(R.id.spCustomers);
        fieldsContainer = findViewById(R.id.fieldsContainer);
        etNotes = findViewById(R.id.etMeasurementNotes);

        tvGarmentName.setText(displayName(garmentName));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddCustomer).setOnClickListener(v ->
                startActivity(new Intent(this, CustomersActivity.class)));
        findViewById(R.id.btnSaveMeasurements).setOnClickListener(v -> saveMeasurements());

        spCustomers.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                       int position, long id) {
                loadLatestMeasurements(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        loadMeasurementFields();
        loadCustomers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (spCustomers != null) loadCustomers();
    }

    private void loadCustomers() {
        customers.clear();
        customers.addAll(db.getAllCustomers());

        ArrayAdapter<Customer> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, customers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCustomers.setAdapter(adapter);

        boolean hasCustomers = !customers.isEmpty();
        spCustomers.setEnabled(hasCustomers);
        findViewById(R.id.tvNoCustomers).setVisibility(hasCustomers ? View.GONE : View.VISIBLE);
        findViewById(R.id.btnSaveMeasurements).setEnabled(hasCustomers);
    }

    // Pre-fill the form with the customer's latest saved measurements for this garment.
    // Empty fields stay empty when the customer has no previous measurement set.
    private void loadLatestMeasurements(int position) {
        for (EditText input : inputs) {
            input.setText("");
        }
        etNotes.setText("");

        if (position < 0 || position >= customers.size()) return;

        Customer customer = customers.get(position);
        MeasurementSet latest = db.getLatestSet(customer.getID(), garmentId);
        if (latest == null) return;

        java.util.Map<Long, Double> savedValues = new java.util.HashMap<>();
        for (MeasurementSet.MeasurementValue value : latest.getValues()) {
            savedValues.put(value.fieldId, value.value);
        }

        for (int i = 0; i < fields.size(); i++) {
            Double value = savedValues.get(fields.get(i).getID());
            if (value != null) {
                inputs.get(i).setText(String.valueOf(value));
            }
        }

        if (latest.getNotes() != null) {
            etNotes.setText(latest.getNotes());
        }
    }

    private void loadMeasurementFields() {
        fields.clear();
        inputs.clear();
        fields.addAll(db.getFieldsForGarment(garmentId));
        fieldsContainer.removeAllViews();

        for (MeasurementField field : fields) {
            TextView label = new TextView(this);
            label.setText(field.getName() + " (cm)");
            label.setTextColor(getColor(R.color.tm_text_dark));
            label.setTextSize(14);
            label.setTypeface(null, android.graphics.Typeface.BOLD);

            EditText input = new EditText(this);
            input.setSingleLine(true);
            input.setHint("Enter " + field.getName().toLowerCase(Locale.ROOT));
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setTextSize(15);
            input.setPadding(dp(14), 0, dp(14), 0);
            input.setBackgroundResource(R.drawable.tm_bg_input);

            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = dp(12);
            fieldsContainer.addView(label, labelParams);

            LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
            inputParams.topMargin = dp(6);
            fieldsContainer.addView(input, inputParams);
            inputs.add(input);
        }
    }

    private void saveMeasurements() {
        if (customers.isEmpty() || spCustomers.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Please add a customer first", Toast.LENGTH_SHORT).show();
            return;
        }

        List<MeasurementSet.MeasurementValue> values = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            String raw = inputs.get(i).getText().toString().trim();
            if (raw.isEmpty()) {
                inputs.get(i).setError("Required");
                inputs.get(i).requestFocus();
                return;
            }
            try {
                double number = Double.parseDouble(raw);
                if (number <= 0) {
                    inputs.get(i).setError("Enter a value greater than 0");
                    inputs.get(i).requestFocus();
                    return;
                }
                values.add(new MeasurementSet.MeasurementValue(fields.get(i).getID(), number));
            } catch (NumberFormatException e) {
                inputs.get(i).setError("Enter a valid number");
                inputs.get(i).requestFocus();
                return;
            }
        }

        Customer customer = customers.get(spCustomers.getSelectedItemPosition());
        MeasurementSet set = new MeasurementSet();
        set.setCustomerId(customer.getID());
        set.setGarmentTypeId(garmentId);
        set.setMeasuredAt(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        set.setNotes(etNotes.getText().toString().trim());
        set.setValues(values);

        try {
            long savedId = db.addMeasurementSet(set);
            if (savedId > 0) {
                Toast.makeText(this, "Measurements saved for " + customer.getName(),
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Could not save measurements", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String displayName(String name) {
        if (name.startsWith("Shirt")) return "Shirt";
        if (name.startsWith("Trousers")) return "Pants";
        if (name.startsWith("Jacket")) return "Jacket";
        if (name.startsWith("Tailored Dress")) return "Dress";
        return name;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
