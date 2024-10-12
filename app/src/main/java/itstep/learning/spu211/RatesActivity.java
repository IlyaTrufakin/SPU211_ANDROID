package itstep.learning.spu211;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class RatesActivity extends AppCompatActivity {
    private final static String nbuUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private LinearLayout ratesContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rates);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ratesContainer = findViewById(R.id.rates_ll_container);
        ratesContainer.post(() -> new Thread(this::loadRates).start());
    }


    private void loadRates() {
        try {
            URL url = new URL(nbuUrl);
            InputStream urlStream = url.openStream();
            String jsonString = readAsString(urlStream);
            runOnUiThread(() -> showRates(jsonString));
            urlStream.close();

        } catch (
                MalformedURLException ex) {
            Log.d("LoadRates", "MalformedURLException" + ex.getMessage());
        } catch (
                IOException ex) {
            Log.d("LoadRates", "IOException" + ex.getMessage());
        } catch (
                android.os.NetworkOnMainThreadException ex) {
            Log.d("LoadRates", "NetworkOnMainThreadException" + ex.getMessage());
        } catch (
                java.lang.SecurityException ex) {
            Log.d("LoadRates", "SecurityException" + ex.getMessage());
        }


    }

    private void showRates(String jsonString) {
        JSONArray rates;
        try {
            rates = new JSONArray(jsonString);
            for (int i = 0; i < rates.length(); i++) {
                JSONObject rate = rates.getJSONObject(i);
// "txt": "Австралійський долар",
//        "rate": 27.7244,

                TextView tv = new TextView(RatesActivity.this);
                TextView tvRates = new TextView(RatesActivity.this);
                tv.setText(rate.getString("txt"));
                tvRates.setText(rate.getString("rate"));
                tv.setBackground(AppCompatResources.getDrawable(
                        getApplicationContext(),
                        R.drawable.game_btn_top_right_bottom_with_background));

                tv.setPadding(10, 5, 10, 5);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins(10, 5, 10, 5);
                tv.setLayoutParams(layoutParams);

                ratesContainer.addView(tv);
                ratesContainer.addView(tvRates);
            }

        } catch (JSONException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }


    }

    private String readAsString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            byteBuilder.write(buffer, 0, len);
        }
        return byteBuilder.toString();
    }

//https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json


}


//проблеми при підключенні:
//android.os.NetworkOnMainThreadException
// в тому потоці де працює юзер интерфейс - неможливо підключення до мережі

//java.lang.SecurityException: Permission denied (missing INTERNET permission?)
// для доступу до мережі необхідно заявити в маніфесті
//  <uses-permission android:name="android.permission.INTERNET" />

//Can't toast on a thread that has not called Looper.prepare()
//запущені активності в іншому потоці ніж UI, не можуть мати доступ до UI у тому числі тостів
//