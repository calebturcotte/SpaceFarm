package com.example.spacefarm;

import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

//test code for implementing a 2d multidirectional scrollview
//to implement add as an OnTouchListener to a ScrollView with a HorizontalScrollView inside
public class SpaceBackground implements View.OnTouchListener {
    private float mx, my, curX, curY;
    private boolean started = false;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;
    public SpaceBackground(ScrollView vScroll, HorizontalScrollView hScroll) {
        this.vScroll = vScroll;
        this.hScroll = hScroll;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        curX = event.getX();
        curY = event.getY();
        int dx = (int) (mx - curX);
        int dy = (int) (my - curY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (started) {
                    if(dy!=0) vScroll.scrollBy(0, dy);
                    if(dx!=0) hScroll.scrollBy(dx, 0);
                } else {
                    started = true;
                }
                mx = curX;
                my = curY;
                break;
            case MotionEvent.ACTION_UP:
                if(dy!=0) vScroll.scrollBy(0, dy);
                if(dx!=0) hScroll.scrollBy(dx, 0);
                started = false;
                break;
        }
        return false;
    }
}