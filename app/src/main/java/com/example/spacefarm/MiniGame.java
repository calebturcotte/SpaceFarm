package com.example.spacefarm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MiniGame extends AppCompatActivity {
    /**
     * Our MiniGame Activity to earn bonus cash for the Main Activity
     *
     * points: the amount of points earned in a round of the game
     * bombs:number of bombs in our minigame
     * totalmeteors: number of meteors left in our minigame
     */

    private static LayoutInflater inflater;
    private static int points;
    private boolean gameplayed;
    private RewardedAd rewardedAd;
    private MediaPlayer minigamemusic;
    private static boolean isplaying;
    private int mainvolume;
    private int soundeffectvolume;
    private static SoundPool soundPool;
    Context context;
    private int bombs;
    private int totalmeteors;
    private boolean firstabout;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minigame);
        context = getApplicationContext();
        inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        findViewById(R.id.mainscreen).setOnTouchListener(new BackGroundTouch(MiniGame.this, inflater));
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isplaying)soundPool.load(context, R.raw.pressdown, 1);
                start();
            }
        });
        findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isplaying)soundPool.load(context, R.raw.pressdown, 1);
                about();
            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isplaying)soundPool.load(context, R.raw.pressdown, 1);
                back();
            }
        });
        //TODO: add method for sound effects on touch and release while still having onclick functionality
