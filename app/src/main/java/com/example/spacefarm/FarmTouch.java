package com.example.spacefarm;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.spacefarm.MainActivity.PREFS_NAME;

public class FarmTouch implements View.OnTouchListener{
    Context context;
    public static final String PREFS_NAME = "MyPrefsFile";
    ImageView planet;
    TextView moneydisplay;
    Farm farm;
    int money;
    SharedPreferences sharedPrefs;

    public FarmTouch(ImageView planet, TextView view, Farm farm, Context context){
        this.planet = planet;
        this.moneydisplay = view;
        this.context = context.getApplicationContext();
        this.sharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        this.farm = farm;


    }
    public boolean onTouch(View v, MotionEvent event) {
//        if(v.getId() == R.id.farm) {
//            money += farm1.contains();
//            farm1.reset();//resets any money that might be inside the farm
//            saveCash();//saves the money to shared preferences
//        }
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
                money = MainActivity.money;
                money += farm.contains();
                farm.reset();//resets any money that might be inside the farm
                saveCash();//saves the money to shared preferences
                moneydisplay.setText(String.valueOf(money));
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
                //money.setText("goodbye");

                break;
        }
        return true;
    }
    public void saveCash(){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt("money",money);
        editor.apply();
        MainActivity.money = money;
    }
}
