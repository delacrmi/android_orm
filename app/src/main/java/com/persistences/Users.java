package com.persistences;

import android.database.sqlite.SQLiteDatabase;
import android.hardware.camera2.params.StreamConfigurationMap;

import com.delacrmi.persistences.Entity;
import com.delacrmi.persistences.annotation.Column;
import com.delacrmi.persistences.annotation.ManyToOne;
import com.delacrmi.persistences.annotation.OneToOne;
import com.delacrmi.persistences.annotation.Table;

/**
 * Created by delacrmi on 12/15/2015.
 */

@Table(Name = "USUARIO_SISTEMAS",
        NickName = "usuario",
        AfterToCreated = {"setInsert"})
public class Users extends Entity {

    @Column(Name = "usuario",
            PrimaryKey = true)
    public String user;

    @Column(Name = "contrasena",
            NotNull = true)
    String password;

    @Column(NotNull = true)
     public String email;

    @Column(NotNull = true)
    public String role;

    @Column
    @ManyToOne(ForeingKey = {"u","e"})
    public Text text;

    @Column
    @OneToOne(ForeingKey = {"u"})
    public Text text2;

    @Column(PrimaryKey = true,
            AutoIncrement = true)
    public int id;

    private void setInsert(SQLiteDatabase db){
        /*user = "Miguel";
        password = "1234";
        email = "delacrmi@gmail.com";
        role = "admin";

        text = new Text();
        //text.e = 50l;
        text.user = "lol";
        text.save(db);

        text2 = new Text2();
        //text.e = 50l;
        text2.user = "lol";
        text2.save(db);
        save(db);*/
    }

    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }
}