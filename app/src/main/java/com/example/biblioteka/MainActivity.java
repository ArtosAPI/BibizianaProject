package com.example.biblioteka;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private int startIndex = 0;
    private final int maxResults = 10;
    private boolean isLoading = false;
    private boolean hasMore = true;

    private RequestQueue mRequestQueue;
    private ArrayList<BookInfo> bookInfoArrayList;
    private EditText searchEdt;
    private ImageButton searchBtn;

    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing our views.
        searchEdt = findViewById(R.id.idEdtSearchBooks);
        searchBtn = findViewById(R.id.idBtnSearch);
        mRecyclerView = findViewById(R.id.idRVBooks);

        // initializing on click listener for our button.
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // checking if our edittext field is empty or not.
                if (searchEdt.getText().toString().isEmpty()) {
                    searchEdt.setError("Please enter search query");
                    return;
                }
                // if the search query is not empty then we are
                // calling get book info method to load all
                // the books from the API.
                getBooksInfo(searchEdt.getText().toString());
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && hasMore) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        getBooksInfo(searchEdt.getText().toString());
                    }
                }
            }
        });
    }

    private void updateAdapter() {
        runOnUiThread(() -> {
            if (bookInfoArrayList.isEmpty()) {
                Toast.makeText(this, "No books found", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mRecyclerView.getAdapter() == null) {
                BookAdapter adapter = new BookAdapter(bookInfoArrayList, this);
                mRecyclerView.setAdapter(adapter);
            } else {
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }

    private void showLoading(boolean show) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showNoResults() {
        runOnUiThread(() ->
                Toast.makeText(this, "No more books", Toast.LENGTH_SHORT).show());
    }

    private void handleError(Exception e) {
        runOnUiThread(() ->
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void getBooksInfo(String query) {

        // creating a new array list.
        bookInfoArrayList = new ArrayList<>();

        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(MainActivity.this);

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.getCache().clear();

        // below is the url for getting data from API in json format.
        if (isLoading || !hasMore) return;
        isLoading = true;

        // Показываем ProgressBar внизу списка
        showLoading(true);

        String url = "https://www.googleapis.com/books/v1/volumes?q=" + query
                + "&startIndex=" + startIndex
                + "&maxResults=" + maxResults;

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);


        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // inside on response method we are extracting all our json data.
                try {
                    int totalItems = response.optInt("totalItems", 0);
                    hasMore = startIndex + maxResults < totalItems;

                    JSONArray itemsArray = response.getJSONArray("items");

                    if (itemsArray == null) {
                        showNoResults();
                        return;
                    }

                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject itemsObj = itemsArray.getJSONObject(i);
                        JSONObject volumeObj = itemsObj.optJSONObject("volumeInfo");
                        if (volumeObj == null) continue;

                        // Обработка авторов
                        ArrayList<String> authorsArrayList = new ArrayList<>();
                        if (volumeObj.has("authors")) {
                            JSONArray authorsArray = volumeObj.optJSONArray("authors");
                            for (int j = 0; j < authorsArray.length(); j++) {
                                authorsArrayList.add(authorsArray.optString(j));
                            }
                        }

                        // Обработка imageLinks
                        String thumbnail = "";
                        if (volumeObj.has("imageLinks")) {
                            JSONObject imageLinks = volumeObj.optJSONObject("imageLinks");
                            thumbnail = imageLinks.optString("thumbnail", "")
                                    .replace("http://", "https://");
                        }

                        // Обработка saleInfo
                        String buyLink = "";
                        JSONObject saleInfoObj = itemsObj.optJSONObject("saleInfo");
                        if (saleInfoObj != null) {
                            buyLink = saleInfoObj.optString("buyLink", "");
                        }

                        // Извлечение остальных полей с проверками
                        String title = volumeObj.optString("title", "No Title");
                        String subtitle = volumeObj.optString("subtitle", "");
                        String publisher = volumeObj.optString("publisher", "N/A");
                        String publishedDate = volumeObj.optString("publishedDate", "N/A");
                        String description = volumeObj.optString("description", "No Description");
                        int pageCount = volumeObj.optInt("pageCount", 0);
                        String previewLink = volumeObj.optString("previewLink", "");
                        String infoLink = volumeObj.optString("infoLink", "");

                        BookInfo bookInfo = new BookInfo(
                                title, subtitle, authorsArrayList, publisher,
                                publishedDate, description, pageCount, thumbnail,
                                previewLink, infoLink, buyLink
                        );
                        bookInfoArrayList.add(bookInfo);
                    }

                    // Обновление RecyclerView вне цикла
                    runOnUiThread(() -> {
                        mRecyclerView = findViewById(R.id.idRVBooks);
                        BookAdapter adapter = new BookAdapter(bookInfoArrayList, MainActivity.this);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        mRecyclerView.setAdapter(adapter);
                    });
                    startIndex += maxResults;
                    updateAdapter();
                } catch (JSONException e) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                } finally {
                    isLoading = false;
                    showLoading(false);
                }
                //
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // also displaying error message in toast.
                Toast.makeText(MainActivity.this, "Error found is " + error, Toast.LENGTH_SHORT).show();
            }
        });
        // at last we are adding our json object
        // request in our request queue.
        queue.add(booksObjrequest);
    }
}