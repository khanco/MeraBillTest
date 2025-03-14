package com.sbk.merabilltest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sbk.merabilltest.models.PaymentDetails;
import com.sbk.merabilltest.utils.AddPaymentDialogHelper;
import com.sbk.merabilltest.utils.FileHelper;
import com.sbk.merabilltest.utils.UtilFunctions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AddPaymentDialogHelper.PaymentDetailsCallback, FileHelper.FileReadWriteCallback {
    private int totalAmount = 0;
    private AddPaymentDialogHelper addPaymentDialogHelper;
    private ChipGroup chipGroup;
    private TextView tvNoPayments;
    private TextView tvTotalAmount;
    private Button btnSave;
    private boolean unSavedData = false;
    private final FileHelper fileHelper = new FileHelper();

    private final ArrayList<PaymentDetails> listPayments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        initData();
        initClicks();
    }

    private void initData() {
        tvTotalAmount.setText(HtmlCompat.fromHtml(String.format(getString(R.string.total_amount), totalAmount), HtmlCompat.FROM_HTML_MODE_COMPACT));
        addPaymentDialogHelper = new AddPaymentDialogHelper();
        fileHelper.loadSavedPaymentDetails(getApplicationContext(), this);
    }

    private void initClicks() {
        findViewById(R.id.tvAddPayment).setOnClickListener(view -> {
            if (listPayments.size() == 3) {
                UtilFunctions.showToast(getApplicationContext(), getString(R.string.all_payment_types_added));
            } else {
                addPaymentDialogHelper.showAddPaymentDialog(this, listPayments);
            }
        });
        btnSave.setOnClickListener(view -> {
            if (checkForUnsavedData() || unSavedData) {
                unSavedData = false;
                btnSave.setText(R.string.saving);
                if (listPayments.isEmpty()) {
                    fileHelper.savePaymentDetails(getApplicationContext(), listPayments, this, false);
                    UtilFunctions.showToast(getApplicationContext(), getString(R.string.all_records_deleted));
                } else {
                    fileHelper.savePaymentDetails(getApplicationContext(), listPayments, this, true);
                }
            } else {
                UtilFunctions.showToast(getApplicationContext(), getString(R.string.data_already_saved));
            }
        });
    }

    private boolean checkForUnsavedData() {
        for (PaymentDetails paymentDetails : listPayments) {
            if (!paymentDetails.saved) {
                return true;
            }
        }
        return false;
    }

    private void initUi() {
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvNoPayments = findViewById(R.id.tvNoPayments);
        chipGroup = findViewById(R.id.chipGrp);
        btnSave = findViewById(R.id.btnSave);
    }

    @Override
    public void onPaymentEntered(PaymentDetails paymentDetails) {
        tvNoPayments.setVisibility(View.GONE);
        listPayments.add(paymentDetails);
        addChip(paymentDetails);
        UtilFunctions.showToast(getApplicationContext(), String.format(getString(R.string.payment_added), paymentDetails.paymentType));
        updateTotalAmount();
    }

    @Override
    public void onDataSaved(boolean success, boolean showMsg) {
        btnSave.setText(getString(R.string.save));
        if (success) {
            for (PaymentDetails paymentDetails : listPayments) {
                paymentDetails.saved = true;
            }
        }
        if (showMsg) {
            if (success) {
                UtilFunctions.showToast(getApplicationContext(), getString(R.string.payment_save_success));
            } else {
                UtilFunctions.showToast(getApplicationContext(), getString(R.string.failed_to_save_data));
            }
        }
    }

    @Override
    public void onDataRetrieved(List<PaymentDetails> paymentDetailsList) {
        if (paymentDetailsList.isEmpty()) {
            tvNoPayments.setVisibility(View.VISIBLE);
        } else {
            tvNoPayments.setVisibility(View.GONE);
            listPayments.addAll(paymentDetailsList);
            for (PaymentDetails paymentDetails : listPayments) {
                paymentDetails.saved = true;
                addChip(paymentDetails);
            }
            UtilFunctions.showToast(getApplicationContext(), getString(R.string.saved_payment_retrieved));
            updateTotalAmount();
        }
    }

    private void updateTotalAmount() {
        totalAmount = 0;
        for (PaymentDetails paymentDetails : listPayments) {
            totalAmount += paymentDetails.amount;
        }
        tvTotalAmount.setText(HtmlCompat.fromHtml(String.format(getString(R.string.total_amount), totalAmount), HtmlCompat.FROM_HTML_MODE_COMPACT));
    }

    public void addChip(PaymentDetails paymentDetails) {
        Chip chip = new Chip(this);
        chip.setText(String.format(getString(R.string.payment_amount), paymentDetails.paymentType, paymentDetails.amount));
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(view -> {
                    chipGroup.removeView(chip);
                    unSavedData = true;
                    listPayments.remove(paymentDetails);
                    updateTotalAmount();
                }
        );
        chipGroup.addView(chip);
    }
}
