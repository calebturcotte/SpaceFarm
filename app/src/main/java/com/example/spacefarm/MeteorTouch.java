package com.example.spacefarm;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MeteorTouch implements View.OnTouchListener {
    /**
     * OnTouchListener for our meteor objects
     */
    private TextView pointView;
    private Context context;
    private boolean ismeteor;
    public MeteorTouch(TextView pointView, Context context, boolean ismeteor){
        this.pointView = pointView;
        this.context = context;
        this.ismeteor = ismeteor;

    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchedEvent(v);
                break;

            case MotionEvent.ACTION_SCROLL:
                touchedEvent(v);
                break;
        }
        return false;
    }

    /**
     * events that happen when a meteor is touched or swiped
     */
    private void touchedEvent(View v){
        v.setVisibility(View.GONE);
        if(ismeteor) {
            MiniGame.playMeteorSound(context);
            MiniGame.pointEarned(pointView);
        }
        else{
            MiniGame.pointLost(pointView);
        }
    }
}
