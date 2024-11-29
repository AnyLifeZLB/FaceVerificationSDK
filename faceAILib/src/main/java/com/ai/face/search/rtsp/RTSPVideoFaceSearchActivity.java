package com.ai.face.search.rtsp;

import static com.ai.face.FaceAIConfig.CACHE_SEARCH_FACE_DIR;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.EMGINE_INITING;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.FACE_DIR_EMPTY;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.FACE_TOO_SMALL;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.MASK_DETECTION;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.NO_LIVE_FACE;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.NO_MATCHED;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.SEARCHING;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.THRESHOLD_ERROR;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.TOO_MUCH_FACE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;

import com.ai.face.R;
import com.ai.face.base.view.CameraXFragment;
import com.ai.face.databinding.ActivityFaceSearchRtspBinding;
import com.ai.face.faceSearch.search.FaceSearchEngine;
import com.ai.face.faceSearch.search.SearchProcessBuilder;
import com.ai.face.faceSearch.search.SearchProcessCallBack;
import com.ai.face.faceSearch.utils.FaceSearchResult;
import com.ai.face.search.FaceSearchImageMangerActivity;
import com.alexvas.rtsp.widget.RtspDataListener;
import com.alexvas.rtsp.widget.RtspStatusListener;

import java.util.List;


/**
 * RTSP 视频流人脸搜索
 * RTSP Play，Powered by https://github.com/alexeyvasilyev/rtsp-client-android
 *
 */
public class RTSPVideoFaceSearchActivity extends AppCompatActivity {
    //如果设备没有补光灯，UI界面背景多一点白色的区域，利用屏幕的光作为补光
    private ActivityFaceSearchRtspBinding  binding;


    private final RtspDataListener rtspDataListener=new RtspDataListener() {
        @Override
        public void onRtspDataApplicationDataReceived(@NonNull byte[] bytes, int i, int i1, long l) {
            FaceSearchEngine.Companion.getInstance().runSearch(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        }

        @Override
        public void onRtspDataVideoNalUnitReceived(@NonNull byte[] bytes, int i, int i1, long l) {}
        @Override
        public void onRtspDataAudioSampleReceived(@NonNull byte[] bytes, int i, int i1, long l) {}
    };

    private final RtspStatusListener rtspStatusListener=new RtspStatusListener() {
        @Override
        public void onRtspStatusConnecting() {

        }

        @Override
        public void onRtspStatusConnected() {
            binding.pbLoading.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onRtspStatusDisconnecting() {

        }

        @Override
        public void onRtspStatusDisconnected() {

        }

        @Override
        public void onRtspStatusFailedUnauthorized() {
            Toast.makeText(RTSPVideoFaceSearchActivity.this,"RTSP Oauth error",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRtspStatusFailed(@Nullable String s) {
            startRTSP(2000);
            binding.pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        public void onRtspFirstFrameRendered() {

        }

        @Override
        public void onRtspFrameSizeChanged(int i, int i1) {

        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFaceSearchRtspBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tips.setOnClickListener(v -> {
            startActivity(new Intent(this, FaceSearchImageMangerActivity.class)
                    .putExtra("isAdd",false));

        });

        binding.ivVideoImage.setStatusListener(rtspStatusListener);
        binding.ivVideoImage.setDataListener(rtspDataListener);


        // 2.各种参数的初始化设置 （M：N 建议阈值放低）
        SearchProcessBuilder faceProcessBuilder = new SearchProcessBuilder.Builder(this)
                .setLifecycleOwner(this)
                .setThreshold(0.80f)            //识别成功阈值设置，范围仅限 0.8-0.95！默认0.85
                .setFaceLibFolder(CACHE_SEARCH_FACE_DIR)  //内部存储目录中保存N 个图片库的目录
                .setSearchType(SearchProcessBuilder.SearchType.N_SEARCH_M) //M:N 搜索
                .setProcessCallBack(new SearchProcessCallBack() {

                    //坐标框和对应的 搜索匹配到的图片标签
                    //人脸检测成功后画白框，此时还没有标签字段
                    //人脸搜索匹配成功后白框变绿框，并标记出对应的人脸ID Label（建议用唯一ID 命名人脸图片）
                    @Override
                    public void onFaceMatched(List<FaceSearchResult> result, Bitmap contextBitmap) {
                        binding.graphicOverlay.drawRect(result);

                        if (!result.isEmpty()) {
                            binding.searchTips.setText("");
                        }
                    }

                    @Override
                    public void onProcessTips(int i) {
                        showPrecessTips(i);
                    }

                    @Override
                    public void onLog(String log) {
                        binding.tips.setText(log);
                    }

                }).create();


        //3.初始化r引擎
        FaceSearchEngine.Companion.getInstance().initSearchParams(faceProcessBuilder);


        startRTSP(1000);
    }



    private void startRTSP(long timeDelay){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Uri uri=Uri.parse("rtsp://10.39.175.106/cam/realmonitor?channel=1&subtype=0");
                binding.ivVideoImage.init(uri,"admin","rtsp1234","rtsp");

                binding.ivVideoImage.start(true,false,true);
            }
        }, timeDelay);

    }


    /**
     * 显示提示
     *
     * @param code
     */
    private void showPrecessTips(int code) {
        switch (code) {
            default:
                binding.searchTips.setText("Tips Code：" + code);
                break;
            case FACE_TOO_SMALL: //镜头前仅有一个人
                Toast.makeText(this, R.string.come_closer_tips, Toast.LENGTH_SHORT).show();
                break;
            case TOO_MUCH_FACE:
                Toast.makeText(this, R.string.multiple_faces_tips, Toast.LENGTH_SHORT).show();
                break;

            case THRESHOLD_ERROR:
                binding.searchTips.setText(R.string.search_threshold_scope_tips);
                break;

            case MASK_DETECTION:
                binding.searchTips.setText(R.string.no_mask_please); //默认无
                break;

            case NO_LIVE_FACE:
                binding.searchTips.setText(R.string.no_face_detected_tips);
                break;

            case EMGINE_INITING:
                binding.searchTips.setText(R.string.sdk_init);
                break;

            case FACE_DIR_EMPTY:
                //人脸库没有录入照片
                binding.searchTips.setText(R.string.face_dir_empty);
                break;

            case NO_MATCHED:
                //本次摄像头预览帧无匹配而已，会快速取下一帧进行分析检索
                binding.searchTips.setText(R.string.no_matched_face);
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
        binding.ivVideoImage.stop();
    }


}