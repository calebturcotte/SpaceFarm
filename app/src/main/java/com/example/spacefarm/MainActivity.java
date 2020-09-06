package com.example.spacefarm;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import android.os.CountDownTimer;
import android.transition.Fade;
import android.transition.TransitionManager;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    /**
     * gravity: the animator responsible for each satellite
     */
    public static final String PREFS_NAME = "MyPrefsFile";
    private static SoundPool soundPool;
    public Context context;
    static long money;
    private TextView view;
    static MediaPlayer music;
    static int currentvolume;
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
    static SharedPreferences settings;
    static boolean minigameAd;
    static CountDownTimer minigameUseTimer;
    static long timedifference;
    boolean firstabout;
    boolean firstTimetoday;
    int daysmultiplier;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        timerisrunning = false;
        totalplanets = 8;
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        settings = getSharedPreferences(PREFS_NAME, 0);
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
        //final ImageView backgroundThree = findViewById(R.id.background3);
        //final ImageView backgroundFour = findViewById(R.id.background4);
        final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(50000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                final float width = backgroundOne.getWidth();
                final float translationX = (width * progress);
                backgroundOne.setTranslationX(translationX);
                backgroundTwo.setTranslationX(translationX - width);
                //backgroundThree.setTranslationX(translationX - 2*width);
                //backgroundFour.setTranslationX(translationX - 3*width);
            }
        });
        animator.start();

        money = settings.getLong("money", money);
        //view.setText(calculateCash(money));
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
        soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(5).build();
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(sampleId, (float)soundeffvolume/100, (float)soundeffvolume/100, 1, 0, 1.0f);
            }
        });

        music.setVolume((float)currentvolume/100, (float)currentvolume/100);
        isplaying = settings.getBoolean("isplaying",isplaying);
        ImageView musicSetting = findViewById(R.id.soundView);
        if(!isplaying){
            musicSetting.setBackgroundResource(R.drawable.soundbutton_on);
        } else {
            musicSetting.setBackgroundResource(R.drawable.soundbutton_off);
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

        //compare the time that the app spent closed
        Date oldDate = new Date(settings.getLong("currenttime", 0L));
        Date currentDate = new Date();
        timedifference= 0L;
        if(!oldDate.equals(new Date(0))) {
            long oldtime = oldDate.getTime();
            long newtime = currentDate.getTime();
            timedifference = (newtime-oldtime)/1000L;
        }

        //fetch our first login of the day info
        firstTimetoday = settings.getBoolean("firstTimetoday", true);
        daysmultiplier = settings.getInt("daysmultiplier", 1);
        if(!firstTimetoday){
            Calendar newcalendar = Calendar.getInstance();
            Calendar oldcalendar = Calendar.getInstance();
            oldcalendar.setTime(oldDate);
            oldcalendar.set(Calendar.HOUR_OF_DAY, 0);
            oldcalendar.set(Calendar.MINUTE, 0);
            oldcalendar.set(Calendar.SECOND, 0);
            oldcalendar.set(Calendar.MILLISECOND, 0);

            newcalendar.set(Calendar.HOUR_OF_DAY, 0);
            newcalendar.set(Calendar.MINUTE, 0);
            newcalendar.set(Calendar.SECOND, 0);
            newcalendar.set(Calendar.MILLISECOND, 0);
            double daycount = TimeUnit.MILLISECONDS.toDays(
                    Math.abs(newcalendar.getTimeInMillis() - oldcalendar.getTimeInMillis()));
            if( daycount > 0){
                firstTimetoday = true;
                if(daycount == 1){
                    if(daysmultiplier < 14) {
                        daysmultiplier = daysmultiplier + 1;
                    }
                }
                else{
                    daysmultiplier = 1;
                }
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("daysmultiplier", daysmultiplier);
                editor.apply();
            }
        }


        firstabout = settings.getBoolean("mainabout", true);

        final ImageView rightscroll = findViewById(R.id.right_arrow);
        rightscroll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ImageView leftscroll = findViewById(R.id.left_arrow);
                Animation leftscrollanimation = AnimationUtils.loadAnimation(context, R.anim.leftarrow);


                Animation rightscrollanimation = AnimationUtils.loadAnimation(context, R.anim.rightarrow);
                leftscroll.startAnimation(rightscrollanimation);
                rightscroll.startAnimation(leftscrollanimation);
                //Toolbar toolbar1 = findViewById(R.id.toolbar);
                getSupportActionBar().setDisplayShowTitleEnabled(false);

                if(firstTimetoday){
                    final long reward =(long) Math.pow(10, daysmultiplier)*10;
                    LayoutInflater logininflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    assert logininflater != null;
                    View loginView = logininflater.inflate(R.layout.daily_login, null);
                    // create the popup window
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    boolean focusable = false; // lets taps outside the popup also dismiss it
                    final PopupWindow loginWindow = new PopupWindow(loginView, width, height, focusable);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        loginWindow.setElevation(20);
                    }

                    loginWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            firstTimetoday = false;
                            saveCash();
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("firstTimetoday",false);
                            editor.apply();
                        }
                    });
                    loginWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                    Button claimButton = loginView.findViewById(R.id.claim);
                    claimButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            playButtonSound(context);
                            money = money + reward;
                            view.setText(calculateCash(money));
                            loginWindow.dismiss();
                        }
                    });

                    TextView loginText = loginView.findViewById(R.id.logintext);
                    String rewardString = "Thank you for Playing!\nEarned: " + calculateCash(reward);
                    loginText.setText(rewardString);

                }
                if(firstabout){
                    about();
                }
                rightscroll.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        //animation for meteor button
        ImageView meteorbutton = (ImageView) findViewById(R.id.minigamebutton);
        AnimatorSet meteors = new AnimatorSet();
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(
                meteorbutton, "scaleX", 0.95f);
        scaleUpX.setDuration(1000L);
        scaleUpX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleUpX.setRepeatCount(ObjectAnimator.INFINITE);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(
                meteorbutton, "scaleY", 0.95f);
        scaleUpY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleUpY.setRepeatMode(ObjectAnimator.REVERSE);
        scaleUpY.setDuration(1000L);
        meteors.play(scaleUpX).with(scaleUpY);
        meteors.start();

        //animation for 2x button
        ImageView twobutton = (ImageView) findViewById(R.id.button);
        AnimatorSet boosters = new AnimatorSet();
        ObjectAnimator scaleUpX2 = ObjectAnimator.ofFloat(
                twobutton, "scaleX", 0.95f);
        scaleUpX2.setDuration(1000L);
        scaleUpX2.setRepeatMode(ObjectAnimator.REVERSE);
        scaleUpX2.setRepeatCount(ObjectAnimator.INFINITE);
        ObjectAnimator scaleUpY2 = ObjectAnimator.ofFloat(
                twobutton, "scaleY", 0.95f);
        scaleUpY2.setRepeatCount(ObjectAnimator.INFINITE);
        scaleUpY2.setRepeatMode(ObjectAnimator.REVERSE);
        scaleUpY2.setDuration(1000L);
        boosters.play(scaleUpX2).with(scaleUpY2);
        boosters.start();


        // resume timer for minigame if needed
        long minigameAdtime = settings.getLong("minigameAd", 0L);
        if(minigameAdtime <= 0L){
            minigameAd = true;
        }
        else {
            minigameAd = false;
            long minigameAdtime2 = minigameAdtime - timedifference;
            minigameUseTimer(minigameAdtime2);
        }

        //set remaining timer for booster if it exists
        long boostertime = settings.getLong("booster",0L);
        if(boostertime > 0L){
            startTimer(boostertime);
        }


    }

    private void about(){
        final int[] currentabout = {0};
        LayoutInflater aboutinflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        assert aboutinflater != null;
        final View aboutView = aboutinflater.inflate(R.layout.popup_about, null);
        int width2 = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height2 = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable2 = true; // lets taps outside the popup also dismiss it
        final PopupWindow aboutWindow = new PopupWindow(aboutView, width2, height2, focusable2);
        aboutWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                firstabout = false;
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("mainabout",false);
                editor.apply();
            }
        });


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            aboutWindow.setElevation(20);
        }
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        aboutWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        aboutView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutWindow.dismiss();
            }
        });

        Animation leftscrollanimation = AnimationUtils.loadAnimation(context, R.anim.leftarrow);
        Animation rightscrollanimation = AnimationUtils.loadAnimation(context, R.anim.rightarrow);
        aboutView.findViewById(R.id.right_arrow_popup).startAnimation(rightscrollanimation);
        aboutView.findViewById(R.id.left_arrow_popup).startAnimation(leftscrollanimation);

        final View about1 = aboutinflater.inflate(R.layout.aboutmain1, null);
        final View about2 = aboutinflater.inflate(R.layout.aboutmain2, null);
        final View about3 = aboutinflater.inflate(R.layout.aboutmain3, null);

        final FrameLayout frmlayout = (FrameLayout) aboutView.findViewById(R.id.aboutplaceholder);
        frmlayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                frmlayout.addView(about2,0);
                frmlayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        aboutView.findViewById(R.id.right_arrow_popup).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                currentabout[0] = (currentabout[0] +1)%3;
                frmlayout.removeAllViews();
                Fade mFade = new Fade(Fade.IN);
                if(!isplaying)playButtonSound(context);
                TransitionManager.beginDelayedTransition(frmlayout, mFade);
                if(currentabout[0] == 0){
                    frmlayout.addView(about2, 0);
                }
                else if(currentabout[0] == 1){
                    frmlayout.addView(about3, 0);
                }
                else if(currentabout[0] == 2){
                    frmlayout.addView(about1, 0);
                }
            }
        });

        aboutView.findViewById(R.id.left_arrow_popup).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                currentabout[0] = (currentabout[0] +2)%3;
                frmlayout.removeAllViews();
                if(!isplaying)playButtonSound(context);
                Fade mFade = new Fade(Fade.IN);
                TransitionManager.beginDelayedTransition(frmlayout, mFade);
                if(currentabout[0] == 0){
                    frmlayout.addView(about2, 0);
                }
                else if(currentabout[0] == 1){
                    frmlayout.addView(about3, 0);
                }
                else if(currentabout[0] == 2){
                    frmlayout.addView(about1, 0);
                }
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
        playButtonSound(context);
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
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putInt("daysmultiplier", 1);
                                editor.putBoolean("gamecomplete", false);
                                editor.apply();
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
                about();
                return true;
            case R.id.minigame:
                startMiniGame(null);
                return true;
            case R.id.rate:
                rateGame();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void music(View v){
        if(!isplaying){
            v.setBackgroundResource(R.drawable.soundbutton_off);
            music.pause();
        } else {
            v.setBackgroundResource(R.drawable.soundbutton_on);
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
        if(minigameUseTimer !=null){
            minigameUseTimer.cancel();
        }
        saveCurrentTime();
        if(!isplaying)music.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveCurrentTime();
        if(!isplaying)music.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        view.setText(calculateCash(money));
        if(!minigameAd){
            findViewById(R.id.minigamebutton).setVisibility(View.INVISIBLE);
            findViewById(R.id.minigamebuttonback).setVisibility(View.INVISIBLE);
        }
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
     * @param value: total seconds left in the booster timer
     */
    public void saveBooster(long value){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("booster",value);
        editor.apply();
    }

    /**
     * save current time to use to calculate time that has passed since app was closed
     */
    public void saveCurrentTime(){
        Date date = new Date();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("currenttime",date.getTime());
        editor.apply();
    }

//    /**
//     * Unused code for making a satellite circle its respective planet
//     * @param satelliteview the view of the satellite
//     * @param planetview the view of the planet it orbits
//     */
//    public void moveAnimation(View satelliteview, View planetview){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Path path = new Path();
//            float[] location1 = new float[2];
//            location1[0] = satelliteview.getX();
//            location1[1] = satelliteview.getY();
//            //satelliteview.getLocationOnScreen(location1);
//            //where location[0] is x, and location[1] is y
//            float[] location2 = new float[2];
//
//            //planetview.getLocationOnScreen(location2);
//            location2[0] = planetview.getX();
//            location2[1] = planetview.getY();
//            int distance = (int) Math.sqrt(Math.pow(location1[0]-location2[0],2)+Math.pow(location1[1]-location2[1],2));
//            //view.setText(String.valueOf(distance));
//            //path.arcTo(0, 465, 855, 1250, 0f, 359f, true);
//            float left = location2[0]-distance;
//            float top = location2[1]-distance;
//            float right = location2[0]+distance;
//            float bottom = location2[1]+distance;
//            path.arcTo(left, top, right, bottom, 0f, 359f, true); //with first four parameters you determine four edge of a rectangle by pixel , and fifth parameter is the path's start point from circle 360 degree and sixth parameter is end point of path in that circle
//            ObjectAnimator animator = ObjectAnimator.ofFloat(satelliteview, View.X, View.Y, path); //at first parameter (view) put the target view
//            animator.setDuration(10000);
//            animator.setRepeatCount(Animation.INFINITE);
//            animator.setRepeatMode(ValueAnimator.RESTART);
//            animator.start();
//        }
//    }

    public void loadAd(){
        rewardedAd = new RewardedAd(this,
                "ca-app-pub-3940256099942544/5224354917");
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                super.onRewardedAdLoaded();
                // Ad successfully loaded.
                //Toast.makeText(MainActivity.this, "adloaded", Toast.LENGTH_SHORT).show();
                 if (!timerisrunning){
                     findViewById(R.id.button).setVisibility(View.VISIBLE);
                     findViewById(R.id.twoxbuttonback).setVisibility(View.VISIBLE);
                 }
                 if (!minigameAd){
                     findViewById(R.id.minigamebutton).setVisibility(View.VISIBLE);
                     findViewById(R.id.minigamebuttonback).setVisibility(View.VISIBLE);
                 }
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                super.onRewardedAdFailedToLoad(errorCode);
                // Ad failed to load.
            }


        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
    }

    /**
     * show the advertisement used to play the 2x booster
     * @param view
     */
    public void showAd(View view){
        if(this.rewardedAd.isLoaded()){
            RewardedAdCallback callback = new RewardedAdCallback(){
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    //reward code
                    //Toast.makeText(MainActivity.this, "reward earned", Toast.LENGTH_SHORT).show();
                    startTimer(60L);
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
                }
            };
            if(!isplaying)music.pause();
            this.rewardedAd.show(this, callback);

        }
    }

    /**
     * show the advertisement used to play the minigame if it has been played before
     */
    public void showminigameAd(){
        if(this.rewardedAd.isLoaded()){
            if(!isplaying)music.pause();
            RewardedAdCallback callback = new RewardedAdCallback(){
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    //reward code
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putLong("minigameAd", 0L);
                    editor.apply();
                    minigameAd = true;
                    findViewById(R.id.minigamebutton).setVisibility(View.VISIBLE);
                    findViewById(R.id.minigamebuttonback).setVisibility(View.VISIBLE);
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
                    if(minigameAd)startMiniGame(null);
                }
            };
            this.rewardedAd.show(this, callback);

        }
    }

    /**
     * Starts our booster timer for extra cash earning
     * @param totalseconds : number of seconds for the timer
     */
    private void startTimer(final long totalseconds) {
        final ProgressBar barTimer = findViewById(R.id.barTimer);
        final TextView textTimer = findViewById(R.id.textView);
        findViewById(R.id.button).setVisibility(View.GONE);
        findViewById(R.id.twoxbuttonback).setVisibility(View.INVISIBLE);
        timerisrunning = true;
        universe1.setBooster(2);


        countDownTimer = new CountDownTimer(totalseconds * 1000, 500) {
            // 500 means, onTick function will be called at every 500 milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                double percent = 1.0*seconds / (totalseconds)*100;
                barTimer.setProgress((int) percent);
                saveBooster(seconds);
                String timetext = String.format(Locale.getDefault(),"%02d", seconds / 60) + ":" + String.format(Locale.getDefault(),"%02d", seconds % 60);
                textTimer.setText(timetext);
                // format the textview to show the easily readable format

            }

            @Override
            public void onFinish() {
                textTimer.setText("");
                timerisrunning = false;
                saveBooster(0L);
                findViewById(R.id.button).setVisibility(View.VISIBLE);
                findViewById(R.id.twoxbuttonback).setVisibility(View.VISIBLE);
                universe1.setBooster(1);
            }
        }.start();
    }
    /**
     * PrevActivity is our fragments switcher used to swap our "universe" view with another before if available
     * @param view the view of the ui
     */
    public void PrevActivity(View view){
        TextView textView = (TextView) findViewById(R.id.money);
        // we will create a transaction between fragments
        FragmentTransaction fts = getSupportFragmentManager().beginTransaction();
        fts.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        // Replace the content of the container
        switch(currentUniverse) {
            case 0:
                //universe3 = new Universe3(settings, MainActivity.this, context, textView);
                fts.replace(R.id.placeholder, universe3);
                currentUniverse = 2;
                break;
            case 1:
                //universe1 = new Universe1(settings, MainActivity.this, context, textView);
                fts.replace(R.id.placeholder, universe1);
                currentUniverse = currentUniverse -1;
                break;
            case 2:
                //universe2 = new Universe2(settings, MainActivity.this, context, textView);
                fts.replace(R.id.placeholder, universe2);
                currentUniverse = currentUniverse -1;
                break;
        }
        // Commit the changes
        if(!isplaying)soundPool.load(context, R.raw.arrow, 1);
        fts.commit();
    }

    /**
     * NextActivity is our fragments switcher used to swap our "universe" view with another
     * @param view the view of the ui
     */
    public void NextActivity(View view) {
        TextView textView = (TextView) findViewById(R.id.money);
        FragmentTransaction fts = getSupportFragmentManager().beginTransaction();
        fts.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        switch(currentUniverse) {
            case 0:
                // we will create a transaction between fragments
                // Replace the content of the container
                //universe2 = new Universe2(settings, MainActivity.this, context, textView);
                fts.replace(R.id.placeholder, universe2);
                break;
            case 1:
                //universe3 = new Universe3(settings, MainActivity.this, context, textView);
                fts.replace(R.id.placeholder,universe3);
                break;
            case 2:
                //universe1 = new Universe1(settings, MainActivity.this, context, textView);
                fts.replace(R.id.placeholder, universe1);
                break;
        }
        // Commit the changes
        if(!isplaying)soundPool.load(context, R.raw.arrow, 1);
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
    static public void playSatelliteSound(Context context){
        if(!isplaying)soundPool.load(context, R.raw.satellitepress, 1);
    }

    /**
     * plays the sound for when a planet is pressed
     */
    static public void playPlanetSound(boolean purchased, Context context) {
        if (!isplaying) {
            if (purchased) {
                soundPool.load(context, R.raw.buttonpress, 1);
            } else {
                soundPool.load(context, R.raw.buttondull, 1);
            }
        }
    }

    /**
     * plays the sound for when a button is pressed
     */
    static public void playButtonSound(Context context){
        if(!isplaying)soundPool.load(context, R.raw.pressdown, 1);
    }

    /**
     * plays the sound for when a button is released
     */
    static public void playButtonSoundUp(Context context){
        if(!isplaying)soundPool.load(context, R.raw.pressup, 1);
    }

    /**
     * start the minigame activity
     * @param v
     */
    public void startMiniGame(View v){
        if(!minigameAd){ showminigameAd();}
        if(minigameAd) {
            if(!isplaying)music.pause();
            Intent intent = new Intent(this, MiniGame.class);
            startActivity(intent);
            //where right side is current view
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    /**
     * timer for being able to play the minigame without watching an ad
     */
    public static void minigameUseTimer(long secondsleft){
        if(minigameUseTimer !=null){
            minigameUseTimer.cancel();
        }
        if (secondsleft >0) {
            minigameUseTimer = new CountDownTimer(secondsleft * 1000, 1000) {
                // 500 means, onTick function will be called at every 500 milliseconds

                @Override
                public void onTick(long leftTimeInMilliseconds) {
                    long seconds = leftTimeInMilliseconds / 1000;
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putLong("minigameAd", seconds);
                    editor.apply();
                }

                @Override
                public void onFinish() {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putLong("minigameAd", 0L);
                    editor.apply();
                    minigameAd = true;
                }
            }.start();
        }
        else{
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("minigameAd", 0L);
            editor.apply();
            minigameAd = true;
        }
    }

    /**
     * Rate the game on the playstore
     */
    public void rateGame(){
        try{
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } catch (ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }
}
