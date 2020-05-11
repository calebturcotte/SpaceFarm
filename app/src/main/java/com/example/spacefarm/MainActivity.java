package com.example.spacefarm;




import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    /**
     * gravity: the animator responsible for each satellite
     */
    public static final String PREFS_NAME = "MyPrefsFile";
    public Context context;
    static long money;
    TextView view;
    ArrayList<Farm> farm;
    ArrayList<Integer> modifier;
    ArrayList<Boolean> boughtfarm;
    ArrayList<FarmTouch> touchcontrol;
    ArrayList<Boolean> autofarm;
    ImageView[] farmbutton;
    MediaPlayer music;
    boolean isplaying;
    int satelliteselect;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize all content for the farms and money obtained
        farm = new ArrayList<>(8);
        modifier = new ArrayList<>(8);
        boughtfarm = new ArrayList<>(8);
        autofarm = new ArrayList<>(8);
        for (int i = 0; i < 8; i++){
            int numbought = 1;
            boolean autofarmis = false;
            modifier.add(settings.getInt("modifier"+i,numbought));
            autofarm.add(settings.getBoolean("auto"+i,autofarmis));
        }
        farmbutton =new ImageView[] {findViewById(R.id.farm1), findViewById(R.id.farm2), findViewById(R.id.farm3), findViewById(R.id.farm4),
                findViewById(R.id.farm5), findViewById(R.id.farm6), findViewById(R.id.farm7), findViewById(R.id.farm8),};
        boughtfarm.add(true);
        for (int i = 1; i < 8; i++){
            boolean farmbought = false;
            boughtfarm.add(settings.getBoolean("boughtfarm"+i, farmbought));
            if (!boughtfarm.get(i)){
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);  //0 means grayscale
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                farmbutton[i].setColorFilter(cf);
                //if we wanted to fade
                //farmbutton[i].setImageAlpha(128);   // 128 = 0.5
            }
        }
        farm.add(new Farm(1, modifier.get(0)));
        farm.add(new Farm(2, modifier.get(1)));
        farm.add(new Farm(5, modifier.get(2)));
        farm.add(new Farm(10, modifier.get(3)));
        farm.add(new Farm(50, modifier.get(4)));
        farm.add(new Farm(100, modifier.get(5)));
        farm.add(new Farm(500, modifier.get(6)));
        farm.add(new Farm(1000, modifier.get(7)));
        view = findViewById(R.id.money);
        money = settings.getLong("money", money);
        view.setText(String.valueOf(money));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //add music to our app

        music = MediaPlayer.create(MainActivity.this,R.raw.spacefarmmaintheme);
        music.setLooping(true);

        isplaying = settings.getBoolean("isplaying",isplaying);
        ImageView musicSetting = findViewById(R.id.soundView);
        if(!isplaying){
            musicSetting.setBackgroundResource(R.drawable.ic_music_on);
            music.start();
        } else {
            musicSetting.setBackgroundResource(R.drawable.ic_music_off);
        }

        touchcontrol = new ArrayList<>(8);
        for (int i = 0; i < 8; i++){
            touchcontrol.add(new FarmTouch(farmbutton[i],view, farm.get(i), context, MainActivity.this, boughtfarm.get(i)));
            farmbutton[i].setOnTouchListener(touchcontrol.get(i));
            if (autofarm.get(i))farm.get(i).enable();
        }


