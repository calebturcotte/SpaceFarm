package com.application.spacefarm;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Random;


public class FarmTouch implements View.OnTouchListener{

    private static final String PREFS_NAME = "MyPrefsFile";
    private ImageView planet;
    private TextView moneydisplay;
    private Farm farm;
    private long money;
    private SharedPreferences sharedPrefs;
    private Boolean bought;
    private Activity activity;
    private AnimatorSet scaleDown;
    private Context context;
    private Random random;

    public FarmTouch(ImageView planet, TextView view, Farm farm, Context context, Activity activity, Boolean bought){
        this.planet = planet;
        this.moneydisplay = view;
        this.context = context;
        this.sharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        this.farm = farm;
        this.bought = bought;
        this.activity = activity;
        random = new Random();

    }
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                MainActivity.planetTouched(planet, event);
                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(planet,
                        "scaleX", 0.9f);
                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(planet,
                        "scaleY", 0.9f);
                scaleDownX.setDuration(500);
                scaleDownY.setDuration(500);
                scaleDown = new AnimatorSet();
                scaleDown.play(scaleDownX).with(scaleDownY);
                if(bought) {
                    money = MainActivity.money;
                    long earnings = farm.contains();
                    money += earnings;
                    farm.reset();//resets any money that might be inside the farm
                    saveCash();//saves the money to shared preferences
                    moneydisplay.setText(MainActivity.calculateCash(MainActivity.money));
                    createText(earnings,event);
                    scaleDown.start();
                    MainActivity.playPlanetSound(true, context);
                }
                else {
                    MainActivity.playPlanetSound(false, context);
                }
                break;

            case MotionEvent.ACTION_UP:
                ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(
                        planet, "scaleX", 1f);
                ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(
                        planet, "scaleY", 1f);
                scaleDownX2.setDuration(500);
                scaleDownY2.setDuration(500);

                scaleDown.play(scaleDownX2).with(scaleDownY2);
                if(bought) {
                    scaleDown.start();
                }
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

    /**
     * Display text for amount earned by the tap
     * @param earnings the earnings to display
     * @param event touch event where we get coordinates
     */
    public void createText(long earnings,MotionEvent event){
        final ConstraintLayout framelayout = activity.findViewById(R.id.zoomView);
        final FrameLayout applayout = new FrameLayout(activity);
        ConstraintLayout.LayoutParams mParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT*2);
        final TextView popuptext = new TextView(activity);
        int x = (int) event.getX()/2-10;
        int y = (int) event.getY()/2-10;
        int planetx = (int) planet.getX();
        int planety = (int) planet.getY();
        //popup text for money earned
        if (x != 0 && y != 0) {
            applayout.setLayoutParams(mParams);
            int ranx = random.nextInt(100);
            int rany = random.nextInt(100);
            applayout.setPadding(x + planetx + ranx, y + planety + rany, 0, 0);
            popuptext.setLayoutParams(mParams);
            String gain = "+"+MainActivity.calculateCash(earnings);
            popuptext.setText(gain);
            popuptext.setTextSize(25);
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

}
