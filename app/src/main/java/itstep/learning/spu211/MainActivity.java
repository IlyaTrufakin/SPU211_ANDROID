package itstep.learning.spu211;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
        findViewById(R.id.main_anim_button).setOnClickListener(this::onAnimClick);
        findViewById(R.id.main_game_button).setOnClickListener(this::onGameClick);
    }


    private void onCalcClick(View view) {
        //Toast.makeText(this, "OnCalcClick", Toast.LENGTH_LONG).show();
        Intent activityIntent = new Intent(     //завдання для запуску
                getApplicationContext(),        // як "наши", так і системні - фотоаппарат, тлф
                CalcActivity.class              //після виконання буде поверненя до данного intent
        );

        startActivity(activityIntent);
    }

    private void onAnimClick(View view) {
        startActivity(new Intent(     //завдання для запуску
                getApplicationContext(),        // як "наши", так і системні - фотоаппарат, тлф
                AnimActivity.class));

    }


    private void onGameClick(View view) {
        startActivity(new Intent(     //завдання для запуску
                getApplicationContext(),        // як "наши", так і системні - фотоаппарат, тлф
                GameActivity.class));

    }
}
