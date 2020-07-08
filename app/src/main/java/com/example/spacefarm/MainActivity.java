package com.example.spacefarm;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.io.IOException;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    /**
     * gravity: the animator responsible for each satellite
     */
    public static final String PREFS_NAME = "MyPrefsFile";
    private static SoundPool test;
    public static Context context;
    static long money;
    static TextView view;
    static MediaPlayer music;
    int currentvolume;
    static int soundeffvolume;
    public static boolean isplaying;
    private RewardedAd rewardedAd;
    CountDownTimer countDownTimer;
    static boolean timerisrunning;
    int totalplanets;
    Universe1 universe1;
    Universe2 universe2;
    Universe3 universe3;
    static int currentUniverse;
    SharedPreferences settings;
    //MusicPlayer test;
    //SoundPool test;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        settings = getSharedPreferences(PREFS_NAME, 0);
        totalplanets = 8;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        view = findViewById(R.id.money);
        int tempvolume = 80;
        soundeffvolume = settings.getInt("soundeffvolume", tempvolume);
        currentUniverse = 0;
        universe1 = new Universe1(settings, MainActivity.this, context, view);
        universe2 = new Universe2(settings, MainActivity.this, context, view);
        universe3 = new Universe3(settings, MainActivity.this, context, view);
        ft.replace(R.id.placeholder, universe1);
        ft.commit();
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        findViewById(R.id.mainscreen).setOnTouchListener(new BackGroundTouch(MainActivity.this, inflater));
//        final SpaceBackground spaceBackground = new SpaceBackground((ScrollView) findViewById(R.id.vScroll), (HorizontalScrollView) findViewById(R.id.hScroll));
//        findViewById(R.id.vScroll).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            public void onGlobalLayout() {
//                ScrollView vScroll = findViewById(R.id.vScroll);
//                HorizontalScrollView hScroll = findViewById(R.id.hScroll);
//                vScroll.scrollTo(0, vScroll.getBottom()/2);
//                hScroll.scrollTo(hScroll.getWidth()/2, 0);
//                vScroll.setOnTouchListener(spaceBackground);
////                hScroll.setOnTouchListener(spaceBackground);
////                vScroll.setScaleX(2f);
////                vScroll.setScaleY(2f);
////                RelativeLayout.LayoutParams mParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
////                findViewById(R.id.zoomView).setLayoutParams(mParams);
//                findViewById(R.id.vScroll).getViewTreeObserver().removeOnGlobalLayoutListener(this);
//            }
//        });

        //Set up our moving background
        final ImageView backgroundOne = findViewById(R.id.background);
        final ImageView backgroundTwo = findViewById(R.id.background2);
        final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(50000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                final float width = backgroundOne.getWidth();
                final float translationX = width * progress;
                backgroundOne.setTranslationX(translationX);
                backgroundTwo.setTranslationX(translationX - width);
            }
        });
        animator.start();

        timerisrunning = false;
        loadAd();

        money = settings.getLong("money", money);
        view.setText(calculateCash(money));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //add music to our app
        music = MediaPlayer.create(MainActivity.this,R.raw.spacefarmmaintheme);
        music.setLooping(true);
        currentvolume = settings.getInt("bgvolume", tempvolume);

        //SoundPool used for short music sound effects
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        test = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(5).build();
        test.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(sampleId, (float)soundeffvolume/100, (float)soundeffvolume/100, 1, 0, 1.0f);
            }
        });

        music.setVolume((float)currentvolume/100, (float)currentvolume/100);
        isplaying = settings.getBoolean("isplaying",isplaying);
        ImageView musicSetting = findViewById(R.id.soundView);
        if(!isplaying){
            musicSetting.setBackgroundResource(R.drawable.ic_music_on);
        } else {
            musicSetting.setBackgroundResource(R.drawable.ic_music_off);
        }

