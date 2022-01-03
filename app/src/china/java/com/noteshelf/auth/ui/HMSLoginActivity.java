package com.noteshelf.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf2.R;
import com.huawei.agconnect.auth.AGConnectAuth;

public class HMSLoginActivity extends FTBaseActivity {
    private static final int SIGN_CODE = 9901;
    protected AGConnectAuth auth;
//    private GoogleSignInClient client;
//    private CallbackManager callbackManager = CallbackManager.Factory.create();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hmslogin);
        auth = AGConnectAuth.getInstance();
//        GoogleSignInOptions options =
//                new GoogleSignInOptions
//                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestScopes(new Scope("email"))
//                        .requestIdToken("47590175345-k6eb0hfunrr41qq5sj4h704s3rhf26sr.apps.googleusercontent.com")
//                        .build();
//        client = GoogleSignIn.getClient(this, options);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_email_login: {
                startActivity(new Intent(HMSLoginActivity.this, HMSUSerLoginActivity.class));
            }
            break;
            case R.id.btn_google_login: {
//                startActivityForResult(client.getSignInIntent(), SIGN_CODE);
            }
            break;
            case R.id.btn_facebook_login: {
//                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
//                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//                    @Override
//                    public void onSuccess(LoginResult loginResult) {
//                        String token = loginResult.getAccessToken().getToken();
//                        AGConnectAuthCredential credential = FacebookAuthProvider.credentialWithToken(token);
//                        auth.signIn(credential)
//                                .addOnSuccessListener(signInResult -> {
//                                    startActivity(new Intent(HMSLoginActivity.this, FTStoreActivity.class));
//                                    finish();
//                                })
//                                .addOnFailureListener(e -> showToast(e.getMessage()));
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        showToast("Cancel");
//                    }
//
//                    @Override
//                    public void onError(FacebookException error) {
//                        showToast(error.getMessage());
//                    }
//                });
            }
            break;
        }
    }

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_CODE) {
//            GoogleSignIn.getSignedInAccountFromIntent(data)
//                    .addOnSuccessListener(googleSignInAccount -> {
//                        AGConnectAuthCredential credential =
//                                GoogleAuthProvider.credentialWithToken(googleSignInAccount.getIdToken());
//                        auth.signIn(credential)
//                                .addOnSuccessListener(signInResult -> {
//                                            startActivity(new Intent(this, FTStoreActivity.class));
//                                            finish();
//                                        }
//                                )
//                                .addOnFailureListener(e -> {
//                                    showToast(e.getMessage());
//                                });
//                    })
//                    .addOnFailureListener(e -> showToast(e.getMessage()));
        } else {
//            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
}
