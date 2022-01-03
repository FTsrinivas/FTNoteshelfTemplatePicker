package com.fluidtouch.noteshelf.welcome.screens;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTDialogFactory;
import com.fluidtouch.noteshelf.commons.ui.FTPrivacyPolicyDialog;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.commons.utils.FTNetworkConnectionUtil;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity;
import com.fluidtouch.noteshelf.shelf.activities.FTShelfGroupableFragment;
import com.fluidtouch.noteshelf.welcome.FTChooseRestoreCloudDialog;
import com.fluidtouch.noteshelf.welcome.FTWelcomeScreenActivity;
import com.fluidtouch.noteshelf2.R;
import com.noteshelf.cloud.FTCloudServices;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTWelcomeScreenFifthFragment extends Fragment implements FTChooseRestoreCloudDialog.RestoreDialogListener, FTPrivacyPolicyDialog.Listener {
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.welcome_screen5_image1)
    ImageView image1View;
    @BindView(R.id.welcome_screen5_image2_left)
    ImageView image2LeftView;
    @BindView(R.id.welcome_screen5_image2_right)
    ImageView image2RightView;
    @BindView(R.id.welcome_screen5_image3_left)
    ImageView image3LeftView;
    @BindView(R.id.welcome_screen5_image3_right)
    ImageView image3RightView;
    @BindView(R.id.welcome_screen5_button_layout)
    LinearLayout buttonLayout;
    @BindView(R.id.welcome_screen5_title)
    TextView titleTextView;
    @BindView(R.id.welcome_screen5_start_button)
    TextView startButton;
    @BindView(R.id.welcome_screen5_restore_backup_button)
    TextView restoreBackupButton;
    @BindView(R.id.welcome_screen5_gdrive)
    ImageView gdriveImageView;
    @BindView(R.id.welcome_screen5_dropbox)
    ImageView dropboxImageView;
    @BindView(R.id.welcome_screen5_one_drive)
    ImageView oneDriveImageView;
    @BindView(R.id.agreeCheckBox)
    ImageView agreeToCheckBox;
    @BindView(R.id.agreeCheckLayout)
    RelativeLayout agreeToCheckLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        return inflater.inflate(R.layout.welcome_screen5, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        FTLog.crashlyticsLog(FTWelcomeScreenFifthFragment.class.getSimpleName());
        if (FTApp.isForHuawei()) {
            gdriveImageView.setVisibility(View.GONE);
            dropboxImageView.setVisibility(View.GONE);
            oneDriveImageView.setVisibility(View.GONE);
            restoreBackupButton.setVisibility(View.GONE);
            titleTextView.setVisibility(View.GONE);
        }

        agreeToCheckLayout.setBackgroundResource(R.drawable.check_unselected_bg);
        startButton.setAlpha(0.5f);
        restoreBackupButton.setAlpha(0.5f);
        agreeToCheckBox.setVisibility(View.GONE);
        if (!FTCloudServices.INSTANCE.isRestoreEnabled()) {
            gdriveImageView.setVisibility(View.GONE);
            dropboxImageView.setVisibility(View.GONE);
            oneDriveImageView.setVisibility(View.GONE);
            restoreBackupButton.setVisibility(View.GONE);
            titleTextView.setVisibility(View.GONE);
        }

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) startButton.getLayoutParams();
        if (getResources().getConfiguration().smallestScreenWidthDp < 560) {
            buttonLayout.setOrientation(LinearLayout.VERTICAL);
        } else {
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            layoutParams.topMargin = 0;
        }
        startButton.setLayoutParams(layoutParams);

        gdriveImageView.setAlpha(0.0f);
        dropboxImageView.setAlpha(0.0f);
        oneDriveImageView.setAlpha(0.0f);
        scrollView.post(() -> {
            scrollView.fullScroll(View.FOCUS_DOWN);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize <= Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            FTAnimationUtils.valueAnimatorRight(image2LeftView, 0, getResources().getDimensionPixelOffset(R.dimen._128dp));
            FTAnimationUtils.valueAnimatorLeft(image2RightView, 0, getResources().getDimensionPixelOffset(R.dimen._128dp));
            FTAnimationUtils.valueAnimatorRight(image3LeftView, 0, getResources().getDimensionPixelOffset(R.dimen._256dp));
            FTAnimationUtils.valueAnimatorLeft(image3RightView, 0, getResources().getDimensionPixelOffset(R.dimen._256dp));
        } else {
            FTAnimationUtils.valueAnimatorRight(image2LeftView, 0, getResources().getDimensionPixelOffset(R.dimen.new_128dp));
            FTAnimationUtils.valueAnimatorLeft(image2RightView, 0, getResources().getDimensionPixelOffset(R.dimen.new_128dp));
            FTAnimationUtils.valueAnimatorRight(image3LeftView, 0, getResources().getDimensionPixelOffset(R.dimen.new_256dp));
            FTAnimationUtils.valueAnimatorLeft(image3RightView, 0, getResources().getDimensionPixelOffset(R.dimen.new_256dp));
        }
        gdriveImageView.animate().alpha(1.0f).setDuration(500).start();
        dropboxImageView.animate().alpha(1.0f).setDuration(500).start();
        oneDriveImageView.animate().alpha(1.0f).setDuration(500).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        RelativeLayout.LayoutParams image2LeftViewLayoutParams = (RelativeLayout.LayoutParams) image2LeftView.getLayoutParams();
        image2LeftViewLayoutParams.rightMargin = 0;
        image2LeftView.setLayoutParams(image2LeftViewLayoutParams);

        RelativeLayout.LayoutParams image2RightViewLayoutParams = (RelativeLayout.LayoutParams) image2RightView.getLayoutParams();
        image2RightViewLayoutParams.leftMargin = 0;
        image2RightView.setLayoutParams(image2RightViewLayoutParams);

        RelativeLayout.LayoutParams image3LeftViewLayoutParams = (RelativeLayout.LayoutParams) image3LeftView.getLayoutParams();
        image3LeftViewLayoutParams.rightMargin = 0;
        image3LeftView.setLayoutParams(image3LeftViewLayoutParams);

        RelativeLayout.LayoutParams image3RightViewLayoutParams = (RelativeLayout.LayoutParams) image3RightView.getLayoutParams();
        image3RightViewLayoutParams.leftMargin = 0;
        image3RightView.setLayoutParams(image3RightViewLayoutParams);

        gdriveImageView.setAlpha(0.0f);
        dropboxImageView.setAlpha(0.0f);
        oneDriveImageView.setAlpha(0.0f);
    }

    @OnClick(R.id.welcome_screen5_start_button)
    void onStartClicked() {
        if (agreeToCheckBox.getVisibility() == View.VISIBLE) {
            startButton.setClickable(false);
            Intent intent = new Intent(getContext(), FTBaseShelfActivity.class);
            intent.putExtra("restoreBackup", false);
            startActivity(intent);
            getActivity().finish();
        } else {
            showToast();
        }
    }

    @OnClick(R.id.welcome_screen5_restore_backup_button)
    void onRestoreButtonClicked() {
        FTFirebaseAnalytics.logEvent("welcome_tour", "fifth_screen", "restore");
        if (agreeToCheckBox.getVisibility() == View.VISIBLE) {
            if (FTApp.isForSamsungStore()) {
                FTApp.userConsentDialog(getContext(), new FTDialogFactory.OnAlertDialogShownListener() {
                    @Override
                    public void onPositiveClick(DialogInterface dialog, int which) {
                        FTChooseRestoreCloudDialog restoreCloudDialog = new FTChooseRestoreCloudDialog();
                        restoreCloudDialog.show(getChildFragmentManager(), restoreCloudDialog.getTag());
                    }

                    @Override
                    public void onNegativeClick(DialogInterface dialog, int which) {
                        //Do Nothing
                    }
                });
            } else {
                FTChooseRestoreCloudDialog restoreCloudDialog = new FTChooseRestoreCloudDialog();
                restoreCloudDialog.show(getChildFragmentManager(), restoreCloudDialog.getTag());
            }
        } else {
            showToast();
        }
    }

    @OnClick(R.id.tvPrivacyPolicyLink)
    void onLinkClicked() {
        FTFirebaseAnalytics.logEvent("welcome_tour", "fifth_screen", "privacy_policy");
        new FTPrivacyPolicyDialog().show(getChildFragmentManager(), FTPrivacyPolicyDialog.class.getName());
    }

    @OnClick(R.id.agreeCheckLayout)
    void onCheckBoxSelected() {
        boolean isChecked = agreeToCheckBox.getVisibility() == View.VISIBLE;
        startButton.setAlpha(isChecked ? 0.5f : 1.0f);
        restoreBackupButton.setAlpha(isChecked ? 0.5f : 1.0f);
        agreeToCheckBox.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        agreeToCheckLayout.setBackgroundResource(isChecked ? R.drawable.check_unselected_bg : R.drawable.check_selected_bg);
        String policyVersion = AssetsUtil.getNewPolicyVersion(getContext());
        if (!TextUtils.isEmpty(policyVersion))
            FTApp.getPref().save(SystemPref.PRIVACY_POLICY_VERSION, policyVersion);
    }

    @Override
    public void onCloudChosen() {
        restoreBackUp();
    }

    @Override
    public void onUserAccepted() {
        agreeToCheckBox.setVisibility(View.GONE);
        onCheckBoxSelected();
    }

    private void restoreBackUp() {
        if (FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
            restoreBackupButton.setClickable(false);
            Intent intent = new Intent(getContext(), FTBaseShelfActivity.class);
            intent.putExtra("restoreBackup", true);
            startActivity(intent);
            getActivity().finish();
        } else {
            Toast.makeText(getContext(), R.string.check_your_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast() {
        agreeToCheckLayout.clearAnimation();
        agreeToCheckLayout.setBackgroundResource(R.drawable.check_selected_bg);
        agreeToCheckLayout.setAlpha(0.5f);
        agreeToCheckLayout.postDelayed(() -> agreeToCheckLayout.animate().alpha(0.0f).setDuration(50).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                agreeToCheckLayout.setAlpha(1.0f);
                agreeToCheckLayout.setBackgroundResource(R.drawable.check_unselected_bg);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start(), 500);
        /*Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        View toastView = getLayoutInflater().inflate(R.layout.toast_privacy_policy_error, null);
        toastView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        toast.setView(toastView);
        toast.show();*/
    }
}