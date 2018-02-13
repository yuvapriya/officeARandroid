package com.example.yumanoha.officeARandroid;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.example.yumanoha.officeARandroid.barcode.BarcodeCaptureActivity;

import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.PromptBehavior;

import static com.microsoft.aad.adal.AuthenticationConstants.OAuth2.AUTHORITY;

public class MainActivity extends AppCompatActivity {

    private static final int BARCODE_READER_REQUEST_CODE = 1;
    private TextView mResultTextView;

    private AuthenticationContext authenticationContext;
    private static final String CLIENT_ID = "da1a049e-1bf8-407c-8e76-bac9ff2f34dd";
    private static final String TENANT_ID = "72f988bf-86f1-41af-91ab-2d7cd011db47";
    private static final String REDIRECT_URI_BASE = "ms-appx-web://Microsoft.AAD.BrokerPlugin";
    private static final String REDIRECT_URI = REDIRECT_URI_BASE + "/" + CLIENT_ID;
    private static final String GRAPH_RESOURCE = "https://graph.microsoft.com";
    private static final String AUTHORITY = "https://login.microsoftonline.com/" + TENANT_ID;
    //private static final String LOG_TAG = "AUTH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create the authentication context.
        authenticationContext = new AuthenticationContext(MainActivity.this,
                AUTHORITY, true);

        // Acquire tokens using necessary UI.
        authenticationContext.acquireToken(MainActivity.this, GRAPH_RESOURCE, CLIENT_ID, REDIRECT_URI,
        PromptBehavior.Always, new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(AuthenticationResult result) {
                String idToken = result.getIdToken();
                String accessToken = result.getAccessToken();

                // Print tokens.
                /*Log.d(LOG_TAG, "ID Token: " + idToken);
                Log.d(LOG_TAG, "Access Token: " + accessToken);*/
            }

            @Override
            public void onError(Exception exc) {
                // TODO: Handle error
            }
        });

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        mResultTextView = findViewById(R.id.result_textview);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                startActivityForResult(new Intent(MainActivity.this.getApplicationContext(), BarcodeCaptureActivity.class), BARCODE_READER_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = (Barcode)data.getParcelableExtra("Barcode");
                    Point[] p = barcode.cornerPoints;
                    mResultTextView.setText(barcode.displayValue);
                } else
                    mResultTextView.setText(R.string.decode_error_text);
            }
            /*else*/
                /*Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode)))*/
        } else
            super.onActivityResult(requestCode, resultCode, data);
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
}

