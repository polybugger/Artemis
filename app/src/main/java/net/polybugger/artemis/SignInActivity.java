package net.polybugger.artemis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity implements SignInMethodFragment.SignInListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private SignInLoaderDialogFragment mLoaderDialogFragment;

    public enum SignInMethod {
        EMAIL_PASSWORD,
        GOOGLE,
        FACEBOOK,
        ANONYMOUS
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.sign_in);
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if(firebaseUser != null) { }
                else { }
            }
        };

        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.preferences_file), MODE_PRIVATE);
        boolean isSignedIn = sharedPrefs.getBoolean(getString(R.string.preference_is_signed_in_key), getResources().getBoolean(R.bool.preference_default_is_signed_in));

        if(isSignedIn) {
            String defaultSignInMethodString = getString(R.string.preference_default_sign_in_method);
            SignInMethod defaultSignInMethod = SignInMethod.ANONYMOUS;
            try {
                defaultSignInMethod = SignInMethod.valueOf(defaultSignInMethodString);
            }
            catch(IllegalArgumentException iae) {
                if(BuildConfig.DEBUG) {
                    Log.e(MainActivity.class.toString(), "Invalid default sign in method!");
                }
            }
            catch(NullPointerException npe) {
                if (BuildConfig.DEBUG) {
                    Log.e(MainActivity.class.toString(), "Missing default sign in method!");
                }
            }
            SignInMethod signInMethod = defaultSignInMethod;
            try {
                signInMethod = SignInMethod.valueOf(sharedPrefs.getString(getString(R.string.preference_sign_in_method_key), defaultSignInMethodString));
            }
            catch(IllegalArgumentException iae) {
                if (BuildConfig.DEBUG) {
                    Log.e(MainActivity.class.toString(), "Invalid sign in method!");
                }
            }
            catch(NullPointerException npe) {
                if (BuildConfig.DEBUG) {
                    Log.e(MainActivity.class.toString(), "Missing sign in method!");
                }
            }

            String email = sharedPrefs.getString(getString(R.string.preference_sign_in_email_key), null);
            String password = sharedPrefs.getString(getString(R.string.preference_sign_in_password_key), null);

            //isSignedIn = true;
            //email = "polybugger@gmail.com";
            //password = "masha723";
            //signInMethod = SignInMethod.EMAIL_PASSWORD;

            signIn(email, password, signInMethod);

        }
        else {
            FragmentManager fm = getSupportFragmentManager();
            SignInMethodFragment signInMethodFragment = (SignInMethodFragment) fm.findFragmentByTag(SignInMethodFragment.TAG);
            if(signInMethodFragment == null) {
                signInMethodFragment = SignInMethodFragment.newInstance();
                fm.beginTransaction().replace(R.id.fragment_container, signInMethodFragment, SignInMethodFragment.TAG).commit();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void signIn(String email, String password, SignInMethod signInMethod) {
        FragmentManager fm = getSupportFragmentManager();
        mLoaderDialogFragment = (SignInLoaderDialogFragment) fm.findFragmentByTag(SignInLoaderDialogFragment.TAG);
        if(mLoaderDialogFragment == null) {
            mLoaderDialogFragment = SignInLoaderDialogFragment.newInstance();
            mLoaderDialogFragment.show(fm, SignInLoaderDialogFragment.TAG);
        }
        switch(signInMethod) {
            case EMAIL_PASSWORD:
                emailPasswordSignIn(email, password, signInMethod);
                break;
            case ANONYMOUS:
                anonymousSignIn(signInMethod);
                break;
            default:
                anonymousSignIn(signInMethod);
        }
    }

    private void emailPasswordSignIn(final String email, final String password, final SignInMethod signInMethod) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mLoaderDialogFragment.dismiss();
                if(task.isSuccessful()) {
                    SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.preferences_file), MODE_PRIVATE);
                    sharedPrefs.edit().putBoolean(getString(R.string.preference_is_signed_in_key), true)
                            .putString(getString(R.string.preference_sign_in_email_key), email)
                            .putString(getString(R.string.preference_sign_in_password_key), password)
                            .putString(getString(R.string.preference_sign_in_method_key), signInMethod.toString())
                            .apply();
                    startMainActivity();
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinator_layout), R.string.error_sign_in_failed, Snackbar.LENGTH_SHORT).show();
                    FragmentManager fm = getSupportFragmentManager();
                    SignInMethodFragment signInMethodFragment = (SignInMethodFragment) fm.findFragmentByTag(SignInMethodFragment.TAG);
                    if(signInMethodFragment == null) {
                        signInMethodFragment = SignInMethodFragment.newInstance();
                        fm.beginTransaction().replace(R.id.fragment_container, signInMethodFragment, SignInMethodFragment.TAG).commit();
                    }
                    else {
                        signInMethodFragment.focusEmailPassword();
                    }
                }
            }
        });
    }

    private void anonymousSignIn(final SignInMethod signInMethod) {
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mLoaderDialogFragment.dismiss();
                if(task.isSuccessful()) {
                    SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.preferences_file), MODE_PRIVATE);
                    sharedPrefs.edit().putBoolean(getString(R.string.preference_is_signed_in_key), true)
                            .putString(getString(R.string.preference_sign_in_method_key), signInMethod.toString())
                            .apply();
                    startMainActivity();
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinator_layout), R.string.error_sign_in_failed, Snackbar.LENGTH_SHORT).show();
                    FragmentManager fm = getSupportFragmentManager();
                    SignInMethodFragment signInMethodFragment = (SignInMethodFragment) fm.findFragmentByTag(SignInMethodFragment.TAG);
                    if(signInMethodFragment == null) {
                        signInMethodFragment = SignInMethodFragment.newInstance();
                        fm.beginTransaction().replace(R.id.fragment_container, signInMethodFragment, SignInMethodFragment.TAG).commit();
                    }
                }
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        Bundle args = new Bundle();
        intent.putExtras(args);
        startActivity(intent);
        finish();
    }
}
