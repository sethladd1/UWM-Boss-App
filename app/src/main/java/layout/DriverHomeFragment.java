package layout;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uwmbossapp.uwmboss.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DriverHomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DriverHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverHomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String DRIVER_LOC_ARG = "param1";
    private static final String PSNGR_PICK_UP_ARG = "param3";
    private static final String PSNGR_DROP_OFF_ARG = "param2";
    // TODO: Rename and change types of parameters
    private float mParam1;
    private float mParam2;
    private float mParam3;

    private OnFragmentInteractionListener mListener;

    public DriverHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 driver geolocations.
     * @param param2 psngr pickup geolocation.
     * @param param3 psngr dropoff geolocation.
     * @return A new instance of fragment DriverHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DriverHomeFragment newInstance(float param1, float param2, float param3) {
        DriverHomeFragment fragment = new DriverHomeFragment();
        Bundle args = new Bundle();
        args.putFloat(DRIVER_LOC_ARG, param1);
        args.putFloat(PSNGR_PICK_UP_ARG, param2);
        args.putFloat(PSNGR_DROP_OFF_ARG, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getFloat(DRIVER_LOC_ARG);
            mParam2 = getArguments().getFloat(PSNGR_PICK_UP_ARG);
            mParam3 = getArguments().getFloat(PSNGR_DROP_OFF_ARG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_home, container, false);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
