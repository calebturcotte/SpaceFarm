package com.example.spacefarm;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    int money;
    TextView view;
    Farm farm1, farm2;
    boolean boughtfarm2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize all content for the farms and money obtained
        farm1 = new Farm(1);
        farm2 = new Farm(2);
        boughtfarm2 = false;
        view = findViewById(R.id.money);
        money = 0;

    }

    public void  click(View view2) {

        if(view2.getId() == R.id.farm) {
            money += farm1.contains();
            farm1.reset();//resets any money that might be inside the farm
        }
        else if(view2.getId() == R.id.farm2){
            if(boughtfarm2) {//checks if farm2 has been bought
                money += farm2.contains();
                farm2.reset();
            }
            else if (money >= 20){
                money = money - 20;
                boughtfarm2 = true;

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
        }
        else if(view2.getId() == R.id.upgrade){
            if(money >= 10){//buys an upgrade if there is enough money
                money = money - 10;
                farm1.upgrade();
                farm2.upgrade();
            }
        }
        view.setText(String.valueOf(money));
    }


}
