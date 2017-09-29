package com.example.kocja.shroomer;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by kocja on 03/08/2017.
 */
@Table(database = AppDatabase.class)
class markerLocatio extends BaseModel {
    @PrimaryKey(autoincrement = true)
    int primary;
    @Column
    String latitude;
    @Column
    String longtitude;
    @Column
    String InfoWindow;
    @Column
    String Shroom_type;
    @Column
    String dateofShroomFound;
    @Column
    String photoURI;
    @Column
    int indexOfType;

}
