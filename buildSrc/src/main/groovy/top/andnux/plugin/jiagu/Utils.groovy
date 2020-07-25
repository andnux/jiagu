package top.andnux.plugin.jiagu


import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class Utils {
    //首字母转小写
    static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0)))
                    .append(s.substring(1)).toString()
    }

    //首字母转大写
    static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0)))
            return s
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0)))
                    .append(s.substring(1)).toString()
    }

    static String download() {
        String usrHome = System.getProperty("user.home")
        File file = new File(usrHome, "JiaGu")
        File jieGuJar = new File(file, "jiagu/jiagu.jar")
        boolean isWin = false
        if (!jieGuJar.exists()) {
            File temp = new File(file, "jiagu.zip")
            println temp.getAbsolutePath()
            if (!temp.exists()) {
                temp.getParentFile().mkdirs()
                println("本地文件不存在，开始下载")
                String urlString
                String osName = System.getProperty("os.name");
                if (osName.startsWith("Windows")) {
                    isWin = true
                    urlString = "https://down.360safe.com/360Jiagu/360jiagubao_windows_32.zip"
                } else if (osName.contains("Mac")) {
                    isWin = false
                    urlString = "https://down.360safe.com/360Jiagu/360jiagubao_mac.zip"
                } else if (osName.startsWith("Linux")) {
                    isWin = false
                    urlString = "https://down.360safe.com/360Jiagu/360jiagubao_linux_64.zip"
                } else {
                    isWin = false
                    println("系统不支持，无法加固")
                    return ""
                }
                println(urlString)
                URL url = new URL(urlString)
                HttpURLConnection conn = url.openConnection()
                conn.connect()
                InputStream inputStream = conn.getInputStream()
                FileOutputStream fos = new FileOutputStream(temp)
                byte[] buffer = new byte[1024 * 1014]
                int length = 0
                int current = 0
                int total = conn.getContentLength()
                println("total - " + total)
                while ((length = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, length)
                    fos.flush()
                    current += length
                    double progress = Math.floor(1000.0 * current / total) / 10
                    println("下载进度：" + progress + "%")
                }
                //5. 文件下载完成
                fos.close()
                println("下载完成：" + temp.getAbsolutePath())
            }
            if (!isWin) {
                println exec("unzip " + temp.getAbsolutePath() + " -d " + file.getAbsolutePath())
            } else {
                unzip(temp.getAbsolutePath(), file.getAbsolutePath())
            }
        }
        return jieGuJar.getAbsolutePath()
    }

    static String exec(String command) throws Exception {
        String returnString = ""
        Process pro = null
        Runtime runTime = Runtime.getRuntime()
        if (runTime == null) {
            System.err.println("Create runtime false!")
        }
        try {
            pro = runTime.exec(command)
            BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream()))
            PrintWriter output = new PrintWriter(new OutputStreamWriter(pro.getOutputStream()))
            String line
            while ((line = input.readLine()) != null) {
                returnString = returnString + line + "\n"
            }
            input.close()
            output.close()
            pro.destroy()
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        return returnString
    }

    static void unzip(String srcPath,
                      String destPath) throws Exception {
        long start = System.currentTimeMillis()
        File srcFile = new File(srcPath)
        if (!srcFile.exists()) {
            throw new FileNotFoundException(srcFile.getPath())
        }
        ZipFile zipFile = new ZipFile(srcFile)
        Enumeration<? extends ZipEntry> entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement()
            System.out.println(entry.getName())
            if (entry.isDirectory()) {
                String dirPath = destPath + "/" + entry.getName()
                File dir = new File(dirPath)
                dir.mkdirs()
            } else {
                File targetFile = new File(destPath + "/" + entry.getName())
                if (!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs()
                }
                targetFile.createNewFile()
                InputStream is = zipFile.getInputStream(entry)
                FileOutputStream fos = new FileOutputStream(targetFile)
                int len
                byte[] buf = new byte[1024 * 1024]
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len)
                }
                fos.close()
                is.close()
            }
        }
        long end = System.currentTimeMillis()
        System.out.println("time: " + (end - start) + " ms")
        try {
            zipFile.close()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}