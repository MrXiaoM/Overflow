package moe.karla.maven.publishing

import moe.karla.maven.publishing.advtask.UploadToMavenCentral
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Zip

import java.nio.file.Paths
import java.util.regex.Pattern

class MavenPublishingPlugin implements Plugin<Project> {
    @Override
    void apply(Project target) {
        def rootProject = target.rootProject
        if (target != rootProject) {
            target.logger.warn('maven-publish-publish requires be applied on root project.')
            rootProject.apply plugin: MavenPublishingPlugin.class
            return
        } else {
            rootProject.apply plugin: 'java-base'
        }

        def cacheRepoLocation = rootProject.layout.buildDirectory.get()
                .dir('maven-publishing-stage')
                .asFile

        def ext = rootProject.extensions.create('mavenPublishing', MavenPublishingExtension.class)
        rootProject.afterEvaluate {
            if (ext.url == null || ext.url.isEmpty()) {
                def output = new ByteArrayOutputStream()

                rootProject.exec {
                    commandLine = ['git', 'remote', 'get-url', 'origin']
                    standardOutput = output
                }.assertNormalExitValue()

                def remote = output.toString().trim()

                while (true) {
                    def githubMatcher = Pattern.compile("(?:git@github.com:|https://github.com/)(.+)(?:\\.git)?").matcher(remote)
                    if (githubMatcher.matches()) {
                        ext.url = "https://github.com/" + githubMatcher.group(1)
                        break
                    }

                    ext.url = remote
                    break
                }
            }
            rootProject.allprojects {
                if (ext.automaticSourcesAndJavadoc) {
                    apply plugin: PublishingStubsSetupPlugin.class
                }

                pluginManager.withPlugin('maven-publish') {
                    def currentProject = project
                    def publishing = currentProject.extensions.findByName('publishing') as PublishingExtension
                    publishing.repositories {
                        maven {
                            name = 'MavenStage'
                            url = cacheRepoLocation.toURI()
                        }
                    }
                }
            }
        }


        def cleanTask = rootProject.tasks.register('cleanMavenPublishingStage') {
            doLast { cacheRepoLocation.deleteDir() }
        }
        rootProject.tasks.clean.dependsOn(cleanTask)

        def packBundleTask = rootProject.tasks.register('packMavenPublishingStage', Zip.class) {
            destinationDirectory.set(temporaryDir)
            archiveFileName.set('bundle.zip')

            from(cacheRepoLocation)
        }

        def dependencies = [
                "org.apache.httpcomponents:httpclient:4.5.13",
                "org.apache.httpcomponents:httpmime:4.5.13",
        ]
        def externalTaskConfiguration = rootProject.configurations.create('mavenPublishingExternalModuleClasspath')
        dependencies.forEach { externalTaskConfiguration.dependencies.add(rootProject.dependencies.create(it)) }
        def jarMe = findJarMe()

        rootProject.tasks.register('publishToMavenCentral', JavaExec.class) {
            group = 'publishing'
            dependsOn(packBundleTask)
            inputs.files(packBundleTask.get().outputs.files)

            classpath = externalTaskConfiguration
            if (jarMe != null) {
                classpath = classpath + rootProject.files(jarMe)
            }
            mainClass.set('moe.karla.maven.publishing.advtask.UploadToMavenCentral')

            args(packBundleTask.get().outputs.files.singleFile.absolutePath)

            environment('MAVEN_PUBLISH_USER',
                    System.getenv('MAVEN_PUBLISH_USER')
                            ?: rootProject.findProperty('maven.publish.user')
                            ?: System.getProperty('maven.publish.user')
                            ?: ''
            )
            environment('MAVEN_PUBLISH_PASSWORD',
                    System.getenv('MAVEN_PUBLISH_PASSWORD')
                            ?: rootProject.findProperty('maven.publish.password')
                            ?: System.getProperty('maven.publish.password')
                            ?: ''
            )

            doFirst {
                environment('MAVEN_PUBLISH_PUBLISHING_NAME', rootProject.name)
                environment('MAVEN_PUBLISH_PUBLISHING_TYPE', ext.publishingType.name())
            }
        }
    }

    static File findJarMe() {
        def pd = UploadToMavenCentral.class.protectionDomain
        if (pd == null) return null
        def cs = pd.codeSource
        if (cs == null) return null
        def loc = cs.location
        if (loc == null) return null
        if (loc.protocol == "file") {
            return Paths.get(loc.toURI()).toFile()
        }
        return null
    }
}
