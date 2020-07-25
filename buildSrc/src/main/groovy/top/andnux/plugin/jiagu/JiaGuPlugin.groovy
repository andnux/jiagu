package top.andnux.plugin.jiagu

import org.gradle.api.Plugin
import org.gradle.api.Project

class JiaGuPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'JiaGu'

    @Override
    void apply(Project project) {
        if (!project.android) {
            throw new IllegalStateException('Must apply \'com.android.application\' first!')
        }
        project.extensions.create(EXTENSION_NAME, JiaGuExtension, project)
        def variants = project.android.applicationVariants
        project.afterEvaluate {
            variants.all { variant ->
                def apkFile = ""
                variant.outputs.all {
                    apkFile = outputFileName
                }
                def name = Utils.toUpperCaseFirstOne(variant.name)
                //安装
                def installTask = project.tasks.create("installJiaGu${name}", JiaGuTask)
                installTask.variant = variant
                installTask.apkFile = apkFile
                installTask.dependsOn("install${name}")

                //构建
                def task = project.tasks.create("assembleJiaGu${name}", JiaGuTask)
                task.variant = variant
                task.apkFile = apkFile
                task.dependsOn("assemble${name}")
            }
        }
    }
}
