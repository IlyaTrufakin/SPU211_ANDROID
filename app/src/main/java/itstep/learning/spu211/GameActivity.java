package itstep.learning.spu211;

import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
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
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

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
    private int[][] visualEffectTable = new int[N][N];
    private int[][] cells = new int[N][N];
    private TextView[][] tvCells = new TextView[N][N];
    private Animation fadeInAnimation;
    private Animation scale1Animation;
    private Animation rotate1Animation;
    private TextView tvScore;
    private TextView tvBestScore;
    private long score;
    private long bestScore;
    SpringAnimation springAnim;
    SpringForce spring = new SpringForce();
    SpringForce spring2 = new SpringForce();

    @SuppressLint({"DiscouragedApi", "ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        LinearLayout gameField = findViewById(R.id.game_field);
        tvScore = findViewById(R.id.score);
        tvBestScore = findViewById(R.id.game_tv_best_score);
        findViewById(R.id.game_btn_undo).setOnClickListener(this::undoClick);
        findViewById(R.id.game_btn_new).setOnClickListener(this::newClick);

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
        clearGameCells();
        // load animation
        scale1Animation = AnimationUtils.loadAnimation(this, R.anim.scale_1);
        scale1Animation.reset();
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeInAnimation.reset();
        rotate1Animation = AnimationUtils.loadAnimation(this, R.anim.rotate_1);
        rotate1Animation.reset();

        spring.setFinalPosition(0f);  // Конечная позиция пружины
        spring.setStiffness(SpringForce.STIFFNESS_LOW);  // Жесткость пружины (сила сопротивления)
        spring.setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);  // Коэффициент затухания (количество "отскоков")
        spring2.setFinalPosition(0f);  // Конечная позиция пружины
        spring2.setStiffness(SpringForce.STIFFNESS_LOW);  // Жесткость пружины (сила сопротивления)
        spring2.setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);  // Коэффициент затухания (количество "отскоков")

        gameField.setOnTouchListener(
                new SwipeTouchListener(this) {
                    @Override
                    public void onSwipeBottom() {
                        if (moveDown()) {
                            spawnCell();
                        } else {
                            Toast.makeText(GameActivity.this, "Немає ходу", Toast.LENGTH_SHORT).show();
                        }
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
                        if (moveUp()) {
                            spawnCell();
                        } else {
                            Toast.makeText(GameActivity.this, "Немає ходу", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        startGame();
    }


    private void clearGameCells() {
        int val = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                cells[i][j] = val;
                visualEffectTable[i][j] = val;
            }
        }
    }


    private void startGame() {
        addScore(-score);
        loadMaxScore();
        showMaxScore();
        spawnCell();
    }

    private void undoClick(View view) {
        new AlertDialog.Builder(this,
                com.google.android.material.R.style.Base_Theme_AppCompat_Dialog)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Dialog example")
                .setMessage("Приклад модального діалогу")
                .setCancelable(true)
                .setPositiveButton("Добре", (dialog, which) -> {
                })
                .setNegativeButton("Закрити", (dialog, which) -> this.finish())
                .setNeutralButton("Нова гра", (dialog, which) -> this.startGame())
                .show();
    }


    private void newClick(View view) {
        clearGameCells();
        startGame();
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
        if (score > bestScore) {
            bestScore = score;
            saveMaxScore();
            showMaxScore();
        }
    }

    @SuppressLint("DiscouragedApi")
    private void showField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {


                //animation for changed cells
                if (abs(visualEffectTable[i][j]) == 4) {
                    tvCells[i][j].startAnimation(scale1Animation);
                    visualEffectTable[i][j] = 0;

                } else if (abs(visualEffectTable[i][j]) == 8) {
                    tvCells[i][j].startAnimation(rotate1Animation);
                    visualEffectTable[i][j] = 0;
                } else if (visualEffectTable[i][j] > 0 && visualEffectTable[i][j] >= 16) {
                    SpringAnimation springAnim = new SpringAnimation(tvCells[i][j], SpringAnimation.TRANSLATION_X, 0);
                    springAnim.setSpring(spring);
                    springAnim.setStartVelocity(2000);  // Начальная скорость
                    springAnim.start();
                    visualEffectTable[i][j] = 0;
                } else if (visualEffectTable[i][j] < 0 && visualEffectTable[i][j] <= -16) {
                    SpringAnimation springAnim = new SpringAnimation(tvCells[i][j], SpringAnimation.TRANSLATION_Y, 0);
                    springAnim.setSpring(spring2);
                    springAnim.setStartVelocity(2000);  // Начальная скорость
                    springAnim.start();
                    visualEffectTable[i][j] = 0;
                }


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

    private boolean moveUp() {
        boolean wasMove = false;

        for (int j = 0; j < N; j++) {
            int index = 0; // Следующая позиция для размещения плитки сверху
            int previous = 0; // Хранит значение предыдущей плитки
            int previousIndex = -1; // Хранит индекс предыдущей плитки

            for (int i = 0; i < N; i++) {
                int current = cells[i][j];
                if (current == 0) {
                    continue; // Пропускаем пустые ячейки
                }

                if (previous == 0) {
                    // Нет предыдущей плитки для сравнения, сохраняем текущую как предыдущую
                    previous = current;
                    previousIndex = i;
                } else if (previous == current) {
                    // Объединяем плитки
                    cells[index][j] = previous + current;
                    addScore(cells[index][j]);
                    visualEffectTable[index][j] = -cells[index][j]; // Отмечаем позицию объединения
                    if (index != previousIndex || index != i) {
                        wasMove = true; // Плитки были перемещены или объединены
                    }
                    index++;
                    previous = 0;
                    previousIndex = -1;
                } else {
                    // Перемещаем предыдущую плитку в текущую позицию index
                    cells[index][j] = previous;
                    if (index != previousIndex) {
                        wasMove = true; // Плитка была перемещена
                    }
                    index++;
                    previous = current;
                    previousIndex = i;
                }
            }

            if (previous != 0) {
                // Размещаем последнюю плитку, если она не была объединена
                cells[index][j] = previous;
                if (index != previousIndex) {
                    wasMove = true; // Плитка была перемещена
                }
                index++;
            }

            // Устанавливаем оставшиеся ячейки в ноль
            for (int k = index; k < N; k++) {
                if (cells[k][j] != 0) {
                    cells[k][j] = 0;
                    wasMove = true;
                }
            }
        }

        return wasMove;
    }

    private boolean moveDown() {
        boolean wasMove = false;

        for (int j = 0; j < N; j++) {
            int index = N - 1; // Следующая позиция для размещения плитки
            int previous = 0; // Хранит значение предыдущей плитки
            int previousIndex = -1; // Хранит индекс предыдущей плитки

            for (int i = N - 1; i >= 0; i--) {
                int current = cells[i][j];
                if (current == 0) {
                    continue; // Пропускаем пустые ячейки
                }

                if (previous == 0) {
                    // Нет предыдущей плитки для сравнения, сохраняем текущую как предыдущую
                    previous = current;
                    previousIndex = i;
                } else if (previous == current) {
                    // Объединяем плитки
                    cells[index][j] = previous + current;
                    addScore(cells[index][j]);
                    visualEffectTable[index][j] = -cells[index][j]; // Отмечаем позицию объединения
                    if (index != previousIndex || index != i) {
                        wasMove = true; // Плитки были перемещены или объединены
                    }
                    index--;
                    previous = 0;
                    previousIndex = -1;
                } else {
                    // Перемещаем предыдущую плитку в следующую позицию
                    cells[index][j] = previous;
                    if (index != previousIndex) {
                        wasMove = true; // Плитка была перемещена
                    }
                    index--;
                    previous = current;
                    previousIndex = i;
                }
            }

            if (previous != 0) {
                // Размещаем последнюю плитку, если она не была объединена
                cells[index][j] = previous;
                if (index != previousIndex) {
                    wasMove = true; // Плитка была перемещена
                }
                index--;
            }

            // Устанавливаем оставшиеся ячейки в ноль
            for (int k = index; k >= 0; k--) {
                if (cells[k][j] != 0) {
                    cells[k][j] = 0;
                    wasMove = true;
                }
            }
        }

        return wasMove;
    }

    private boolean moveLeft() {
        boolean wasMove = false;

        for (int i = 0; i < N; i++) {
            int index = 0; // Следующая позиция для размещения плитки
            int previous = 0; // Хранит значение предыдущей плитки
            int previousIndex = -1; // Хранит индекс предыдущей плитки

            for (int j = 0; j < N; j++) {
                int current = cells[i][j];
                if (current == 0) {
                    continue; // Пропускаем пустые ячейки
                }

                if (previous == 0) {
                    // Нет предыдущей плитки для сравнения, сохраняем текущую как предыдущую
                    previous = current;
                    previousIndex = j;
                } else if (previous == current) {
                    // Объединяем плитки
                    cells[i][index] = previous + current;
                    addScore(cells[i][index]);
                    visualEffectTable[i][index] = cells[i][index]; // Отмечаем позицию объединения
                    if (index != previousIndex || index != j) {
                        wasMove = true; // Плитки были перемещены или объединены
                    }
                    index++;
                    previous = 0;
                    previousIndex = -1;
                } else {
                    // Перемещаем предыдущую плитку в следующую позицию
                    cells[i][index] = previous;
                    if (index != previousIndex) {
                        wasMove = true; // Плитка была перемещена
                    }
                    index++;
                    previous = current;
                    previousIndex = j;
                }
            }

            if (previous != 0) {
                // Размещаем последнюю плитку, если она не была объединена
                cells[i][index] = previous;
                if (index != previousIndex) {
                    wasMove = true; // Плитка была перемещена
                }
                index++;
            }

            // Устанавливаем оставшиеся ячейки в ноль
            for (int k = index; k < N; k++) {
                if (cells[i][k] != 0) {
                    cells[i][k] = 0;
                    wasMove = true;
                }
            }
        }

        return wasMove;
    }

    private boolean moveRight() {
        boolean wasMove = false;

        for (int i = 0; i < N; i++) {
            int index = N - 1; // Следующая позиция для размещения плитки справа
            int previous = 0; // Хранит значение предыдущей плитки
            int previousIndex = -1; // Хранит индекс предыдущей плитки

            for (int j = N - 1; j >= 0; j--) {
                int current = cells[i][j];
                if (current == 0) {
                    continue; // Пропускаем пустые ячейки
                }

                if (previous == 0) {
                    // Нет предыдущей плитки для сравнения, сохраняем текущую как предыдущую
                    previous = current;
                    previousIndex = j;
                } else if (previous == current) {
                    // Объединяем плитки
                    cells[i][index] = previous + current;
                    addScore(cells[i][index]);
                    visualEffectTable[i][index] = cells[i][index]; // Отмечаем позицию объединения
                    if (index != previousIndex || index != j) {
                        wasMove = true; // Плитки были перемещены или объединены
                    }
                    index--;
                    previous = 0;
                    previousIndex = -1;
                } else {
                    // Перемещаем предыдущую плитку в текущую позицию index
                    cells[i][index] = previous;
                    if (index != previousIndex) {
                        wasMove = true; // Плитка была перемещена
                    }
                    index--;
                    previous = current;
                    previousIndex = j;
                }
            }

            if (previous != 0) {
                // Размещаем последнюю плитку, если она не была объединена
                cells[i][index] = previous;
                if (index != previousIndex) {
                    wasMove = true; // Плитка была перемещена
                }
                index--;
            }

            // Устанавливаем оставшиеся ячейки в ноль
            for (int k = index; k >= 0; k--) {
                if (cells[i][k] != 0) {
                    cells[i][k] = 0;
                    wasMove = true;
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

