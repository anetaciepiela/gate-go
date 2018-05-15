package edu.rosehulman.ciepieab.gatego;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private boolean mLoggingIn;
    private OnLoginListener mListener;
    private SignInButton mGoogleSignInButton;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoggingIn = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mGoogleSignInButton = (SignInButton) view.findViewById(R.id.google_sign_in_button);
        mGoogleSignInButton.setColorScheme(SignInButton.COLOR_LIGHT);
        mGoogleSignInButton.setSize(SignInButton.SIZE_WIDE);
        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithGoogle();
            }
        });
        return view;
    }

    private void showProgress(boolean show) {
        mGoogleSignInButton.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginListener) {
            mListener = (OnLoginListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void loginWithGoogle() {
        if (mLoggingIn) {
            return;
        }
        showProgress(true);
        mLoggingIn = true;
        mListener.onGoogleLogin();
    }

    public interface OnLoginListener {
        void onGoogleLogin();
    }

}
