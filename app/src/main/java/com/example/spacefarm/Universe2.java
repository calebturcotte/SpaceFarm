package com.example.spacefarm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class Universe2 extends Fragment {
    private int totalplanets;
    public static final String PREFS_NAME = "MyPrefsFile";
    private TextView moneyview;
    private ArrayList<Farm> farm;
    private ArrayList<Integer> modifier;
    private ArrayList<Boolean> boughtfarm;
    private ArrayList<FarmTouch> touchcontrol;
    private ArrayList<Boolean> autofarm;
    private ImageView[] farmbutton;
    private SharedPreferences settings;
    private Activity activity;
    private Context context;
    private LayoutInflater inflater;
    private int satelliteselect;
    private boolean unlocked;
    private PopupWindow unlockWindow;
    private TextView text;
    private Toast toast;
    private View popupView;
    private PopupWindow popupWindow;

    public Universe2(SharedPreferences settings, Activity activity, Context context, TextView view){
        this.settings = settings;
        this.activity = activity;
        this.context = context;
        moneyview = view;
        totalplanets=8;
        //initialize all content for the farms and money obtained
        farm = new ArrayList<>(totalplanets);
        modifier = new ArrayList<>(totalplanets);
        boughtfarm = new ArrayList<>(totalplanets);
        autofarm = new ArrayList<>(totalplanets);
        touchcontrol = new ArrayList<>(totalplanets);
        for (int i = 8; i < totalplanets+8; i++){
            int numbought = 1;
            boolean autofarmis = false;
            modifier.add(settings.getInt("modifier"+i,numbought));
            autofarm.add(settings.getBoolean("auto"+i,autofarmis));
            boolean farmbought = false;
            boughtfarm.add(settings.getBoolean("boughtfarm" + i, farmbought));
        }
        boolean temp = false;
        unlocked = settings.getBoolean("universe2", temp);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        this.inflater = inflater;
        return inflater.inflate(R.layout.universe2, container, false);
    }

    /**
     * Code for after the view is created
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
        //code for using stuff that requires a created view use getView()
        farmbutton = new ImageView[] {view.findViewById(R.id.farm1), view.findViewById(R.id.farm2), view.findViewById(R.id.farm3), view.findViewById(R.id.farm4),
                view.findViewById(R.id.farm5), view.findViewById(R.id.farm6), view.findViewById(R.id.farm7), view.findViewById(R.id.farm8),};
        final ImageView[] satellites = new ImageView[] {view.findViewById(R.id.satellite1), view.findViewById(R.id.satellite2),view.findViewById(R.id.satellite3), view.findViewById(R.id.satellite4),
                view.findViewById(R.id.satellite5), view.findViewById(R.id.satellite6),view.findViewById(R.id.satellite7), view.findViewById(R.id.satellite8)};

        farmbutton[7].getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onGlobalLayout() {
                farm.add(new Farm(2,5, modifier.get(0), farmbutton[0], activity, inflater));
                farm.add(new Farm(2,20, modifier.get(1), farmbutton[1], activity, inflater));
                farm.add(new Farm(2,50, modifier.get(2), farmbutton[2], activity, inflater));
                farm.add(new Farm(2,100, modifier.get(3), farmbutton[3], activity, inflater));
                farm.add(new Farm(2,500, modifier.get(4), farmbutton[4], activity, inflater));
                farm.add(new Farm(2,1000, modifier.get(5), farmbutton[5], activity, inflater));
                farm.add(new Farm(2,5000, modifier.get(6), farmbutton[6], activity, inflater));
                farm.add(new Farm(2,10000, modifier.get(7), farmbutton[7], activity, inflater));

                for (int i = 0; i < totalplanets; i++){
                    touchcontrol.add(new FarmTouch(farmbutton[i],moneyview, farm.get(i), context, activity, boughtfarm.get(i)));
                    farmbutton[i].setOnTouchListener(touchcontrol.get(i));
                    satellites[i].setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                                if(unlocked) {
                                    MainActivity.playSatelliteSound();
                                    showPopup(v);
                                }

                            }
                            return true;
                        }
                    });
                    if (autofarm.get(i))farm.get(i).enable();
                }
                if(MainActivity.timerisrunning){setBooster(2);}
                farmbutton[7].getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        for (int i = 0; i < totalplanets; i++) {
            if (!boughtfarm.get(i)) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);  //0 means grayscale
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                farmbutton[i].setColorFilter(cf);
                //if we wanted to fade
                //farmbutton[i].setImageAlpha(128);   // 128 = 0.5
            }
        }

        if(!unlocked) {
            createLockPopup();
        }

        LayoutInflater toastinflater = getLayoutInflater();
        View layout = toastinflater.inflate(R.layout.custom_toast,
                (ViewGroup) getView().findViewById(R.id.custom_toast_container));
        text = layout.findViewById(R.id.text);
        toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT/2);
        toast.setView(layout);

        //view stuff for popups
        popupView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_menu, null);
        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20);
        }

    }
    public void showPopup(View v){
        final View satelliteview = v;
        RotateAnimation rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setFillAfter(true);
        rotate.setInterpolator(new LinearInterpolator());
        v.startAnimation(rotate);
        switch(v.getId()){
            case R.id.satellite1:
                satelliteselect = 0;
                break;
            case R.id.satellite2:
                satelliteselect = 1;
                break;
            case R.id.satellite3:
                satelliteselect = 2;
                break;
            case R.id.satellite4:
                satelliteselect = 3;
                break;
            case R.id.satellite5:
                satelliteselect = 4;
                break;
            case R.id.satellite6:
                satelliteselect = 5;
                break;
            case R.id.satellite7:
                satelliteselect = 6;
                break;
            case R.id.satellite8:
                satelliteselect = 7;
                break;
        }
        //final View popupView = inflater.inflate(R.layout.popup_menu, container , false);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(farmbutton[0], Gravity.TOP | Gravity.START, (int)v.getX() + 200, (int)v.getY());
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        //add click options for the popup window buttons
        Button buyButton = (Button) popupView.findViewById(R.id.buy);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyFarm(popupView,satelliteselect,toast,text, popupWindow);
            }
        });
        String buyString = "Buy ("+ MainActivity.calculateCash((int)(farm.get(satelliteselect).getScale()*Math.pow(5,satelliteselect+1)))+")";
        buyButton.setText(buyString);

        Button upgradeButton = (Button) popupView.findViewById(R.id.upgrade);
        upgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upgradeFarm(popupView,satelliteselect,toast,text);
            }
        });
        String upgradeString = "Upgrade ("+ farm.get(satelliteselect).upgradeCost()+"$)";
        upgradeButton.setText(upgradeString);

        Button sellButton = (Button) popupView.findViewById(R.id.sell);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setCancelable(true);
                String sell = "Sell Planet #" + (satelliteselect+1) + "?";
                builder.setTitle(sell);
                //builder.setMessage("Message");
                //Negative Button is on left
                builder.setNegativeButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sellFarm(popupView, satelliteselect, toast, text, popupWindow);
                            }
                        });
                builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                popupWindow.dismiss();


            }
        });
        String sellString = "Sell ("+ MainActivity.calculateCash(farm.get(satelliteselect).sellCost())+")";
        sellButton.setText(sellString);

        Button autoButton = (Button) popupView.findViewById(R.id.auto);
        autoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFarm(v,satelliteselect,toast,text);
            }
        });
        String autoString = "Farm \nautomatically ("+ MainActivity.calculateCash(farm.get(satelliteselect).getScale()*100)+")";
        autoButton.setText(autoString);

        if(boughtfarm.get(satelliteselect)){
            popupView.findViewById(R.id.buy).setVisibility(View.GONE);
            popupView.findViewById(R.id.upgrade).setVisibility(View.VISIBLE);
            popupView.findViewById(R.id.sell).setVisibility(View.VISIBLE);
            if(farm.get(satelliteselect).isAutoEnabled()){
                popupView.findViewById(R.id.auto).setVisibility(View.GONE);
            }
            else {
                popupView.findViewById(R.id.auto).setVisibility(View.VISIBLE);
            }
        }
        else {
            popupView.findViewById(R.id.buy).setVisibility(View.VISIBLE);
            popupView.findViewById(R.id.upgrade).setVisibility(View.GONE);
            popupView.findViewById(R.id.sell).setVisibility(View.GONE);
            popupView.findViewById(R.id.auto).setVisibility(View.GONE);
        }

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                RotateAnimation rotate = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(500);
                rotate.setFillAfter(true);
                rotate.setInterpolator(new LinearInterpolator());
                satelliteview.startAnimation(rotate);
            }
        });

    }


    public void buyFarm(View v, int satelliteselect, Toast toast, TextView text, PopupWindow popupWindow){
        String buy;
        if(MainActivity.money >= farm.get(satelliteselect).getScale()*Math.pow(5,satelliteselect+1)){
            MainActivity.money = MainActivity.money - (long)(farm.get(satelliteselect).getScale()*Math.pow(5,satelliteselect+1));
            boughtfarm.set(satelliteselect,true);
            touchcontrol.get(satelliteselect).buyFarm();
            saveBought(satelliteselect+8,boughtfarm.get(satelliteselect));
            saveCash();
            buy = "Planet #"+(satelliteselect+1)+" was bought";
            v.setVisibility(View.GONE);
            popupWindow.dismiss();
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(1);  //1 means identity colour
            ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
            farmbutton[satelliteselect].setColorFilter(cf);
        }
        else {
            buy = "Not enough funds to buy this Planet";
        }
        text.setText(buy);
        toast.show();
        moneyview.setText(MainActivity.calculateCash(MainActivity.money));
    }

    public void upgradeFarm(View v, int satelliteselect, Toast toast, TextView text){
        String upgrade;
        if(MainActivity.money >= farm.get(satelliteselect).upgradeCost()) {//buys an upgrade if there is enough money
            MainActivity.money = MainActivity.money - farm.get(satelliteselect).upgradeCost();
            farm.get(satelliteselect).upgrade();
            saveCash();
            saveModifier(satelliteselect+8,farm.get(satelliteselect).getModifier());
            upgrade = "Planet #"+ (satelliteselect+1) + " was upgraded.";
        }
        else {
            upgrade = "Planet #"+ (satelliteselect+1) + " needs more money to upgrade";
        }
        text.setText(upgrade);
        toast.show();
        Button upgradeButton = (Button) v.findViewById(R.id.upgrade);
        String upgradeString = "Upgrade ("+ farm.get(satelliteselect).upgradeCost()+"$)";
        upgradeButton.setText(upgradeString);
        Button sellButton = (Button) v.findViewById(R.id.sell);
        String sellString = "Sell ("+ farm.get(satelliteselect).sellCost()+"$)";
        sellButton.setText(sellString);
        moneyview.setText(MainActivity.calculateCash(MainActivity.money));
    }

    public void sellFarm(View v, int satelliteselect, Toast toast, TextView text, PopupWindow popupWindow){
        MainActivity.money += farm.get(satelliteselect).sellCost();
        farm.get(satelliteselect).sell();
        //farm.get(satelliteselect).disable();
        saveCash();
        saveModifier(satelliteselect+8,farm.get(satelliteselect).getModifier());
        boughtfarm.set(satelliteselect, false);
        touchcontrol.get(satelliteselect).buyFarm();
        saveBought(satelliteselect, boughtfarm.get(satelliteselect));
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        farmbutton[satelliteselect].setColorFilter(cf);
        saveAuto(satelliteselect+8, false);
        String sell = "Planet #"+ (satelliteselect+1) + " was sold.";
        text.setText(sell);
        toast.show();
        moneyview.setText(MainActivity.calculateCash(MainActivity.money));
        popupWindow.dismiss();
    }

    public void autoFarm(View v, int satelliteselect, Toast toast, TextView text){
        String auto;
        if(MainActivity.money >= farm.get(satelliteselect).getScale()*100) {
            farm.get(satelliteselect).enable();
            MainActivity.money = MainActivity.money - farm.get(satelliteselect).getScale()*100;
            auto = "Planet #" + (satelliteselect + 1) + " can now be farmed automatically.";
            saveAuto(satelliteselect+8,true);
            v.setVisibility(View.GONE);
        }
        else {
            auto = "Not enough funds to purchase this upgrade";
        }
        text.setText(auto);
        toast.show();
        moneyview.setText(MainActivity.calculateCash(MainActivity.money));
    }

    void reset(){
        unlocked = false;
        saveUnlock();
        if(getView()!=null)createLockPopup();
        for (int i = 0; i < modifier.size(); i++){
            if(getView()!=null)farm.get(i).sell();
            saveModifier(i+8,1);
            autofarm.set(i,false);
            saveAuto(i+8,autofarm.get(i));
        }
        for (int i = 0; i < 8; i++){
            if(boughtfarm.get(i)&& getView()!=null)touchcontrol.get(i).buyFarm();
            boughtfarm.set(i, false);
            saveBought(i+8,boughtfarm.get(i));
            if (!boughtfarm.get(i) && getView()!=null){
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);  //0 means grayscale
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                farmbutton[i].setColorFilter(cf);
            }
        }
    }

    void setBooster(int value){
        for (int i = 0; i < totalplanets; i++){
            farm.get(i).setBooster(value);
        }
    }

    /**
     * Saves the modifier per each farm
     * index: the index of the modifier saved
     * purchased: the number of times modifier has been upgraded - 1
     */
    public void saveModifier(int index, int purchased){
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("modifier"+index,purchased);
        editor.apply();
    }

    /**
     * Saves the status of if a farm has been bought or sold
     * @param index: planet #
     * @param bought: boolean if the farm has been bought or not
     */
    private void saveBought(int index, boolean bought){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("boughtfarm"+index,bought);
        editor.apply();
    }

    private void saveAuto(int index, boolean autoEnabled){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("auto"+index,autoEnabled);
        editor.apply();
    }

    /**
     * This method saves our money value to SharedPreferences
     */
    private void saveCash(){
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("money",MainActivity.money);
        editor.apply();
    }

    private void saveUnlock(){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("universe2",unlocked);
        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(unlockWindow != null)unlockWindow.dismiss();
    }

    public void createLockPopup(){
        final int unlockcost = 500000000;
        View unlockView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_unlock, null);
        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = false; // lets taps outside the popup also dismiss it
        unlockWindow = new PopupWindow(unlockView, width, height, focusable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            unlockWindow.setElevation(20);
        }
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        unlockWindow.showAtLocation(farmbutton[0], Gravity.CENTER, 0, 0);
        unlockView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        Button unlockButton = unlockView.findViewById(R.id.unlock);
        String unlockString = "Unlock: ("+MainActivity.calculateCash(unlockcost)+ ")";
        unlockButton.setText(unlockString);
        unlockButton.setTextColor(Color.BLACK);
        unlockButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String string;
                if(MainActivity.money >= unlockcost){
                    MainActivity.money = MainActivity.money - unlockcost;
                    moneyview.setText(MainActivity.calculateCash(MainActivity.money));
                    saveCash();
                    unlocked = true;
                    saveUnlock();
                    unlockWindow.dismiss();
                    string = "Universe 2 unlocked";
                }
                else {
                    string = "Not enough funds to unlock";
                }
                text.setText(string);
                toast.show();
            }
        });
    }
}