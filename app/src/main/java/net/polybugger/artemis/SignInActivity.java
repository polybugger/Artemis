package net.polybugger.artemis;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SignInActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener {

    private static final int MSG_HIDE_SYSTEM_BAR = 23;

    private Handler mHandler;

    public enum SignInMethod {
        EMAIL_PASSWORD,
        GOOGLE,
        FACEBOOK,
        ANONYMOUS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_sign_in);

        immersiveMode();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);

        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.preferences_file), MODE_PRIVATE);
        boolean isSignedIn = sharedPrefs.getBoolean(getString(R.string.preference_is_signed_in_key), getResources().getBoolean(R.bool.preference_default_is_signed_in));

        if(isSignedIn) {
            String defaultSignInMethodString = getString(R.string.preference_default_sign_in_method);
            SignInMethod defaultSignInMethod = SignInMethod.ANONYMOUS;
            try {
                defaultSignInMethod = SignInMethod.valueOf(defaultSignInMethodString);
            }
            catch(IllegalArgumentException iae) {
                if(BuildConfig.DEBUG)
                    Log.e(MainActivity.class.toString(), "Invalid default sign in method!");
            }
            catch(NullPointerException npe) {
                if(BuildConfig.DEBUG)
                    Log.e(MainActivity.class.toString(), "Missing default sign in method!");
            }

            SignInMethod signInMethod = defaultSignInMethod;
            try {
                signInMethod = SignInMethod.valueOf(sharedPrefs.getString(getString(R.string.preference_sign_in_method_key), defaultSignInMethodString));
            }
            catch(IllegalArgumentException iae) {
                if(BuildConfig.DEBUG)
                    Log.e(MainActivity.class.toString(), "Invalid sign in method!");
            }
            catch(NullPointerException npe) {
                if(BuildConfig.DEBUG)
                    Log.e(MainActivity.class.toString(), "Missing sign in method!");
            }

            String email = sharedPrefs.getString(getString(R.string.preference_sign_in_email_key), null);
            String password = sharedPrefs.getString(getString(R.string.preference_sign_in_password_key), null);

            // load fragment signInLoaderFragment
        }
        else {
            // load fragment signInMethodFragment

            FragmentManager fm = getSupportFragmentManager();
            SignInMethodFragment signInMethodFragment = (SignInMethodFragment) fm.findFragmentByTag(SignInMethodFragment.TAG);
            if(signInMethodFragment == null) {
                signInMethodFragment = SignInMethodFragment.newInstance();
                fm.beginTransaction().replace(android.R.id.content, signInMethodFragment).commit();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //immersiveMode();
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        immersiveMode();
    }

    private void immersiveMode() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();
    }

}
