package top.andnux.plugin.jiagu

import org.gradle.api.Project

class JiaGuExtension {
    File inFile; //输入APK文件
    File outDir; //输入路径
    String username //用户名
    String password //密码
    File channelFile //指向通道备注文件.txt
    String[] config;//扩展配置
    String[] config_so;//配置需要加固的SO文件，以空格分隔
    String[] config_assets;//配置需要忽略的资源文件，以空格分隔
    String[] config_so_private;//配置防盗用的SO文件，以空格分隔
    JiaGuExtension(Project project) {

    }
}