package cuexpo.cuexpo2017.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cuexpo.cuexpo2017.R;
import cuexpo.cuexpo2017.datatype.InterestItem;

/**
 * Created by Administrator on 2/15/2017.
 */

public class InterestListAdapterNew extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private ArrayList<InterestItem> interestItems;
    private Context context;

    public InterestListAdapterNew(Context context, ArrayList<InterestItem> interestItems) {
        this.context = context;
        this.interestItems = interestItems;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return interestItems.size();
    }

    @Override
    public Object getItem(int position) {
        return interestItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class InterestViewHolder {
        TextView titleTxt;
        ImageView interestImage;
        ImageView checkImage;
        ImageView iconImage;
        TextView titleEngTxt;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        InterestViewHolder holder = new InterestViewHolder();
        View interestView;
        InterestItem interestItem = (InterestItem) this.getItem(position);
        GridView.LayoutParams layoutParams = new GridView.LayoutParams(parent.getWidth()/3,parent.getWidth()/3);

        if (convertView != null) {
            interestView = convertView;
            Log.d("old view-1-", "load old view position = " + position + " with dim = " + interestView.getMeasuredWidth() + " : " +  interestView.getMeasuredHeight());
            if (interestView.getWidth() == 0)
                interestView.setLayoutParams(layoutParams);
            Log.d("old view-2-", "load old view position = " + position + " with dim = " + interestView.getMeasuredWidth() + " : " +  interestView.getMeasuredHeight());
        } else {
            interestView = inflater.inflate(R.layout.item_interest_v2, null);
            interestView.setLayoutParams(layoutParams);
            Log.d("new view-1-", "load position : " + position + " dim : " + layoutParams.height + " - " + layoutParams.width);
        }

        holder.titleTxt = (TextView) interestView.findViewById(R.id.interest_title);
        holder.titleEngTxt = (TextView) interestView.findViewById(R.id.interest_title_eng);
        holder.interestImage = (ImageView) interestView.findViewById(R.id.interest_image);
        holder.checkImage = (ImageView) interestView.findViewById(R.id.interest_check);
        holder.iconImage = (ImageView) interestView.findViewById(R.id.interest_icon);
//        ((ImageView) interestView.findViewById(R.id.dim)).setImageResource(R.drawable.dim_overlay);

        holder.interestImage.setImageResource(interestItem.getImageSrc());
        holder.iconImage.setImageResource(interestItem.getIconSrc());

        if(interestItem.isInterest()) holder.checkImage.setVisibility(View.VISIBLE);
        else holder.checkImage.setVisibility(View.INVISIBLE);

        holder.titleTxt.setText(interestItem.getName());
        holder.titleEngTxt.setText(interestItem.getNameEng());


        return interestView;
    }
}
