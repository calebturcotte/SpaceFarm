package com.example.spacefarm;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
     */
    private int money;
    final private int scale;
    private int modifier;
    private boolean upkeep;
    private CountDownTimer time;
    private int booster;
    ProgressBar bar;
    final TextView container;
    LinearLayout autolayout;
    private SharedPreferences sharedPrefs;


    public Farm(final int scale, final int modifier, ImageView farmbutton, Activity activity){
        this.scale = scale;
        this.sharedPrefs = activity.getApplicationContext().getSharedPreferences("MyPrefsFile", 0);
        money = sharedPrefs.getInt("money"+scale, 0);
        upkeep = false;
        this.modifier = modifier;
        booster = 1;
        LayoutInflater inflater = activity.getLayoutInflater();
        final ConstraintLayout framelayout = activity.findViewById(R.id.zoomView);
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
        bar = (ProgressBar ) inflater.inflate(R.layout.autoprogress, null);
        bar.setLayoutParams(mParams);
        autolayout.addView(bar);
        framelayout.addView(autolayout);

        autolayout.setVisibility(View.INVISIBLE);

        time = new CountDownTimer(2000*scale, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                money++;
                container.setText(String.valueOf(money));
                bar.setProgress(0);
                ObjectAnimator.ofInt(bar, "progress", 100)
                        .setDuration(2000*scale)
                        .start();
                saveMoney();
                time.start();
            }
        };
    }

    /**
     * @return the money that the farm contains, either the scale itself or the farm value if enabled
     */
    public int contains(){
        if(upkeep) {
            return money * scale * modifier + scale*modifier;
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
            bar.setProgress(0);
            ObjectAnimator.ofInt(bar, "progress", 100)
                    .setDuration(2000*scale)
                    .start();
            time.start();
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
     * @return the value of the farm assets sold
     */
    protected int sell(){
        disable();
        modifier = 1;
        money = 0;
        saveMoney();

        return sellCost();
    }

    /**
     *
     * @return the money earned by selling the farm and all of its assets
     */
    int sellCost(){
        if(upkeep) {
            return (upgradeCost() + scale*(101))/2;
        }
        return (upgradeCost() + scale)/2;
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
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt("money"+scale,money);
        editor.apply();
    }
}
