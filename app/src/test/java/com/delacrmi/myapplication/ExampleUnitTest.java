package com.delacrmi.myapplication;

import android.content.Context;
import com.delacrmi.persistences.EntityManager;
import com.persistences.WriterText;
import com.persistences.Text;
import com.persistences.Writer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */

@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {
    @Mock
    Context mockContext;

    Text text;
    Writer user;
    WriterText writeText;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        new EntityManager(mockContext,"prueba",null,1)
                .addEntity(Writer.class)
                .addEntity(Text.class)
                .addEntity(WriterText.class).init();

        text = new Text();
        text.text = "Prueba";

        user = new Writer();
        writeText = new WriterText();
    }

    @Test
    public void managerNotNull() throws Exception{
        assertNotNull("Will not be null",text.getEntityManager());
    }

    @Test
    public void createString() throws  Exception{
        //System.out.println(text.getCreateString());
        assertEquals("Error in the create table string",
                "CREATE TABLE TEXT(ID INTEGER PRIMARY KEY AUTOINCREMENT, TEXT TEXT NOT NULL)",
                text.getCreateString());
    }

    @Test
    public void createStringWithRelationshipOneToMany() throws  Exception{
        //System.out.println(user.getCreateString());
        assertEquals("Error in the create table string",
                "CREATE TABLE WRITER(ID INTEGER PRIMARY KEY AUTOINCREMENT, USER TEXT NOT NULL, EMAIL TEXT NOT NULL)",
                user.getCreateString());
    }

    @Test
    public void createStringWithRelationshipManyToOne() throws  Exception{
        //System.out.println(user.getCreateString());
        assertEquals("Error in the create table string",
                "CREATE TABLE RELATIONSHIPWRITERTEXT(WRITER_ID INTEGER NOT NULL, TEXT_ID INTEGER NOT NULL)",
                writeText.getCreateString());
    }

    @Test
    public void getColumnValueNotNull() throws Exception{
        assertNotNull("The column can't be null",text.getColumnValue("text"));
    }

    @Test
    public void getColumnValueNull() throws Exception{
        assertNull("The column will be null",user.user);
        assertEquals("The column will be 0",0,text.id);
    }

}