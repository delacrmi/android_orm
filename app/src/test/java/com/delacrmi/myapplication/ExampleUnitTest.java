package com.delacrmi.myapplication;

import android.content.Context;

import com.delacrmi.persistences.EntityFilter;
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
    EntityFilter filter;

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

        assertEquals("Error in the create table string",
                "CREATE TABLE TEXT(ID INTEGER PRIMARY KEY AUTOINCREMENT, TEXT TEXT NOT NULL)",
                text.getCreateString());
    }

    @Test
    public void createStringWithRelationshipOneToMany() throws  Exception{

        assertEquals("Error in the create table string",
                "CREATE TABLE WRITER(ID INTEGER PRIMARY KEY AUTOINCREMENT, DATE_INSERT NUMERIC, USER TEXT NOT NULL, EMAIL TEXT NOT NULL)",
                user.getCreateString());
    }

    @Test
    public void createStringWithRelationshipManyToOne() throws  Exception{

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

    @Test
    public void createASimpleFilter() throws Exception{
        filter = text.createSimpleFilter();
        assertNotNull("The filter Object can't be null",filter);
        assertEquals("The string returned should be like","ID = ?",filter.getWhereValue());
    }

    @Test
    public void createASimpleFilter2() throws Exception{
        user.id = 1;
        user.user = "Ericka";
        filter = user.createSimpleFilter();
        assertNotNull("The filter Object can't be null",filter);
        assertEquals("The string returned will be like","ID = ? and USER = ?",filter.getWhereValue());
        assertEquals("The length should be equals",2,filter.getArgumentValue().length);
        assertEquals("The id should be","1",filter.getArgumentValue()[0]);
        assertEquals("The id should be","Ericka",filter.getArgumentValue()[1]);
    }

    @Test
    public void addValueFilter(){
        filter = new EntityFilter()
                .addArgument("id","1");
        assertEquals("Filter should be equals to","id = ?",filter.getWhereValue());
        assertNotNull("The Object can't be null",filter.getArgumentValue());
    }

    @Test
    public void addValueFilter1(){
        filter = new EntityFilter()
                .addArgument("user",null,"is null");
        assertEquals("Filter should be equals to","user is null",filter.getWhereValue());
        assertNull("The Object should be null",filter.getArgumentValue());
    }

    @Test
    public void addValueFilter2(){
        filter = new EntityFilter()
                .addArgument("user",null,"is null","and")
                .addArgument("id","1","like");
        assertEquals("Filter should be equals to","user is null and id like ?",filter.getWhereValue());
        assertEquals("The length should be equals",1,filter.getArgumentValue().length);
    }

}