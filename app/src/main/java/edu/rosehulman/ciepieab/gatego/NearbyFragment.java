package edu.rosehulman.ciepieab.gatego;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class NearbyFragment extends Fragment {
    private OnSwipeListener mListener;
    private GestureDetectorCompat mGestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetectorCompat(getContext(), new MyGestureDetector());
    }

    public NearbyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSwipeListener) {
            mListener = (OnSwipeListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + "must implement OnSwipeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
        return view;
    }

    public interface OnSwipeListener {
        void onSwipe();
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mListener.onSwipe();
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

}
