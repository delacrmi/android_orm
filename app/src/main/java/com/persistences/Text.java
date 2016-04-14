package com.persistences;

import com.delacrmi.persistences.Entity;
import com.delacrmi.persistences.annotation.Column;
import com.delacrmi.persistences.annotation.OneToOne;
import com.delacrmi.persistences.annotation.Table;

/**
 * Created by delacrmi on 12/15/2015.
 */

@Table(Name = "text")
public class Text extends Entity {

    @Column(Name = "u",
            PrimaryKey = true,
            NotNull = true)
    public String user;

    @Column(Name = "contrasena")
    public String password;

    @Column(NotNull = true,
            PrimaryKey = true,
            AutoIncrement = true)
    public int e;

    /*@Column
    @OneToOne
    public Users users;*/
}