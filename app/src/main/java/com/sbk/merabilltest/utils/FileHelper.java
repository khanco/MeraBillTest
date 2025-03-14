package com.sbk.merabilltest.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sbk.merabilltest.models.PaymentDetails;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileHelper {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final String fileName = "LastPayment.txt";

    public void savePaymentDetails(Context context, List<PaymentDetails> paymentList, FileReadWriteCallback callback, boolean showMsg) {
        executorService.execute(() -> {
            Gson gson = new Gson();
            String data = gson.toJson(paymentList);
            boolean success;
            try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
                fos.write(data.getBytes());
                success = true;
            } catch (IOException e) {
                Log.e("Error", Objects.requireNonNull(e.getMessage()));
                success = false;
            }
            boolean finalSuccess = success;
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onDataSaved(finalSuccess, showMsg);
                }
            });
        });
    }

    public void loadSavedPaymentDetails(Context context, FileReadWriteCallback callback) {
        executorService.execute(() -> {
            Type listType = new TypeToken<List<PaymentDetails>>() {
            }.getType();
            Gson gson = new Gson();
            List<PaymentDetails> dataList = null;
            try {
                dataList = gson.fromJson(getStringBuilder(context).toString(), listType);
            } catch (IOException e) {
                Log.e("Error", Objects.requireNonNull(e.getMessage()));
            }

            List<PaymentDetails> finalDataList = dataList == null ? Collections.emptyList() : dataList;
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onDataRetrieved(finalDataList);
                }
            });
        });
    }

    @NonNull
    private StringBuilder getStringBuilder(Context context) throws IOException {
        File file = new File(context.getFilesDir(), fileName);
        if (!file.exists()) {
            throw new IOException("File not found: " + fileName);
        }

        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
        }
        return jsonString;
    }

    public interface FileReadWriteCallback {
        void onDataSaved(boolean success, boolean showMsg);

        void onDataRetrieved(List<PaymentDetails> paymentDetailsList);
    }
}
