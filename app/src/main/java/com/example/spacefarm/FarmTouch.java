package com.example.spacefarm;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


public class FarmTouch implements View.OnTouchListener{
    private Context context;
    private static final String PREFS_NAME = "MyPrefsFile";
    private ImageView planet;
    private TextView moneydisplay;
    private Farm farm;
    private long money;
    private SharedPreferences sharedPrefs;
    private Boolean bought;
    private Activity activity;
    private AnimatorSet scaleDown;
    private MediaPlayer soundeffect;

    public FarmTouch(ImageView planet, TextView view, Farm farm, Context context, Activity activity, Boolean bought){
        this.planet = planet;
        this.moneydisplay = view;
        this.context = context.getApplicationContext();
        this.sharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        this.farm = farm;
        this.bought = bought;
        this.activity = activity;
        soundeffect = MediaPlayer.create(context, R.raw.buttonpress);

    }
    public boolean onTouch(View v, MotionEvent event) {
//
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(planet,
                        "scaleX", 0.9f);
                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(planet,
                        "scaleY", 0.9f);
                scaleDownX.setDuration(100);
                scaleDownY.setDuration(100);
                scaleDown = new AnimatorSet();
                scaleDown.play(scaleDownX).with(scaleDownY);
                if(bought) {
                    money = MainActivity.money;
                    int earnings = farm.contains();
                    money += earnings;
                    farm.reset();//resets any money that might be inside the farm
                    saveCash();//saves the money to shared preferences
                    moneydisplay.setText(String.valueOf(money));
                    createText(earnings,event);
                    scaleDown.start();
                    if(!MainActivity.isplaying) {
                        soundeffect.start();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(
                        planet, "scaleX", 1f);
                ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(
                        planet, "scaleY", 1f);
                scaleDownX2.setDuration(100);
                scaleDownY2.setDuration(100);

                AnimatorSet scaleDown2 = new AnimatorSet();
                scaleDown.play(scaleDownX2).with(scaleDownY2);

                scaleDown.start();
                break;
        }
        return true;
    }
    private void saveCash(){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong("money",money);
        editor.apply();
        MainActivity.money = money;
    }

    /**
     * change the bought boolean to allow or stop collection of money
     */
    public void buyFarm(){
        bought = !bought;
    }

    public void createText(int earnings,MotionEvent event){
        final ConstraintLayout framelayout = activity.findViewById(R.id.zoomView);
        final FrameLayout applayout = new FrameLayout(activity);
        ConstraintLayout.LayoutParams mParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT*2);
        final TextView popuptext = new TextView(activity);
        int x = (int) event.getX();
        int y = (int) event.getY();
        int planetx = (int) planet.getX();
        int planety = (int) planet.getY();
        //popup text for money earned
        if (x != 0 && y != 0) {
            applayout.setLayoutParams(mParams);
            applayout.setPadding(x + planetx, y + planety, 0, 0);
            popuptext.setLayoutParams(mParams);
            String gain = "+"+earnings;
            popuptext.setText(gain);
            popuptext.setTextSize(20);
            popuptext.setTextColor(Color.parseColor("#39FF14"));
            popuptext.setShadowLayer(10,0,0,Color.BLACK);
            applayout.addView(popuptext);
            framelayout.addView(applayout);

            Animation animSlide = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.slide);
            animSlide.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    popuptext.setVisibility(View.GONE);
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

            applayout.startAnimation(animSlide);
        }
    }

    /**
     * sets the volume for sound effects
     * @param volume: value from 0-100 for set sound effect volume
     */
    public void setVolume(int volume){
        soundeffect.setVolume((float)volume/100, (float)volume/100);
    }
}
