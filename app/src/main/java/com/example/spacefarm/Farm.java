package com.example.spacefarm;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;


public class Farm {
    /**
     * money: the money inside the Farm  collected by auto farm
     * scale: the income scale of the farm earnings
     * modifier: the multiplier for farm earnings
     * upkeep: boolean for if auto farm is enabled or not
     * time: the timer used for this farm
     * booster: multiplier earned by watching ads
     * percent: the percentage the autofarm timer bar is currently
     */
    private int money;
    final private int scale;
    private int modifier;
    private boolean upkeep;
    private CountDownTimer time;
    private int booster;
    private ProgressBar bar;
    private final TextView container;
    private LinearLayout autolayout;
    private SharedPreferences.Editor editor;
    private int universe;
    private double percent;
    private TextView barText;


    public Farm(int universe, final int scale, final int modifier, final ImageView farmbutton, Activity activity, LayoutInflater inflater){
        this.scale = scale;
        this.universe = universe;
        SharedPreferences sharedPrefs = activity.getApplicationContext().getSharedPreferences("MyPrefsFile", 0);
        money = sharedPrefs.getInt("money"+scale+":"+universe, 0);
        percent = sharedPrefs.getInt("timer"+scale+":"+universe,100);
        upkeep = false;
        this.modifier = modifier;
        booster = 1;
        ConstraintLayout framelayout = activity.findViewById(R.id.zoomView);
        autolayout = new LinearLayout(activity);
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        autolayout.setLayoutParams(mParams);
        autolayout.setOrientation(LinearLayout.VERTICAL);
        autolayout.setPadding((int) farmbutton.getX() + farmbutton.getWidth()/4, (int) farmbutton.getY() + farmbutton.getHeight(), 0, 0);
        container = new TextView(activity.getApplicationContext());
        autolayout.addView(container);
        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        container.setGravity(Gravity.CENTER);
        String farmcontainer = String.valueOf(money);
        container.setTextColor(Color.parseColor("#39FF14"));
        container.setText(farmcontainer);
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

        setTimer();
        editor = sharedPrefs.edit();
    }

    /**
     * @return the money that the farm contains, either the scale itself or the farm value if enabled
     */
    public int contains(){
        if(upkeep) {
            if(money == 10){
                barText.setText("");
                bar.setProgress(0);
                ObjectAnimator.ofInt(bar, "progress", 100)
                        .setDuration((2000 * scale))
                        .start();
                time.start();
            }
            return (money * scale * modifier + scale*modifier)*booster;
        }

        return scale*modifier*booster;
    }

    /**
     * enables the auto collect in the farm
     */
    public void enable(){
        if(!upkeep) {
            upkeep = true;
            autolayout.setVisibility(View.VISIBLE);

            if(money != 10) {
                bar.setProgress((int)(100-percent));
                ObjectAnimator.ofInt(bar, "progress", (int)(100-percent), 100)
                        .setDuration((long)((2000*scale)*((percent)/100.0)))
                        .start();
                final CountDownTimer temptime = new CountDownTimer((long) ((2000*scale)*((percent)/100.0)), 500) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long seconds = millisUntilFinished / 1000 + (long)((100-percent)/100.0)*(2000*scale);
                        percent = (seconds / (2.0*scale))*100;
                        if(percent > 100)percent = 100;
                        bar.setProgress((int)((100-percent)));
                        String tempString =(int)(100-percent) + "%" ;
                        barText.setText(tempString);
                        saveTimer((int)percent);
                    }

                    @Override
                    public void onFinish() {
                        if(money < 9) {
                            bar.setProgress(0);
                    ObjectAnimator.ofInt(bar, "progress", 100)
                            .setDuration(2000 * scale)
                            .start();
                            //cancel();
                            time.start();
                        }else {
                            bar.setProgress(100);
                            barText.setText(R.string.max);
                            cancel();
                        }
                        money++;
                        container.setText(String.valueOf(money));
                        saveMoney();

                    }
                };
                temptime.start();

            }
            else{
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
        container.setText(String.valueOf(money));
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
    protected int upgradeCost(){
        return (int) (scale * Math.pow(1.15,modifier));
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
    protected int getScale(){
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
    int sellCost(){
        if(upkeep) {
            return (int) ((scale * Math.pow(1.15,modifier-1))*modifier)/4+scale*50;
        }
        return ((int) (scale * Math.pow(1.15,modifier-1))*modifier)/4;
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
        time = new CountDownTimer(2000*scale, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                percent = (seconds / (2.0*scale))*100;
                String tempString = (int)(100-percent)+"%";
                barText.setText(tempString);
                saveTimer((int)percent);
            }

            @Override
            public void onFinish() {
                if(money < 9) {
                    bar.setProgress(0);
                    ObjectAnimator.ofInt(bar, "progress", 100)
                            .setDuration(2000 * scale)
                            .start();
                    time.start();
                }else {
                    bar.setProgress(100);
                    barText.setText(R.string.max);
                }
                money++;
                container.setText(String.valueOf(money));
                saveMoney();

            }
        };
    }
}
