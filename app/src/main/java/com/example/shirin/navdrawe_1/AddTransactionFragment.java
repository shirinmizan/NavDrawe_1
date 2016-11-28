package com.example.shirin.navdrawe_1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.ParseException;
import java.util.Locale;

import static android.app.AlertDialog.THEME_HOLO_DARK;


public class AddTransactionFragment extends Fragment {
    EditText edDesc, edAmt, edDate;
    Spinner spinType, spinCatInc, spinCatExp;
    Button btnAdd;
    String desc, amount, type, category, date;
    TextView responseView;
    ProgressBar progressBar;
    static final String INSERT_URL = "http://moneymoney.zapto.org:8080/insertDataAPI";

    public String token;
    String accesstoken;

    //public empty constructor
    public AddTransactionFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);


        //get the token from login
        /**Intent intent = getActivity().getIntent();
        intent.getStringExtra("TOKEN");
        //Log.d("TOKEN");
        token = intent.getStringExtra("TOKEN");
        accesstoken = intent.getStringExtra("ACCESS");
        Log.d("THETOKEN", token);**/
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setting all edittext and spinners
        edDesc = (EditText) view.findViewById(R.id.txtDesc);
        edAmt = (EditText) view.findViewById(R.id.txtAmt);
        //spinner for type
        spinType = (Spinner) view.findViewById(R.id.spinnerType);
        ArrayAdapter<CharSequence> adapterType =
                ArrayAdapter.createFromResource(getActivity().
                        getBaseContext(), R.array.transaction_type, android.R.layout.simple_spinner_dropdown_item);
        spinType.setAdapter(adapterType);
        spinType.setSelection(spinType.getAdapter().getCount() - 1);


        //spinner for Income category
        spinCatInc = (Spinner) view.findViewById(R.id.spinnerCatInc);
        ArrayAdapter<CharSequence> adapterCatInc =
                ArrayAdapter.createFromResource(getActivity().
                        getBaseContext(), R.array.Income_List, android.R.layout.simple_spinner_dropdown_item);
        spinCatInc.setAdapter(adapterCatInc);
        //this will set the last item as default in the spinner
        spinCatInc.setSelection(spinCatInc.getAdapter().getCount() - 1);

        //spinner for expense category
        spinCatExp = (Spinner) view.findViewById(R.id.spinnerCatExp);
        ArrayAdapter<CharSequence> adapterCatExp =
                ArrayAdapter.createFromResource(getActivity().
                        getBaseContext(), R.array.Expense_List, android.R.layout.simple_spinner_dropdown_item);
        spinCatExp.setAdapter(adapterCatExp);
        spinCatExp.setSelection(spinCatExp.getAdapter().getCount() - 1);

        //show category depending on the Type
        spinType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //getting the type of transaction on user selectin
                type = parent.getItemAtPosition(position).toString();
                //Category Spinner will be greyed out untill a type is selected
                if (!type.equalsIgnoreCase("Income") && !type.equalsIgnoreCase("Expense")) {
                    spinCatInc.setEnabled(false);
                    spinCatExp.setVisibility(View.GONE);
                } else if (type.equalsIgnoreCase("Income")) {
                    spinCatInc.setEnabled(true);  //enable when type selected
                    spinCatInc.setVisibility(View.VISIBLE); //make income type visible
                    spinCatExp.setVisibility(View.GONE);  //make expense gone
                } else if (type.equalsIgnoreCase("Expense")) {
                    spinCatExp.setVisibility(View.VISIBLE); //vice versa
                    spinCatInc.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinCatInc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinCatExp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAdd = (Button) view.findViewById(R.id.buttonAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Added Successfully",Toast.LENGTH_LONG).show();
            }
        });


        //for date picker
        edDate = (EditText) view.findViewById(R.id.txtDate);
        //by clicking edit text date picker will pop up
        edDate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    //show date picker method
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showDatePicker() {
        DatePickerFragment date = new DatePickerFragment();
        //set up current dialog into input
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        //set callback to capture the selected date
        date.setCallBack(ondate);
        date.show(getFragmentManager(), "Date Picker");

    }

    //datepicker dialog to fetch the date into a date object and then set it with LONG format to edittext
    //LONG format is January 17, 2016
    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            //this will give January 26, 2016 kind of format
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
            Calendar cal = Calendar.getInstance();
            //always year will be first in calendar or else
            //it will show weird year like 0017
            cal.set(year, monthOfYear, dayOfMonth);
            String dt = sdf.format(cal.getTime());
            //this is to show the date in the editText
            edDate.setText(dt);
        }
    };

    /**class RetrieveFeedTask extends AsyncTask<Void, Void, String> {
        private Exception exception;
        Context context;
        ProgressDialog loading;

        @RequiresApi(api = Build.VERSION_CODES.N)
        protected void onPreExecute() {
            //UI thread
            desc = edDesc.getText().toString();
            amount = edAmt.getText().toString();
            //get the date string from db
            date = edDate.getText().toString();
            //read the format which was set in the date editText edDate
            DateFormat readFormat = new SimpleDateFormat("MMMM dd, yyyy");
            //write it in the db in a different format like Wednesday, July 12, 2016 12:00 PM
            DateFormat writeFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm aaa");
            //initialize a java.util.Date object
            java.util.Date dt = null;
            try{
                dt = readFormat.parse(date);  //parse the date string in the read format
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String formattedDate = "";   //to hold the formatted date
            if(dt != null){
                formattedDate = writeFormat.format(dt); //then write the formatted date in db
            }
            date = formattedDate;  //now date sring is the formatted date

            super.onPreExecute();
            //loading = ProgressDialog.show(getContext(), "Adding...", "Wait...", false, false);
        }

        protected String doInBackground(Void... urls) {
            String result = null;
            String data = null;
            // Do some validation here

            try {
                URL url = new URL(INSERT_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Authorization", "Bearer " + accesstoken); //passing Auth0 idtoken
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("user_Id", token);
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();
                /*urlConnection.setDoOutput(true);
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");

                JSONObject jsonParam = new JSONObject();

                //sending a JSONObject with the token header
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Authorization", "Bearer " + accesstoken);
                jsonObject.put("Content-Type", "application/json");
                jsonObject.put("user_Id", token);
                /**jsonParam.put("type", "income");
                 jsonParam.put("amount", "$88.88");
                 jsonParam.put("desc", "Test Shirin");
                 jsonParam.put("date", "Friday, Octber 13");

                jsonParam.put("type", type);
                jsonParam.put("amount", amount);
                jsonParam.put("desc", desc);
                jsonParam.put("category", category);
                jsonParam.put("date", date);  //this will write the formatteDate in the db
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                out.write(jsonObject.toString());
                out.close();
                int responsecode = urlConnection.getResponseCode();

                Log.d("inbackRESPCode", String.valueOf(responsecode));
                if (responsecode == HttpURLConnection.HTTP_OK) {

                    Log.d("inback", "ok code");
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        Log.d("thisisline", line);
                        sb.append(line);
                    }
                    br.close();
                    Log.d("Sucessfully added", jsonParam.getString(sb.toString()));
                    return sb.toString();
                }

                /*OutputStream os = urlConnection.getOutputStream();
                //BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                //write JSON object to the output stream
                os.write(jsonParam.toString().getBytes());

                Log.d("Sucessfully added", jsonParam.toString());
                //writer.close();
                os.close();

                //Read
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                br.close();
                result = sb.toString();

                Log.d("Success", result);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String s) {
            //s = "Transaction added successfully";
            //super.onPostExecute(s);
           // loading.dismiss();

           // Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Success");
            builder.setMessage("Transaction Added Successfully");
            builder.setPositiveButton("Ok", null);
            builder.setCancelable(true);
            builder.show();
            loading.dismiss();
        }
    }
}**/
//use for set callback and set Arguments
@SuppressLint("ValidFragment")
class DatePickerFragment extends DialogFragment {
        DatePickerDialog.OnDateSetListener ondateSet;
        private int year, month, day;

        public DatePickerFragment() {
        }

        public void setCallBack(DatePickerDialog.OnDateSetListener ondate) {
            ondateSet = ondate;
        }

        @SuppressLint("NewApi")
        @Override
        public void setArguments(Bundle args) {
            super.setArguments(args);
            year = args.getInt("year");
            month = args.getInt("month");
            day = args.getInt("day");
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(getActivity(), ondateSet, year, month, day);
        }
    }
}


