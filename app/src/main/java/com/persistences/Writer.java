package com.persistences;

import android.database.sqlite.SQLiteDatabase;

import com.delacrmi.simorm.Entity;
import com.delacrmi.simorm.annotation.Column;
import com.delacrmi.simorm.annotation.OneToMany;
import com.delacrmi.simorm.annotation.Table;

import java.util.Date;
import java.util.List;

/**
 * Created by delacrmi on 12/15/2015.
 */

@Table(AfterToCreated = {"setInsert"})
public class Writer extends Entity<Writer> {

    @Column(PrimaryKey = true,
            AutoIncrement = true)
    public int id;

    @Column(PrimaryKey = true)
    public String user;

    @Column(NotNull = true)
    public String email;

    @Column(Name = "date_insert")
    public Date date;

    @Column
    @OneToMany(ForeingKey = {"writer"})
    public List<WriterText> texts;

    /*@Column
    @OneToOne(ForeingKey = {"u"})
    public Text text2;*/

    private void setInsert(SQLiteDatabase db){}
}