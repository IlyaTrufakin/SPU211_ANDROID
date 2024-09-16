package itstep.learning.spu211;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //R - resource
        Button calcButton = findViewById(R.id.main_calc_button);
        calcButton.setOnClickListener(this::onCalcClick);

    }


    private void onCalcClick(View view) {
        //Toast.makeText(this, "OnCalcClick", Toast.LENGTH_LONG).show();
        Intent activityIntent = new Intent(     //завдання для запуску
                getApplicationContext(),        // як "наши", так і системні - фотоаппарат, тлф
                CalcActivity.class              //після виконання буде поверненя до данного intent
        );

        startActivity( activityIntent);
    }
}