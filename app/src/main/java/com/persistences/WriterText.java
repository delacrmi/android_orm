package com.persistences;

import com.delacrmi.persistences.Entity;
import com.delacrmi.persistences.annotation.Column;
import com.delacrmi.persistences.annotation.ManyToOne;
import com.delacrmi.persistences.annotation.Table;

/**
 * Created by delacrmi on 12/15/2015.
 */

@Table(Name = "RelationshipWriterText")
public class WriterText extends Entity {

    @Column(NotNull = true)
    @ManyToOne(ForeingKey = {"id"})
    public Writer writer;

    @Column(NotNull = true)
    @ManyToOne(ForeingKey = {"id"})
    public Text text;
}