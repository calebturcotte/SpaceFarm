package com.application.spacefarm;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Random;


public class Farm {
    /**
     * money: the money inside the Farm  collected by auto farm
     * scale: the income scale of the farm earnings
     * modifier: the multiplier for farm earnings
     * upkeep: boolean for if auto farm is enabled or not
     * time: the timer used for this farm
     * booster: multiplier earned by watching ads
     * percent: the percentage the autofarm timer bar is currently
     * totaltimes: the new amount of times that autofarm has collected since the application was closed
     * maxmoney: the maximum money that can be stored by autofarm
     */
    private int money;
    final private long scale;
    private int modifier;
    private boolean upkeep;
    private CountDownTimer time;
    private int booster;
    private ProgressBar bar;
    private LinearLayout autolayout;
    private SharedPreferences.Editor editor;
    private int universe;
    private double percent;
    private TextView barText;
    private int maxmoney = 10;
    private ObjectAnimator baranimator;
    private long countdownvalue;
    private TextView moneydisplay;
    private Activity activity;
    private Random random;
    private ImageView farmbutton;
    private ConstraintLayout framelayout;
    private LayoutInflater inflater;


    public Farm(int universe, final long scale, final int modifier, final ImageView farmbutton, Activity activity, LayoutInflater inflater, TextView moneydisplay){
        this.scale = scale;
        this.universe = universe;
        this.moneydisplay = moneydisplay;
        this.activity = activity;
        this.farmbutton = farmbutton;
        this.inflater = inflater;
        random = new Random();
        SharedPreferences sharedPrefs = activity.getApplicationContext().getSharedPreferences("MyPrefsFile", 0);
        money = sharedPrefs.getInt("money"+scale+":"+universe, 0);
        percent = sharedPrefs.getInt("timer"+scale+":"+universe,100);
        upkeep = false;
        this.modifier = modifier;
        booster = 1;
        editor = sharedPrefs.edit();

        if(universe == 1){
            countdownvalue = (2000L*scale);
        }
        else if(universe == 2){
            countdownvalue = (100L*scale);
        }
        else if(universe == 3){
            countdownvalue = (long)(scale/2.0);
        }
    }

    /**
     * @return the money that the farm contains, either the scale itself or the farm value if enabled
     */
    public long contains(){
        return scale*modifier*booster;
    }

    private long barcount(){
        money = 0;
        if(MainActivity.currentUniverse == universe-1){
            createText(scale*modifier*booster);
        }
        return scale*modifier*booster;
    }

    /**
     * shows our bar upon creation
     */
    public void showBar(){
        autolayout.setVisibility(View.VISIBLE);
        bar.setProgress((int)(100-percent));
        baranimator = ObjectAnimator.ofInt(bar, "progress", (int)(100-percent), 100)
                .setDuration((long)((countdownvalue)*((percent)/100.0)));
        baranimator.start();
    }

