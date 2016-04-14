package com.delacrmi.myapplication;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

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


/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */

@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {
    @Mock
    Context mockContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        new EntityManager(mockContext,"prueba",null,1)
                .addEntity(Users.class)
                .addEntity(Text.class)
                .addEntity(Text2.class).init();
        //cacheManager = new FileCacheManager(fakeContext);
    }

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void managerNotNull() throws Exception{
        Text text = new Text();
        assertNotNull("Will not be null",text.getEntityManager());
    }

    @Test
    public void createString() throws  Exception{
        Text text = new Text();
        assertEquals("Error in the create table string",
                "CREATE TABLE TEXT(E INTEGER PRIMARY KEY AUTOINCREMENT, CONTRASENA TEXT, U TEXT NOT NULL)",
                text.getCreateString());
    }
}