package com.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import com.delacrmi.myapplication.R;

import com.delacrmi.simorm.ConnectSQLite;
import com.delacrmi.simorm.Entity;
import com.delacrmi.simorm.EntityFilter;
import com.delacrmi.simorm.EntityManager;
import com.persistences.Text;
import com.persistences.WriterText;
import com.persistences.Writer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    public static Context context;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        new EntityManager(this,"prueba",null,2){
            @Override
            public void onCreateDataBase(ConnectSQLite conn, SQLiteDatabase db) {
                super.onCreateDataBase(conn, db);
                Log.d("onCreateDatabase","testing the creation");
            }
        }.addEntity(Writer.class)
         .addEntity(Text.class)
         .addEntity(WriterText.class).init();

        EntityFilter filter = new EntityFilter("?");
        Text text = new Text();
        filter//.addArgument("user",null,"is not null","and")
                .addArgument("text","t%","like");

        //text.findOnce(filter);

        Writer writer = new Writer();

        Entity writeText = new WriterText();
        /*writer.user = "Ericka";
        writer.email = "e@gmail.com";
        writer.date = new Date();

        writeText.text = text;
        writeText.writer = writer;*/
        //writeText.save();

        //writer.save();

        /*text.text = "text 1";
        text.value = new BigDecimal("24000.50");
        text.save();*/

        //text.value = new BigDecimal("24000.2");
        //text.update();

        //writer.findOnce(filter);
        //writer.email = "er1@gmail.com";
        //writer.update();
        //Log.e(writer.toString(),writer.texts+" "+writer.date);

        for(Writer writer1: writer.find()) {
            Object o = writer1.getColumnValue("texts");

            if(o == null)
                Log.e("Object null","null null mull");
            else
                Log.i(writer1.toString() + " loop", ((Entity)((List)o).get(0)).refresh().getColumnValue("writer")+"");

            //Log.e(writer1.toString()+" JSON",writer1.getJSON().toString());
            Log.e("JSON created",new Writer().setColumnsFromJSON(writer1.getJSON()).getJSON().toString());
        }

        /*for(WriterText writerText: new WriterText().find()) {
            writerText.refresh();
            Log.e(writerText.toString(), writerText.writer.toString());
        }*/

       // Log.i(text.toString(),text.id+" "+text.value);

       /* text.value = new BigDecimal("24000.1");
        text.update();
        text.findOnce(filter);
        Log.i(text.toString(),text.id+" "+text.value);*/

        /*writeText.writer = writer;
        writeText.text = text;
        writeText.save();

        writeText.resetEntity();

        filter = new EntityFilter();
        filter.addArgument("id","2");
        writer.findOnce(filter);

        writeText.writer = writer;
        writeText.text = text;
        writeText.save();*/


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
}
