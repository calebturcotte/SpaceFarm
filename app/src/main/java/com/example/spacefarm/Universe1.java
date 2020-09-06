package com.example.spacefarm;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import java.util.Random;

public class Universe1 extends Fragment {
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
    private AnimatorSet[] pulse;
    private boolean firstcreate;
    private boolean popupcreated;

    public Universe1(SharedPreferences settings, Activity activity, Context context, TextView view){
        this.settings = settings;
        this.activity = activity;
        this.context = context;
        moneyview = view;
        firstcreate = true;
        popupcreated = false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        this.inflater = inflater;
        totalplanets=8;
        //initialize all content for the farms and money obtained
        farm = new ArrayList<>(totalplanets);
        modifier = new ArrayList<>(totalplanets);
        boughtfarm = new ArrayList<>(totalplanets);
        autofarm = new ArrayList<>(totalplanets);
        touchcontrol = new ArrayList<>(totalplanets);
        for (int i = 0; i < totalplanets; i++){
            int numbought = 1;
            modifier.add(settings.getInt("modifier"+i,numbought));
            autofarm.add(settings.getBoolean("auto"+i,false));
        }



        return inflater.inflate(R.layout.universe1, container, false);
    }

    /**
     * Code for after the view is created
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        //code for using stuff that requires a created view use getView()
        farmbutton = new ImageView[] {view.findViewById(R.id.farm1), view.findViewById(R.id.farm2), view.findViewById(R.id.farm3), view.findViewById(R.id.farm4),
                view.findViewById(R.id.farm5), view.findViewById(R.id.farm6), view.findViewById(R.id.farm7), view.findViewById(R.id.farm8),};
        final ImageView[] satellites = new ImageView[] {view.findViewById(R.id.satellite1), view.findViewById(R.id.satellite2),view.findViewById(R.id.satellite3), view.findViewById(R.id.satellite4),
                view.findViewById(R.id.satellite5), view.findViewById(R.id.satellite6),view.findViewById(R.id.satellite7), view.findViewById(R.id.satellite8)};
        pulse = new AnimatorSet[8];
        farmbutton[7].getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onGlobalLayout() {
                farm.add(new Farm(1,1, modifier.get(0), farmbutton[0], activity, inflater));
                farm.add(new Farm(1,2, modifier.get(1), farmbutton[1], activity, inflater));
                farm.add(new Farm(1,5, modifier.get(2), farmbutton[2], activity, inflater));
                farm.add(new Farm(1,10, modifier.get(3), farmbutton[3], activity, inflater));
                farm.add(new Farm(1,50, modifier.get(4), farmbutton[4], activity, inflater));
                farm.add(new Farm(1,100, modifier.get(5), farmbutton[5], activity, inflater));
                farm.add(new Farm(1,500, modifier.get(6), farmbutton[6], activity, inflater));
                farm.add(new Farm(1,1000, modifier.get(7), farmbutton[7], activity, inflater));

                for (int i = 0; i < totalplanets; i++){
                    touchcontrol.add(new FarmTouch(farmbutton[i],moneyview, farm.get(i), context, activity, boughtfarm.get(i)));
                    farmbutton[i].setOnTouchListener(touchcontrol.get(i));
                    satellites[i].setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                                MainActivity.playSatelliteSound(context);
                                showPopup(v);
                            }
                            return true;
                        }
                    });
                    if (autofarm.get(i)) {
                        if(firstcreate)farm.get(i).uncountedTime();
                        farm.get(i).enable();
                    }
                }
                if(MainActivity.timerisrunning){setBooster(2);}
                firstcreate = false;
                farmbutton[7].getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        boughtfarm.add(true);
        long delay = 100L + new Random().nextInt(900);
        pulse[0] = new AnimatorSet();
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(
                farmbutton[0], "scaleX", 1.05f);
        scaleUpX.setDuration(1000L);
        scaleUpX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleUpX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleUpX.setStartDelay(delay);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(
                farmbutton[0], "scaleY", 1.05f);
        scaleUpY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleUpY.setRepeatMode(ObjectAnimator.REVERSE);
        scaleUpY.setDuration(1000L);
        scaleUpY.setStartDelay(delay);
        pulse[0].play(scaleUpX).with(scaleUpY);
        for (int i = 1; i < totalplanets; i++) {
            delay = 100L + new Random().nextInt(900);
            pulse[i] = new AnimatorSet();
            scaleUpX = ObjectAnimator.ofFloat(
                    farmbutton[i], "scaleX", 1.05f);
            scaleUpX.setDuration(1000L);
            scaleUpX.setRepeatMode(ObjectAnimator.REVERSE);
            scaleUpX.setRepeatCount(ObjectAnimator.INFINITE);
            scaleUpX.setStartDelay(delay);
            scaleUpY = ObjectAnimator.ofFloat(
                    farmbutton[i], "scaleY", 1.05f);
            scaleUpY.setRepeatCount(ObjectAnimator.INFINITE);
            scaleUpY.setRepeatMode(ObjectAnimator.REVERSE);
            scaleUpY.setDuration(1000L);
            scaleUpY.setStartDelay(delay);
            pulse[i].play(scaleUpX).with(scaleUpY);

            boughtfarm.add(settings.getBoolean("boughtfarm" + i, false));
            if (!boughtfarm.get(i)) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);  //0 means grayscale
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                farmbutton[i].setColorFilter(cf);
                //if we wanted to fade
                //farmbutton[i].setImageAlpha(128);   // 128 = 0.5
            } else{
                pulse[i].start();
            }
        }
        pulse[0].start();
    }

    /**
     * Creates our option popup once a satellite is clicked
     * @param v: satelliteview clicked
     */
    public void showPopup(View v){
        if(popupcreated){
            return;
        }
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

        final View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_menu, null);
        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        //boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20);
        }
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.showAtLocation(farmbutton[0], Gravity.TOP | Gravity.START, (int)v.getX() + 200, (int)v.getY());
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        LayoutInflater toastinflater = getLayoutInflater();
        View layout = toastinflater.inflate(R.layout.custom_toast,
                (ViewGroup) getView().findViewById(R.id.custom_toast_container));
        final TextView text = layout.findViewById(R.id.text);
        final Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT/2);
        toast.setView(layout);

        popupView.findViewById(R.id.close).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return false;
            }
        });


        //add click options for the popup window buttons
        popupView.findViewById(R.id.buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.playButtonSound(context);
                buyFarm(popupView,satelliteselect,toast,text, popupWindow);
            }
        });
        Button buyButton = (Button) popupView.findViewById(R.id.buy);
        String buyString = "Buy ("+ MainActivity.calculateCash((long)(farm.get(satelliteselect).getScale()*Math.pow(5,satelliteselect+1)))+")";
        buyButton.setText(buyString);
        popupView.findViewById(R.id.upgrade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.playButtonSound(context);
                upgradeFarm(popupView,satelliteselect,toast,text);
            }
        });
        Button upgradeButton = (Button) popupView.findViewById(R.id.upgrade);
        String upgradeString = "Upgrade ("+ MainActivity.calculateCash(farm.get(satelliteselect).upgradeCost())+")";
        upgradeButton.setText(upgradeString);
        popupView.findViewById(R.id.sell).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.playButtonSound(context);
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
        Button sellButton = (Button) popupView.findViewById(R.id.sell);
        String sellString = "Sell ("+ MainActivity.calculateCash(farm.get(satelliteselect).sellCost())+")";
        sellButton.setText(sellString);
        final Button autoButton = (Button) popupView.findViewById(R.id.auto);
        autoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.playButtonSound(context);
                autoFarm(popupView,satelliteselect,toast,text, autoButton);
            }
        });
        String autoString = "Auto \nFarm ("+ MainActivity.calculateCash(farm.get(satelliteselect).getScale()*100L)+")";
        autoButton.setText(autoString);

        if(boughtfarm.get(satelliteselect)){
            popupView.findViewById(R.id.buy).setVisibility(View.GONE);
            if(farm.get(satelliteselect).isAutoEnabled()){
                popupView.findViewById(R.id.auto).setVisibility(View.GONE);
            }
        }
        else {
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
                popupcreated = false;
            }
        });

        popupcreated = true;

    }


    public void buyFarm(View v, int satelliteselect, Toast toast, TextView text, PopupWindow popupWindow){
        String buy;
        if(MainActivity.money >= farm.get(satelliteselect).getScale()*Math.pow(5,satelliteselect+1)){
            MainActivity.money = MainActivity.money - (long)(farm.get(satelliteselect).getScale()*Math.pow(5,satelliteselect+1));
            boughtfarm.set(satelliteselect,true);
            touchcontrol.get(satelliteselect).buyFarm();
            saveBought(satelliteselect,boughtfarm.get(satelliteselect));
            saveCash();
            buy = "Planet #"+(satelliteselect+1)+" was bought";
            popupWindow.dismiss();
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(1);  //1 means identity colour
            ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
            farmbutton[satelliteselect].setColorFilter(cf);
            pulse[satelliteselect].start();
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
            saveModifier(satelliteselect,farm.get(satelliteselect).getModifier());
            upgrade = "Planet #"+ (satelliteselect+1) + " was upgraded.";
        }
        else {
            upgrade = "Planet #"+ (satelliteselect+1) + " needs more money to upgrade";
        }
        text.setText(upgrade);
        toast.show();
        Button upgradeButton = (Button) v.findViewById(R.id.upgrade);
        String upgradeString = "Upgrade ("+ MainActivity.calculateCash(farm.get(satelliteselect).upgradeCost())+")";
        upgradeButton.setText(upgradeString);
        Button sellButton = (Button) v.findViewById(R.id.sell);
        String sellString = "Sell ("+ MainActivity.calculateCash(farm.get(satelliteselect).sellCost())+")";
        sellButton.setText(sellString);
        moneyview.setText(MainActivity.calculateCash(MainActivity.money));
    }

    public void sellFarm(View v, int satelliteselect, Toast toast, TextView text, PopupWindow popupWindow){
        MainActivity.money += farm.get(satelliteselect).sellCost();
        farm.get(satelliteselect).sell();
        saveCash();
        saveModifier(satelliteselect,farm.get(satelliteselect).getModifier());
        if(satelliteselect != 0) {
            boughtfarm.set(satelliteselect, false);
            touchcontrol.get(satelliteselect).buyFarm();
            saveBought(satelliteselect, boughtfarm.get(satelliteselect));
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);  //0 means grayscale
            ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
            farmbutton[satelliteselect].setColorFilter(cf);
            pulse[satelliteselect].end();
        }
        saveAuto(satelliteselect, false);
        String sell = "Planet #"+ (satelliteselect+1) + " was sold.";
        text.setText(sell);
        toast.show();
        moneyview.setText(MainActivity.calculateCash(MainActivity.money));
        popupWindow.dismiss();
    }

    public void autoFarm(View v, int satelliteselect, Toast toast, TextView text, Button thisbutton){
        String auto;
        if(MainActivity.money >= farm.get(satelliteselect).getScale()*100) {
            farm.get(satelliteselect).enable();
            MainActivity.money = MainActivity.money - farm.get(satelliteselect).getScale()*100;
            auto = "Planet #" + (satelliteselect + 1) + " can now be farmed automatically.";
            saveAuto(satelliteselect,true);
            thisbutton.setVisibility(View.GONE);
        }
        else {
            auto = "Not enough funds to purchase this upgrade";
        }
        Button sellButton = (Button) v.findViewById(R.id.sell);
        String sellString = "Sell ("+ MainActivity.calculateCash(farm.get(satelliteselect).sellCost())+")";
        sellButton.setText(sellString);
        text.setText(auto);
        toast.show();
        moneyview.setText(MainActivity.calculateCash(MainActivity.money));
    }

    void reset(){
        for (int i = 0; i < modifier.size(); i++){
            farm.get(i).sell();
            saveModifier(i,farm.get(i).getModifier());
            autofarm.set(i,false);
            saveAuto(i,autofarm.get(i));
        }
        for (int i = 1; i < touchcontrol.size(); i++){
            if(boughtfarm.get(i))touchcontrol.get(i).buyFarm();
            boughtfarm.set(i, false);
            saveBought(i,boughtfarm.get(i));
            if (!boughtfarm.get(i)){
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);  //0 means grayscale
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                farmbutton[i].setColorFilter(cf);
            }
        }
    }

    /**
     * sets the booster for each farm in this universe to the appropriate value
     * @param value: the multiplier gained by the booster
     */
    void setBooster(int value){
        for (int i = 0; i < totalplanets; i++){
            farm.get(i).setBooster(value);
        }
    }

    /**
     * Saves the modifier per each farm
     * index: the index of the modifier saved
     * purchased: the number of times modifier has been upgraded
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

//Unused option for pausing and resuming
//    /**
//     * code for when the fragment ends
//     */
//    @Override
//    public void onStop() {
//        super.onStop();
//        for(int i = 0; i < 8; i++){
//            farm.get(i).cancelTimer();
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        farmbutton[7].getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public void onGlobalLayout() {
//                if(autofarm != null && farm != null) {
//                    for(int i = 0; i < totalplanets; i++){
//                        if (autofarm.get(i)){
//                            farm.get(i).uncountedTime();
//                            farm.get(i).enable();
//                        }
//                    }
//                }
//                farmbutton[7].getViewTreeObserver().removeOnGlobalLayoutListener(this);
//        }
//        });
//    }
}
