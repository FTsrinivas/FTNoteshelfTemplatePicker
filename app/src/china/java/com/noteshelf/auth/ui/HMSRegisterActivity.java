package com.noteshelf.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.store.ui.FTStoreActivity;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.textfield.TextInputLayout;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.EmailUser;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.PhoneUser;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;

public class HMSRegisterActivity extends FTBaseActivity implements View.OnClickListener {
    private EditText countryCodeEdit;
    private EditText accountEdit;
    private EditText passwordEdit;
    private EditText verifyCodeEdit;
    private TextView applicableToTextView;
    private TextInputLayout accountEditLayout;

    private HMSUSerLoginActivity.Type type;
    private long mLastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        type = (HMSUSerLoginActivity.Type) getIntent().getSerializableExtra("registerType");
        initView();

        if (FTApp.isForAppGallery() && FTApp.getInstance().isChinaRegion()) {
            applicableToTextView.setVisibility(View.VISIBLE);
        }
    }

    private void initView() {
        applicableToTextView = findViewById(R.id.register_applicable_to_text_view);
        countryCodeEdit = findViewById(R.id.et_country_code);
        accountEdit = findViewById(R.id.et_account);
        accountEditLayout = findViewById(R.id.et_account_layout);
        passwordEdit = findViewById(R.id.et_password);
        verifyCodeEdit = findViewById(R.id.et_verify_code);
        ViewGroup countryCodeLayout = findViewById(R.id.layout_cc);
        accountEditLayout.setHint("");
        if (type == HMSUSerLoginActivity.Type.EMAIL) {
            if (FTApp.isForAppGallery() && FTApp.getInstance().isChinaRegion()) {
                accountEditLayout.setHint("abc@huawei.com");
            } else {
                accountEditLayout.setHint(R.string.email);
            }
            countryCodeLayout.setVisibility(View.INVISIBLE);
        } else {
            accountEditLayout.setHint(R.string.phone);
            countryCodeLayout.setVisibility(View.VISIBLE);
        }

        Button registerBtn = findViewById(R.id.btn_register);
        registerBtn.setOnClickListener(this);

        Button send = findViewById(R.id.btn_send);
        send.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                login();
                break;
            case R.id.btn_send:
                sendVerificationCode();
                break;
        }
    }

    private void login() {
        if (type == HMSUSerLoginActivity.Type.EMAIL) {
            String email = accountEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();
            String verifyCode = verifyCodeEdit.getText().toString().trim();

            EmailUser emailUser = new EmailUser.Builder()
                    .setEmail(email)
                    .setPassword(password)//optional
                    .setVerifyCode(verifyCode)
                    .build();
            AGConnectAuth.getInstance().createUser(emailUser)
                    .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                        @Override
                        public void onSuccess(SignInResult signInResult) {
                            startActivity(new Intent(HMSRegisterActivity.this, FTStoreActivity.class));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(HMSRegisterActivity.this, "createUser fail:" + e, Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            String countryCode = countryCodeEdit.getText().toString().trim();
            String phoneNumber = accountEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();
            String verifyCode = verifyCodeEdit.getText().toString().trim();
            PhoneUser phoneUser = new PhoneUser.Builder()
                    .setCountryCode(countryCode)
                    .setPhoneNumber(phoneNumber)
                    .setPassword(password)//optional
                    .setVerifyCode(verifyCode)
                    .build();
            AGConnectAuth.getInstance().createUser(phoneUser)
                    .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                        @Override
                        public void onSuccess(SignInResult signInResult) {
                            startActivity(new Intent(HMSRegisterActivity.this, FTStoreActivity.class));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(HMSRegisterActivity.this, "createUser fail:" + e, Toast.LENGTH_SHORT).show();
                        }
                    });
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
        if (type == HMSUSerLoginActivity.Type.EMAIL) {
            String email = accountEdit.getText().toString().trim();
            Task<VerifyCodeResult> task = EmailAuthProvider.requestVerifyCode(email, settings);
            task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
                @Override
                public void onSuccess(VerifyCodeResult verifyCodeResult) {
                    mLastClickTime = SystemClock.elapsedRealtime();
                    Toast.makeText(HMSRegisterActivity.this, "Verification Code sent successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(HMSRegisterActivity.this, "Fail to sent Verification Code", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(HMSRegisterActivity.this, getString(R.string.verification_code_success), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(HMSRegisterActivity.this, getString(R.string.verification_code_fail), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
