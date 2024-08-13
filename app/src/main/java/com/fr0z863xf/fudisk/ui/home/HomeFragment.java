package com.fr0z863xf.fudisk.ui.home;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.compose.ui.node.LookaheadAlignmentLines;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.fr0z863xf.fudisk.FileSystem.FileManager;
import com.fr0z863xf.fudisk.R;
import com.fr0z863xf.fudisk.Utils.AccountManager;
import com.fr0z863xf.fudisk.databinding.FragmentHomeBinding;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MaterialCardView tipCard;
    private TextView tipTitle;
    private TextView tipCardContent;
    private ImageView tipIcon;
    private Animation rotateAnimation;

    private MaterialCardView uploadCard;
    private TextView uploadTitle;
    private TextView uploadContent;
    private ImageView uploadIcon;

    private ExecutorService executorService;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
            HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        tipCard = root.findViewById(R.id.HomeTipCard);
        tipTitle = root.findViewById(R.id.TipTitle);
        tipCardContent = root.findViewById(R.id.TipCardContent);
        tipIcon = root.findViewById(R.id.TipIcon);
        tipIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.md_theme_primary));
        uploadCard = root.findViewById(R.id.UploadTaskCard);
        uploadTitle = root.findViewById(R.id.UploadTaskTitle);
        uploadContent = root.findViewById(R.id.UploadTaskCardContent);
        uploadIcon = root.findViewById(R.id.UploadTaskIcon);
        rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        executorService = Executors.newSingleThreadExecutor();
        initializeCard();
        tipCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCardClick();
            }
        });
        return root;
    }



    private void initializeCard() {

        tipIcon.setImageResource(R.drawable.baseline_cached_24);
        tipIcon.startAnimation(rotateAnimation);

        AccountManager accountManager = AccountManager.getInstance(null);
        accountManager.accountStatus.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer status) {
                if (status == 0) {
                    tipTitle.setText("未登录");
                    tipCardContent.setText("请前往设置配置帐号");
                    tipIcon.clearAnimation();
                    tipCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorWarningContainer));
                    tipIcon.setImageResource(R.drawable.baseline_info_24);
                    tipIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorWarning));

                } else if (status == 2) {
                    tipTitle.setText("欢迎");
                    tipCardContent.setText("帐号正常");
                    rotateAnimation.cancel();
                    tipCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_theme_primaryContainer));
                    tipIcon.setImageResource(R.drawable.baseline_check_circle_24);
                    tipIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.md_theme_primary));
                } else if (status == 3) {
                    tipTitle.setText("帐号异常");
                    tipCardContent.setText("帐号登录失败");
                    rotateAnimation.cancel();
                    tipCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_theme_errorContainer));
                    tipIcon.setImageResource(R.drawable.baseline_error_24);
                    tipIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.md_theme_error));
                } else {
                    Log.i("HomeTipCard","帐号存在，开始刷新");
                    tipTitle.setText("正在刷新帐号");
                    tipCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_theme_tertiaryContainer));
                    tipIcon.setImageResource(R.drawable.baseline_cached_24);
                    tipIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.md_theme_tertiary));
                    tipIcon.startAnimation(rotateAnimation);
                    //refreshAccount();
                }
            }
        });
        FileManager.getInstance(null).uploadTasks.observe(getViewLifecycleOwner(), new Observer<List<Integer>>() {
            @Override
            public void onChanged(List<Integer> status) {

                if (status.isEmpty()) {
                    uploadTitle.setText("没有上传任务");
                    uploadIcon.clearAnimation();
                } else {
                    uploadTitle.setText("正在上传文件");
                    uploadContent.setText("");
                    uploadIcon.startAnimation(rotateAnimation);
                    if (status.get(status.size() - 1) == 2) {
                        //附上时间,如11:20
                        uploadContent.setText("刚刚上传了一个文件 - " + new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date()));
                    } else if (status.get(status.size() -1) == 3) {
                        uploadContent.setText("刚刚上传文件失败" + new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date()));

                    }
                    /*
                    for (int i = 0; i < status.toArray().length; i++) {
                        if (status.get(i) == 0) {
                            //异常情况
                            status.remove(i);
                            FileManager.getInstance(null).uploadTasks.postValue(status);
                            Log.w("HomeFragment","检测到异常任务值");
                            break;
                        }
                        if (status.get(i) == 2) {
                            uploadContent.setText("刚刚上传了文件");
                        } else if (status.get(i) == 3) {
                            uploadContent.setText("刚刚上传文件失败");
                        }
                    }

                     */
                }
            }
        });


/*
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                AccountManager accountManager = AccountManager.getInstance(null);
                boolean accountExistence = accountManager.checkAccountExists();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (accountExistence) {
                            Log.i("HomeTipCard","帐号存在，开始刷新");
                            tipTitle.setText("正在检测帐号");
                            refreshAccount();
                        } else {
                            tipTitle.setText("未登录");
                            tipCardContent.setText("请前往设置配置帐号");
                            tipIcon.clearAnimation();
                            tipCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorWarningContainer));
                            tipIcon.setImageResource(R.drawable.baseline_info_24);
                        }
                    }
                });
            }
        });*/
    }

    private void onCardClick() {
        AccountManager accountManager = AccountManager.getInstance(null);
        boolean accountExists = accountManager.accountStatus.getValue() != 0;

        Log.i("HomeTipCard","点击卡片，帐号存在状态为"+accountExists);

        if (accountExists) {
            Toast.makeText(this.getContext(), "正在刷新帐号", Toast.LENGTH_SHORT).show();
            //refreshAccount();
        } else {
            // 处理未登录时点击卡片的逻辑，比如跳转到设置页面
            //goToSettings();
        }
    }


    private void refreshAccount() {
/*
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                AccountManager accountManager = AccountManager.getInstance(null);
                String result = accountManager.refreshAccount();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ("ok".equals(result)) {
                            tipTitle.setText("欢迎");
                            tipCardContent.setText("帐号正常");
                            rotateAnimation.cancel();
                            tipCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_theme_primaryContainer));
                            tipIcon.setImageResource(R.drawable.baseline_check_circle_24);
                        } else {
                            tipTitle.setText("帐号异常");
                            tipCardContent.setText("帐号登录失败：" + result);
                            rotateAnimation.cancel();
                            tipCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_theme_errorContainer));
                            tipIcon.setImageResource(R.drawable.baseline_error_24);
                        }
                    }
                });
            }
        }); */
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}