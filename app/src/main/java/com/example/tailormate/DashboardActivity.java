package com.example.tailormate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Screen 2: dashboard with counters read from SQLite. */
public class DashboardActivity extends AppCompatActivity {

    private DatabaseHandler db;
    private TextView tvShopName, tvCustomerCount, tvPending, tvTrial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Initialize database helper (creates the database on first launch)
        db = new DatabaseHandler(this);

        tvShopName = findViewById(R.id.tvShopName);
        tvCustomerCount = findViewById(R.id.tvCustomerCount);
        tvPending = findViewById(R.id.tvPending);
        tvTrial = findViewById(R.id.tvTrial);

        // 2. Buttons
        findViewById(R.id.tvViewAll).setOnClickListener(v ->
                startActivity(new Intent(this, CustomersActivity.class)));

        Button btnNewCustomer = findViewById(R.id.btnNewCustomer);
        btnNewCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomersActivity.class);
            intent.putExtra("OPEN_ADD", true); // opens the "Add Customer" dialog directly
            startActivity(intent);
        });

        findViewById(R.id.btnNewMeasure).setOnClickListener(v ->
                startActivity(new Intent(this, GarmentSelectionActivity.class)));
        findViewById(R.id.btnHistory).setOnClickListener(v -> comingSoon());
    }

    // 3. Reload the numbers every time we come back to this screen
    @Override
    protected void onResume() {
        super.onResume();
        tvShopName.setText(db.getSetting("shop_name", "My Atelier"));
        tvCustomerCount.setText(String.valueOf(db.getCustomerCount()));
        tvPending.setText(String.valueOf(db.countPendingOrders()));
        tvTrial.setText(String.valueOf(db.countOrdersByStatus(Order.STATUS_READY_FOR_TRIAL)));
    }

    private void comingSoon() {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    }
}
