package com.example.spacefarm;

public class MainControl {
    /**
     * MainControl is used to control some common variables and functions from different activities in order to reduce the amount of code used
     */
    private long money;
    MainControl(){
        this.money = 0;
    }

    /**
     *
     * @param money: parameter for how much money the user has currently
     */
    public void setMoney(long money){
        this.money = money;
    }

    /**
     *
     * @return: how much money the user has currently
     */
    public long getMoney(){
        return money;
    }
}
