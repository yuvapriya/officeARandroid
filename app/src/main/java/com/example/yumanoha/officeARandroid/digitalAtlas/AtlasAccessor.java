package com.example.yumanoha.officeARandroid.digitalAtlas;

import android.app.Application;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;


/**
 * Created by yumanoha on 2/14/18.
 */

public class AtlasAccessor extends Application{
    private static String BASEURL = "https://officear-ppe.cloudapp.net/api/v1/adal";
    private final String accessToken;
    private final String idToken;
    private final RequestQueue queue;

    public AtlasAccessor(String idToken, String accessToken) {
        this.idToken = idToken;
        this.accessToken = accessToken;
        this.queue = Volley.newRequestQueue(this);
    }

    public void GetAtlasEntry(String qrcode) {

        String url = BASEURL + qrcode;
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);
    }


}
