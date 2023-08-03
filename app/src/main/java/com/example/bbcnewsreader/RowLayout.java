package com.example.bbcnewsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class RowLayout extends AppCompatActivity {

    //setup textviews
    TextView title;
    TextView pubDate;
    TextView description;
    Button link;
    Button favouriteButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_row_layout);

        //get intent
        Intent dataSent = getIntent();

        //instantiate textviews
        title = (TextView) findViewById(R.id.newsArticleTitle);
        pubDate = (TextView) findViewById(R.id.newsArticlePubDate);
        description = (TextView) findViewById(R.id.newsArticleDescription);
        link = (Button) findViewById(R.id.newsArticleLink);
        favouriteButton = (Button) findViewById(R.id.buttonFavourite);

        //set incoming data
        title.setText(getIntent().getStringExtra("title"));
        pubDate.setText(getIntent().getStringExtra("pubDate"));
        description.setText(getIntent().getStringExtra("description"));
        link.setText(getIntent().getStringExtra("link"));

        link.setOnClickListener( (click) -> {
            //make a link that opens the article in a browser
            Uri uri = Uri.parse(getIntent().getStringExtra("link"));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        favouriteButton.setOnClickListener( (click) -> {

        });

    }
}