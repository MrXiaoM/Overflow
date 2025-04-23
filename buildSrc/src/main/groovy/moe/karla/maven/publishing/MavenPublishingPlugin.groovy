package moe.karla.maven.publishing

import moe.karla.maven.publishing.advtask.UploadToMavenCentral
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.bundling.Zip

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
                        def user = System.getenv("MAVEN_SNAPSHOTS_USERNAME")
                        def token = System.getenv("MAVEN_SNAPSHOTS_TOKEN")
                        if (user != null && token != null) {
                            maven {
                                name = 'CentralSnapshots'
                                url = 'https://central.sonatype.com/repository/maven-snapshots/'
                                credentials {
                                    username = user
                                    password = token
                                }
                            }
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

        rootProject.tasks.register('publishToMavenCentral') {
            group = 'publishing'
            dependsOn(packBundleTask)
            inputs.files(packBundleTask.get().outputs.files)
            doFirst {
                UploadToMavenCentral.execute(rootProject.name, ext.publishingType.name(), packBundleTask.get().outputs.files.singleFile)
            }
        }
    }
}
