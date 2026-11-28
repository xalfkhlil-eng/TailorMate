package com.example.tailormate;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

/** Screen: choose a garment type before entering its measurements. */
public class GarmentSelectionActivity extends AppCompatActivity {

    private DatabaseHandler db;
    private LinearLayout garmentContainer;
    private long selectedGarmentId = -1;
    private String selectedGarmentName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garment_selection);

        db = new DatabaseHandler(this);
        garmentContainer = findViewById(R.id.garmentContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnContinue).setOnClickListener(v -> continueWithSelection());

        loadGarments();
    }

    private void loadGarments() {
        List<GarmentType> garments = db.getAllGarmentTypes();
        garmentContainer.removeAllViews();

        for (GarmentType garment : garments) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.bottomMargin = dp(12);
            card.setLayoutParams(cardParams);
            card.setRadius(dp(16));
            card.setCardElevation(dp(1));
            card.setStrokeWidth(dp(1));
            card.setStrokeColor(ContextCompat.getColor(this, R.color.tm_card_stroke));
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.HORIZONTAL);
            content.setGravity(Gravity.CENTER_VERTICAL);
            content.setPadding(dp(18), dp(17), dp(18), dp(17));

            TextView icon = new TextView(this);
            icon.setText(iconFor(garment.getName()));
            icon.setTextSize(27);
            icon.setGravity(Gravity.CENTER);
            content.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));

            LinearLayout textColumn = new LinearLayout(this);
            textColumn.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            textParams.leftMargin = dp(12);
            content.addView(textColumn, textParams);

            TextView name = new TextView(this);
            name.setText(displayName(garment.getName()));
            name.setTextColor(ContextCompat.getColor(this, R.color.tm_navy_dark));
            name.setTextSize(16);
            name.setTypeface(null, android.graphics.Typeface.BOLD);
            textColumn.addView(name);

            TextView hint = new TextView(this);
            hint.setText("Tap to select this garment");
            hint.setTextColor(ContextCompat.getColor(this, R.color.tm_text_gray));
            hint.setTextSize(12);
            LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            hintParams.topMargin = dp(4);
            textColumn.addView(hint, hintParams);

            TextView check = new TextView(this);
            check.setText("✓");
            check.setTextSize(22);
            check.setTextColor(ContextCompat.getColor(this, R.color.tm_navy));
            check.setVisibility(View.GONE);
            content.addView(check);

            card.addView(content);
            card.setTag(new View[]{check});
            card.setOnClickListener(v -> selectGarment(garment, card));
            garmentContainer.addView(card);
        }
    }

    private void selectGarment(GarmentType garment, MaterialCardView selectedCard) {
        selectedGarmentId = garment.getID();
        selectedGarmentName = garment.getName();

        for (int i = 0; i < garmentContainer.getChildCount(); i++) {
            MaterialCardView card = (MaterialCardView) garmentContainer.getChildAt(i);
            TextView check = (TextView) ((View[]) card.getTag())[0];
            boolean selected = card == selectedCard;
            check.setVisibility(selected ? View.VISIBLE : View.GONE);
            card.setStrokeWidth(dp(selected ? 2 : 1));
            card.setStrokeColor(ContextCompat.getColor(this,
                    selected ? R.color.tm_navy : R.color.tm_card_stroke));
            card.setCardBackgroundColor(ContextCompat.getColor(this,
                    selected ? R.color.tm_selected_background : R.color.white));
        }
    }

    private void continueWithSelection() {
        if (selectedGarmentId == -1) {
            Toast.makeText(this, "Please select a garment first", Toast.LENGTH_SHORT).show();
            return;
        }

        // The next step will use these values to open the dynamic measurement form.
        Toast.makeText(this, "Selected: " + displayName(selectedGarmentName),
                Toast.LENGTH_SHORT).show();
    }

    private String displayName(String name) {
        if (name.startsWith("Shirt")) return "Shirt";
        if (name.startsWith("Trousers")) return "Pants";
        if (name.startsWith("Jacket")) return "Jacket";
        if (name.startsWith("Tailored Dress")) return "Dress";
        return name;
    }

    private String iconFor(String name) {
        if (name.startsWith("Shirt")) return "👕";
        if (name.startsWith("Trousers")) return "👖";
        if (name.startsWith("Jacket")) return "🧥";
        if (name.startsWith("Tailored Dress")) return "👗";
        return "◈";
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
