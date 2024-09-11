package com.ai.face.search;

import static com.ai.face.FaceApplication.CACHE_SEARCH_FACE_DIR;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.*;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.THRESHOLD_ERROR;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;

import com.ai.face.R;
import com.ai.face.base.view.CameraXFragment;
import com.ai.face.databinding.ActivityFaceSearchBinding;
import com.ai.face.faceSearch.search.FaceSearchEngine;
import com.ai.face.faceSearch.search.SearchProcessBuilder;
import com.ai.face.faceSearch.search.SearchProcessCallBack;
import com.ai.face.faceSearch.utils.FaceSearchResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import java.io.File;
import java.util.List;

/**
 * 应多位用户要求，默认使用java 版本演示怎么快速接入SDK。JAVA FIRST
 *
 * 1：N 和 M：N 人脸检索迁移到了 https://github.com/AnyLifeZLB/FaceSearchSDK_Android
 */
public class FaceSearch1NActivity extends AppCompatActivity {
    private ActivityFaceSearchBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFaceSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tips.setOnClickListener(v -> {
            startActivity(new Intent(this, FaceSearchImageMangerActivity.class));
        });

        SharedPreferences sharedPref = getSharedPreferences("faceVerify", Context.MODE_PRIVATE);

        int cameraLens = sharedPref.getInt("cameraFlag", sharedPref.getInt("cameraFlag", 1));

        /**
         * 1. Camera 的初始化。第一个参数0/1 指定前后摄像头；
         * 第二个参数linearZoom [0.001f,1.0f] 指定焦距，参考{@link CameraControl#setLinearZoom(float)}
         */
        CameraXFragment cameraXFragment = CameraXFragment.newInstance(cameraLens,0.01f);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_camerax, cameraXFragment)
                .commit();

        cameraXFragment.setOnAnalyzerListener(imageProxy -> {
            //可以加个红外检测之类的，有人靠近再启动检索服务，不然机器老化快
            if (!isDestroyed() && !isFinishing()) {
                //runSearch() 方法第二个参数是指圆形人脸框到屏幕边距，有助于加快裁剪图像
                FaceSearchEngine.Companion.getInstance().runSearch(imageProxy, 0);
            }
        });


        // 2.各种参数的初始化设置
        SearchProcessBuilder faceProcessBuilder = new SearchProcessBuilder.Builder(this)
                .setLifecycleOwner(this)
                .setThreshold(0.85f) //阈值设置，范围限 [0.80 , 0.95] 识别可信度，也是识别灵敏度
                .setFaceLibFolder(CACHE_SEARCH_FACE_DIR)  //内部存储目录中保存N 个图片库的目录
                .setImageFlipped(cameraLens == CameraSelector.LENS_FACING_FRONT) //手机的前置摄像头imageProxy 拿到的图可能左右翻转
                .setProcessCallBack(new SearchProcessCallBack() {

                    @Override
                    public void onMostSimilar(String similar,float score, Bitmap bitmap) {
                        binding.searchTips.setText(similar);
                        Glide.with(getBaseContext())
                                .load(CACHE_SEARCH_FACE_DIR + File.separatorChar + similar)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .transform(new RoundedCorners(11))
                                .into(binding.image);
                    }

                    @Override
                    public void onProcessTips(int i) {
                        showPrecessTips(i);
                    }

                    @Override
                    public void onFaceMatched(List<FaceSearchResult> result, Bitmap contextBitmap) {
                        // 1:N 仅仅用来演示画人脸框位置
                        binding.graphicOverlay.drawRect(result, cameraXFragment);
                    }

                    @Override
                    public void onLog(String log) {
                        binding.tips.setText(log);
                    }

                }).create();


        //3.初始化引擎，是个耗时耗资源操作
        FaceSearchEngine.Companion.getInstance().initSearchParams(faceProcessBuilder);

    }


    /**
     * 显示提示
     *
     * @param code
     */
    private void showPrecessTips(int code) {
        binding.image.setImageResource(R.mipmap.ic_launcher);
        switch (code) {
            default:
                binding.searchTips.setText("提示码："+code);
                break;
            // 识别到多人脸    ！！

            case THRESHOLD_ERROR :
                binding.searchTips.setText("识别阈值Threshold范围为0.8-0.95");
                break;

            case MASK_DETECTION:
                binding.searchTips.setText("请摘下口罩"); //默认无
                break;

            case NO_LIVE_FACE:
                binding.searchTips.setText("未检测到人脸");
                break;

            case EMGINE_INITING:
                binding.searchTips.setText("初始化中");
                break;

            case FACE_DIR_EMPTY:
                binding.searchTips.setText("人脸库为空");
                break;

            case NO_MATCHED:
                //本次摄像头预览帧无匹配而已，会快速取下一帧进行分析检索
                binding.searchTips.setText("无匹配，请正对人脸");
                break;

            case SEARCHING:
                binding.searchTips.setText("");
                break;

        }
    }

    /**
     * 销毁，停止
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FaceSearchEngine.Companion.getInstance().stopSearchProcess();
    }


}