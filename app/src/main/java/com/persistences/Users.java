package com.persistences;

import android.database.sqlite.SQLiteDatabase;

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
    String email;

    @Column(NotNull = true)
    String role;

    @Column
    @OneToOne(ForeingKey = {"u","e"})
    public Text text;

    @Column
    @OneToOne(ForeingKey = {"u","e"},
            Create = false)
    public Text2 text2;

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
}