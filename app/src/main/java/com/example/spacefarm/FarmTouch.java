package com.example.spacefarm;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FarmTouch implements View.OnTouchListener{
    private Context context;
    private static final String PREFS_NAME = "MyPrefsFile";
    private ImageView planet;
    private TextView moneydisplay;
    private Farm farm;
    private long money;
    private SharedPreferences sharedPrefs;
    private RelativeLayout applayout;
    private RelativeLayout framelayout;
    private Boolean bought;

    public FarmTouch(ImageView planet, TextView view, Farm farm, Context context, Activity activity, Boolean bought){
        this.planet = planet;
        this.moneydisplay = view;
        this.context = context.getApplicationContext();
        this.sharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        this.farm = farm;
        applayout = new RelativeLayout(context);
        framelayout = (RelativeLayout) activity.findViewById(R.id.zoomView);
        this.bought = bought;

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
                AnimatorSet scaleDown = new AnimatorSet();
                scaleDown.play(scaleDownX).with(scaleDownY);
                scaleDown.start();
                if(bought) {
                    money = MainActivity.money;
                    money += farm.contains();
                    farm.reset();//resets any money that might be inside the farm
                    saveCash();//saves the money to shared preferences
                    moneydisplay.setText(String.valueOf(money));
                }
                //popup text for money earned
//                RelativeLayout.LayoutParams mParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//                TextView popuptext = new TextView(context);
//                int x = (int) event.getX();
//                int y = (int) event.getY();
//                applayout.setLayoutParams(mParams);
//                applayout.setPadding(x,y,0,0);
//                popuptext.setLayoutParams(mParams);
//                popuptext.setText(String.valueOf(money));
//                applayout.addView(popuptext);
//                framelayout.addView(applayout);
                break;

            case MotionEvent.ACTION_UP:
                ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(
                        planet, "scaleX", 1f);
                ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(
                        planet, "scaleY", 1f);
                scaleDownX2.setDuration(100);
                scaleDownY2.setDuration(100);

                AnimatorSet scaleDown2 = new AnimatorSet();
                scaleDown2.play(scaleDownX2).with(scaleDownY2);

                scaleDown2.start();
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
}
