package itstep.learning.spu211;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.game_layout)
                .setOnTouchListener(
                        new SwipeTouchListener(this) {
                            @Override
                            public void onSwipeBottom() {
                                Toast.makeText(GameActivity.this, "Swipe Bottom", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSwipeLeft() {
                                Toast.makeText(GameActivity.this, "Swipe Left", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSwipeRight() {
                                Toast.makeText(GameActivity.this, "Swipe Right", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSwipeTop() {
                                Toast.makeText(GameActivity.this, "Swipe Top", Toast.LENGTH_SHORT).show();
                            }

                        }
                );
    }
}