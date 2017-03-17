package cuexpo.cuexpo2017.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.inthecheesefactory.thecheeselibrary.manager.Contextor;

import cuexpo.cuexpo2017.R;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegisAdultActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    EditText  etRegisName,etEmail, etBirth, etCareer;
    Spinner   spGender;
    View btnNext;
    ImageView ivRegisProfile;
    String id,name,email,gender,birthday;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    String[] genderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regis_adult);
        initInstances();

        //TODO:Resize for fragmentation
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        btnNext.setOnClickListener(this);

        //get SharedPref
        sharedPref = getSharedPreferences("FacebookInfo", MODE_PRIVATE);
        editor = sharedPref.edit();
        id = sharedPref.getString("id", "");
        name = sharedPref.getString("name", "");
        email = sharedPref.getString("email", "");
        gender = sharedPref.getString("gender", "Male");
        //birthday = sharedPref.getString("birthday", "");
        editor.putString("type","Worker");
        editor.putString("profile","http://graph.facebook.com/"+id+"/picture?type=large");
        editor.apply();

        etRegisName.setText(name);
        etEmail.setText(email);
        etBirth.setText(birthday);

        etRegisName.addTextChangedListener(this);
        etEmail.addTextChangedListener(this);
        etBirth.addTextChangedListener(this);
        etCareer.addTextChangedListener(this);


        //Load Image
        Glide.with(this)
                .load("http://graph.facebook.com/"+id+"/picture?type=large")
                .placeholder(R.drawable.iv_profile_temp)
                .error(R.drawable.iv_profile_temp)
                .bitmapTransform(new CropCircleTransformation(this))
                .into(ivRegisProfile);
        //Spinner
        genderList = getResources().getStringArray(R.array.gender);
        ArrayAdapter<String> adapterGender = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, genderList);
        spGender.setAdapter(adapterGender);
        spGender.setSelection(gender.equals("Male")? 0 : 1,true);
        View spinnerSelectedView = spGender.getSelectedView();
        ((TextView)spinnerSelectedView).setTextColor(Color.WHITE);
    }

    private void initInstances() {
        etRegisName = (EditText)findViewById(R.id.etRegisName);
        etEmail = (EditText)findViewById(R.id.etEmail);
        etBirth = (EditText) findViewById(R.id.etBirth);
        etCareer = (EditText) findViewById(R.id.etCareer);
        spGender = (Spinner) findViewById(R.id.spGender);
        ivRegisProfile = (ImageView) findViewById(R.id.ivRegisProfile);
        btnNext = findViewById(R.id.btnNext);
        spGender.setOnItemSelectedListener(spGenderlistener);
    }


    @Override
    public void onClick(View v) {
        if(v == btnNext){
            if(etRegisName.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "กรุณาระบุชื่อผู้ใช้", Toast.LENGTH_SHORT).show();
            } else if (etEmail.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "กรุณาระบุอีเมล", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, InterestActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        editor.putString("name",etRegisName.getText().toString());
        Log.d("regis","Name: "+etRegisName.getText().toString());
        editor.putString("email",etEmail.getText().toString());
        try {
            editor.putInt("age", Integer.parseInt(etBirth.getText().toString()));
        } catch (NumberFormatException exception){
            editor.putInt("age", 0);
        }
        editor.putString("workerJob",etCareer.getText().toString());
        editor.commit();
    }

    AdapterView.OnItemSelectedListener spGenderlistener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            View spinnerSelectedView = spGender.getSelectedView();
            ((TextView)spinnerSelectedView).
                    setTextColor(ContextCompat.getColor(Contextor.getInstance().getContext(),R.color.dark_blue));

            String gen = genderList[spGender.getSelectedItemPosition()];
            if(gen.equals("ชาย")) gen = "Male";
            else if(gen.equals("หญิง")) gen = "Female";
            else gen = "Other";
            editor.putString("gender",gen);
            editor.commit();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
