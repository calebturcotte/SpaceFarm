package com.example.spacefarm;




import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";

    int money;
    TextView view;
    Farm farm1, farm2;
    boolean boughtfarm2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //initialize all content for the farms and money obtained
        farm1 = new Farm(1);
        farm2 = new Farm(2);
        boughtfarm2 = false;
        view = findViewById(R.id.money);
        money = settings.getInt("money", money);
        view.setText(String.valueOf(money));

    }

    public void  click(View view2) {

        if(view2.getId() == R.id.farm) {
            money += farm1.contains();
            farm1.reset();//resets any money that might be inside the farm
            saveCash();//saves the money to shared preferences
        }
        else if(view2.getId() == R.id.farm2){
            if(boughtfarm2) {//checks if farm2 has been bought
                money += farm2.contains();
                farm2.reset();
                saveCash();//saves the money to shared preferences
            }
            else if (money >= 20){
                money = money - 20;
                boughtfarm2 = true;
                saveCash();//saves the money to shared preferences
            }
        }
        else if(view2.getId() == R.id.permafarm){
            farm1.enable();
            farm2.enable();

        }
        else if(view2.getId() == R.id.normiefarm){
            farm1.disable();
            farm2.disable();
        }
        else if(view2.getId() == R.id.sell){
            money += farm1.sell();
            money += farm2.sell();
            boughtfarm2 = false;
            saveCash();//saves the money to shared preferences
        }
        else if(view2.getId() == R.id.upgrade){
            if(money >= 10){//buys an upgrade if there is enough money
                money = money - 10;
                farm1.upgrade();
                farm2.upgrade();
                saveCash();//saves the money to shared preferences
            }
        }
        else if(view2.getId() == R.id.reset){
            money = 0;
            boughtfarm2 = false;
        }
        view.setText(String.valueOf(money));
    }

    public void saveCash(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("money",money);
        editor.commit();
    }

}
