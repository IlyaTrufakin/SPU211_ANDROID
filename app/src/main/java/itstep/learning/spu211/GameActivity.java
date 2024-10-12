package itstep.learning.spu211;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final int N = 4;
    private final String bestScoreFilename = "best_score.dat";
    private final Random random = new Random();

    private int[][] cells = new int[N][N];
    private TextView[][] tvCells = new TextView[N][N];
    private Animation fadeInAnimation;
    private TextView tvScore;
    private TextView tvBestScore;
    private long score;
    private long bestScore;

    @SuppressLint({"DiscouragedApi", "ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        LinearLayout gameField = findViewById(R.id.game_field);
        tvScore = findViewById(R.id.score);
        tvBestScore = findViewById(R.id.game_tv_best_score);
        findViewById(R.id.game_btn_undo).setOnClickListener(this::undoClick);
        gameField.post(() -> {

            int w = this.getWindow().getDecorView().getWidth();
            int p = 2 * 3;

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(  //
                    w - p,   // однакова ширина
                    w - p    // та висота
            );
            layoutParams.setMargins(5, 10, 5, 10);
            layoutParams.gravity = Gravity.CENTER;
            gameField.setLayoutParams(layoutParams);
        });
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j] = findViewById(
                        getResources()   // R
                                .getIdentifier(
                                        ("game_cell_" + i) + j,
                                        "id",  // .id
                                        getPackageName()
                                )

                );
            }
        }
        int val = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                cells[i][j] = val;

            }
        }
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeInAnimation.reset();
        gameField.setOnTouchListener(
                new SwipeTouchListener(this) {
                    @Override
                    public void onSwipeBottom() {
                        spawnCell();
                        Toast.makeText(GameActivity.this, "onSwipeBottom", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSwipeLeft() {
                        if (moveLeft()) {
                            spawnCell();
                        } else {
                            Toast.makeText(GameActivity.this, "Немає ходу", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeRight() {
                        if (moveRight()) {
                            spawnCell();
                        } else {
                            Toast.makeText(GameActivity.this, "Немає ходу", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeTop() {
                        spawnCell();
                        Toast.makeText(GameActivity.this, "onSwipeTop", Toast.LENGTH_SHORT).show();
                    }
                });

        startGame();
    }

    private void startGame() {
        addScore(-score);
        loadMaxScore();
        showMaxScore();
        spawnCell();
    }

    private void undoClick(View view){
new AlertDialog.Builder(this,
        com.google.android.material.R.style.Base_Theme_AppCompat_Dialog)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle("Dialog example")
        .setMessage("Приклад модального діалогу")
        .setCancelable(true)
        .setPositiveButton("Добре", (dialog, which) -> {})
        .setNegativeButton("Закрити", (dialog, which) -> this.finish())
        .setNeutralButton("Нова гра", (dialog, which) -> this.startGame())
        .show();
    }

    private void saveMaxScore() {
        try (FileOutputStream fos = openFileOutput(
                bestScoreFilename,
                Context.MODE_PRIVATE);
             DataOutputStream writer = new DataOutputStream(fos);
        ) {
            writer.writeLong(bestScore);
            writer.flush();
        } catch (IOException ex) {
            Log.e("SaveMaxScore", "fos" + ex.getMessage());
        }
    }

    private void loadMaxScore() {
        try (FileInputStream fis = openFileInput(
                bestScoreFilename);
             DataInputStream Reader = new DataInputStream(fis);
        ) {
            bestScore = Reader.readLong();
        } catch (IOException ex) {
            Log.e("loadMaxScore", "fis" + ex.getMessage());
        }
    }


    private void showMaxScore() {
        tvBestScore.setText(
                getString(
                        R.string.game_best_score_tpl,
                        bestScore
                )
        );
    }


    private void addScore(long value) {
        score += value;
        tvScore.setText(
                getString(
                        R.string.game_score_tpl,
                        score
                )
        );
        if(score>bestScore){
            bestScore = score;
            saveMaxScore();
            showMaxScore();
        }
    }

    @SuppressLint("DiscouragedApi")
    private void showField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j].setText(String.valueOf(cells[i][j]));
                int val = cells[i][j];
                if (val > 4096) val = 4096;
                tvCells[i][j].setTextAppearance(
                        getResources().getIdentifier(
                                "game_cell_" + val,
                                "style",
                                getPackageName()
                        )
                );
                tvCells[i][j].setBackgroundColor(
                        getResources().getColor(
                                getResources().getIdentifier(
                                        "game_digit_" + val,
                                        "color",
                                        getPackageName()
                                ),
                                getTheme()
                        )
                );
            }
        }
    }

    private boolean moveLeft() {

        boolean wasMove = false;
        for (int i = 0; i < N; i++) {

            int pos1 = -1;
            for (int j = 0; j < N; j++) {
                if (cells[i][j] != 0) {
                    if (pos1 != -1) {
                        if (cells[i][pos1] == cells[i][j]) {  // з'єднання
                            cells[i][pos1] += cells[i][j];
                            cells[i][j] = 0;
                            addScore(cells[i][pos1]);
                            pos1 = -1;
                            wasMove = true;
                        } else {
                            pos1 = j;
                        }
                    } else {
                        pos1 = j;
                    }
                }
            }


            pos1 = -1;  // позиція нуля
            for (int j = 0; j < N; j++) {
                if (cells[i][j] == 0) {
                    if (pos1 == -1) pos1 = j;
                } else {
                    if (pos1 != -1) {
                        cells[i][pos1] = cells[i][j];
                        cells[i][j] = 0;
                        pos1++;
                        wasMove = true;
                    }
                }
            }
        }
        return wasMove;
    }

    private boolean moveRight() {
        boolean wasMove = false;
        for (int i = 0; i < N; i++) {  //цикл по рядках

            int pos1 = -1;
            for (int j = N - 1; j >= 0; j--) {
                if (cells[i][j] != 0) {
                    if (pos1 != -1) {  //раніше було число лівіше
                        if (cells[i][pos1] == cells[i][j]) {
                            cells[i][pos1] += cells[i][j];
                            cells[i][j] = 0;
                            addScore(cells[i][pos1]);
                            pos1 = -1;
                            wasMove = true;
                        } else {
                            pos1 = j;
                        }
                    } else {
                        pos1 = j;
                    }
                }
            }

            pos1 = -1;
            for (int j = N - 1; j >= 0; j--) {
                if (cells[i][j] == 0) {
                    if (pos1 == -1) pos1 = j;
                } else {
                    if (pos1 != -1) {
                        cells[i][pos1] = cells[i][j];
                        cells[i][j] = 0;
                        pos1--;
                        wasMove = true;
                    }
                }
            }
        }
        return wasMove;
    }


    private boolean spawnCell() {
        List<Coord> coords = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (cells[i][j] == 0) {
                    coords.add(new Coord(i, j));
                }
            }
        }
        if (coords.isEmpty()) {
            return false;
        }

        int randomIndex = random.nextInt(coords.size());
        Coord randomCoord = coords.get(randomIndex);

        cells[randomCoord.i][randomCoord.j] = random.nextInt(10) == 0 ? 4 : 2;
        tvCells[randomCoord.i][randomCoord.j].startAnimation(fadeInAnimation);

        showField();
        return true;
    }

    static class Coord {
        int i;
        int j;

        public Coord(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }
}

