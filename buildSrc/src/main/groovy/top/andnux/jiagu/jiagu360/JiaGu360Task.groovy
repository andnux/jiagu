package top.andnux.jiagu.jiagu360

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import top.andnux.jiagu.Constant
import top.andnux.jiagu.OSUtils
import top.andnux.jiagu.ZipUtils

class JiaGu360Task extends DefaultTask {

    static String exec(String command) throws Exception {
        String returnString = "";
        Process pro = null;
        Runtime runTime = Runtime.getRuntime();
        if (runTime == null) {
            System.err.println("Create runtime false!");
        }
        try {
            pro = runTime.exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            PrintWriter output = new PrintWriter(new OutputStreamWriter(pro.getOutputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                returnString = returnString + line + "\n";
            }
            input.close();
            output.close();
            pro.destroy();
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        return returnString;
    }

    static String download360() {
        String usrHome = System.getProperty("user.home")
        File file = new File(usrHome, Constant.JIAGU_360_DIR)
        File jieGuJar = new File(file, "jiagu/jiagu.jar")
        if (!jieGuJar.exists()) {
            File temp = new File(file, "temp.zip")
            println temp.getAbsolutePath()
            if (!temp.exists()) {
                temp.getParentFile().mkdirs()
                println("本地文件不存在，开始下载")
                String urlString;
                if (OSUtils.WINDOWS) {
                    urlString = "https://down.360safe.com/360Jiagu/360jiagubao_windows_32.zip"
                } else if (OSUtils.MAC) {
                    urlString = "https://down.360safe.com/360Jiagu/360jiagubao_mac.zip"
                } else if (OSUtils.LINUX) {
                    urlString = "https://down.360safe.com/360Jiagu/360jiagubao_linux_64.zip"
                } else {
                    println("系统不支持，无法加固")
                    return ""
                }
                println(urlString)
                URL url = new URL(urlString)
                HttpURLConnection conn = url.openConnection()
                conn.connect()
                InputStream inputStream = conn.getInputStream()
                FileOutputStream fos = new FileOutputStream(temp);
                byte[] buffer = new byte[1024 * 100];
                int length = 0;
                int current = 0;
                int total = conn.getContentLength()
                while ((length = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                    fos.flush();
                    current += length;
                    double progress = Math.floor(1000.0 * current / total) / 10;
                    println("下载进度：" + progress + "%")
                }
                //5. 文件下载完成
                fos.close();
                println("下载完成：" + temp.getAbsolutePath())
            }
            if (OSUtils.MAC || OSUtils.LINUX) {
                println exec("unzip " + temp.getAbsolutePath() + " -d " + file.getAbsolutePath())
            } else if (OSUtils.WINDOWS) {
                ZipUtils.unzip(temp.getAbsolutePath(),
                        file.getAbsolutePath(),
                        true)
            }
        }
        return jieGuJar.getAbsolutePath()
    }

    JiaGu360Task() {
        group = Constant.GROUP
        description = "use 360 jiagu"
    }

    @TaskAction
    def start() {
        String jieGuJar = download360()
        def ext = project.extensions.findByName(JiaGu360Plugin.EXTENSION_NAME) as JiaGu360Extension
        String rerult = exec("java -jar ${jieGuJar} -login ${ext.username} ${ext.password}")
        println(rerult)
        if (rerult.concat("success")) {
            String extCmd = ""
            if (ext.storeFile != null && ext.storeFile.exists()) {
                //要签名
                rerult = exec("java -jar ${jieGuJar} -importsign ${ext.storeFile.getAbsolutePath()} ${ext.storePassword}  ${ext.keyAlias}  ${ext.keyPassword}")
                println(rerult)
                extCmd += " -autosign "
            }

            if (ext.channelFile != null && ext.channelFile.exists()) {
                //有通道
                rerult = exec("java -jar ${jieGuJar} -importmulpkg ${ext.channelFile}")
                println(rerult)
                extCmd += " -automulpkg "
            }

            if (ext.config != null && ext.config.size() > 0) {
                //额外配置
                rerult = exec("java -jar ${jieGuJar} -config ${ext.config}")
                println(rerult)
            }
            File inFile = new File(project.getBuildDir(),"outputs/apk/release/app-release.apk")
            File outFile = inFile.getParentFile()
            // 加固
            String cmd = "java -jar ${jieGuJar} -jiagu ${inFile.getAbsolutePath()} " + outFile.getAbsolutePath()
            rerult = exec(cmd + extCmd)
            println(rerult)
        }
    }
}