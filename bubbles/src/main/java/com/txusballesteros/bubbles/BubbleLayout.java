/*
 * Copyright Txus Ballesteros 2015 (@txusballesteros)
 *
 * This file is part of some open source application.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contact: Txus Ballesteros <txus.ballesteros@gmail.com>
 */
package com.txusballesteros.bubbles;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;

public class BubbleLayout extends BubbleBaseLayout {
    private final static int HOLDING_TIME = 1000;

    private float initialTouchX;
    private float initialTouchY;
    private int initialX;
    private int initialY;
    private OnBubbleRemoveListener onBubbleRemoveListener;
    private OnBubbleClickListener onBubbleClickListener;
    private OnHoldingBubbleListener onHoldingBubbleListener;
    private OnBubbleStickToWallListener onBubbleStickToWallListener;
    private OnBubbleGoToCenterListener onBubbleGoToCenterListener;
    private static final int TOUCH_TIME_THRESHOLD = 150;
    private long lastTouchDown;
    private MoveAnimator animator;
    private int width;
    private int height;
    private WindowManager windowManager;
    private boolean shouldStickToWall = true;
    private Object tag;
    private CountDownTimer holdingTimer;
    private View dialogView;

    public void setOnBubbleRemoveListener(OnBubbleRemoveListener listener) {
        onBubbleRemoveListener = listener;
    }

    public void setOnBubbleClickListener(OnBubbleClickListener listener) {
        onBubbleClickListener = listener;
    }

    public void setOnHoldingBubbleListener(OnHoldingBubbleListener listener) {
        onHoldingBubbleListener = listener;
    }

    public void setOnBubbleStickToWallListener(OnBubbleStickToWallListener listener) {
        onBubbleStickToWallListener = listener;
    }

    public void setOnBubbleGoToCenterListener(OnBubbleGoToCenterListener listener) {
        onBubbleGoToCenterListener = listener;
    }

    public BubbleLayout(Context context) {
        super(context);
        animator = new MoveAnimator();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initializeView();
    }

    public BubbleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        animator = new MoveAnimator();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initializeView();
    }

    public BubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        animator = new MoveAnimator();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initializeView();
    }

    public void setShouldStickToWall(boolean shouldStick) {
        this.shouldStickToWall = shouldStick;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public View getDialogView() {
        return dialogView;
    }

    public void setDialogView(View view) {
        dialogView = view;
    }

    void notifyBubbleRemoved() {
        if (onBubbleRemoveListener != null) {
            onBubbleRemoveListener.onBubbleRemoved(this);
        }
    }

    private void initializeView() {
        setClickable(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        playAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = getViewParams().x;
                    initialY = getViewParams().y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    // playAnimationClickDown();
                    lastTouchDown = System.currentTimeMillis();
                    updateSize();
                    animator.stop();

                    setTimer();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = initialX + (int) (event.getRawX() - initialTouchX);
                    int y = initialY + (int) (event.getRawY() - initialTouchY);
                    getViewParams().x = x;
                    getViewParams().y = y;
                    getWindowManager().updateViewLayout(this, getViewParams());
                    if (getLayoutCoordinator() != null) {
                        getLayoutCoordinator().notifyBubblePositionChanged(this, x, y);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (getLayoutCoordinator() != null) {
                        getLayoutCoordinator().notifyBubbleRelease(this);
                        // playAnimationClickUp();
                    }
                    if (System.currentTimeMillis() - lastTouchDown < TOUCH_TIME_THRESHOLD) {
                        if (onBubbleClickListener != null) {
                            onBubbleClickListener.onBubbleClick(this);
                        }
                    }
                    stopTimer();
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    private void playAnimation() {
        if (!isInEditMode()) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater
                    .loadAnimator(getContext(), R.animator.bubble_shown_animator);
            animator.setTarget(this);
            animator.start();
        }
    }

    private void playAnimationClickDown() {
        if (!isInEditMode()) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater
                    .loadAnimator(getContext(), R.animator.bubble_down_click_animator);
            animator.setTarget(this);
            animator.start();
        }
    }

    private void playAnimationClickUp() {
        if (!isInEditMode()) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater
                    .loadAnimator(getContext(), R.animator.bubble_up_click_animator);
            animator.setTarget(this);
            animator.start();
        }
    }

    public void updateSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = (size.x - this.getWidth());
        height = (size.y - this.getHeight());

    }

    public interface OnBubbleRemoveListener {
        void onBubbleRemoved(BubbleLayout bubble);
    }

    public interface OnBubbleClickListener {
        void onBubbleClick(BubbleLayout bubble);
    }

    public interface OnHoldingBubbleListener {
        void onHoldingBubble(BubbleLayout bubble);
    }

    public interface OnBubbleStickToWallListener {
        void onBubbleStickToWall(BubbleLayout bubble, boolean leftSide);
    }

    public interface OnBubbleGoToCenterListener {
        void onBubbleGoToCenterListener(BubbleLayout bubble, int oldX, int oldY);
    }

    public void goToWall() {
        if (shouldStickToWall) {
            int middle = width / 2;
            float nearestXWall = getViewParams().x >= middle ? width : 0;
            animator.start(nearestXWall, getViewParams().y);


            if (onBubbleStickToWallListener != null) {
                onBubbleStickToWallListener.onBubbleStickToWall(this, getViewParams().x < middle);
            }
        }
    }

    public void goToCenter() {
        int oldX = (int) this.getViewParams().x;
        int oldY = (int) this.getViewParams().y;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        animator.start((width/2) - (getWidth()/2), (height/2) - (getHeight()/2));


        if (onBubbleGoToCenterListener != null) {
            onBubbleGoToCenterListener.onBubbleGoToCenterListener(this, oldX, oldY);
        }
    }

    public void goTo(int coordinateX, int coordinateY) {
        animator.start(coordinateX, coordinateY);
    }

    private void move(float deltaX, float deltaY) {
        getViewParams().x += deltaX;
        getViewParams().y += deltaY;
        windowManager.updateViewLayout(this, getViewParams());
    }


    private class MoveAnimator implements Runnable {
        private Handler handler = new Handler(Looper.getMainLooper());
        private float destinationX;
        private float destinationY;
        private long startingTime;

        private void start(float x, float y) {
            this.destinationX = x;
            this.destinationY = y;
            startingTime = System.currentTimeMillis();
            handler.post(this);
        }

        @Override
        public void run() {
            if (getRootView() != null && getRootView().getParent() != null) {
                float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / 400f);
                float deltaX = (destinationX - getViewParams().x) * progress;
                float deltaY = (destinationY - getViewParams().y) * progress;
                move(deltaX, deltaY);
                if (progress < 1) {
                    handler.post(this);
                }
            }
        }

        private void stop() {
            handler.removeCallbacks(this);
        }
    }

    private void setTimer() {
        if (holdingTimer != null) {
            stopTimer();
        }

        startTimer();
    }

    private void stopTimer() {
        holdingTimer.cancel();
    }

    private void startTimer() {
        holdingTimer = new CountDownTimer(HOLDING_TIME, HOLDING_TIME) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (onHoldingBubbleListener != null) {
                    onHoldingBubbleListener.onHoldingBubble(BubbleLayout.this);
                }
            }
        }.start();
    }
}
