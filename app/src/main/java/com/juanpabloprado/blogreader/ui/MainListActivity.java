package com.juanpabloprado.blogreader.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.juanpabloprado.blogreader.R;
import com.juanpabloprado.blogreader.model.Post;


public class MainListActivity extends ListActivity {
	
	public static final int NUMBER_OF_POSTS = 20;
	public static final String TAG = MainListActivity.class.getSimpleName();
	protected JSONObject mBlogData;
    protected ObjectMapper mapper = new ObjectMapper(); // create once, reuse
	protected ProgressBar mProgressBar;
	
	private final String KEY_TITLE = "title";
	private final String KEY_AUTHOR = "author";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

        getBlogPosts();
    }

    private void getBlogPosts() {
        try {
            URL blogFeedUrl = new URL("http://juanpabloprado.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);
            if (isNetworkAvailable()) {
                mProgressBar.setVisibility(View.VISIBLE);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(blogFeedUrl)
                        .build();

                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                        updateDisplayForError();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                        try {
                            String jsonData = response.body().string();
                            Log.v(TAG, jsonData);
                            if (response.isSuccessful()) {
                                //mForecast = parseForecastDetails(jsonData);
                                mBlogData = new JSONObject(jsonData);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        handleBlogResponse();
                                    }
                                });
                            } else {
                                updateDisplayForError();
                            }
                        }
                        catch (IOException e) {
                            Log.e(TAG, "Exception caught: ", e);
                        }
                        catch (JSONException e) {
                            Log.e(TAG, "Exception caught: ", e);
                        }
                    }
                });
            } else {
                Toast.makeText(this, getString(R.string.network_unavailable_message),
                        Toast.LENGTH_LONG).show();
            }
        } catch (MalformedURLException e) {
            logException(e);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	try {
    		JSONArray jsonPosts = mBlogData.getJSONArray("posts");
    		JSONObject jsonPost = jsonPosts.getJSONObject(position);
    		String blogUrl = jsonPost.getString("url");
    		
    		Intent intent = new Intent(this, BlogWebViewActivity.class);
    		intent.setData(Uri.parse(blogUrl));
    		startActivity(intent);
    	}
    	catch (JSONException e) {
    		logException(e);
    	}
    }

    private void logException(Exception e) {
    	Log.e(TAG, "Exception caught!", e);
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		
		boolean isAvailable = false;
		if (networkInfo != null && networkInfo.isConnected()) {
			isAvailable = true;
		}
		
		return isAvailable;
	}

	public void handleBlogResponse() {
		mProgressBar.setVisibility(View.INVISIBLE);
		
		if (mBlogData == null) {
			updateDisplayForError();
		}
		else {
			try {
				JSONArray jsonPosts = mBlogData.getJSONArray("posts");
                TypeReference ref = new TypeReference<ArrayList<Post>>() { };
                List<Post> posts = mapper.readValue(jsonPosts.toString(), ref);
				ArrayList<HashMap<String, String>> blogPosts =
						new ArrayList<HashMap<String, String>>();

                for(Post post: posts){
                    String title = Html.fromHtml(post.getTitle()).toString();
                    String author = Html.fromHtml(post.getAuthor()).toString();
                    HashMap<String, String> blogPost = new HashMap<String, String>();
                    blogPost.put(KEY_TITLE, title);
                    blogPost.put(KEY_AUTHOR, author);
                    blogPosts.add(blogPost);
                }
				
				String[] keys = { KEY_TITLE, KEY_AUTHOR };
				int[] ids = { android.R.id.text1, android.R.id.text2 };
				SimpleAdapter adapter = new SimpleAdapter(this, blogPosts,
						android.R.layout.simple_list_item_2, 
						keys, ids);
				
				setListAdapter(adapter);
			} 
			catch (JSONException e) {
				logException(e);
			} catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

	private void updateDisplayForError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
		
		TextView emptyTextView = (TextView) getListView().getEmptyView();
		emptyTextView.setText(getString(R.string.no_items));
	}

}
