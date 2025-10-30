package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quizapp.databinding.ActivityUpgradeBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class UpgradeActivity extends AppCompatActivity {
    private ActivityUpgradeBinding binding;
    private UserManager um;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpgradeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        um = new UserManager(this);

        // Toolbar with back arrow
        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Starter plan
        binding.btnPurchaseStarter.setOnClickListener(v ->
                showPaymentOptions("Starter")
        );

        // Intermediate plan
        binding.btnPurchaseIntermediate.setOnClickListener(v ->
                showPaymentOptions("Intermediate")
        );

        // Advanced plan
        binding.btnPurchaseAdvanced.setOnClickListener(v ->
                showPaymentOptions("Advanced")
        );
    }

    private void showPaymentOptions(String planName) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater()
                .inflate(R.layout.payment_options_bottom_sheet, null);
        sheet.setContentView(sheetView);

        LinearLayout gpay = sheetView.findViewById(R.id.optionGPay);
        LinearLayout paypal = sheetView.findViewById(R.id.optionPayPal);

        gpay.setOnClickListener(v -> {
            um.setPlan(um.getCurrent(), planName);
            Toast.makeText(
                    this,
                    planName + " plan purchased via Google Pay!",
                    Toast.LENGTH_LONG
            ).show();
            sheet.dismiss();
            finish();
        });

        paypal.setOnClickListener(v -> {
            um.setPlan(um.getCurrent(), planName);
            Toast.makeText(
                    this,
                    planName + " plan purchased via PayPal!",
                    Toast.LENGTH_LONG
            ).show();
            sheet.dismiss();
            finish();
        });

        sheet.show();
    }
}
