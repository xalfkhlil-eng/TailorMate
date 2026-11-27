package com.example.tailormate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/** Screen 3: customers list with search, add (dialog) and delete (long press). */
public class CustomersActivity extends AppCompatActivity {

    private DatabaseHandler db;
    private CustomerAdapter adapter;
    private EditText etSearch;
    private TextView tvEmpty, tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers);

        db = new DatabaseHandler(this);

        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvCount = findViewById(R.id.tvCount);
        ListView lvCustomers = findViewById(R.id.lvCustomers);
        FloatingActionButton fab = findViewById(R.id.fabAdd);

        adapter = new CustomerAdapter(this, new ArrayList<>());
        lvCustomers.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Live search: every time the text changes, query SQLite again
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) { refreshList(); }
        });

        fab.setOnClickListener(v -> showAddCustomerDialog());

        // Short click: for now only a message. Later it will open the customer details screen.
        lvCustomers.setOnItemClickListener((parent, view, position, id) -> {
            Customer c = adapter.getItem(position);
            if (c != null) {
                Toast.makeText(this, c.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Long click: delete (with confirmation)
        lvCustomers.setOnItemLongClickListener((parent, view, position, id) -> {
            Customer c = adapter.getItem(position);
            if (c != null) confirmDelete(c);
            return true;
        });

        // Opened from the dashboard's "+ Customer" button
        if (getIntent().getBooleanExtra("OPEN_ADD", false)) {
            showAddCustomerDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    // Reloads the list from SQLite using the text in the search box
    private void refreshList() {
        List<Customer> customers = db.searchCustomers(etSearch.getText().toString());
        adapter.clear();
        adapter.addAll(customers);
        adapter.notifyDataSetChanged();

        tvCount.setText(String.valueOf(customers.size()));
        tvEmpty.setVisibility(customers.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddCustomerDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etAddress = view.findViewById(R.id.etAddress);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Customer")
                .setView(view)
                .setPositiveButton("Save", null)   // set below, so the dialog stays open on errors
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Name is required");
                return;
            }
            Customer customer = new Customer(name,
                    etPhone.getText().toString().trim(),
                    etAddress.getText().toString().trim(), "");
            long newId = db.addCustomer(customer);
            if (newId == -1) {
                Toast.makeText(this, "Could not save the customer", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Customer saved", Toast.LENGTH_SHORT).show();
                refreshList();
                dialog.dismiss();
            }
        });
    }

    private void confirmDelete(Customer c) {
        new AlertDialog.Builder(this)
                .setTitle("Delete customer")
                .setMessage("Delete " + c.getName() + " with all his measurements and orders?")
                .setPositiveButton("Delete", (d, which) -> {
                    db.deleteCustomer(c.getID());
                    Toast.makeText(this, "Customer deleted", Toast.LENGTH_SHORT).show();
                    refreshList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