//
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

        final ImageView rightscroll = findViewById(R.id.right_arrow);
        rightscroll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ImageView leftscroll = findViewById(R.id.left_arrow);
                Animation leftscrollanimation = AnimationUtils.loadAnimation(context, R.anim.leftarrow);


                Animation rightscrollanimation = AnimationUtils.loadAnimation(context, R.anim.rightarrow);
                leftscroll.startAnimation(rightscrollanimation);
                rightscroll.startAnimation(leftscrollanimation);
                rightscroll.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

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
                builder.setIcon(R.drawable.ic_planet_7);
                builder.setTitle("Restart the Game?");
                builder.setMessage("This will sell all your planets and reset your money to 0.");
                //Negative Button is on left
                builder.setNegativeButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                money = 0;
                                view.setText(calculateCash(money));
                                saveCash();
                                universe1.reset();
                                universe2.reset();
                                universe3.reset();
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
            case R.id.options:
                // create the popup window
                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                assert inflater != null;
                final View optionsView = inflater.inflate(R.layout.options, null);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow optionsWindow = new PopupWindow(optionsView, width, height, focusable);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    optionsWindow.setElevation(20);
                }
                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                optionsWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                optionsView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                int maxVolume = 100;
                SeekBar volControl = (SeekBar)optionsView.findViewById(R.id.volumeBar);
                volControl.setMax(maxVolume);
                volControl.setProgress(currentvolume);
                volControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                        music.setVolume((float)arg1/100,(float)arg1/100);
                        currentvolume = arg1;
                        saveVolume("bgvolume",arg1);
                    }
                });
                SeekBar soundeffControl = (SeekBar)optionsView.findViewById(R.id.sound_effectBar);
                soundeffControl.setMax(maxVolume);
                soundeffControl.setProgress(soundeffvolume);
                soundeffControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                        soundeffvolume = arg1;
                        saveVolume("soundeffvolume",arg1);
                    }
                });
                return true;
            case R.id.about:
                LayoutInflater inflater2 = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                assert inflater2 != null;
                final View optionsView2 = inflater2.inflate(R.layout.popup_about, null);
                int width2 = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height2 = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable2 = true; // lets taps outside the popup also dismiss it
                final PopupWindow optionsWindow2 = new PopupWindow(optionsView2, width2, height2, focusable2);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    optionsWindow2.setElevation(20);
                }
                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                optionsWindow2.showAtLocation(view, Gravity.CENTER, 0, 0);
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
        loadAd();
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


    public void saveVolume(String volume, int value){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(volume,value);
        editor.apply();
    }

    /**
     * save any remaining booster from watched advertisement
     * @param value
     */
    public void saveBooster(int value){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("booster",value);
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
            //view.setText(String.valueOf(distance));
            //path.arcTo(0, 465, 855, 1250, 0f, 359f, true);
            float left = location2[0]-distance;
            float top = location2[1]-distance;
            float right = location2[0]+distance;
            float bottom = location2[1]+distance;
            path.arcTo(left, top, right, bottom, 0f, 359f, true); //with first four parameters you determine four edge of a rectangle by pixel , and fifth parameter is the path's start point from circle 360 degree and sixth parameter is end point of path in that circle
            ObjectAnimator animator = ObjectAnimator.ofFloat(satelliteview, View.X, View.Y, path); //at first parameter (view) put the target view
            animator.setDuration(10000);
            animator.setRepeatCount(Animation.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.start();
        }
    }

    public void loadAd(){
        rewardedAd = new RewardedAd(this,
                "ca-app-pub-3940256099942544/5224354917");
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                super.onRewardedAdLoaded();
                // Ad successfully loaded.
                //Toast.makeText(MainActivity.this, "adloaded", Toast.LENGTH_SHORT).show();
                 if (!timerisrunning)findViewById(R.id.button).setVisibility(View.VISIBLE);
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                super.onRewardedAdFailedToLoad(errorCode);
                // Ad failed to load.
            }


        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
    }

    public void showAd(View view){
        if(this.rewardedAd.isLoaded()){
            RewardedAdCallback callback = new RewardedAdCallback(){
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    //reward code
                    Toast.makeText(MainActivity.this, "reward earned", Toast.LENGTH_SHORT).show();
                    startTimer(1);
                }

                @Override
                public void onRewardedAdFailedToShow(int i) {
                    super.onRewardedAdFailedToShow(i);
                    //code for if ad fails to load
                }

                @Override
                public void onRewardedAdClosed() {
                    super.onRewardedAdClosed();
                    loadAd();
                    if(!isplaying)music.start();
                    Toast.makeText(MainActivity.this, "ad closed", Toast.LENGTH_SHORT).show();
                }
            };
            if(!isplaying)music.pause();
            this.rewardedAd.show(this, callback);

        }
    }

    /**
     * Starts our booster timer for extra cash earning
     * @param minuti : number of minutes for the timer
     */
    private void startTimer(final int minuti) {
        final ProgressBar barTimer = findViewById(R.id.barTimer);
        final TextView textTimer = findViewById(R.id.textView);
        findViewById(R.id.button).setVisibility(View.GONE);
        timerisrunning = true;
        universe1.setBooster(2);


        countDownTimer = new CountDownTimer(60 * minuti * 1000, 500) {
            // 500 means, onTick function will be called at every 500 milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                double percent = seconds / (60.0 * minuti)*100;
                barTimer.setProgress((int) percent);
                String timetext = String.format(Locale.getDefault(),"%02d", seconds / 60) + ":" + String.format(Locale.getDefault(),"%02d", seconds % 60);
                textTimer.setText(timetext);
                // format the textview to show the easily readable format

            }

            @Override
            public void onFinish() {
                textTimer.setText("");
                timerisrunning = false;
                findViewById(R.id.button).setVisibility(View.VISIBLE);
                universe1.setBooster(1);
            }
        }.start();
    }
    /**
     * PrevActivity is our fragments switcher used to swap our "universe" view with another before if available
     * @param view the view of the ui
     */
    public void PrevActivity(View view){
        // we will create a transaction between fragments
        FragmentTransaction fts = getSupportFragmentManager().beginTransaction();
        fts.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        // Replace the content of the container
        switch(currentUniverse) {
            case 0:
                universe3 = new Universe3(settings, MainActivity.this, context, MainActivity.view);
                fts.replace(R.id.placeholder, universe3);
                currentUniverse = 2;
                break;
            case 1:
                universe1 = new Universe1(settings, MainActivity.this, context, MainActivity.view);
                fts.replace(R.id.placeholder, universe1);
                currentUniverse = currentUniverse -1;
                break;
            case 2:
                universe2 = new Universe2(settings, MainActivity.this, context, MainActivity.view);
                fts.replace(R.id.placeholder, universe2);
                currentUniverse = currentUniverse -1;
                break;
        }
        // Commit the changes
        fts.commit();
    }

    /**
     * NextActivity is our fragments switcher used to swap our "universe" view with another
     * @param view the view of the ui
     */
    public void NextActivity(View view) {
//        old way to swap activities
//        Intent intent = new Intent(this, SecondActivity.class);
//        startActivity(intent);
//        //where right side is current view
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        FragmentTransaction fts = getSupportFragmentManager().beginTransaction();
        fts.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        switch(currentUniverse) {
            case 0:
                // we will create a transaction between fragments
                // Replace the content of the container
                universe2 = new Universe2(settings, MainActivity.this, context, MainActivity.view);
                fts.replace(R.id.placeholder, universe2);
                break;
            case 1:
                universe3 = new Universe3(settings, MainActivity.this, context, MainActivity.view);
                fts.replace(R.id.placeholder,universe3);
                break;
            case 2:
                universe1 = new Universe1(settings, MainActivity.this, context, MainActivity.view);
                fts.replace(R.id.placeholder, universe1);
                break;
        }
        // Commit the changes
        fts.commit();
        currentUniverse = (currentUniverse + 1) % 3;
    }

    /**
     * Converts a money value to a more readable format
     * @param money the money to be converted to a string
     * @return String value of money in a more readable view
     */
    static public String calculateCash(long money){
        long million = 1000000;
        long trillion = 1000000000000L;
        if(money >= trillion){
            return String.format(Locale.getDefault(),"%.2f", (double)money/trillion) + "T$";
        }
        else if(money >= million){
            return String.format(Locale.getDefault(),"%.2f", (double)money/million) + "M$";
        }
        else {
            return money + "$";
        }


    }

    /**
     * plays the sound for when a satellite is pressed
     */
    static public void playSatelliteSound(){
        if(!isplaying)test.load(context, R.raw.satellitepress, 1);
    }

    /**
     * plays the sound for when a planet is pressed
     */
    static public void playPlanetSound(boolean purchased) {
        if (!isplaying) {
            if (purchased) {
                test.load(context, R.raw.press5, 1);
            } else {
                test.load(context, R.raw.dull1, 1);
            }
        }
    }
}
