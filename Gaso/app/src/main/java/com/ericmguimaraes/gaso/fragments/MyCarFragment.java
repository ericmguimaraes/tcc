package com.ericmguimaraes.gaso.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ericmguimaraes.gaso.Config;
import com.ericmguimaraes.gaso.R;
import com.ericmguimaraes.gaso.model.Car;
import com.ericmguimaraes.gaso.model.User;
import com.ericmguimaraes.gaso.persistence.UserDAO;
import com.ericmguimaraes.gaso.registers.RegisterActivity;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyCarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyCarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyCarFragment extends Fragment {


    @Bind(R.id.fab)
    FloatingActionButton fab;

    @Bind(R.id.no_user_text)
    TextView noUserText;

    @Bind(R.id.name)
    TextView nameText;

    @Bind(R.id.model)
    TextView modelText;

    @Bind(R.id.description)
    TextView descriptionText;

    User user;
    Car car;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MyCarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyCarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyCarFragment newInstance(String param1, String param2) {
        MyCarFragment fragment = new MyCarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_car, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fab.setColorFilter(Color.WHITE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateCarAndUser() {
        Config config = Config.getInstance();
        user = config.currentUser;
        car = config.currentCar;
        if(user == null || car == null){
            nameText.setVisibility(View.GONE);
            modelText.setVisibility(View.GONE);
            descriptionText.setVisibility(View.GONE);
            noUserText.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
        } else {
            nameText.setVisibility(View.VISIBLE);
            modelText.setVisibility(View.VISIBLE);
            descriptionText.setVisibility(View.VISIBLE);
            noUserText.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);

            nameText.setText(user.getName());
            modelText.setText(car.getModel());
            descriptionText.setText(car.getDescription());
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCarAndUser();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
