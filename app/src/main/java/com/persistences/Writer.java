package com.persistences;

import android.database.sqlite.SQLiteDatabase;

import com.delacrmi.persistences.Entity;
import com.delacrmi.persistences.annotation.Column;
import com.delacrmi.persistences.annotation.OneToMany;
import com.delacrmi.persistences.annotation.Table;

import java.util.List;

/**
 * Created by delacrmi on 12/15/2015.
 */

@Table(AfterToCreated = {"setInsert"})
public class Writer extends Entity {

    @Column(PrimaryKey = true,
            AutoIncrement = true)
    public int id;

    @Column(PrimaryKey = true)
    public String user;

    @Column(NotNull = true)
    public String email;

    @Column
    @OneToMany(ForeingKey = {"writer"})
    public List<WriteText> texts;

    /*@Column
    @OneToOne(ForeingKey = {"u"})
    public Text text2;*/

    private void setInsert(SQLiteDatabase db){}
}