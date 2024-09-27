package itstep.learning.spu211;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalcActivity extends AppCompatActivity {
    private final int maxDigits = 11;
    private TextView tvHistory;
    private TextView tvResult;
    private String zeroSign;
    private String dotSign;
    private String minusSign;
    private String squareSign;
    private String msgOverflow;
    private boolean needClearResult;


    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);

        // Основной контейнер, для которого будут применяться системные вставки
        View container = findViewById(R.id.calc_layout);

        ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

         //   v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         //   return insets;

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = systemBars.top;
            params.bottomMargin = systemBars.bottom;
            params.leftMargin = systemBars.left;
            params.rightMargin = systemBars.right;
            v.setLayoutParams(params);

            return WindowInsetsCompat.CONSUMED;
        });

        tvHistory = findViewById(R.id.calc_tv_history);
        tvResult = findViewById(R.id.calc_tv_result);
        zeroSign = getString(R.string.calc_btn_digit_0);
        dotSign = getString(R.string.calc_btn_dot);
        minusSign = getString(R.string.calc_minusSign);
        squareSign = getString(R.string.calc_btn_square);
        msgOverflow = getString(R.string.calc_msg_overflow);
        findViewById(R.id.calc_btn_clear_C).setOnClickListener(this::clearClick);
        findViewById(R.id.calc_btn_dot).setOnClickListener(this::dotClick);
        findViewById(R.id.calc_btn_sign_toggle).setOnClickListener(this::signToggleClick);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::backspaceClick);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::squareClick);
        for (int i = 0; i < 10; i++) {
            findViewById(//R.id.calc_btn....
                    getResources() //R
                            .getIdentifier(//id
                                    "calc_btn_digit_" + i,
                                    "id",
                                    getPackageName()
                            )
            ).setOnClickListener(this::digitClick);
        }

        if (savedInstanceState == null) {
            this.clearClick(null);
            needClearResult = false;
        }


    }

    /*
    при изменении конфигурации происходит пересбирание активности
    из-за перезапуска исчезают данные
    для их сохранения - необходимо использовать события жизненого цикла активности
     */

    private void squareClick(View view) {
        String result = tvResult.getText().toString();
        tvHistory.setText(String.format("%s%s", result, squareSign));
        result = result
                .replace(zeroSign, "0")
                .replace(minusSign, "-")
                .replace(dotSign, ".");
        double x = Double.parseDouble(result);
        needClearResult = true;
        showResult(x *x);

    }


    private void showResult (double x) {
        if(x >= 1e11 || x<= - 1e11) {
            tvResult.setText(msgOverflow);
            return;
        }

        String result = x == (int) x
                ? String.valueOf((int)x)
                :String.valueOf(x);

        result = result
                .replace("0", zeroSign)
                  .replace("-", minusSign)
                   .replace(".", dotSign);
        int limit = maxDigits;
        if(result.startsWith(minusSign)){
            limit +=1;
        }
        if(result.contains(dotSign)){
            limit +=1;
        }
        if(result.length() > limit) {
            result =result.substring(0 , limit);
        }

        tvResult.setText(result);

    }
      /*
    операции
     */


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState - map которая сохраняет разл. типы данных по принципу ключ-значение
        outState.putCharSequence("savedResult", tvResult.getText());
        outState.putBoolean("needClearResult", needClearResult);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("savedResult"));
        needClearResult = savedInstanceState.getBoolean("needClearResult");

    }

    private void clearClick(View view) {
        tvHistory.setText("");
        tvResult.setText(zeroSign);
    }

    private void digitClick(View view) {
        String result = needClearResult ? "" : tvResult.getText().toString();
        needClearResult = false;
        if (digitLength(result) >= maxDigits) {
            Toast.makeText(this, R.string.calc_msgMaxDegReached, Toast.LENGTH_SHORT).show();
            return;
        }
        if (zeroSign.equals(result)) {
            result = "";
        }
        result += ((Button) view).getText();
        tvResult.setText(result);
    }

    private void dotClick(View view) {
        String res = tvResult.getText().toString();
        if (res.contains(dotSign)) {
            if (res.endsWith(dotSign)) {
                res = res.substring(0, res.length() - 1);

            } else {
                Toast.makeText(this, R.string.calc_msgTwoDots, Toast.LENGTH_SHORT).show();
                return;
            }

        } else {
            res = res + dotSign;
        }
        tvResult.setText(res);
    }

    private void signToggleClick(View view) {
        String res = tvResult.getText().toString();
        if (zeroSign.equals(res)) {
            Toast.makeText(this, R.string.calc_msgMinusZero, Toast.LENGTH_SHORT).show();
            return;
        }
        if (res.startsWith(minusSign)) {
            res = res.substring(minusSign.length());
        } else {
            res = minusSign + res;
        }
        tvResult.setText(res);
    }

    private void backspaceClick(View view) {
        String result = tvResult.getText().toString();
        int len = result.length();
        if (len > 1) {
            result = result.substring(0, len - 1);
            if (minusSign.equals(result)) {
                result = zeroSign;
            }
        } else {
            result = zeroSign;
        }
        tvResult.setText(result);
    }

    private int digitLength(String input) {
        //подсчет длины строки цифр
        int ret = input.length();
        if (input.startsWith(minusSign)) {
            ret -= 1;
        }
        if (input.contains(dotSign)) {
            ret -= 1;
        }
        return ret;

    }

}
