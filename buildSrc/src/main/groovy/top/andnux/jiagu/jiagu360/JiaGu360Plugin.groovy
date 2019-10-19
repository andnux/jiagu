package top.andnux.jiagu.jiagu360

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class JiaGu360Plugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'JiaGu'
    static final String NAME = "jiaGuRelease"

    @Override
    void apply(Project project) {
        println('JiaGuPlugin')
        project.extensions.create(EXTENSION_NAME, JiaGu360Extension, project)
        boolean isResGuard = project.plugins.hasPlugin("AndResGuard")
        Task jiaGuTask = project.tasks.create(NAME, JiaGu360Task)
        project.tasks.whenTaskAdded { Task theTask ->
            if (isResGuard) {
                if (theTask.name == 'resguardRelease') {
                    println("有资源混淆")
                    jiaGuTask.dependsOn(theTask)
                }
            } else {
                if (theTask.name == 'assembleRelease') {
                    println("无资源混淆")
                    jiaGuTask.dependsOn(theTask)
                }
            }
        }
    }
}
