package com.example.cynosure_10;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class PieChartActivity1 extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {

    String[] dropdown_content = {
            "now",
            "next_year"};
    PieChart pieChart;
    PieData pieData;
    PieDataSet pieDataSet;
    ArrayList pieEntries;
    ArrayList PieEntryLabels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart1);

        Spinner spin = (Spinner) findViewById(R.id.dropdown);
        spin.setOnItemSelectedListener(this);

//Creating the ArrayAdapter instance having the bank name list
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,dropdown_content);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);

        pieChart = findViewById(R.id.pieChart);
        Intent intent = getIntent();
        //System.out.println("*********************************************************"+intent.getIntExtra("value",0));
        int val = intent.getIntExtra("value",8);
        getEntries(val);
        pieDataSet = new PieDataSet(pieEntries, "Co2 Emission You saved!");
        pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        pieDataSet.setSliceSpace(2f);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(10f);
        pieDataSet.setSliceSpace(5f);
       Toast.makeText(this,"You are a Hero, thanks for saving the world!", Toast.LENGTH_LONG).show();
    }


    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
    {
        if(position==0)
        {
          //
        }
        else
        {
            Intent intent = new Intent(this,PieChartActivity2.class);
            intent.putExtra("value",10);
            startActivity(intent);
        }
    }



    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
// TODO Auto-generated method stub

    }
    private void getEntries(int val) {
        pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(val, 0));
        pieEntries.add(new PieEntry(val*20, 1));
    }

}

