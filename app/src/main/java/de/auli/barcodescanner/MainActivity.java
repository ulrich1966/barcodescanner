package de.auli.barcodescanner;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    Button cmdQrCode;
    Button cmdProduct;
    Button cmdOther;
    TextView txtState;
    TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cmdQrCode = findViewById(R.id.cmd_qrcode);
        cmdProduct = findViewById(R.id.cmd_product);
        cmdOther = findViewById(R.id.cmd_other);
        txtState = findViewById(R.id.txt_state);
        txtResult = findViewById(R.id.txt_result);
    }

    public void qrcodeHandler(View view) {
        Toast.makeText(this, "QR Code clicked", Toast.LENGTH_SHORT).show();

        productHandler(view);

    }

    public void productHandler(View view) {
        Toast.makeText(this, "Product clicked", Toast.LENGTH_SHORT).show();
        // data/data/com.google.zxing.client.android
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        if (intent == null) {
            Toast.makeText(this, "No Intent", Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra("SCAN_MODE", view.getId());
        try {
            startActivityForResult(intent, 1);
        } catch (Exception e) {
            Toast.makeText(this, "Scanner nicht installiert!", Toast.LENGTH_SHORT).show();
        }
    }

    public void otherHandler(View view) {
        Toast.makeText(this, "Other clicked", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            txtState.setText(data.getStringExtra("SCAN_RESULT_FORMAT"));
            txtResult.setText(getProductName(data.getStringExtra("SCAN_RESULT")));
        } else if (resultCode == RESULT_CANCELED) {
            String msg = "Scan wurde abgebrochen";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Log.e(TAG, msg);
            txtResult.setText(msg);
        } else {
            String msg = "Fehler Req Code: " + requestCode;
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Log.e(TAG, msg);
            txtResult.setText(msg);
        }
    }

    private String getProductName(String scanResult) {
        HoleDatenTask task = new HoleDatenTask();
        String product$ = "product";
        String name = "product_name";
        String result = "";
        try {
            String jsonResult = task.execute(scanResult).get();
            JSONObject root = new JSONObject(jsonResult);
            Log.d(TAG, "Result:\n" + root.toString());
            if (root.has(product$)) {
                JSONObject product = root.getJSONObject(product$);
                if (product.has("product_name")) {
                    result = product.get(name).toString().trim();
                }
            } else {
                Log.d(TAG, product$ + " nicht gefunden");
                result = "Kein Produkt gefunden";
            }
        } catch (Exception e) {
            Log.e(TAG, "Fehler", e);
        }
        return result;
    }

    public class HoleDatenTask extends AsyncTask<String, Void, String> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param strings The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected String doInBackground(String... strings) {
            final String baseUrl$ = "https://world.openfoodfacts.org/api/v0/product";
            final String requestUrl$ = String.format("%s/%s.json", baseUrl$, strings[0]);
            URL requestUrl = null;
            StringBuilder sb = new StringBuilder();
            try {
                Log.d(TAG, "Request:  " + requestUrl$);
                requestUrl = new URL(requestUrl$);
                InputStream is = requestUrl.openConnection().getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    sb.append(String.format("%s\n", line));
                }
            } catch (Exception e) {
                Log.e(TAG, "Fehler", e);
            }
            return sb.toString();
        }
    }
}
