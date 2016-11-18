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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity implements SignInMethodFragment.SignInListener {

    public static final int RC_GOOGLE_SIGN_IN = 1;
    public static final int RC_FACEBOOK = 7000;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mCallbackManager;
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

        FacebookSdk.sdkInitialize(getApplicationContext(), SignInActivity.RC_FACEBOOK);
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookSign(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() { }
            @Override
            public void onError(FacebookException error) { }
        });

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

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, null /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        switch(resultCode) {
            case RC_GOOGLE_SIGN_IN:
                googleSignIn(data);
                break;
            default: // facebook
                if(FacebookSdk.isFacebookRequestCode(requestCode)) {
                    if(requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
                        mCallbackManager.onActivityResult(requestCode, resultCode, data);
                    }
                }
        }
    }

    @Override
    public void signIn(String email, String password, SignInMethod signInMethod) {
        switch(signInMethod) {
            case EMAIL_PASSWORD:
                emailPasswordSignIn(email, password);
                break;
            case ANONYMOUS:
                anonymousSignIn();
                break;
            case GOOGLE:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
                break;
            case FACEBOOK:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
                break;
            default:
                anonymousSignIn();
        }
    }

    private void emailPasswordSignIn(final String email, final String password) {
        FragmentManager fm = getSupportFragmentManager();
        mLoaderDialogFragment = (SignInLoaderDialogFragment) fm.findFragmentByTag(SignInLoaderDialogFragment.TAG);
        if(mLoaderDialogFragment == null) {
            mLoaderDialogFragment = SignInLoaderDialogFragment.newInstance();
            mLoaderDialogFragment.show(fm, SignInLoaderDialogFragment.TAG);
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mLoaderDialogFragment.dismiss();
                if(task.isSuccessful()) {
                    SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.preferences_file), MODE_PRIVATE);
                    sharedPrefs.edit().putBoolean(getString(R.string.preference_is_signed_in_key), true)
                            .putString(getString(R.string.preference_sign_in_email_key), email)
                            .putString(getString(R.string.preference_sign_in_password_key), password)
                            .putString(getString(R.string.preference_sign_in_method_key), SignInMethod.EMAIL_PASSWORD.toString())
                            .apply();
                    startMainActivity();
                }
                else {
                    snackbarSignInFailed(true);
                }
            }
        });
    }

    private void anonymousSignIn() {
        FragmentManager fm = getSupportFragmentManager();
        mLoaderDialogFragment = (SignInLoaderDialogFragment) fm.findFragmentByTag(SignInLoaderDialogFragment.TAG);
        if(mLoaderDialogFragment == null) {
            mLoaderDialogFragment = SignInLoaderDialogFragment.newInstance();
            mLoaderDialogFragment.show(fm, SignInLoaderDialogFragment.TAG);
        }
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mLoaderDialogFragment.dismiss();
                if(task.isSuccessful()) {
                    SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.preferences_file), MODE_PRIVATE);
                    sharedPrefs.edit().putBoolean(getString(R.string.preference_is_signed_in_key), true)
                            .putString(getString(R.string.preference_sign_in_method_key), SignInMethod.ANONYMOUS.toString())
                            .apply();
                    startMainActivity();
                }
                else {
                    snackbarSignInFailed(false);
                }
            }
        });
    }

    private void googleSignIn(Intent data) {
        FragmentManager fm = getSupportFragmentManager();
        mLoaderDialogFragment = (SignInLoaderDialogFragment) fm.findFragmentByTag(SignInLoaderDialogFragment.TAG);
        if(mLoaderDialogFragment == null) {
            mLoaderDialogFragment = SignInLoaderDialogFragment.newInstance();
            mLoaderDialogFragment.show(fm, SignInLoaderDialogFragment.TAG);
        }
        boolean signInFailed = true;
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if(result.isSuccess()) {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = result.getSignInAccount();
            if(account != null) {
                signInFailed = false;
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mLoaderDialogFragment.dismiss();
                        if(task.isSuccessful()) {
                            SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.preferences_file), MODE_PRIVATE);
                            sharedPrefs.edit().putBoolean(getString(R.string.preference_is_signed_in_key), true)
                                    .putString(getString(R.string.preference_sign_in_method_key), SignInMethod.GOOGLE.toString())
                                    .apply();
                            startMainActivity();
                        }
                        else {
                            snackbarSignInFailed(false);
                        }
                    }
                });
            }
        }
        if(signInFailed) {
            mLoaderDialogFragment.dismiss();
            snackbarSignInFailed(false);
        }
    }

    private void facebookSign(AccessToken accessToken) {
        FragmentManager fm = getSupportFragmentManager();
        mLoaderDialogFragment = (SignInLoaderDialogFragment) fm.findFragmentByTag(SignInLoaderDialogFragment.TAG);
        if(mLoaderDialogFragment == null) {
            mLoaderDialogFragment = SignInLoaderDialogFragment.newInstance();
            mLoaderDialogFragment.show(fm, SignInLoaderDialogFragment.TAG);
        }
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mLoaderDialogFragment.dismiss();
                if(task.isSuccessful()) {
                    SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.preferences_file), MODE_PRIVATE);
                    sharedPrefs.edit().putBoolean(getString(R.string.preference_is_signed_in_key), true)
                            .putString(getString(R.string.preference_sign_in_method_key), SignInMethod.FACEBOOK.toString())
                            .apply();
                    startMainActivity();
                }
                else {
                    snackbarSignInFailed(false);
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

    private void snackbarSignInFailed(boolean focusEmailTextEdit) {
        Snackbar.make(findViewById(R.id.coordinator_layout), R.string.error_sign_in_failed, Snackbar.LENGTH_SHORT).show();
        FragmentManager fm = getSupportFragmentManager();
        SignInMethodFragment signInMethodFragment = (SignInMethodFragment) fm.findFragmentByTag(SignInMethodFragment.TAG);
        if(signInMethodFragment == null) {
            signInMethodFragment = SignInMethodFragment.newInstance();
            fm.beginTransaction().replace(R.id.fragment_container, signInMethodFragment, SignInMethodFragment.TAG).commit();
        }
        else if(focusEmailTextEdit) {
            signInMethodFragment.focusEmailPassword();
        }
    }
}
