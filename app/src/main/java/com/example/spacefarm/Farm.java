package com.example.spacefarm;

import java.util.Timer;

public class Farm {
    /**
     * money: the money inside the Farm  collected by auto farm
     * scale: the income scale of the farm earnings
     * modifier: the multiplier for farm earnings
     * upkeep: boolean for if auto farm is enabled or not
     * time: the timer used for this farm
     */
    private int money;
    private int scale;
    private int modifier;
    private boolean upkeep;
    private Timer time;


    public Farm(int scale, int modifier){
        this.scale = scale;
        money = 0;
        time = new Timer();
        upkeep = false;
        this.modifier = modifier;
    }

    /**
     * @return the money that the farm contains, either the scale itself or the farm value if enabled
     */
    public int contains(){
        if(upkeep) {
            return money * scale * modifier + scale*modifier;
        }

        return scale*modifier;
    }

    /**
     * enables the auto collect in the farm
     */
    public void enable(){
        if(!upkeep) {
            upkeep = true;
                time.schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                money++;
                            }
                        }, 0, 2000
                );
        }
    }

    /**
     * disables the auto farm
     */
    public void disable(){
        if(upkeep) {
            upkeep = false;
            time.cancel();
        }
    }

    /**
     * resets the money inside the farm
     */
    protected void reset(){
        money = 0;
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
        upkeep = false;
        modifier = 1;
        money = 0;

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
}