//        findViewById(R.id.start).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent e) {
//                switch (e.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        if(!isplaying)soundPool.load(context, R.raw.pressdown, 1);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        start();
//                        if(!isplaying)soundPool.load(context, R.raw.pressup, 1);
//                        v.performClick();
//                        break;
//                }
//                return false;
//            }
//        });
//        findViewById(R.id.about).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent e) {
//                switch (e.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        if(!isplaying)soundPool.load(context, R.raw.pressdown, 1);
//                        v.performClick();
//                        break;
//                    case MotionEvent.ACTION_CANCEL:
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        about();
//                        if (!isplaying) soundPool.load(context, R.raw.pressup, 1);
//                        break;
//                }
//                return false;
//            }
//        });
//        findViewById(R.id.back).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent e) {
//                switch (e.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        if(!isplaying)soundPool.load(context, R.raw.pressdown, 1);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        back();
//                        if(!isplaying)soundPool.load(context, R.raw.pressup, 1);
//                        v.performClick();
//                        break;
//                }
//                return false;
//            }
//        });
        points = 0;
        gameplayed = false;

        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        int tempvolume = 80;
        soundeffectvolume = settings.getInt("soundeffvolume", tempvolume);
        mainvolume = settings.getInt("bgvolume", tempvolume);
        isplaying = settings.getBoolean("isplaying",isplaying);

        minigamemusic = MediaPlayer.create(MiniGame.this,R.raw.meteorshower);
        minigamemusic.setLooping(true);
        minigamemusic.setVolume((float)mainvolume/100, (float)mainvolume/100);

        //SoundPool used for short music sound effects
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(5).build();
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(sampleId, (float)soundeffectvolume/100, (float)soundeffectvolume/100, 1, 0, 1.0f);
            }
        });

        firstabout = settings.getBoolean("minigameabout", true);
        findViewById(R.id.about).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if(firstabout){
                    about();
                }
                findViewById(R.id.about).getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }});
    }

    /**
     * code for when the app resumes after being "closed"
     */
    @Override
    protected void onResume(){
        super.onResume();
        if(!isplaying)minigamemusic.start();
        loadAd();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveCurrentTime();
        if(!isplaying)minigamemusic.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveCurrentTime();
        if(!isplaying)minigamemusic.pause();
    }

    /**
     * override our finish method to include an exit animation
     */
    @Override
    public void finish(){
        super.finish();
        minigamemusic.pause();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * starts the game
     */
    public void start(){
        findViewById(R.id.start).setVisibility(View.INVISIBLE);
        findViewById(R.id.about).setVisibility(View.INVISIBLE);
        findViewById(R.id.back).setVisibility(View.INVISIBLE);
        countdownTimer(3);

    }

    /**
     * return to the main activity
     */
    public void back(){
//        //where right side is current view
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    /**
     * create an information popup about the minigame
     */
    public void about(){
        final int[] currentabout = {0};
        final LayoutInflater inflater2 = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        assert inflater2 != null;
        final View aboutView = inflater2.inflate(R.layout.popup_minigameabout, null);
        int width2 = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height2 = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow aboutWindow = new PopupWindow(aboutView, width2, height2, true);

        aboutWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                firstabout = false;
                SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("minigameabout", false);
                editor.apply();
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            aboutWindow.setElevation(20);
        }
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        aboutWindow.showAtLocation(findViewById(R.id.mainscreen), Gravity.CENTER, 0, 0);

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

        final View about1 = inflater2.inflate(R.layout.about1, null);
        final View about2 = inflater2.inflate(R.layout.about2, null);
        final View about3 = inflater2.inflate(R.layout.about3, null);

        final FrameLayout frmlayout = (FrameLayout) aboutView.findViewById(R.id.aboutplaceholder);
        frmlayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                frmlayout.addView(about1,0);
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
                if(!isplaying)soundPool.load(context, R.raw.pressdown, 1);
                TransitionManager.beginDelayedTransition(frmlayout, mFade);
                if(currentabout[0] == 0){
                    frmlayout.addView(about1, 0);
                }
                else if(currentabout[0] == 1){
                    frmlayout.addView(about2, 0);
                }
                else if(currentabout[0] == 2){
                    frmlayout.addView(about3, 0);
                }
            }
        });

        aboutView.findViewById(R.id.left_arrow_popup).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                currentabout[0] = (currentabout[0] +2)%3;
                frmlayout.removeAllViews();
                if(!isplaying)soundPool.load(context, R.raw.pressdown, 1);
                Fade mFade = new Fade(Fade.IN);
                TransitionManager.beginDelayedTransition(frmlayout, mFade);
                if(currentabout[0] == 0){
                    frmlayout.addView(about1, 0);
                }
                else if(currentabout[0] == 1){
                    frmlayout.addView(about2, 0);
                }
                else if(currentabout[0] == 2){
                    frmlayout.addView(about3, 0);
                }
            }
        });

    }

    /**
     * Counts down from specified value to 0 for the start of the game
     * @param count: number of seconds for the countdown
     */
    public void countdownTimer(int count){
        final int tempcount = count;
        final TextView startText = findViewById(R.id.text);
        ValueAnimator animator = ObjectAnimator.ofFloat(startText, "textSize", 30f);
        animator.setDuration(1000);
        if(count <= 0){
            String temp = "Go!";
            if(!isplaying)soundPool.load(context, R.raw.start, 1);
            startText.setText(temp);
            animator.addListener(new AnimatorListenerAdapter(){
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    startText.setVisibility(View.GONE);
                    startTimer(1);
                    // done
                }
            });
        } else {
            if(!isplaying)soundPool.load(context, R.raw.countdown, 1);
            startText.setText(String.valueOf(count));
            animator.addListener(new AnimatorListenerAdapter(){
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    countdownTimer(tempcount-1);
                    // done
                }
            });
        }
        animator.start();
    }

    /**
     * Starts the timer for our minigame
     * @param minuti tens of seconds used to countdown
     */
    private void startTimer(final int minuti) {
        final TextView textTimer = findViewById(R.id.gametime);
        textTimer.setVisibility(View.VISIBLE);
        TextView pointTimer = findViewById(R.id.gamepoint);
        pointTimer.setVisibility(View.VISIBLE);
        pointTimer.setText(String.valueOf(0));
        final Random random = new Random();
        totalmeteors = 60;
        bombs = 10;
        new CountDownTimer(30 * minuti * 1000, 500) {
            // 500 means, onTick function will be called at every 500 milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                float probability = 1 - (float)(1.0*(totalmeteors - bombs)/totalmeteors);
                boolean ismeteor = random.nextFloat() > probability;
                meteorShower(ismeteor);
                long seconds = leftTimeInMilliseconds / 1000;
                String timetext = String.format(Locale.getDefault(),"%02d", seconds % 60)+ " s";
                textTimer.setText(timetext);

                if(!ismeteor) {
                    bombs = bombs -1;
                }
                totalmeteors = totalmeteors -1;
            }

            @Override
            public void onFinish() {
                Handler h = new Handler();
                //Run a runnable after 100ms (after that time it is safe to remove the view)
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        endGame();
                    }
                }, 2000);
            }
        }.start();
    }

    /**
     * create a meteor and animate it down a random path of the screen
     */
    private void meteorShower(boolean ismeteor){
        final ConstraintLayout framelayout = findViewById(R.id.mainscreen);
        final FrameLayout applayout = new FrameLayout(this);
        ConstraintLayout.LayoutParams mParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT*2);
        applayout.setLayoutParams(mParams);
        final int ranx = new Random().nextInt(framelayout.getWidth());
        applayout.setPadding(0, 0 , 0, 0);
        final int ranx2 = new Random().nextInt(framelayout.getWidth());
        View meteor;
        if(ismeteor) {
            meteor = inflater.inflate(R.layout.meteor, null);
        }
        else {
            meteor = inflater.inflate(R.layout.bomb, null);
        }
        applayout.setClipChildren(false);
        applayout.setClipToPadding(false);
        applayout.addView(meteor);
        applayout.setOnTouchListener(new MeteorTouch((TextView) findViewById(R.id.gamepoint), context, ismeteor, MiniGame.this, inflater));
        framelayout.addView(applayout);

        final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(2000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                float width = ranx - (ranx-ranx2)*progress;
                if(width > framelayout.getWidth() - applayout.getWidth()){
                    width = framelayout.getWidth() - applayout.getWidth();
                }
                final float height = framelayout.getHeight();
                final float scaleX = 0.4f + (0.6f*progress);
                final float scaleY = 0.4f + (0.6f*progress);
                final float translationY = height*progress;
                applayout.setScaleX(scaleX);
                applayout.setScaleY(scaleY);
                applayout.setTranslationY(translationY);
                applayout.setTranslationX(width);
            }
        });
        ObjectAnimator rotate = ObjectAnimator.ofFloat(meteor,"rotation", 0f, 360f );
        rotate.setDuration(1000L);
        rotate.setRepeatCount(2);
        rotate.setInterpolator(new LinearInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator).with(rotate);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                applayout.setVisibility(View.GONE);
                //Create handler on the current thread (UI thread)
                Handler h = new Handler();
                //Run a runnable after 100ms (after that time it is safe to remove the view)
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        framelayout.removeView(applayout);
                    }
                }, 100);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    /**
     * Earn a point and display it for our minigame
     * @param pointView: The TextView displaying points earned
     */
    public static void pointEarned(TextView pointView){
        points++;
        if(pointView !=null) pointView.setText(String.valueOf(points));
    }

    /**
     * Lose 5 points and display it for our minigame
     * @param pointView: The TextView displaying points earned
     */
    public static void pointLost(TextView pointView){
        points = points -5;
        if(points < 0)points = 0;
        if(pointView !=null) pointView.setText(String.valueOf(points));
    }

    /**
     * ends the game and displays any needed message
     */
    private void endGame(){
        TextView text = findViewById(R.id.text);
        text.setVisibility(View.VISIBLE);
        long tempmoney = MainActivity.money;
        long moneyprize = (long)(tempmoney*(points/50.0)*0.05+ 0.5);
        if(moneyprize < 100){
            moneyprize = 100;
        }
        String end = "Earned "+ MainActivity.calculateCash(moneyprize);
        text.setText(end);
        gameplayed = true;
        MainActivity.minigameAd = false;
        MainActivity.minigameUseTimer(60*5);
        points = 0;
        findViewById(R.id.about).setVisibility(View.VISIBLE);
        findViewById(R.id.back).setVisibility(View.VISIBLE);
        Button start = findViewById(R.id.start);
        String playagain = "Play again?\n(watch ad)";
        start.setText(playagain);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAd(v);
            }
        });
        start.setVisibility(View.VISIBLE);
        MainActivity.money = MainActivity.money + moneyprize;
        saveCash();
    }

    /**
     * load ad for replaying game
     */
    public void loadAd(){
        rewardedAd = new RewardedAd(this,
                "ca-app-pub-3940256099942544/5224354917");
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                super.onRewardedAdLoaded();
                // Ad successfully loaded.
//                if (!timerisrunning){
//                    findViewById(R.id.button).setVisibility(View.VISIBLE);
//                }
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
     * show the advertisement which will start the minigame if watched
     * @param view our view
     */
    public void showAd(View view){
        if(this.rewardedAd.isLoaded()){
            RewardedAdCallback callback = new RewardedAdCallback(){
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    //reward code
                    gameplayed = false;
                }

                @Override
                public void onRewardedAdFailedToShow(int i) {
                    super.onRewardedAdFailedToShow(i);
                    //code for if ad fails to load
                }

                @Override
                public void onRewardedAdClosed() {
                    super.onRewardedAdClosed();
                    if(!gameplayed) {
                        countdownTimer(3);
                        findViewById(R.id.start).setVisibility(View.INVISIBLE);
                        findViewById(R.id.about).setVisibility(View.INVISIBLE);
                        findViewById(R.id.back).setVisibility(View.INVISIBLE);
                        if(!isplaying)minigamemusic.start();

                    }
                    loadAd();

                }
            };
            if(!isplaying)minigamemusic.pause();
            this.rewardedAd.show(this, callback);
        }
    }

    /**
     * plays sound when meteor is tapped
     */
    static public void playMeteorSound(Context context, boolean ismeteor){
        if(!isplaying){
            if(ismeteor){
                soundPool.load(context, R.raw.point, 1);
            }
            else {
                soundPool.load(context, R.raw.crash, 1);
            }
        }
    }

    /**
     * This method saves our money value to SharedPreferences
     */
    public void saveCash(){
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("money",MainActivity.money);
        editor.apply();
    }

    /**
     * save current time to use to calculate time that has passed since app was closed
     */
    public void saveCurrentTime(){
        Date date = new Date();
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("currenttime",date.getTime());
        editor.apply();
    }
}
