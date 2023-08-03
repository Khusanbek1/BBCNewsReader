package com.example.bbcnewsreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //setup components
    ListView listViewRSS;
    //list of NewsPage objects to store the articles
    ArrayList<NewsPage> newsPages;
    //list to store favourite articles
    ArrayList<NewsPage> favouriteNewsPages;
    //arrays to hold news page titles for listview, description, publication date, and link
    ArrayList<String> newsPageTitles;
    ArrayList<String> newsPageDescriptions;
    ArrayList<String> newsPagePubDates;
    ArrayList<String> newsPageLinks;
    //links
    private final String rssToJSON = "https://api.rss2json.com/v1/api.json?rss_url=";
    private final String rssLink = "https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml";
    private final String rssToJSONAPIKey = "&api_key=hhifmganyodcnmolmrpoxfv35kvpvkh4vn6bwuzi&count=15";
    HTTPRequest req;
    //toolbar and navigation drawer
    Toolbar toolbar;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newsPages = new ArrayList<NewsPage>();
        favouriteNewsPages = new ArrayList<NewsPage>();

        //instantiate components
        listViewRSS = (ListView) findViewById(R.id.listView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //navigation drawer
        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navView = (NavigationView)findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);

        //on clicking a listview item, display article information
        listViewRSS.setOnItemClickListener( (p, b, pos, id) -> {
            Intent rowLayout = new Intent(this, RowLayout.class);
            //set intent to move data to rowlayout activity
            rowLayout.putExtra("title", newsPages.get(pos).getTitle());
            rowLayout.putExtra("pubDate", newsPages.get(pos).getPubDate());
            rowLayout.putExtra("description", newsPages.get(pos).getDescription());
            rowLayout.putExtra("link", newsPages.get(pos).getLink());
            startActivity(rowLayout);

        });

        //instantiate arraylists
        newsPageTitles = new ArrayList<String>();
        newsPageDescriptions = new ArrayList<String>();
        newsPagePubDates = new ArrayList<String>();
        newsPageLinks = new ArrayList<String>();

         req = new HTTPRequest();
        //call the HTTPRequest
        //req.execute("https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");

        req.execute(rssToJSON + rssLink + rssToJSONAPIKey);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.About) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle(R.string.dialogTitle)
                    .setMessage(R.string.dialogMessage)
                    .setPositiveButton(R.string.dialogConfirm, (click, arg) -> {})
                    .create().show();
        } else if (item.getItemId() == R.id.refresh) {
            Toast.makeText(MainActivity.this, R.string.refreshMenuButton, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu items
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //navigation drawer menu selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.About) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle(R.string.dialogTitle)
                    .setMessage(R.string.dialogMessage)
                    .setPositiveButton(R.string.dialogConfirm, (click, arg) -> {})
                    .create().show();
        } else if (item.getItemId() == R.id.refresh) {
            Snackbar.make(listViewRSS, R.string.refreshMenuButton, Snackbar.LENGTH_LONG).show();
        }
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    //asynctask class
    public class HTTPRequest extends AsyncTask<String, Integer, String> {

        //progress dialog to track loading of news articles
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //display loading message
            progressDialog.setMessage("BBC News feed loading...");
            progressDialog.show();
        }

        /**
        //makes connection, reads from RSS feed, and places news into an arraylist
        @Override
        protected String doInBackground(String... strings) {

            try {
                //setup URL object
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream response = urlConnection.getInputStream();
                //setup XML factory
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();

                //extract data from RSS feed
                xpp.setInput(response, "UTF_8");

                //tracks if inside the article item tag
                boolean insideItem = false;

                //gets the tag to track where we are in the xml
                int eventType = xpp.getEventType();

                //loop over the xml document while we are not at the end of the document
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    //if we find a start tag, check if its an item start tag.
                    //indicates we are inside an article item and can start reading the news feed
                    if (eventType == XmlPullParser.START_TAG) {
                        //inside news article
                        if (xpp.getName().equalsIgnoreCase("item")) {
                            insideItem = true;

                        //start extracting data from RSS feed and place inside NewsPage object
                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                            //ensure we are inside the item tag and get the title
                            if (insideItem) {
                                newsPageTitles.add(xpp.nextText());
                            }
                        } else if (xpp.getName().equalsIgnoreCase("description")) {
                            //set description
                            if (insideItem) {
                                newsPageDescriptions.add(xpp.nextText());
                                System.out.println("in description insideItem else if");
                            }
                        } else if (xpp.getName().equalsIgnoreCase("link")) {
                            //set link
                            if (insideItem) {
                                newsPageLinks.add(xpp.nextText());
                            }
                        } else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                            //set publication date
                            if (insideItem) {
                                newsPagePubDates.add(xpp.nextText());
                            }
                        }

                        System.out.println("end of start tag if statement: " + xpp.nextTag());
                        System.out.println("insideItem status: " + insideItem);
                        System.out.println("xpp getName: " + xpp.getName());



                    //if we are at an end tag and its an item end tag then we are not inside the article item
                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                    }
                    //increment to next tag
                    eventType = xpp.next();
                    //eventType = xpp.nextTag();
                }

            } catch (MalformedURLException e) {
                System.out.println(e);
            } catch (XmlPullParserException e) {
                System.out.println(e);
            } catch (IOException e) {
                System.out.println(e);
            }

            return "Done";
        }
        **/

        //JSON version
        @Override
        protected String doInBackground(String... strings) {

            try {
                //setup URL object
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream response = urlConnection.getInputStream();

                //read the JSON
                BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                String result = sb.toString();

                //convert string to JSON
                JSONObject newsJSON = new JSONObject(result);

                //iterate over JSON array and set article components to their arraylists
                JSONArray newsJSONArray = newsJSON.getJSONArray("items");
                for (int i = 0; i < newsJSONArray.length(); i++) {
                    try {
                        //pull items from JSON array
                        JSONObject jObject = newsJSONArray.getJSONObject(i);
                        //place items into title, description, pubDate, link arrays
                        newsPageTitles.add(jObject.getString("title"));
                        newsPageDescriptions.add(jObject.getString("description"));
                        newsPagePubDates.add(jObject.getString("pubDate"));
                        newsPageLinks.add(jObject.getString("link"));
                    } catch (JSONException e) {
                        System.out.println(e);
                    }
                }
            } catch (MalformedURLException e) {
                System.out.println(e);
            } catch (IOException e) {
                System.out.println(e);
            } catch (Exception e) {
                System.out.println(e);
            }

            return "Done";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //place article info from each array into NewsPage object then into newsPages array
            NewsPage newsPage;
            for (int i = 0; i < newsPageTitles.size(); i++) {
                String title = newsPageTitles.get(i);
                String description = newsPageDescriptions.get(i);
                String pubDate = newsPagePubDates.get(i);
                String link = newsPageLinks.get(i);
                newsPage = new NewsPage(title, description, link, pubDate);
                //add news page into the newsPage array
                newsPages.add(newsPage);
            }
            //set listview title to display the news article title
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_list_item_1, newsPageTitles);
            listViewRSS.setAdapter(adapter);

            //close progress dialog
            progressDialog.dismiss();
        }
    }

}