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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class CalcActivity extends AppCompatActivity {
    private final int maxDigits = 11;
    //---- отображение
    private TextView tvHistory;
    private TextView tvResult;
    private String view_zeroSign;
    private String view_dotSign;
    private String view_minusSign;
    private String msgOverflow;

    //---- операции
    private String plusSign;
    private String minusSign;
    private String multiplySign;
    private String divideSign;
    private String squareSign;
    private String sqrtSign;
    private String percentSign;
    private String inverseSign;
    private int typeOfOperation = 0;
    private Map<Integer, BiFunction<Double, Double, Double>> operations;
    //---- операнды
    private double firstOperand;
    private double currentResult;
    //---- логические флаги
    private boolean needClearResult;
    private boolean isNewOperation = true;


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

        initializeOperations();
        tvHistory = findViewById(R.id.calc_tv_history);
        tvResult = findViewById(R.id.calc_tv_result);
        view_zeroSign = getString(R.string.calc_btn_digit_0);
        view_dotSign = getString(R.string.calc_btn_dot);
        view_minusSign = getString(R.string.calc_view_minusSign);
        plusSign = getString(R.string.calc_btn_operation_plus);
        minusSign = getString(R.string.calc_btn_operation_minus);
        multiplySign = getString(R.string.calc_btn_operation_multiply);
        divideSign = getString(R.string.calc_btn_operation_divide);
        squareSign = getString(R.string.calc_btn_square);
        sqrtSign = getString(R.string.calc_btn_sqrt);
        percentSign = getString(R.string.calc_btn_percent);
        inverseSign = getString(R.string.calc_btn_inverse);
        msgOverflow = getString(R.string.calc_msg_overflow);
        findViewById(R.id.calc_btn_clear_C).setOnClickListener(this::clearClick);
        findViewById(R.id.calc_btn_dot).setOnClickListener(this::dotClick);
        findViewById(R.id.calc_btn_sign_toggle).setOnClickListener(this::signToggleClick);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::backspaceClick);

        findViewById(R.id.calc_btn_operation_plus).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_operation_minus).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_operation_multiply).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_operation_divide).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_percent).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::operationClick);
        findViewById(R.id.calc_btn_equal).setOnClickListener(this::equalsClick);
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


    private void initializeOperations() {
        operations = new HashMap<>();
        operations.put(1, this::add);
        operations.put(2, this::subtract);
        operations.put(3, this::multiply);
        operations.put(4, this::divide);
        operations.put(5, this::square);
        operations.put(6, this::sqrt);
        operations.put(7, this::percent);
        operations.put(8, this::inverse);
    }


    // Бинарные операции
    private double add(double a, double b) { return a + b; }
    private double subtract(double a, double b) { return a - b; }
    private double multiply(double a, double b) { return a * b; }
    private double divide(double a, double b) {
        if (b == 0) throw new ArithmeticException("Division by zero");
        return a / b;
    }

    // Унарные операции
    private double percent(double a, double ignored) { return a / 100; }
    private double square(double a, double ignored) { return a * a; }

    private double sqrt(double a, double ignored) {
        if (a < 0) throw new ArithmeticException("Square root of negative number");
        return Math.sqrt(a);
    }
    private double inverse(double a, double ignored) {
        if (a == 0) throw new ArithmeticException("Division by zero");
        return 1 / a;
    }


    /*
    при изменении конфигурации происходит пересбирание активности
    из-за перезапуска исчезают данные
    для их сохранения - необходимо использовать события жизненого цикла активности
     */


    private void operationClick(View view) {
        String operation = ((Button) view).getText().toString();
        String result = tvResult.getText().toString();
        double operand = parseResult(result);

        int operationType;
        if (operation.equals(plusSign)) operationType = 1;
        else if (operation.equals(minusSign)) operationType = 2;
        else if (operation.equals(multiplySign)) operationType = 3;
        else if (operation.equals(divideSign)) operationType = 4;
        else if (operation.equals(squareSign)) operationType = 5;
        else if (operation.equals(sqrtSign)) operationType = 6;
        else if (operation.equals(percentSign)) operationType = 7;
        else if (operation.equals(inverseSign)) operationType = 8;
        else return;

        if (operationType <= 4) { // Бинарные операции
            if (!isNewOperation) {
                equalsClick(null);
            }
            firstOperand = operand;
            currentResult = firstOperand;
            typeOfOperation = operationType;
            tvHistory.setText(String.format("%s %s", result, operation));
            needClearResult = true;
            isNewOperation = false;
        } else { // Унарные операции
            BiFunction<Double, Double, Double> unaryOperation = operations.get(operationType);
            if (unaryOperation != null) {
                try {
                    double newResult = unaryOperation.apply(operand, 0.0); // Игнорируем второй аргумент
                    String history = String.format("%s(%s)", operation, result);
                    showResult(newResult, history);
                } catch (ArithmeticException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            needClearResult = true;
            isNewOperation = true;
        }
    }


    private void equalsClick(View view) {
        if (typeOfOperation == 0) return;

        String result = tvResult.getText().toString();
        String history = tvHistory.getText().toString();
        double secondOperand = parseResult(result);

        BiFunction<Double, Double, Double> operation = operations.get(typeOfOperation);
        if (operation != null) {
            try {
                currentResult = operation.apply(currentResult, secondOperand);
                history += " " + result + " =";
                showResult(currentResult, history);
            } catch (ArithmeticException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        needClearResult = true;
        isNewOperation = true;
        typeOfOperation = 0;
    }



    private void showResult(double x, String history) {
        if (x >= 1e11 || x <= -1e11) {
            tvResult.setText(msgOverflow);
            return;
        }

        String result = x == (int) x
                ? String.valueOf((int)x)
                : String.valueOf(x);

        result = result
                .replace("0", view_zeroSign)
                .replace("-", view_minusSign)
                .replace(".", view_dotSign);

        int limit = maxDigits;
        if (result.startsWith(view_minusSign)) {
            limit += 1;
        }
        if (result.contains(view_dotSign)) {
            limit += 1;
        }
        if (result.length() > limit) {
            result = result.substring(0, limit);
        }

        tvResult.setText(result);
        tvHistory.setText(history);
    }
      /*
    операции
     */


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState - map которая сохраняет разл. типы данных по принципу ключ-значение
        outState.putCharSequence("savedResult", tvResult.getText());
        outState.putCharSequence("savedHistory", tvHistory.getText());
        outState.putBoolean("needClearResult", needClearResult);
        outState.putDouble("firstOperand", firstOperand);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("savedResult"));
        tvHistory.setText(savedInstanceState.getCharSequence("savedHistory"));
        needClearResult = savedInstanceState.getBoolean("needClearResult");
        firstOperand = savedInstanceState.getDouble("firstOperand");
    }

    private void clearClick(View view) {
        tvHistory.setText("");
        tvResult.setText(view_zeroSign);
        currentResult = 0;
        isNewOperation = true;
        typeOfOperation = 0;
    }

    private void digitClick(View view) {
        String result = needClearResult ? "" : tvResult.getText().toString();
        needClearResult = false;
        if (digitLength(result) >= maxDigits) {
            Toast.makeText(this, R.string.calc_msgMaxDegReached, Toast.LENGTH_SHORT).show();
            return;
        }
        if (view_zeroSign.equals(result)) {
            result = "";
        }
        result += ((Button) view).getText();
        tvResult.setText(result);
    }

    private void dotClick(View view) {
        String res = tvResult.getText().toString();
        if (res.contains(view_dotSign)) {
            if (res.endsWith(view_dotSign)) {
                res = res.substring(0, res.length() - 1);

            } else {
                Toast.makeText(this, R.string.calc_msgTwoDots, Toast.LENGTH_SHORT).show();
                return;
            }

        } else {
            res = res + view_dotSign;
        }
        tvResult.setText(res);
    }

    private void signToggleClick(View view) {
        String res = tvResult.getText().toString();
        if (view_zeroSign.equals(res)) {
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
                result = view_zeroSign;
            }
        } else {
            result = view_zeroSign;
        }
        tvResult.setText(result);
    }

    private int digitLength(String input) {
        //подсчет длины строки цифр
        int ret = input.length();
        if (input.startsWith(minusSign)) {
            ret -= 1;
        }
        if (input.contains(view_dotSign)) {
            ret -= 1;
        }
        return ret;

    }

    private double parseResult(String result) {
        return Double.parseDouble(result
                .replace(view_zeroSign, "0")
                .replace(minusSign, "-")
                .replace(view_dotSign, "."));
    }
}
