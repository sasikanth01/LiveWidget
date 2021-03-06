package com.primudesigns.livewidget.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.primudesigns.livewidget.R;
import com.primudesigns.livewidget.config.Config;
import com.primudesigns.livewidget.config.RemotePoint;
import com.primudesigns.livewidget.models.Constants;
import com.primudesigns.livewidget.models.Event;
import com.primudesigns.livewidget.ui.adapters.EventsAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpcomingFragment extends Fragment implements LoaderManager.LoaderCallbacks<Integer> {

    private static final int HTTP_OK = 200;
    private static final int HTTP_NOT_OK = 400;
    private static final String PARAM = "-utf-8";

    private ArrayList<Event> eventArrayList;
    private RecyclerView recyclerView;
    private TextView noConnection;
    private ProgressBar loading;

    public UpcomingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);

        int LOADER = 2;

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_events_list_upcoming);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        noConnection = (TextView) view.findViewById(R.id.tv_no_connection);

        Bundle queryBundle = new Bundle();
        queryBundle.putString("link", Config.BASE_URL);

        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<Integer> runtimeLoader = loaderManager.getLoader(LOADER);

        if (runtimeLoader == null) {
            loaderManager.initLoader(LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(LOADER, queryBundle, this);
        }


        return view;
    }

    @Override
    public Loader<Integer> onCreateLoader(int id, final Bundle args) {

        return new AsyncTaskLoader<Integer>(getContext()) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if (args == null) {
                    return;
                }

                forceLoad();
                loading.setVisibility(View.VISIBLE);

            }

            @Override
            public Integer loadInBackground() {

                int result = 0;
                String jsonData = "";

                eventArrayList = new ArrayList<>();

                String query = args.getString("link");

                try {

                    jsonData = RemotePoint.getJsonString(new URL(query));

                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray responseArray = jsonObject.getJSONArray("response");

                    for (int i = 0; i < responseArray.length(); i++) {

                        JSONObject item = responseArray.optJSONObject(i);

                        Event event = new Event();

                        event.setTitle(item.getString(Constants.TITLE));
                        event.setDescription(item.getString(Constants.DESCRIPTION));
                        event.setstartTimestamp(item.getString(Constants.START_TIME));
                        event.setendTimestamp(item.getString(Constants.END_TIME));
                        event.setStatus(item.getString(Constants.STATUS));
                        event.setCollege(item.getString(Constants.COLLEGE));
                        event.setcoverImage(item.optString(Constants.COVER_IMAGE));
                        event.setUrl(item.getString(Constants.URL));

                        if (Objects.equals(event.getStatus(), "UPCOMING")) {
                            eventArrayList.add(event);
                        }
                    }

                    result = HTTP_OK;


                } catch (Exception e) {
                    e.printStackTrace();
                }

                return result;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Integer> loader, Integer data) {

        loading.setVisibility(View.INVISIBLE);

        if (data == HTTP_OK) {

            EventsAdapter adapter = new EventsAdapter(getActivity(), eventArrayList);
            recyclerView.setAdapter(adapter);

        } else {
            noConnection.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Integer> loader) {

    }

}