    /**
     * enables the auto collect in the farm
     */
    public void enable(){
        if(!upkeep) {
            upkeep = true;
            if(money < maxmoney) {
                time = new CountDownTimer((long) ((countdownvalue)*((percent)/100.0)), 500) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long seconds = millisUntilFinished / 1000 + (long)((100-percent)/100.0)*(countdownvalue);
                        percent = (seconds / (countdownvalue/1000.0))*100;
                        if(percent > 100)percent = 100;
                        bar.setProgress((int)((100-percent)));
                        String tempString =(int)(100-percent) + "%" ;
                        barText.setText(tempString);
                        saveTimer((int)percent);
                    }

                    @Override
                    public void onFinish() {
                        if(money < (maxmoney-1)) {
                            bar.setProgress(0);
                    ObjectAnimator.ofInt(bar, "progress", 100)
                            .setDuration(countdownvalue)
                            .start();
                            setTimer();
                            time.start();
                        }else {
                            bar.setProgress(100);
                            barText.setText(R.string.max);
                            cancel();
                        }
                        saveMoney();
                        MainActivity.money += barcount();
                        saveCash();
                        moneydisplay.setText(MainActivity.calculateCash(MainActivity.money));

                    }
                };
                time.start();

            }
            else{
                setTimer();
                bar.setProgress(100);
                barText.setText(R.string.max);
            }
        }
    }

    /**
     * disables the auto farm
     */
    public void disable(){
        if(upkeep) {
            upkeep = false;
            autolayout.setVisibility(View.INVISIBLE);
            time.cancel();
            saveTimer(0);
        }
    }

    /**
     * resets the money inside the farm
     */
    protected void reset(){
        money = 0;
        saveMoney();
        //container.setText(String.valueOf(money));
    }

    /**
     * increases the amount earned in farm
     */
    protected void upgrade(){
        modifier++;
    }

    /**
     * @return if the farm has been enabled or not
     */
    protected boolean isAutoEnabled(){return upkeep;}

    /**
     * @return current cost to upgrade
     */
    protected long upgradeCost(){
        return (long) (scale * Math.pow(1.15,modifier));
    }

    /**
     * @return the modifier scale for this farm
     */
    protected int getModifier(){
        return modifier;
    }

    /**
     * @return the original scale for this farm
     */
    protected long getScale(){
        return scale;
    }
    /**
     * sells the farm and resets all the values, including stopping auto farming
     */
    protected void sell(){
        disable();
        modifier = 1;
        money = 0;
        saveMoney();
        percent = 100;
        saveTimer(100);
    }

    /**
     *
     * @return the money earned by selling the farm and all of its assets
     */
    long sellCost(){
        if(upkeep) {
            return (long) ((scale * Math.pow(1.137,modifier-1))*modifier)/4+scale*50;
        }
        return ((long) (scale * Math.pow(1.137,modifier-1))*modifier)/4;
    }

    /**
     * @param booster: the multiplier based on ad watched or bought boosters
     */
    void setBooster(int booster){
        this.booster = booster;
    }

    /**
     * saves the current money stored in the farm
     */
    private void saveMoney(){
        editor.putInt("money"+scale+":"+universe,money);
        editor.apply();
    }

    /**
     * saves the money we have to the system
     */
    private void saveCash(){
        editor.putLong("money",MainActivity.money);
        editor.apply();
    }

    /**
     * saves our progress of the timer so we can resume from the proper point
     * @param progress current percent progress on the progress bar
     */
    private void saveTimer(int progress){
        editor.putInt("timer"+scale+":"+universe,progress);
        editor.apply();
    }

    /**
     * sets the timer used for auto farm
     */
    private void setTimer(){
        time = new CountDownTimer(countdownvalue, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                percent = (seconds / (countdownvalue/1000.0))*100;
                String tempString = (int)(100-percent)+"%";
                barText.setText(tempString);
                saveTimer((int)percent);
            }

            @Override
            public void onFinish() {
                if(money < (maxmoney-1)) {
                    bar.setProgress(0);
                    baranimator = ObjectAnimator.ofInt(bar, "progress", 100)
                            .setDuration(countdownvalue);
                    baranimator.start();
                    time.start();
                }else {
                    bar.setProgress(100);
                    barText.setText(R.string.max);
                }
                saveMoney();
                MainActivity.money += barcount();
                saveCash();
                moneydisplay.setText(MainActivity.calculateCash(MainActivity.money));

            }
        };
    }

    /**
     * cancels the timer when the application is paused
     */
    public void cancelTimer(){
        if(time != null){
            time.cancel();
            upkeep = false;
            time = null;
        }
    }

    /**
     * calculates the time that was not accounted for while the app was closed
     */
    public void uncountedTime(){
        long secondssofar = (long)((100-percent)/100.0)*(countdownvalue) + MainActivity.timedifference*1000L;
        long remainingtime = secondssofar%((countdownvalue)+1);

        int totaltimes = (int) ((secondssofar) / (countdownvalue));
        money = (money + totaltimes);
        percent = (((remainingtime / (countdownvalue/1000.0)) * 100) + percent) % (101);

        long earned = money * barcount();
        if(MainActivity.currentUniverse == universe){
            createText(earned);
        }

        MainActivity.money += earned;
        saveCash();
        moneydisplay.setText(MainActivity.calculateCash(MainActivity.money));
        saveMoney();
    }

    /**
     * Display text for amount earned by the autofarm
     * @param earnings the earnings to display
     */
    public void createText(long earnings){
        framelayout = activity.findViewById(R.id.zoomView);
        final FrameLayout applayout = new FrameLayout(activity);
        ConstraintLayout.LayoutParams mParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT*2);
        final TextView popuptext = new TextView(activity);

        applayout.setLayoutParams(mParams);
        int ranx = random.nextInt(100);
        int rany = random.nextInt(100);
        applayout.setPadding((int)farmbutton.getX() + farmbutton.getWidth()/4 + ranx, (int) farmbutton.getY() + farmbutton.getHeight()/2 + rany, 0, 0);
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

    public void addAutoBar(LayoutInflater inflater){
        framelayout = activity.findViewById(R.id.zoomView);
        autolayout = new LinearLayout(activity);
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        autolayout.setLayoutParams(mParams);
        autolayout.setOrientation(LinearLayout.VERTICAL);
        autolayout.setPadding((int) farmbutton.getX() + farmbutton.getWidth()/4, (int) farmbutton.getY() + farmbutton.getHeight()+ 20, 0, 0);
        FrameLayout barLayout = new FrameLayout(activity);
        barText = new TextView(activity.getApplicationContext());
        barText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        barText.setGravity(Gravity.CENTER_HORIZONTAL);
        barText.setTypeface(null,Typeface.BOLD);
        bar = (ProgressBar ) inflater.inflate(R.layout.autoprogress, null);
        bar.setLayoutParams(mParams);
        barLayout.addView(bar);
        barLayout.addView(barText);
        autolayout.addView(barLayout);

        autolayout.setVisibility(View.INVISIBLE);
        framelayout.addView(autolayout);

    }
}
