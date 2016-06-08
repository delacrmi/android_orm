package com.persistences;

import com.delacrmi.persistences.Entity;
import com.delacrmi.persistences.annotation.Column;
import com.delacrmi.persistences.annotation.OneToMany;
import com.delacrmi.persistences.annotation.Table;

import java.util.List;

/**
 * Created by delacrmi on 12/15/2015.
 */

@Table
public class Text extends Entity {

    @Column(NotNull = true,
            PrimaryKey = true,
            AutoIncrement = true)
    public int id;

    @Column(NotNull = true)
    public String text;

    @Column
    @OneToMany(ForeingKey = {"text"})
    public List<WriterText> writers;

    /*@Column
    @OneToMany(ForeingKey = {"text"})
    public List<Writer> writers;*/
}