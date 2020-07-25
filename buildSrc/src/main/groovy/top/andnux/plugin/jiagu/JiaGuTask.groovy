package top.andnux.plugin.jiagu

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class JiaGuTask extends DefaultTask {

    def apkFile
    def variant

    JiaGuTask() {
        group = "JiaGu"
        description = "use 360 jiagu"
    }

    @TaskAction
    def start() {
        String jiaGuJar = Utils.download()
        if (jiaGuJar == null || jiaGuJar.length() == 0) {
            return
        }
        def ext = project.extensions.findByName(JiaGuPlugin.EXTENSION_NAME) as JiaGuExtension
        String rerult = Utils.exec("java -jar ${jiaGuJar} -login ${ext.username} ${ext.password}")
        println(rerult)
        if (rerult.concat("success")) {
            String extCmd = ""
            def sing = variant.signingConfig
            if (sing.storeFile != null && sing.storeFile.exists()) {
                //要签名
                println sing.storeFile.getAbsolutePath()
                println sing.storePassword
                println sing.keyAlias
                println sing.keyPassword

                rerult = Utils.exec("java -jar ${jiaGuJar} -importsign ${sing.storeFile.getAbsolutePath()}" +
                        " ${sing.storePassword}  ${sing.keyAlias}  ${sing.keyPassword}")
                println(rerult)
                extCmd += " -autosign "
            }

            if (ext.channelFile != null && ext.channelFile.exists()) {
                //有通道
                rerult = Utils.exec("java -jar ${jiaGuJar} -importmulpkg ${ext.channelFile}")
                println(rerult)
                extCmd += " -automulpkg "
            }

            if (ext.config != null && ext.config.size() > 0) {
                //额外配置
                rerult = Utils.exec("java -jar ${jiaGuJar} -config ${ext.config}")
                println(rerult)
            }

            if (ext.config_so != null && ext.config_so.size() > 0) {
                //加固的SO文件
                rerult = Utils.exec("java -jar ${jiaGuJar} -config_so ${ext.config_so.join(" ")}")
                println(rerult)
            }

            if (ext.config_assets != null && ext.config_assets.size() > 0) {
                //忽略的资源文件
                rerult = Utils.exec("java -jar ${jiaGuJar} -config_assets ${ext.config_assets.join(" ")}")
                println(rerult)
            }

            if (ext.config_so_private != null && ext.config_so_private.size() > 0) {
                //防盗用的SO文件
                rerult = Utils.exec("java -jar ${jiaGuJar} -config_so_private ${ext.config_so_private.join(" ")}")
                println(rerult)
            }
            String variantFlavorName = variant.flavorName
            String name = variant.name
            name = Utils.toLowerCaseFirstOne(name.replace(variantFlavorName, ""))
            File inFile
            File outDir
            if (ext.inFile != null && ext.inFile.exists()) {
                inFile = ext.inFile
            } else {
                String child = "outputs/apk/${variantFlavorName}/${name}/${apkFile}"
                inFile = new File(project.buildDir, child)
            }
            if (ext.outDir != null) {
                outDir = ext.outDir
            } else {
                outDir = inFile.getParentFile()
            }
            println inFile.getAbsolutePath()
            // 加固
            String cmd = "java -jar ${jiaGuJar} -jiagu ${inFile.getAbsolutePath()} " + outDir.getAbsolutePath()
            rerult = Utils.exec(cmd + extCmd)
            println(rerult)
        }
    }
}