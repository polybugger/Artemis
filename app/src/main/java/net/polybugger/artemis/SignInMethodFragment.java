package net.polybugger.artemis;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;

public class SignInMethodFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "net.polybugger.artemis.sign_in_method_fragment";

    public interface SignInListener {
        void signIn(String email, String password, SignInActivity.SignInMethod method);

    }

    private SignInListener mListener;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private TextView mEmailPasswordErrorTextView;

    public SignInMethodFragment() { }

    public static SignInMethodFragment newInstance() {
        SignInMethodFragment f = new SignInMethodFragment();
        //Bundle args = new Bundle();
        //args.putSerializable(PAST_CURRENT_ARG, pastCurrentEnum);
        //f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Activity) {
            try {
                mListener = (SignInListener) context;
            }
            catch(ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement " + SignInListener.class.toString());
            }
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /*
        Bundle args = getArguments();
        mPastCurrent = (PastCurrentEnum) args.getSerializable(PAST_CURRENT_ARG);
        switch(mPastCurrent) {
            case PAST:
                getActivity().setTitle(R.string.past_classes);
                break;
            case CURRENT:
                getActivity().setTitle(R.string.current_classes);
                break;
        }
        */

        View view = inflater.inflate(R.layout.fragment_sign_in_method, container, false);

        mEmailEditText = (EditText) view.findViewById(R.id.email_edit_text);
        mPasswordEditText = (EditText) view.findViewById(R.id.password_edit_text);
        mEmailPasswordErrorTextView = (TextView) view.findViewById(R.id.email_password_error_text_view);

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                mEmailPasswordErrorTextView.setText(null);
            }
        };
        mEmailEditText.addTextChangedListener(tw);
        mPasswordEditText.addTextChangedListener(tw);
        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND) {
                    emailPasswordSignIn();
                    return true;
                }
                return false;
            }
        });

        view.findViewById(R.id.email_password_clear_button).setOnClickListener(this);
        view.findViewById(R.id.email_password_sign_in_button).setOnClickListener(this);
        mEmailEditText.requestFocus();

        SignInButton signInButton = (SignInButton) view.findViewById(R.id.google_sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);

        LoginButton facebookLoginButton = (LoginButton) view.findViewById(R.id.facebook_sign_in_button);
        //facebookLoginButton.setOnClickListener(this);
        facebookLoginButton.setReadPermissions("email", "public_profile");
        //facebookLoginButton.setFragment(this);

        view.findViewById(R.id.anonymous_sign_in_button).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.email_password_clear_button:
                mEmailEditText.setText(null);
                mPasswordEditText.setText(null);
                mEmailEditText.requestFocus();
                break;
            case R.id.email_password_sign_in_button:
                emailPasswordSignIn();
                break;
            case R.id.anonymous_sign_in_button:
                mListener.signIn(null, null, SignInActivity.SignInMethod.ANONYMOUS);
                break;
            case R.id.google_sign_in_button:
                mListener.signIn(null, null, SignInActivity.SignInMethod.GOOGLE);
                break;
            case R.id.facebook_sign_in_button:
                mListener.signIn(null, null, SignInActivity.SignInMethod.FACEBOOK);
                break;
        }
    }

    public void focusEmailPassword() {
        mEmailEditText.requestFocus();
    }

    private void emailPasswordSignIn() {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        if(TextUtils.isEmpty(email)) {
            if(BuildConfig.DEBUG) {
                Log.e(SignInMethodFragment.class.toString(), "Missing email!");
            }
            mEmailPasswordErrorTextView.setText(R.string.error_email_required);
            mEmailEditText.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(password)) {
            if(BuildConfig.DEBUG) {
                Log.e(SignInMethodFragment.class.toString(), "Missing password!");
            }
            mEmailPasswordErrorTextView.setText(R.string.error_password_required);
            mPasswordEditText.requestFocus();
            return;
        }
        mListener.signIn(email, password, SignInActivity.SignInMethod.EMAIL_PASSWORD);
    }

}
