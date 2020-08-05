package com.example.spacefarm;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class MeteorTouch implements View.OnTouchListener {
    /**
     * OnTouchListener for our meteor objects
     */
    private TextView pointView;
    private Context context;
    private boolean ismeteor;
    private Activity activity;
    private LayoutInflater inflater;

    public MeteorTouch(TextView pointView, Context context, boolean ismeteor, Activity activity, LayoutInflater inflater){
        this.pointView = pointView;
        this.context = context;
        this.ismeteor = ismeteor;
        this.activity = activity;
        this.inflater = inflater;

    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchedEvent(v);
                break;

//            case MotionEvent.ACTION_SCROLL:
//                touchedEvent(v, event);
//                break;
        }
        return true;
    }

    /**
     * events that happen when a meteor is touched or swiped
     */
    private void touchedEvent(View v){
        v.setVisibility(View.GONE);
        if(ismeteor) {
            MiniGame.playMeteorSound(context, true);
            MiniGame.pointEarned(pointView);
        }
        else{
            MiniGame.playMeteorSound(context, false);
            MiniGame.pointLost(pointView);
        }
        meteorChunks(v, ismeteor);
    }

    /**
     * animation for chunks of meteor once tapped
     *
     */
    public void meteorChunks(View v, boolean ismeteor){
        final ConstraintLayout framelayout = activity.findViewById(R.id.mainscreen);
        ConstraintLayout.LayoutParams mParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT*2);
        final FrameLayout[] applayout = {new FrameLayout(activity), new FrameLayout(activity), new FrameLayout(activity)};
        int meteorx = (int)v.getX()+10;
        int meteory = (int)v.getY()+10;
        View chunk;
        View chunk2;
        View chunk3;
        if(ismeteor) {
            chunk = inflater.inflate(R.layout.meteor_chunk, null);
            chunk2 = inflater.inflate(R.layout.meteor_chunk, null);
            chunk3 = inflater.inflate(R.layout.meteor_chunk, null);
        }
        else {
            chunk = inflater.inflate(R.layout.bomb_chunk, null);
            chunk2 = inflater.inflate(R.layout.bomb_chunk, null);
            chunk3 = inflater.inflate(R.layout.bomb_chunk, null);
        }

        chunk2.setRotation(120f);
        chunk3.setRotation(240f);
        applayout[0].addView(chunk);
        applayout[1].addView(chunk2);
        applayout[1].setTranslationX(45);
        applayout[1].setTranslationY(40);
        applayout[2].addView(chunk3);
        applayout[2].setTranslationX(-35);
        applayout[2].setTranslationY(45);
        for (int i = 0; i < 3; i++) {
            applayout[i].setLayoutParams(mParams);
            applayout[i].setPadding(meteorx, meteory, 0, 0);
            framelayout.addView(applayout[i]);
        }

        Animation animExpand = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.chunk_expand);
        Animation animExpand2 = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.chunk_expand2);
        Animation animExpand3 = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.chunk_expand3);
        animExpand.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    for (int i = 0; i < 3; i++) {
                        applayout[i].setVisibility(View.GONE);
                    }
                    //Create handler on the current thread (UI thread)
                    Handler h = new Handler();
                    //Run a runnable after 100ms (after that time it is safe to remove the view)
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0; i < 3; i++) {
                                framelayout.removeView(applayout[i]);
                            }

                        }
                    }, 100);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
            }
        });
        applayout[0].startAnimation(animExpand);
        applayout[1].startAnimation(animExpand2);
        applayout[2].startAnimation(animExpand3);
    }
}
