package top.andnux.jiagu.jiagu360

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import top.andnux.jiagu.Constant

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class JiaGu360Task extends DefaultTask {

    static String exec(String command) throws InterruptedException {
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
        } catch (IOException ex) {
            ex.printStackTrace()
        }
        return returnString;
    }

    static boolean download360() {
        String usrHome = System.getProperty("user.home")
        File file = new File(usrHome, Constant.JIAGU_360_DIR)
        File jieGuFile = new File(file, "360JiaGuBao.zip")
        if (!jieGuFile.exists()) {
            file.mkdirs()
            println("本地文件不存在，开始下载")
            URL url = new URL("https://down.360safe.com/360Jiagu/360jiagubao_windows_32.zip")
            HttpURLConnection conn = url.openConnection()
            conn.connect()
            InputStream inputStream = conn.getInputStream()
            FileOutputStream fos = new FileOutputStream(jieGuFile);
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

        }else {
            ZipFile zipFile = new ZipFile(jieGuFile)
            while (zipFile.hasMoreElements()) {
                ZipEntry entry = zipFile.entries().nextElement()
                println(entry.name);
            }
        }
    }

    JiaGu360Task() {
        group = Constant.GROUP
        description = "use 360 jiagu"
    }

    @TaskAction
    def start() {
        def ext = project.extensions.findByName(JiaGu360Plugin.EXTENSION_NAME) as JiaGu360Extension
        download360()
        String rerult = exec("java -jar ${ext.jiaGuFile.getAbsolutePath()} -login ${ext.username} ${ext.password}")
        println(rerult)
        if (rerult.concat("success")) {
            String extCmd = ""
            if (ext.storeFile != null && ext.storeFile.exists()) {
                //要签名
                rerult = exec("java -jar ${ext.jiaGuFile.getAbsolutePath()} -importsign ${ext.storeFile.getAbsolutePath()} ${ext.storePassword}  ${ext.keyAlias}  ${ext.keyPassword}")
                println(rerult)
                extCmd += " -autosign "
            }

            if (ext.channelFile != null && ext.channelFile.exists()) {
                rerult = exec("java -jar ${ext.jiaGuFile.getAbsolutePath()} -importmulpkg ${ext.channelFile}")
                println(rerult)
                extCmd += " -automulpkg "
            }

            println("加固中........")
            if (ext.config != null && ext.config.size() > 0) {
                rerult = exec("java -jar ${ext.jiaGuFile.getAbsolutePath()} -config ${ext.config}")
                println(rerult)
            }
            ext.outputFile.deleteDir();
            ext.outputFile.mkdirs();
            String cmd = "java -jar ${ext.jiaGuFile.getAbsolutePath()} -jiagu ${ext.inputFile.getAbsolutePath()} ${ext.outputFile.getAbsolutePath()}"
            rerult = exec(cmd + extCmd)
            println(rerult)

            //保存加固包和release包
            String version = "v${ext.appVersionName}"
            if (ext.backupsFileDir != null && ext.backupsFileDir.exists()) {
                println("开始备份apk以及mapping.txt")
                copyFile(ext.inputFile, new File(ext.backupsFileDir, version + File.separator + ext.inputFile.name))
                def appJGFile = ext.outputFile.listFiles(new FileFilter() {
                    @Override
                    boolean accept(File file) {
                        return true
                    }
                }).first()
                if (appJGFile != null && appJGFile.exists()) {
                    copyFile(appJGFile, new File(ext.backupsFileDir, version + File.separator + appJGFile.name))
                }
                def file = new File(ext.inputFile.getParentFile().getParentFile().getParentFile(), "/mapping/release/mapping.txt")
                if (file != null && file.exists()) {
                    File desc = new File(ext.backupsFileDir, version + File.separator + "mapping.txt");
                    copyFile(file, desc);
                }
            }
        }
    }

    static void copyFile(File source, File dest)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs()
            }
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
            output.flush()
        } finally {
            if (input != null)
                input.close();
            if (output != null)
                output.close();
        }
    }
}