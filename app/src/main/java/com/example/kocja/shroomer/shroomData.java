package com.example.kocja.shroomer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

/**
 * Intent for viewing, deleting, and calling for editing the contents of the marker
 */

public class shroomData extends AppCompatActivity {
    markerLocatio locatio;
    private static final int requestEditShoom =5;
    RequestOptions options = new RequestOptions().centerCrop();
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shroomdata);

        final ImageView shroomImage = findViewById(R.id.shroomImage);
        final TextView DateOfFound = findViewById(R.id.DateOfFound);
        final TextView ShroomType = findViewById(R.id.ShroomType);
        final ImageButton deleteEntry = findViewById(R.id.deleteEntry);
        final ImageButton editButton = findViewById(R.id.editbutton);

        final Intent thisIntent = getIntent();
        double latitude = thisIntent.getDoubleExtra("setLatitude",-1);
        FlowManager.init(FlowConfig.builder(getApplicationContext())
                .addDatabaseConfig(DatabaseConfig.builder(AppDatabase.class)
                        .databaseName("AppDatabase")
                        .build())
                .build());
         SQLite.select()
                .from(markerLocatio.class)
                .where(markerLocatio_Table.latitude.eq(Double.toString(latitude)))
                .async()
                 .querySingleResultCallback(new QueryTransaction.QueryResultSingleCallback<markerLocatio>() {
                     @Override
                     public void onSingleQueryResult(QueryTransaction transaction, @Nullable markerLocatio markerLocatio) {
                         locatio = markerLocatio;
                         if(locatio != null) {
                             DateOfFound.setText(locatio.dateofShroomFound);
                             ShroomType.setText(locatio.Shroom_type);
                             Glide.with(shroomData.this).load(locatio.photoURI).apply(options).into(shroomImage);
                         }
                     }
                 }).execute();


        deleteEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(shroomData.this)
                        .setTitle("Warning")
                        .setMessage("Do you really want to delete this entry?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(RESULT_OK,thisIntent);
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startEdit = new Intent(shroomData.this,addShroom.class);
                startEdit.putExtra("Id",69);
                startEdit.putExtra("IndexOfImage",locatio.indexOfType);
                startActivityForResult(startEdit,requestEditShoom);
            }
        });

    }
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == requestEditShoom && resultCode == RESULT_OK && data != null) {
            int id = data.getIntExtra("ID", -1);
            if (id == 69) {
                String Uri = data.getStringExtra("URI");
                String dateOfFound = data.getStringExtra("dateOfFound");
                int indexOfType = data.getIntExtra("indexOfImage",-1);
                //this code is useless, since LatLng does not change, but I'll keep it for no apparent reason
                /*double shroomLat = data.getDoubleExtra("shroomlat",-1);
                double shroomLng = data.getDoubleExtra("shroomlng",-1);
                locatio.latitude = Double.toString(shroomLat);
                locatio.longtitude = Double.toString(shroomLng);*/
                locatio.dateofShroomFound = dateOfFound;
                locatio.photoURI = Uri;
                locatio.indexOfType = indexOfType;
                locatio.update();
            }
        }
    }
}
