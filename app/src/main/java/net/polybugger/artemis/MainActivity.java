package net.polybugger.artemis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if(firebaseUser == null) {
                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(intent);
                }
            }
        };

        FacebookSdk.sdkInitialize(getApplicationContext(), SignInActivity.RC_FACEBOOK);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.sign_out_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.preferences_file), MODE_PRIVATE);
                sharedPrefs.edit().putBoolean(getString(R.string.preference_is_signed_in_key), false).apply();

                SignInActivity.SignInMethod signInMethod = SignInActivity.SignInMethod.ANONYMOUS;
                try {
                    signInMethod = SignInActivity.SignInMethod.valueOf(sharedPrefs.getString(getString(R.string.preference_sign_in_method_key), signInMethod.toString()));
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
                switch(signInMethod) {
                    case FACEBOOK:
                        LoginManager.getInstance().logOut();
                        break;
                }

                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                Bundle args = new Bundle();
                intent.putExtras(args);
                startActivity(intent);
                finish();
            }
        });
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
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
