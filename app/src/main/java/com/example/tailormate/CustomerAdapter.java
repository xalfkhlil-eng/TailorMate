package com.example.tailormate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/** Draws one row (initials circle + name + phone) for each Customer in the ListView. */
public class CustomerAdapter extends ArrayAdapter<Customer> {

    public CustomerAdapter(Context context, List<Customer> customers) {
        super(context, 0, customers);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_customer, parent, false);
        }

        Customer c = getItem(position);
        TextView tvInitials = convertView.findViewById(R.id.tvInitials);
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvPhone = convertView.findViewById(R.id.tvPhone);

        if (c != null) {
            tvInitials.setText(getInitials(c.getName()));
            tvName.setText(c.getName());
            tvPhone.setText(c.getPhone() == null || c.getPhone().isEmpty() ? "-" : c.getPhone());
        }
        return convertView;
    }

    // "John Doe" -> "JD", "Ahmed" -> "A"
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        String result = parts[0].substring(0, 1);
        if (parts.length > 1) {
            result += parts[1].substring(0, 1);
        }
        return result.toUpperCase();
    }
}
