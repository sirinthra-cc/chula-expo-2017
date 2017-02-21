package cuexpo.chulaexpo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cuexpo.chulaexpo.R;
import cuexpo.chulaexpo.adapter.StageListAdapter;
import cuexpo.chulaexpo.view.DateSelector;
import cuexpo.chulaexpo.view.StageInsideListItem;
import cuexpo.chulaexpo.view.StageListItem;


/**
 * Created by nuuneoi on 11/16/2014.
 */
@SuppressWarnings("unused")
public class StageFragment extends Fragment implements View.OnClickListener {

    FragmentPagerItemAdapter pagerItemAdapter;
    ViewPager viewPager;
    SmartTabLayout viewPagerTab;
    TextView tvStageName;
    ImageView ivBack;
    int stageNo;

    public StageFragment() {
        super();
    }

    @SuppressWarnings("unused")
    public static StageFragment newInstance(int stageNo) {
        StageFragment fragment = new StageFragment();
        Bundle args = new Bundle();
        args.putInt("stageNo",stageNo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);

        stageNo = getArguments().getInt("stageNo");

        if (savedInstanceState != null)
            onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stage, container, false);
        initInstances(rootView, savedInstanceState);
        tvStageName.setText("Stage " + stageNo +" Schedule");

        return rootView;
    }

    private void init(Bundle savedInstanceState) {
        // Init Fragment level's variable(s) here
    }

    @SuppressWarnings("UnusedParameters")
    private void initInstances(View rootView, Bundle savedInstanceState) {
        // Init 'View' instance(s) with rootView.findViewById here

        tvStageName = (TextView) rootView.findViewById(R.id.stage_toolbar_title);
        ivBack = (ImageView) rootView.findViewById(R.id.stage_back);

        ivBack.setOnClickListener(this);

        Bundle date15 = new Bundle();
        date15.putInt("day", 15);
        date15.putInt("stageNo",stageNo);
        Bundle date16 = new Bundle();
        date16.putInt("day", 16);
        date16.putInt("stageNo",stageNo);
        Bundle date17 = new Bundle();
        date17.putInt("day", 17);
        date17.putInt("stageNo",stageNo);
        Bundle date18 = new Bundle();
        date18.putInt("day", 18);
        date18.putInt("stageNo",stageNo);
        Bundle date19 = new Bundle();
        date19.putInt("day", 19);
        date19.putInt("stageNo",stageNo);

        pagerItemAdapter = new FragmentPagerItemAdapter(
                this.getChildFragmentManager(), FragmentPagerItems.with(getActivity())
                .add("15\nMAR", StageFragmentDetail.class, date15)
                .add("16\nMAR", StageFragmentDetail.class, date16)
                .add("17\nMAR", StageFragmentDetail.class, date17)
                .add("18\nMAR", StageFragmentDetail.class, date18)
                .add("19\nMAR", StageFragmentDetail.class, date19)
                .create());

        viewPager = (ViewPager) rootView.findViewById(R.id.stage_pager);
        viewPager.setAdapter(pagerItemAdapter);

        viewPagerTab = (SmartTabLayout) rootView.findViewById(R.id.stage_pager_tab);
        viewPagerTab.setViewPager(viewPager);

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

    @Override
    public void onClick(View v) {
        if(v==ivBack){
            getActivity().finish();
        }
    }

}
