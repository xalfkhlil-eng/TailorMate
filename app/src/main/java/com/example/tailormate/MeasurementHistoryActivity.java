package com.example.tailormate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/** Shows saved measurement sessions for the selected customer. */
public class MeasurementHistoryActivity extends AppCompatActivity {

    private DatabaseHandler db;
    private Spinner spCustomers;
    private LinearLayout historyContainer;
    private TextView tvEmpty;
    private final List<Customer> customers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement_history);

        db = new DatabaseHandler(this);
        spCustomers = findViewById(R.id.spHistoryCustomers);
        historyContainer = findViewById(R.id.historyContainer);
        tvEmpty = findViewById(R.id.tvHistoryEmpty);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddCustomer).setOnClickListener(v ->
                startActivity(new Intent(this, CustomersActivity.class)));

        spCustomers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < customers.size()) {
                    showHistory(customers.get(position).getID());
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
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
        findViewById(R.id.btnAddCustomer).setVisibility(hasCustomers ? View.GONE : View.VISIBLE);
        tvEmpty.setText(hasCustomers ? "No measurement history for this customer yet."
                : "There are no customers yet. Add a customer to view measurement history.");
        if (!hasCustomers) {
            historyContainer.removeAllViews();
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void showHistory(long customerId) {
        historyContainer.removeAllViews();
        List<MeasurementSet> sets = db.getMeasurementSetsForCustomer(customerId);
        if (sets.isEmpty()) {
            tvEmpty.setText("No measurement history for this customer yet.");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        for (MeasurementSet set : sets) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));
            card.setBackgroundResource(R.drawable.tm_bg_card_light);

            TextView title = new TextView(this);
            title.setText(displayName(set.getGarmentName()));
            title.setTextColor(getColor(R.color.tm_navy_dark));
            title.setTextSize(18);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(title);

            TextView date = new TextView(this);
            date.setText("Measured on " + set.getMeasuredAt());
            date.setTextColor(getColor(R.color.tm_text_gray));
            date.setTextSize(12);
            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dateParams.topMargin = dp(4);
            card.addView(date, dateParams);

            for (MeasurementSet.MeasurementValue value : set.getValues()) {
                TextView line = new TextView(this);
                line.setText(value.fieldName + ": " + value.value + " cm");
                line.setTextColor(getColor(R.color.tm_text_dark));
                line.setTextSize(14);
                LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lineParams.topMargin = dp(8);
                card.addView(line, lineParams);
            }

            if (set.getNotes() != null && !set.getNotes().trim().isEmpty()) {
                TextView notes = new TextView(this);
                notes.setText("Note: " + set.getNotes());
                notes.setTextColor(getColor(R.color.tm_text_gray));
                notes.setTextSize(13);
                LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                noteParams.topMargin = dp(10);
                card.addView(notes, noteParams);
            }

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.bottomMargin = dp(12);
            historyContainer.addView(card, cardParams);
        }
    }

    private String displayName(String name) {
        if (name == null) return "Measurements";
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
