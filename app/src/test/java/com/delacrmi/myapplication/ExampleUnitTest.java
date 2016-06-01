package com.delacrmi.myapplication;

import android.content.Context;
import com.delacrmi.persistences.EntityManager;
import com.persistences.Text;
import com.persistences.Text2;
import com.persistences.Users;

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
    Users user;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        new EntityManager(mockContext,"prueba",null,1)
                .addEntity(Users.class)
                .addEntity(Text.class)
                .addEntity(Text2.class).init();

        text = new Text();
        user = new Users();
        text.password = "1234";
        //cacheManager = new FileCacheManager(fakeContext);
    }

    @Test
    public void managerNotNull() throws Exception{
        assertNotNull("Will not be null",text.getEntityManager());
    }

    @Test
    public void createString() throws  Exception{
        //System.out.println(text.getCreateString());
        assertEquals("Error in the create table string",
                "CREATE TABLE TEXT(E INTEGER PRIMARY KEY AUTOINCREMENT, CONTRASENA TEXT, U TEXT NOT NULL)",
                text.getCreateString());
    }

    @Test
    public void createStringWithRelationship() throws  Exception{
        //System.out.println(user.getCreateString());
        assertEquals("Error in the create table string",
                "CREATE TABLE USUARIO_SISTEMAS(ID INTEGER PRIMARY KEY AUTOINCREMENT, CONTRASENA TEXT NOT NULL, ROLE TEXT NOT NULL, TEXT2_U TEXT, TEXT_U TEXT,TEXT_E INTEGER, EMAIL TEXT NOT NULL, USUARIO TEXT NOT NULL)",
                user.getCreateString());
    }

    @Test
    public void getColumnValueNotNull() throws Exception{
        assertNotNull("The column can't be null",text.getColumnValue("contrasena"));
    }

    @Test
    public void getColumnValueNull() throws Exception{
        assertNull("The column will be null",text.user);
        assertEquals("The column will be 0",0,text.e);
    }
}