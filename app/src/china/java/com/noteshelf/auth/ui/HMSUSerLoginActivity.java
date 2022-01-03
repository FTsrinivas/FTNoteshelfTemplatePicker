package com.noteshelf.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.store.ui.FTStoreActivity;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.textfield.TextInputLayout;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;
import com.huawei.hms.analytics.HiAnalyticsInstance;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HMSUSerLoginActivity extends FTBaseActivity implements View.OnClickListener {
    private EditText countryCodeEdit;
    private EditText accountEdit;
    private EditText passwordEdit;
    private EditText verifyCodeEdit;
    private ViewGroup countryCodeLayout;
    private LinearLayout galleryLayout;
    private TextInputLayout accountEditLayout;
    private Type type = Type.EMAIL;
    //Define a var for Analytics Instance
    HiAnalyticsInstance instance;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        countryCodeEdit = findViewById(R.id.et_country_code);
        accountEdit = findViewById(R.id.et_account);
        accountEditLayout = findViewById(R.id.et_account_layout);
        passwordEdit = findViewById(R.id.et_password);
        verifyCodeEdit = findViewById(R.id.et_verify_code);
        RadioGroup radioGroup = findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radiobutton_email:
                        type = Type.EMAIL;
                        break;
                    case R.id.radiobutton_phone:
                        type = Type.PHONE;
                        break;
                }
                updateView();
            }
        });

        countryCodeLayout = findViewById(R.id.layout_cc);
        Button loginBtn = findViewById(R.id.btn_login);
        loginBtn.setOnClickListener(this);
        Button registerBtn = findViewById(R.id.btn_register);
        registerBtn.setOnClickListener(this);
        Button send = findViewById(R.id.btn_send);
        send.setOnClickListener(this);
    }

    private void updateView() {
        accountEditLayout.setHint("");
        if (type == Type.EMAIL) {
            accountEditLayout.setHint(R.string.email);
            countryCodeLayout.setVisibility(View.GONE);
        } else {
            accountEditLayout.setHint(R.string.phone);
            countryCodeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.btn_register:
                Intent intent = new Intent(HMSUSerLoginActivity.this, HMSRegisterActivity.class);
                intent.putExtra("registerType", type);
                startActivity(intent);
                break;
            case R.id.btn_send:
                sendVerificationCode();
                break;
            case R.id.btn_login_anonymous:
                loginAnonymous();
                break;
        }
    }

    private void sendVerificationCode() {
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < 1000 * 30) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
                .sendInterval(30) //shortest send interval ，30-120s
                .locale(getResources().getConfiguration().getLocales().get(0)) //optional,must contain country and language eg:zh_CN
                .build();
        if (type == Type.EMAIL) {
            String email = accountEdit.getText().toString().trim();
            Task<VerifyCodeResult> task = EmailAuthProvider.requestVerifyCode(email, settings);
            task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
                @Override
                public void onSuccess(VerifyCodeResult verifyCodeResult) {
                    mLastClickTime = SystemClock.elapsedRealtime();
                    Toast.makeText(HMSUSerLoginActivity.this, getString(R.string.verification_code_success), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(HMSUSerLoginActivity.this, getString(R.string.verification_code_fail), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String countryCode = countryCodeEdit.getText().toString().trim();
            String phoneNumber = accountEdit.getText().toString().trim();
            Task<VerifyCodeResult> task = PhoneAuthProvider.requestVerifyCode(countryCode, phoneNumber, settings);
            task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
                @Override
                public void onSuccess(VerifyCodeResult verifyCodeResult) {
                    mLastClickTime = SystemClock.elapsedRealtime();
                    Toast.makeText(HMSUSerLoginActivity.this, "Verification Code sent successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(HMSUSerLoginActivity.this, "Fail to sent Verification Code", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void login() {
        if (type == Type.EMAIL) {
            String email = accountEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();
            String verifyCode = verifyCodeEdit.getText().toString().trim();
            AGConnectAuthCredential credential;
            if (TextUtils.isEmpty(verifyCode)) {
                credential = EmailAuthProvider.credentialWithPassword(email, password);
            } else {
                //If you do not have a password, param password can be null
                credential = EmailAuthProvider.credentialWithVerifyCode(email, password, verifyCode);
            }
            signIn(credential);
        } else {
            String countryCode = countryCodeEdit.getText().toString().trim();
            String phoneNumber = accountEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();
            String verifyCode = verifyCodeEdit.getText().toString().trim();
            AGConnectAuthCredential credential;
            if (TextUtils.isEmpty(verifyCode)) {
                credential = PhoneAuthProvider.credentialWithPassword(countryCode, phoneNumber, password);
            } else {
                //If you do not have a password, param password can be null
                credential = PhoneAuthProvider.credentialWithVerifyCode(countryCode, phoneNumber, password, verifyCode);
            }
            signIn(credential);
        }
    }

    private void signIn(AGConnectAuthCredential credential) {
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        startActivity(new Intent(HMSUSerLoginActivity.this, FTStoreActivity.class));
                        HMSUSerLoginActivity.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(HMSUSerLoginActivity.this, "signIn fail:" + e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginAnonymous() {
        AGConnectAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        startActivity(new Intent(HMSUSerLoginActivity.this, FTStoreActivity.class));
                        HMSUSerLoginActivity.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(HMSUSerLoginActivity.this, "login Anonymous fail:" + e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    enum Type {
        EMAIL,
        PHONE
    }

    private void reportAnswerEvt(String answer) {
        // Report a customzied Event
        // Event Name: Answer
        // Event Parameters:
        //  -- question: String
        //  -- answer:String
        //  -- answerTime: String

        // Initiate Parameters
        Bundle bundle = new Bundle();
        bundle.putString("button", answer);
        bundle.putString("event", "clicked");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        bundle.putString("answerTime", sdf.format(new Date()));

        // Report a preddefined Event
        instance.onEvent(answer, bundle);
    }
}
