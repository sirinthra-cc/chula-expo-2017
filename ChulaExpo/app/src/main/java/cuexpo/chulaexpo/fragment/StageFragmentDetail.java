package cuexpo.chulaexpo.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.inthecheesefactory.thecheeselibrary.manager.Contextor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cuexpo.chulaexpo.R;
import cuexpo.chulaexpo.adapter.StageListAdapter;
import cuexpo.chulaexpo.dao.ActivityItemCollectionDao;
import cuexpo.chulaexpo.manager.HttpManager;
import cuexpo.chulaexpo.view.StageInsideListItem;
import cuexpo.chulaexpo.view.StageListItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by nuuneoi on 11/16/2014.
 */
@SuppressWarnings("unused")
public class StageFragmentDetail extends Fragment {

    ExpandableListView expandableListView;
    StageListAdapter listAdapter;

    int previousGroup = -1;
    int day;
    int stageNo;

    public StageFragmentDetail() {
        super();
    }

    @SuppressWarnings("unused")
    public static StageFragmentDetail newInstance() {
        StageFragmentDetail fragment = new StageFragmentDetail();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            day = bundle.getInt("day", 15);
            stageNo = bundle.getInt("stageNo", 1);
        }

        if (savedInstanceState != null)
            onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stage_detail, container, false);
        initInstances(rootView, savedInstanceState);

        JSONObject range = new JSONObject();
        try {
            String startString = "2017-03-" + day + "T00:00:00.000Z";
            String endString = "2017-03-" + day + "T22:00:00.000Z";
            Date start = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(startString);
            Date end = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(endString);
            range.put("gte", start);
            range.put("lte", end);

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

        Call<ActivityItemCollectionDao> callStageActivity;

        if(stageNo==1){
            callStageActivity = HttpManager
                    .getInstance().getService().loadActivityByZone("589c52dfa8bbbb1c7165d3f1", "start", range);
        }else if(stageNo==2){
            callStageActivity = HttpManager
                    .getInstance().getService().loadActivityByZone("589c5330a8bbbb1c7165d3f2", "start", range);
        }else{
            callStageActivity = HttpManager
                    .getInstance().getService().loadActivityByZone("589c536ca8bbbb1c7165d3f3", "start", range);
        }

        callStageActivity.enqueue(callBackStageActivity);
        return rootView;
    }

    Callback<ActivityItemCollectionDao> callBackStageActivity = new Callback<ActivityItemCollectionDao>() {
        @Override
        public void onResponse(Call<ActivityItemCollectionDao> call, Response<ActivityItemCollectionDao> response) {
            if (response.isSuccessful()) {
                ActivityItemCollectionDao dao = response.body();

                List<StageListItem> head = new ArrayList<>();
                HashMap<StageListItem, StageInsideListItem> tail = new HashMap<>();

                for (int i = 0; i < dao.getResults().size(); i++) {
                    StageListItem item = new StageListItem(getContext());
                    item.setTime(dao.getResults().get(i).getStart().substring(11, 16)
                            , dao.getResults().get(i).getEnd().substring(11, 16));
                    item.setName(dao.getResults().get(i).getName().getEn());

                    head.add(item);

                    StageInsideListItem item2 = new StageInsideListItem(getContext());
                    item2.setDescription(dao.getResults().get(i).getDescription().getEn());
                    tail.put(head.get(i), item2);
                }

                listAdapter = new StageListAdapter(head, tail);
                expandableListView.setAdapter(listAdapter);

            } else {
                try {
                    Toast.makeText(Contextor.getInstance().getContext(), response.errorBody().string(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call<ActivityItemCollectionDao> call, Throwable t) {
            Toast.makeText(Contextor.getInstance().getContext(), t.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    private void init(Bundle savedInstanceState) {
        // Init Fragment level's variable(s) here
    }

    @SuppressWarnings("UnusedParameters")
    private void initInstances(View rootView, Bundle savedInstanceState) {
        // Init 'View' instance(s) with rootView.findViewById here
        expandableListView = (ExpandableListView) rootView.findViewById(R.id.stage_content_container);
        // Listview Group click listener
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return false;
            }
        });

        // Only One Item Will be Expanded
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != previousGroup)
                    expandableListView.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });

        // Listview Group collasped listener
        /*expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {

            }
        });*/

        // Listview on child click listener
        /*expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                return false;
            }
        });*/
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /*
     * Save Instance State Here
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Instance State here
    }

    /*
     * Restore Instance State Here
     */
    @SuppressWarnings("UnusedParameters")
    private void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore Instance State here
    }

}