package com.sbk.merabilltest.utils;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;

import com.sbk.merabilltest.R;
import com.sbk.merabilltest.models.PaymentDetails;

import java.util.ArrayList;
import java.util.Objects;

public class AddPaymentDialogHelper {

    final String paymentTypeCash = "Cash";
    final String paymentTypeBankTransfer = "Bank Transfer";
    final String paymentTypeCreditCard = "Credit Card";
    PaymentDetailsCallback callback;
    PaymentDetails paymentDetails;

    public void showAddPaymentDialog(Activity activity, ArrayList<PaymentDetails> addedListPayments) {
        callback = (PaymentDetailsCallback) activity;
        ArrayList<String> paymentTypes = getPaymentTypes(addedListPayments);

        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.dialog_add_payment, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }

        EditText edtAmount = dialogView.findViewById(R.id.edtAmount);
        EditText edtProvider = dialogView.findViewById(R.id.edtProvider);
        EditText edtReference = dialogView.findViewById(R.id.edtReference);
        Spinner spnPaymentTypes = dialogView.findViewById(R.id.spnPaymentType);
        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        spnPaymentTypes.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, paymentTypes));

        spnPaymentTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (Objects.equals(paymentTypes.get(i), paymentTypeBankTransfer) || Objects.equals(paymentTypes.get(i), paymentTypeCreditCard)) {
                    edtReference.setVisibility(View.VISIBLE);
                    edtProvider.setVisibility(View.VISIBLE);
                } else {
                    edtProvider.setVisibility(View.GONE);
                    edtReference.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnOk.setOnClickListener(view -> {
            if (edtAmount.getText().length() == 0) {
                UtilFunctions.showToast(activity, activity.getString(R.string.add_amount_to_save));
            } else {
                try {
                    int amount = Integer.parseInt(edtAmount.getText().toString());
                    if (amount > 0) {
                        paymentDetails = new PaymentDetails();
                        paymentDetails.amount = amount;
                        paymentDetails.paymentType = paymentTypes.get(spnPaymentTypes.getSelectedItemPosition());
                        paymentDetails.provider = edtProvider.getText().toString();
                        paymentDetails.reference = edtReference.getText().toString();
                        callback.onPaymentEntered(paymentDetails);
                        alertDialog.dismiss();
                    } else {
                        UtilFunctions.showToast(activity, activity.getString(R.string.amount_max_than_zero));
                    }
                } catch (Exception e) {
                    UtilFunctions.showToastLong(activity, String.format(activity.getString(R.string.invalid_amount), Integer.MAX_VALUE));
                }
            }
        });

        btnCancel.setOnClickListener(view -> alertDialog.dismiss());

        alertDialog.show();
    }

    private ArrayList<String> getPaymentTypes(ArrayList<PaymentDetails> addedListPayments) {
        ArrayList<String> paymentTypes = new ArrayList<>();

        paymentTypes.add(paymentTypeCash);
        paymentTypes.add(paymentTypeBankTransfer);
        paymentTypes.add(paymentTypeCreditCard);

        for (PaymentDetails addedListPayment : addedListPayments) {
            switch (addedListPayment.paymentType) {
                case paymentTypeCash: {
                    paymentTypes.remove(paymentTypeCash);
                    break;
                }
                case paymentTypeBankTransfer: {
                    paymentTypes.remove(paymentTypeBankTransfer);
                    break;
                }
                case paymentTypeCreditCard: {
                    paymentTypes.remove(paymentTypeCreditCard);
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + addedListPayment.paymentType);
            }
        }

        return paymentTypes;
    }

    public interface PaymentDetailsCallback {
        void onPaymentEntered(PaymentDetails paymentDetails);
    }
}