//        findViewById(R.id.farm2).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            public void onGlobalLayout() {
//                moveAnimation(findViewById(R.id.satellite1), findViewById(R.id.farm1));
//                moveAnimation(findViewById(R.id.satellite2), findViewById(R.id.farm2));
//                moveAnimation(findViewById(R.id.satellite3), findViewById(R.id.farm3));
//                moveAnimation(findViewById(R.id.satellite4), findViewById(R.id.farm4));
//                moveAnimation(findViewById(R.id.satellite5), findViewById(R.id.farm5));
//                moveAnimation(findViewById(R.id.satellite6), findViewById(R.id.farm6));
//
//                // don't forget to remove the listener to prevent being called again
//                // by future layout events:
//                findViewById(R.id.farm2).getViewTreeObserver().removeOnGlobalLayoutListener(this);
//            }
//        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setTitle("Restart the Game?");
                builder.setMessage("This will sell all your planets and reset your money to 0.");
                //Negative Button is on left
                builder.setNegativeButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                money = 0;
                                view.setText(String.valueOf(money));
                                saveCash();
                                for (int i = 0; i < modifier.size(); i++){
                                    farm.get(i).sell();
                                    saveModifier(i,farm.get(i).getModifier());
                                    farm.get(i).disable();
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
                        });
                builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void music(View v){

        if(!isplaying){
            v.setBackgroundResource(R.drawable.ic_music_off);
            music.pause();
        } else {
            v.setBackgroundResource(R.drawable.ic_music_on);
            music.seekTo(0);
            music.start();
        }
        isplaying = !isplaying;
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isplaying",isplaying);
        editor.apply();
    }

    /**
     * Creates and handles click events for a pop up menu from the satellites
     * @param v the View of the Main activity
     */
    public void showPopup(View v){
        final View satelliteview = v;
        RotateAnimation rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1000);
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
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_menu, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20);
        }
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.TOP | Gravity.LEFT, (int)v.getX() + 200, (int)v.getY());

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        LayoutInflater toastinflater = getLayoutInflater();
        View layout = toastinflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));
        final TextView text = layout.findViewById(R.id.text);
        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT/2);
        toast.setView(layout);

        //add click options for the popup window buttons
        popupView.findViewById(R.id.buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyFarm(popupView,satelliteselect,toast,text, popupWindow);
            }
        });
        Button buyButton = (Button) popupView.findViewById(R.id.buy);
        String buyString = "Buy ("+ (int)(farm.get(satelliteselect).getScale()*Math.pow(4,satelliteselect+1))+"$)";
        buyButton.setText(buyString);
        popupView.findViewById(R.id.upgrade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upgradeFarm(popupView,satelliteselect,toast,text);
            }
        });
        Button upgradeButton = (Button) popupView.findViewById(R.id.upgrade);
        String upgradeString = "Upgrade ("+ farm.get(satelliteselect).upgradeCost()+"$)";
        upgradeButton.setText(upgradeString);
        popupView.findViewById(R.id.sell).setOnClickListener(new View.OnClickListener() {
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
        Button sellButton = (Button) popupView.findViewById(R.id.sell);
        String sellString = "Sell ("+ farm.get(satelliteselect).sellCost()+"$)";
        sellButton.setText(sellString);
        popupView.findViewById(R.id.auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFarm(v,satelliteselect,toast,text);
            }
        });
        Button autoButton = (Button) popupView.findViewById(R.id.auto);
        String autoString = "Farm automatically ("+ farm.get(satelliteselect).getScale()*100+"$)";
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
                rotate.setDuration(1000);
                rotate.setFillAfter(true);
                rotate.setInterpolator(new LinearInterpolator());
                satelliteview.startAnimation(rotate);
            }
        });

    }


    public void buyFarm(View v, int satelliteselect, Toast toast, TextView text, PopupWindow popupWindow){
        String buy;
        if(money >= farm.get(satelliteselect).getScale()*Math.pow(4,satelliteselect+1)){
            money = money - (long)(farm.get(satelliteselect).getScale()*Math.pow(4,satelliteselect+1));
            boughtfarm.set(satelliteselect,true);
            touchcontrol.get(satelliteselect).buyFarm();
            saveBought(satelliteselect,boughtfarm.get(satelliteselect));
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
        view.setText(String.valueOf(money));
    }

    public void upgradeFarm(View v, int satelliteselect, Toast toast, TextView text){
        String upgrade;
        if(money >= farm.get(satelliteselect).upgradeCost()) {//buys an upgrade if there is enough money
            money = money - farm.get(satelliteselect).upgradeCost();
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
        String upgradeString = "Upgrade ("+ farm.get(satelliteselect).upgradeCost()+"$)";
        upgradeButton.setText(upgradeString);
        Button sellButton = (Button) v.findViewById(R.id.sell);
        String sellString = "Sell ("+ farm.get(satelliteselect).sellCost()+"$)";
        sellButton.setText(sellString);
        view.setText(String.valueOf(money));
    }

    public void sellFarm(View v, int satelliteselect, Toast toast, TextView text, PopupWindow popupWindow){
        money += farm.get(satelliteselect).sell();
        farm.get(satelliteselect).disable();
        saveCash();
        saveModifier(satelliteselect,farm.get(satelliteselect).getModifier());
        if(satelliteselect != 0) {
            boughtfarm.set(satelliteselect, false);
            touchcontrol.get(satelliteselect).buyFarm();
            saveBought(satelliteselect, boughtfarm.get(satelliteselect));
        }
        saveAuto(satelliteselect, false);
        String sell = "Planet #"+ (satelliteselect+1) + " was sold.";
        text.setText(sell);
        toast.show();
        view.setText(String.valueOf(money));
        popupWindow.dismiss();
    }

    public void autoFarm(View v, int satelliteselect, Toast toast, TextView text){
        String auto;
        if(money >= farm.get(satelliteselect).getScale()*100) {
            farm.get(satelliteselect).enable();
            money = money - farm.get(satelliteselect).getScale()*100;
            auto = "Planet #" + (satelliteselect + 1) + " can now be farmed automatically.";
            saveAuto(satelliteselect,true);
            v.setVisibility(View.GONE);
        }
        else {
            auto = "Not enough funds to purchase this upgrade";
        }
        text.setText(auto);
        toast.show();
        view.setText(String.valueOf(money));
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(!isplaying)music.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!isplaying)music.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!isplaying)music.start();
    }

    /**
     * This method saves our money value to SharedPreferences
     */
    public void saveCash(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("money",money);
        editor.apply();
    }

    /**
     * Saves the modifier per each farm
     * index: the index of the modifier saved
     * purchased: the number of times modifier has been upgraded
     */
    public void saveModifier(int index, int purchased){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("modifier"+index,purchased);
        editor.apply();
    }

    /**
     * Saves the status of if a farm has been bought or sold
     * @param index: planet #
     * @param bought: boolean if the farm has been bought or not
     */
    public void saveBought(int index, boolean bought){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("boughtfarm"+index,bought);
        editor.apply();
    }

    public void saveAuto(int index, boolean autoEnabled){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("auto"+index,autoEnabled);
        editor.apply();
    }

    /**
     * Unused code for making a satellite circle its respective planet
     * @param satelliteview the view of the satellite
     * @param planetview the view of the planet it orbits
     */
    public void moveAnimation(View satelliteview, View planetview){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Path path = new Path();
            float[] location1 = new float[2];
            location1[0] = satelliteview.getX();
            location1[1] = satelliteview.getY();
            //satelliteview.getLocationOnScreen(location1);
            //where location[0] is x, and location[1] is y
            float[] location2 = new float[2];

            //planetview.getLocationOnScreen(location2);
            location2[0] = planetview.getX();
            location2[1] = planetview.getY();
            int distance = (int) Math.sqrt(Math.pow(location1[0]-location2[0],2)+Math.pow(location1[1]-location2[1],2));
            view.setText(String.valueOf(distance));
            //path.arcTo(0, 465, 855, 1250, 0f, 359f, true);
            float left = location2[0]-distance;
            float top = location2[1]-distance;
            float right = location2[0]+distance;
            float bottom = location2[1]+distance;
            path.arcTo(left, top, right, bottom, 0f, 359f, true); //with first four parameters you determine four edge of a rectangle by pixel , and fifth parameter is the path'es start point from circle 360 degree and sixth parameter is end point of path in that circle
            ObjectAnimator animator = ObjectAnimator.ofFloat(satelliteview, View.X, View.Y, path); //at first parameter (view) put the target view
            animator.setDuration(10000);
            animator.setRepeatCount(Animation.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.start();
        }
    }
}
