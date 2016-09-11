/*
 *     Gaso
 *
 *     Copyright (C) 2016  Eric Guimarães
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ericmguimaraes.gaso.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ericmguimaraes.gaso.R;
import com.ericmguimaraes.gaso.activities.registers.UserRegisterActivity;
import com.ericmguimaraes.gaso.config.Constants;
import com.ericmguimaraes.gaso.config.SessionSingleton;
import com.ericmguimaraes.gaso.model.User;
import com.ericmguimaraes.gaso.persistence.UserDAO;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password or google account.
 */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 99;

    // UI references.
    private View mProgressView;
    private View mLoginFormView;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleApiClient mGoogleApiClient;

    @Bind(R.id.sign_in_button)
    SignInButton signInButton;

    @Bind(R.id.gaso_title)
    TextView gasoTitle;

    @Bind(R.id.beta_text)
    TextView betaText;

    private boolean isToAnimate = true;
    private FirebaseAuth mAuth;
    private String TAG = "LOGIN";
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Typeface face;

        face = Typeface.createFromAsset(getAssets(), "ailerons.otf");

        gasoTitle.setTypeface(face);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                showProgress(true);
            }
        });

        setGooglePlusButtonText();

        if(!isBeta())
            betaText.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    User localuser = new User();
                    localuser.setEmail(user.getEmail());
                    localuser.setName(user.getDisplayName());

                    UserDAO dao = new UserDAO(getApplicationContext());
                    dao.add(localuser);

                    login(localuser);

                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

    }

    public boolean isBeta(){
        return getResources().getBoolean(R.bool.isBeta) || getResources().getBoolean(R.bool.isDebug);
    }


    private void animateShowView(View view, int time) {

        time=time==0?750:time;
        int distance = 50;

        // Prepare the View for the animation
        view.setVisibility(View.VISIBLE);
        view.setY(-distance);
        view.setAlpha(0.0f);

        // Start the animation
        view.animate()
                .translationYBy(distance)
                .setDuration(time)
                .alpha(1.0f);
    }

    private void animate(){
        if(isBeta())
            animateShowView(betaText,300);
        animateShowView(gasoTitle,500);
        animateShowView(signInButton,700);
    }

    private void hide(){
        if(isBeta())
            betaText.setVisibility(View.INVISIBLE);
        gasoTitle.setVisibility(View.INVISIBLE);
        signInButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        User u = getUserLogged();
        if(u!=null)
            login(u);
        if(isToAnimate) {
            hide();
            animate();
            isToAnimate = false;
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showConnectionFailSnackBar();
    }

    private void showConnectionFailSnackBar() {
        Snackbar.make(mLoginFormView,"Não foi possível conectar, por favor tente novamente mais tarde.",Snackbar.LENGTH_LONG).show();
    }

    private User copyUser(@Nullable User user) {
        if(user==null)
            return null;
        User userCopy = new User();
        userCopy.setPassword(user.getPassword());
        userCopy.setEmail(user.getEmail());
        userCopy.setName(user.getName());
        userCopy.setId(user.getId());
        return userCopy;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(LoginActivity.class.getName(), "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            if(account!=null){

                firebaseAuthWithGoogle(account);

            } else if(getResources().getBoolean(R.bool.isDebug)){
                User user = createDebugUser();
                UserDAO dao = new UserDAO(getApplicationContext());
                dao.add(user);
                saveUserLogged(user.getEmail());
                login(user);
            } else
                showConnectionFailSnackBar();
        } else {
            if(getResources().getBoolean(R.bool.isDebug)){
                login(createDebugUser());
            } else
                showConnectionFailSnackBar();
        }
        showProgress(false);
    }

    private User createDebugUser() {
        User u = new User();
        u.setEmail("degub@gaso.com");
        u.setName("debug");
        return u;
    }

    private void login(User user) {
        SessionSingleton.getInstance().setCurrentUser(user);

        saveUserLogged(user.getEmail());

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void saveUserLogged(String email) {
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.USER_LOGGED_TAG, email);
        editor.apply();
    }

    private User getUserLogged(){
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        String email = settings.getString(Constants.USER_LOGGED_TAG,"");
        UserDAO dao = new UserDAO(getApplicationContext());
        User u = dao.findbyEmail(email);
        u = copyUser(u);
        return u;
    }

    protected void setGooglePlusButtonText() {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText("Entrar com sua conta Google");
                return;
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
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            showAuthFailSnackBar();
                        }
                        // ...
                    }
                });
    }

    private void showAuthFailSnackBar() {
        Snackbar.make(mLoginFormView,"Falha na autenticação.",Snackbar.LENGTH_LONG).show();
    }


}

