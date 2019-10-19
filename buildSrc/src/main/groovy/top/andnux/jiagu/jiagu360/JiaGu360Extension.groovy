package top.andnux.jiagu.jiagu360

import org.gradle.api.Project

class JiaGu360Extension {

    String username //用户名
    String password //密码

    File storeFile; //签名文件
    String storePassword//签名密码
    String keyAlias//别名
    String keyPassword ///别名密码

    File channelFile //指向通道备注文件.txt

    String[] config;//扩展配置

    String appVersionName;//版本号
    int appVersionCode;//版本号

    JiaGu360Extension(Project project) {

    }
}