#  服务政策
  SDK 在低配早期魅蓝Note3（Android7） 华为P8（Android6），以及最新高配旗舰机型三星S25，小米15和小米pad 7pro
  经过严格测试验证；大厂生产的标准Android 系统设备目前反馈运行良好。如果你是定制的Android系统/开发板/USB双目摄像头  
  请先检测是否能正常运行，有问题也可以联系我们解答。
  
  SDK 不读取任何敏感信息，严格限制运行获取权限，充分保护用户的隐私权限，不联网就能工作更不会收集上传人脸关键信息。

### 0.特殊定制硬件配置板子支持程度
  FaceAI SDK通用版本的开发适配都是基于手机和平板进行的，有着标准的摄像头外设；为了保证通用版本最大的兼容性，识别过程以一种通用的方式进行，
  如果你的设备硬件配置有加速器支持（ GPU 和NPU）以及运行较高的Android 系统，定制SDK运行的效率和准确度都会有相应的提升

### 1.移动端是否包含iOS 端？
   iOS 有1:1 人脸识别（含静默活体），动作活体移植中.

### 2.为什么小朋友群体1:N ，M：N 误识别率较高？
   小朋友的五官差异相对成年人确实没有那么大，需要专门为小朋友群体训练人脸识别模型了，SDK demo 为通用模型

### 3.是否支持外接USB 摄像头
   如果你的系统摄像头采用标准的 Android Camera2 API 和摄像头 HIDL接口，SDK内部已经集成CameraX管理摄像头，也就是
   标准大厂生产的手机平板设备都是支持的。
   
   目前1.9.0 以上版本已经默认支持了UVC 协议的红外双目摄像头，直接在手机上插上USB 连接摄像头就能体验

   ![红外双目](https://github.com/user-attachments/assets/3e96879d-0757-409e-894b-5d1d0e80231c)

### 4.人脸识别1:N 搜索是否能支持N>万人以上
   本SDK 目前已经支持万人以上,大容量人脸库搜索速度快，也支持分库搜索

### 5.如何提升 1:N/M：N 人脸识别的准确率？
    1:N 人脸搜索Demo 看起来比M：N 反应更慢是因为限制了反应速度防止长期运行设备过热。   

    - 录入高质量的人脸图
    - 良好的设备性能和摄像头品质
    - 不戴深色粗框眼镜

    人脸识别输入要求：
    * 由于照片品质问题，某些人脸可能无法识别，例如：
    * 具有极端照明（例如严重的背光）的图像。
    * 有障碍物挡住了一只或两只眼睛。
    * 发型或胡须的差异。
    * 年龄使面貌发生变化。
    * 极端的面部表情。

### 6.识别的灵敏度准确率参数
   目前人脸检测的环节只要人脸像素大于222就能识别，相识度setThreshold(0.88f) //阈值设置，范围限 [0.85 , 0.95] 识别可信度，也是识别灵敏度
   
### 7.除了支持Android 手机和平板，其他Android 设备是否支持？
   其他基于Android的定制设备只要是符合Google规范没有魔改一般是支持的，可能部分外接摄像头种类不支持需要定制兼容，需邮寄定制硬件调试。
   目前支持的Android 系统版本为Android(5,15] ，其他低版本Android 系统可以根据Demo案例使用强制降级依赖CameraX 和 TFLite

### 8.授权说明？
   **Debug 调试包可以体验所有的功能，不用授权**
   正式包（debuggable=false）授权后，不限终端数量，不限制使用次数。
   授权绑定App包名和签名SHA1进行，需要新的功能和新特性开发根据评估情况再协定。
   需要授权请添加微信或邮件申请，并告知App 名称，包名 ，签名SHA1和 简单使用场景介绍

### 9.摄像头方向调整相关
   目前SDK 的摄像头预览，分析画面都是在内部处理，画面方向和手机，Pad 的姿态（传感器反向）决定，画面能随横竖屏自动切换
这个特性和你的系统相机是一致的。但是有部分用户的设备是自定义的基于Android系统的，软硬件都是自己定义，如果你有需求改变画面
方向建议你在系统底层修改摄像头适配，可以先用系统相机验证。  
   如果无法修改系统底层代码适配需要定制的联系我，邮箱 anylife.zlb@gmail.com。
   如果你要自行管理摄像头接入，画面方向 旋转管理可以参考 https://github.com/AnyLifeZLB/BinocularCameraFaceSearch
   

### 10.调整Target SDK （如target SDK 28）后依赖冲突怎么处理？或者外部依赖的版本需要强制为某个版本怎么处理
   根据Compile SDK 不同，各自项目依赖体系不一样
   主工程和SDK 中的依赖有冲突需要统一依赖,可以参考下面方式处理
   比如TargetSDK 还是 28 的camera_version降低到 1.2.3（最后支持TargetSDK 28）
   更多错误请自行Google，百度搜索解决方法，集成问题不是SDK内部原因，谢谢

   **以下代码配置应该放到主模块build.gradle里面**

   ```
   def camera_version = "1.2.3"
   configurations.configureEach {
   resolutionStrategy {
   force   "androidx.camera:camera-core:$camera_version",
   "androidx.camera:camera-camera2:$camera_version",
   "androidx.camera:camera-lifecycle:$camera_version",
   "androidx.camera:camera-view:$camera_version"
   }
   }
   ```
   
### 11.集成SDK开发环境和Gradle 插件版本是怎样的？
  开发环境 Android Studio Iguana | 2024.2.1
  gradle插件版本 7.4.2  gradle 版本 7.5 
  **java 11**  kotlin 1.7.20

  如果你的项目还有kapt请迁移至KSP，kapt 很早停止维护
  kotlin-android-extensions官方也已经停止维护，建议升级为viewbinding

  更多gradle 集成构建打包基础可以参考文章 https://juejin.cn/post/7160337743552675847

  其他集成问题，请根据报错搜索解决方案,VIP 用户可以联系协助解决

### 12.是否可以使用在金融，强安全场景
  SDK没有达到100% 的准确度，***不建议***使用在金融 军事强安全等严格要求领域，建议的场景如下：

  【1:1】 移动考勤真人校验，App多帐户免密登录，酒店入驻、刷脸解锁、真人校验
  【1:N】 智能门锁，小区门禁，公司门禁，智慧校园、景区、工地、社区、酒店等
  【M:N】 公安布控，人群追踪 监控等等 **

### 13.能通过File 操作直接把人脸照片放到制定目录就开始人脸搜索吗？

    不行，必须要通过SDK 的API进行，因为要提取人脸特征值和建立搜索库索引才能快速搜索
    如FaceSearchImagesManger.Companion.getInstance().insertOrUpdateFaceImage()

### 14.自定义摄像头（方向旋转，相机管理等），双目摄像头是否可以使用？
   支持自定义摄像头，可以在子线程持续输入bitmap 实时预览帧作为参数进行SDK 的调用。
   如果你要自行管理摄像头接入，画面方向 旋转管理可以参考 https://github.com/AnyLifeZLB/BinocularCameraFaceSearch

   双目摄像头也已经支持

### 15.uniApp 插件市场支持
    暂无，有兴趣打包发布插件者请联系我。开发人员也可以根据uniApp官方推荐方式自行集成

### 16.X86，X86_64 位CPU 支持
    X86_64已经打包支持；考虑到X86 系列Android 设备的市场占有率和SDK 体积问题，特殊需要请联系
