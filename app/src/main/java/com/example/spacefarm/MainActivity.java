package com.example.spacefarm;




import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.ViewManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";
    public static Context context;
    static int money;
    TextView view;
    ArrayList<Farm> farm;
    boolean boughtfarm2;
    MediaPlayer music;
    boolean isplaying;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize all content for the farms and money obtained
        farm = new ArrayList<>(8);
        farm.add(new Farm(1));
        farm.add(new Farm(2));
        farm.add(new Farm(5));
        farm.add(new Farm(10));
        farm.add(new Farm(50));
        farm.add(new Farm(100));
        farm.add(new Farm(500));
        farm.add(new Farm(1000));
        boughtfarm2 = false;
        view = findViewById(R.id.money);
        money = settings.getInt("money", money);
        view.setText(String.valueOf(money));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //add music to our app

        music = MediaPlayer.create(MainActivity.this,R.raw.spacefarmmaintheme);
        music.setLooping(true);

        isplaying = settings.getBoolean("isplaying",isplaying);
        ImageView musicSetting = (ImageView) findViewById(R.id.soundView);
        if(!isplaying){
            musicSetting.setBackgroundResource(R.drawable.ic_music_on);
            music.start();
        } else {
            musicSetting.setBackgroundResource(R.drawable.ic_music_off);
        }

        ImageView farmbutton = (ImageView) findViewById(R.id.farm1);
        farmbutton.setOnTouchListener(new FarmTouch(farmbutton, view, farm.get(0), context));
        ImageView farmbutton2 = (ImageView) findViewById(R.id.farm2);
        farmbutton2.setOnTouchListener(new FarmTouch(farmbutton2, view, farm.get(1), context));
        ImageView farmbutton3 = (ImageView) findViewById(R.id.farm3);
        farmbutton3.setOnTouchListener(new FarmTouch(farmbutton3, view, farm.get(2), context));
        ImageView farmbutton4 = (ImageView) findViewById(R.id.farm4);
        farmbutton4.setOnTouchListener(new FarmTouch(farmbutton4, view, farm.get(3), context));
        ImageView farmbutton5 = (ImageView) findViewById(R.id.farm5);
        farmbutton5.setOnTouchListener(new FarmTouch(farmbutton5, view, farm.get(4), context));
        ImageView farmbutton6 = (ImageView) findViewById(R.id.farm6);
        farmbutton6.setOnTouchListener(new FarmTouch(farmbutton6, view, farm.get(5), context));
        ImageView farmbutton7 = (ImageView) findViewById(R.id.farm7);
        farmbutton7.setOnTouchListener(new FarmTouch(farmbutton7, view, farm.get(6), context));
        ImageView farmbutton8 = (ImageView) findViewById(R.id.farm8);
        farmbutton8.setOnTouchListener(new FarmTouch(farmbutton8, view, farm.get(7), context));
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
                money = 0;
                boughtfarm2 = false;
                view.setText(String.valueOf(money));
                saveCash();
                return true;
            case R.id.copy_item:
                // do your code
                return true;
            case R.id.print_item:
                // do your code
                return true;
            case R.id.share_item:
                // do your code
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void  click(View view2) {
        if(view2.getId() == R.id.f2){
            if(boughtfarm2) {//checks if farm2 has been bought
                money += farm.get(1).contains();
                farm.get(1).reset();
                saveCash();//saves the money to shared preferences
            }
            else if (money >= 20){
                money = money - 20;
                boughtfarm2 = true;
                saveCash();//saves the money to shared preferences
            }
        }
        else if(view2.getId() == R.id.permafarm){
            farm.get(0).enable();
            farm.get(1).enable();

        }
        else if(view2.getId() == R.id.normiefarm){
            farm.get(0).disable();
            farm.get(1).disable();
        }
        else if(view2.getId() == R.id.sell){
            money += farm.get(0).sell();
            money += farm.get(1).sell();
            boughtfarm2 = false;
            saveCash();//saves the money to shared preferences
        }
        else if(view2.getId() == R.id.upgrade){
            if(money >= 10){//buys an upgrade if there is enough money
                money = money - 10;
                farm.get(0).upgrade();
                farm.get(1).upgrade();
                saveCash();//saves the money to shared preferences
            }
        }
        else if(view2.getId() == R.id.reset){
            money = 0;
            boughtfarm2 = false;
        }
        view.setText(String.valueOf(money));
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
    }

    public void saveCash(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("money",money);
        editor.apply();
    }

}
