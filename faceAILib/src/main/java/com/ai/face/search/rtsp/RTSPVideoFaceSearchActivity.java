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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.ai.face.R;
import com.ai.face.databinding.ActivityFaceSearchRtspBinding;
import com.ai.face.faceSearch.search.FaceSearchEngine;
import com.ai.face.faceSearch.search.SearchProcessBuilder;
import com.ai.face.faceSearch.search.SearchProcessCallBack;
import com.ai.face.faceSearch.utils.FaceSearchResult;
import com.ai.face.search.FaceSearchImageMangerActivity;
import com.alexvas.rtsp.widget.RtspStatusListener;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * RTSP 视频流人脸搜索，难点之一人脸不会对准你的摄像头
 *
 * 图像分辨率不能太高，你的设备性能配置不一定能跟上，否则闪退！！
 *
 * 这个RTSP 库 compileSdk 35 ；minSDK=24, java version =17;gradle sdk也要同步改为17
 *
 * 然后AS invalidate 后 重启IDE 生效,同步依赖要花费不少时间，耐心等待处理
 */
public class RTSPVideoFaceSearchActivity extends AppCompatActivity {
    //如果设备没有补光灯，UI界面背景多一点白色的区域，利用屏幕的光作为补光
    private ActivityFaceSearchRtspBinding  binding;

    private final ExecutorService singleThreadExecutor  = Executors.newSingleThreadExecutor();

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
            startRTSP(2000);//重新连接
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

        // 2.各种参数的初始化设置 （M：N 建议阈值放低）
        SearchProcessBuilder faceProcessBuilder = new SearchProcessBuilder.Builder(this)
                .setLifecycleOwner(this)
                .setThreshold(0.8f)            //识别成功阈值设置，范围仅限 0.85-0.95！默认0.85
                .setFaceLibFolder(CACHE_SEARCH_FACE_DIR)  //内部存储目录中保存N 个图片库的目录
                .setSearchType(SearchProcessBuilder.SearchType.N_SEARCH_M) //M:N 搜索
                .setProcessCallBack(new SearchProcessCallBack() {

                    // 坐标框和对应的 搜索匹配到的图片标签
                    // 人脸检测成功后画白框，此时还没有标签字段
                    // 人脸搜索匹配成功后白框变绿框，并标记出对应的人脸ID Label（建议用唯一ID 命名人脸图片）
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


        //3.初始化引擎
        FaceSearchEngine.Companion.getInstance().initSearchParams(faceProcessBuilder);


        binding.ivVideoImage.setOnRtspImageBitmapListener(bitmap -> {
            //It is Main UI  Thread , post task to background thread
            singleThreadExecutor.execute(()->{
                //注意Bitmap 的内存管理，压缩但不能影响效果。目前SDK内部还没有处理
                Log.d("RTSP","Thread Name: "+Thread.currentThread().getName());
                FaceSearchEngine.Companion.getInstance().runSearch(bitmap);
            });

        });

        startRTSP(1000);
    }


    /**
     * 请替换为你自己摄像头的账号 密码
     *
     * @param timeDelay 延迟多久后开始播放
     */
    private void startRTSP(long timeDelay){
        new Handler().postDelayed(() -> {
            Uri uri=Uri.parse("rtsp://10.39.175.106/cam/realmonitor?channel=1&subtype=0");
            binding.ivVideoImage.init(uri,"admin","rtsp1234","rtsp");
            binding.ivVideoImage.start(true,false,false);
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