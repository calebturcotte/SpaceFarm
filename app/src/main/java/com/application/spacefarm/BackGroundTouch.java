package com.application.spacefarm;

import android.app.Activity;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.application.spacefarm.R;

public class BackGroundTouch implements View.OnTouchListener {
    /**
     * Our OnTouchListener for touch events on the background.
     * Used to create a circle animation on touch
     */
    private Activity activity;
    private LayoutInflater inflater;

    BackGroundTouch(Activity activity, LayoutInflater inflater){
        this.activity = activity;
        this.inflater = inflater;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
    switch(event.getAction()){
        case MotionEvent.ACTION_DOWN:
            final ConstraintLayout framelayout = activity.findViewById(R.id.mainscreen);
            final FrameLayout applayout = new FrameLayout(activity);
            ConstraintLayout.LayoutParams mParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT*2);
            int x = (int) event.getX();
            int y = (int) event.getY();
            applayout.setLayoutParams(mParams);
            applayout.setPadding(x , y , 0, 0);
            View tapfx = inflater.inflate(R.layout.tapfx, null);
            applayout.addView(tapfx);
            framelayout.addView(applayout);
            Animation animExpand = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.expand);
            animExpand.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    applayout.setVisibility(View.GONE);
                    //Create handler on the current thread (UI thread)
                    Handler h = new Handler();
                    //Run a runnable after 100ms (after that time it is safe to remove the view)
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            framelayout.removeView(applayout);

                        }
                    }, 100);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            applayout.startAnimation(animExpand);
            break;
        case MotionEvent.ACTION_UP:
            break;
    }
        return false;
    }
}
