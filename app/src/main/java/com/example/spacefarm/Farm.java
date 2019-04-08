package com.example.spacefarm;

import java.util.Timer;

public class Farm {
    private int money;

    private int scale;
    private int modifier;
    private boolean upkeep;
    private Timer time;


    public Farm(int scale){
        this.scale = scale;
        money = 0;
        time = new Timer();
        upkeep = false;
        modifier = 1;



    }

    /**
     *
     * @return the money that the farm contains, either the scale itself or the farm value if enabled
     */
    public Integer contains(){
        if(upkeep) {
            return money * scale * modifier + scale*modifier;
        }

        return scale*modifier;
    }

    /**
     * enables the auto collect in the farm
     */
    public void enable(){
            upkeep = true;

        time.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run(){
                        money ++;
                    }
                },0, 2000
        );


    }

    /**
     * disables the auto farm
     */
    public void disable(){
        upkeep = false;
        time.cancel();
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
     * sells the farm and resets all the values
     */
    protected int sell(){
        int value;

        if(upkeep){
            value = 2*(modifier-1);
        }
        else{
            value = modifier -1;
        }


        upkeep = false;
        modifier = 1;
        money = 0;

        return value;

    }
}
