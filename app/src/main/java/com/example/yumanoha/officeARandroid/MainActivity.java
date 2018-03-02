package com.example.yumanoha.officeARandroid;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.example.yumanoha.officeARandroid.digitalAtlas.AtlasAccessor;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.example.yumanoha.officeARandroid.barcode.BarcodeCaptureActivity;

import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.PromptBehavior;

import static com.microsoft.aad.adal.AuthenticationConstants.OAuth2.AUTHORITY;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final int BARCODE_READER_REQUEST_CODE = 1;
    private static final String LOG_TAG = "OFFICEAR";
    private TextView mResultTextView;

    // For enabling overlay permission for React-Native component
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1212;

    private AuthenticationContext authenticationContext;
    private String accessToken = "";
//    private AtlasAccessor atlasAccessor;
    private static final String CLIENT_ID = "da1a049e-1bf8-407c-8e76-bac9ff2f34dd";
    private static final String TENANT_ID = "72f988bf-86f1-41af-91ab-2d7cd011db47";
    private static final String REDIRECT_URI_BASE = "ms-appx-web://Microsoft.AAD.BrokerPlugin";
    private static final String REDIRECT_URI = REDIRECT_URI_BASE + "/" + CLIENT_ID;
    private static final String RESOURCE = "https://microsoft.onmicrosoft.com/d8cab6db-ce19-4a16-b992-396ca763d6cb";
    private static final String AUTHORITY = "https://login.microsoftonline.com/" + TENANT_ID;
    //private static final String LOG_TAG = "AUTH";

    private static final String BASE_URL = "https://officear-ppe.cloudapp.net/api/v1/digitalatlas/";
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For enabling overlay permission for React-Native component
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }

        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);

        // Create the authentication context.
        authenticationContext = new AuthenticationContext(MainActivity.this,
                AUTHORITY, true);

        //authenticationContext.acquireToken(MainActivity.this, RESOURCE, CLIENT_ID, REDIRECT_URI, PromptBehavior.Auto, "", callback);


        // Acquire tokens using necessary UI.
        authenticationContext.acquireToken(MainActivity.this, RESOURCE, CLIENT_ID, REDIRECT_URI,
        PromptBehavior.Auto, new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(AuthenticationResult result) {
                String idToken = result.getIdToken();
                accessToken = result.getAccessToken();

                // Print tokens.
                Log.d(LOG_TAG, "ID Token: " + idToken);
                Log.d(LOG_TAG, "Access Token: " + accessToken);
//                atlasAccessor = new AtlasAccessor(idToken, accessToken);
            }

            @Override
            public void onError(Exception exc) {
                Log.d(LOG_TAG, exc.getMessage());
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
        // For enabling overlay permission for React-Native component
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "You cannot open the React Native app as you have denied the permission", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = (Barcode)data.getParcelableExtra("Barcode");
                    Point[] p = barcode.cornerPoints;
                    mResultTextView.setText(barcode.displayValue);
                    GetAtlasEntry(barcode.displayValue);
                    Intent intent = new Intent(this, ReactActivity.class);
                    startActivity(intent);
                } else
                    mResultTextView.setText(R.string.decode_error_text);
            }
            /*else*/
                /*Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode)))*/
        } else{
            super.onActivityResult(requestCode, resultCode, data);

            if(authenticationContext != null){
                authenticationContext.onActivityResult(requestCode, resultCode, data);
            }
        }
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

    /*private AuthenticationCallback<AuthenticationResult> callback = new AuthenticationCallback<AuthenticationResult>() {

        @Override
        public void onError(Exception exc) {
            if (exc instanceof AuthenticationException) {
                mResultTextView.setText("Cancelled");
            } else {
                mResultTextView.setText("Authentication error:" + exc.getMessage());
            }
        }

        @Override
        public void onSuccess(AuthenticationResult result) {
            //mResult = result;

            if (result == null || result.getAccessToken() == null
                    || result.getAccessToken().isEmpty()) {
                mResultTextView.setText("Token is empty");
            } else {
                // request is successful
                mResultTextView.setText("Passed");
            }
        }
    };*/

    public void GetAtlasEntry(String qrcode) {

        String url = BASE_URL + qrcode;
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        try {
                            String bundleUrl = response.getJSONObject("actions").getJSONObject("meeting").getString("jsBundleUrl");
                            mResultTextView.setText(bundleUrl);
                        } catch (JSONException e) {
                            Log.e("Response", e.toString());
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String, String>();
                headers.put("Authorization","Bearer " + accessToken);
                return headers;
            }
        };

        // add it to the RequestQueue
        queue.add(getRequest);
    }
}

