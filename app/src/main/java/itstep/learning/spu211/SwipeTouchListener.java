package itstep.learning.spu211;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SwipeTouchListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    public SwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new SwipeGestureListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    // Outer interface - для реалізації у коді
    public void onSwipeBottom() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeRight() {
    }

    public void onSwipeTop() {
    }


    private final class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int minDistance = 70;
        private static final int minVelocity = 100;

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;  // початок жесту swipe
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            boolean isHandled = false;
            if (e1 != null) {
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                // Визначити якого типу цей жест - вертикальний чи горизонтальний (або жоден з них)
                // наприклад, критерій - одна з дельт (за модулем) удвічі більша за іншу
                float absX = Math.abs(deltaX);
                float absY = Math.abs(deltaY);
                if (absX >= 2 * absY) {   // горизонтальний жест, важлива лише velocityX
                    if (absX >= minDistance && Math.abs(velocityX) >= minVelocity) {
                        if (deltaX > 0) {   //  e1.X  ---->  e2.X
                            onSwipeRight();
                        } else {               //  e2.X  <----  e1.X
                            onSwipeLeft();
                        }
                        isHandled = true;
                    }
                } else if (absY >= 2 * absX) {   // вертикальний жест
                    if (absY >= minDistance && Math.abs(velocityY) >= minVelocity) {
                        if (deltaY > 0) {    //  e1.X
                            onSwipeBottom();  //    |
                        }                     //    v
                        else {                //  e2.X
                            onSwipeTop();
                        }
                        isHandled = true;
                    }
                }
            }
            return isHandled;
        }
    }
}

