package com.persistences;

import com.delacrmi.simorm.Entity;
import com.delacrmi.simorm.annotation.Column;
import com.delacrmi.simorm.annotation.OneToMany;
import com.delacrmi.simorm.annotation.Table;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by delacrmi on 12/15/2015.
 */

@Table
public class Text extends Entity<Text> {

    @Column(NotNull = true,
            PrimaryKey = true,
            AutoIncrement = true)
    public int id;

    @Column(NotNull = true)
    public String text;

    @Column
    public BigDecimal value;

    @Column
    @OneToMany(ForeingKey = {"text"})
    public List<WriterText> writers;

    /*@Column
    @OneToMany(ForeingKey = {"text"})
    public List<Writer> writers;*/
}