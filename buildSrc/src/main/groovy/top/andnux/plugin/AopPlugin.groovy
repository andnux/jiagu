package top.andnux.plugin

import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

import java.util.function.Consumer

class AopPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println("AopPlugin开始工作了哦")
        if (!project.android) {
            throw new IllegalStateException('Must apply \'com.android.application\' or' +
                    ' \'com.android.library\' first!')
        }
        def isApp = false
        def isLib = false
        project.plugins.forEach(new Consumer<Plugin>() {
            @Override
            void accept(Plugin t) {
                if (t.class.name.equalsIgnoreCase('com.android.build.gradle.AppPlugin')){
                    isApp = true
                }else if (t.class.name.equalsIgnoreCase('com.android.build.gradle.LibraryPlugin')){
                    isLib = true
                }
            }
        })
        println("isApp : " + isApp)
        println("isLib : " + isLib)
        if (!isApp && !isLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }
        final def log = project.logger
        final def variants
        if (isApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }

        project.dependencies {
            implementation 'org.aspectj:aspectjrt:1.9.4'
        }

        variants.all { variant ->
            JavaCompile javaCompile
            if (variant.hasProperty('javaCompileProvider')) {
                javaCompile = variant.javaCompileProvider.get()
            } else {
                javaCompile = variant.javaCompile
            }
            javaCompile.doLast {
                String[] args = ["-showWeaveInfo",
                                 "-1.8",
                                 "-inpath", javaCompile.destinationDir.toString(),
                                 "-aspectpath", javaCompile.classpath.asPath,
                                 "-d", javaCompile.destinationDir.toString(),
                                 "-classpath", javaCompile.classpath.asPath,
                                 "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
                MessageHandler handler = new MessageHandler(true);
                new Main().run(args, handler)
                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            log.error message.message, message.thrown
                            break;
                        case IMessage.WARNING:
                        case IMessage.INFO:
                            log.info message.message, message.thrown
                            break;
                        case IMessage.DEBUG:
                            log.debug message.message, message.thrown
                            break;
                    }
                }
            }
        }
    }
}