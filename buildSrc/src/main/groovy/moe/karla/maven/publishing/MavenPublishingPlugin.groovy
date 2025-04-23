package moe.karla.maven.publishing

import moe.karla.maven.publishing.advtask.UploadToMavenCentral
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Zip

import java.nio.file.Files
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
            if (ext.scmUrl == null || ext.scmUrl.isEmpty()) {
                ext.scmUrl = ext.url
            }
            if (ext.scmConnection == null || ext.scmConnection.isEmpty()) {
                ext.scmConnection = "scm:git:" + ext.url
            }
            if (ext.scmDeveloperConnection == null || ext.scmDeveloperConnection.isEmpty()) {
                ext.scmDeveloperConnection = "scm:git:" + ext.url
            }
            if (ext.developers == null || ext.developers.isEmpty()) {
                def output = new ByteArrayOutputStream()

                rootProject.exec {
                    commandLine = ['git', 'log', '--format=%an<%ae>', 'HEAD']
                    standardOutput = output
                }.assertNormalExitValue()

                def lastCommitAuthor = output.toString().trim()
                def matcher = Pattern.compile("(.+)<(.+)>").matcher(lastCommitAuthor)

                if (matcher.matches()) {
                    ext.developer(matcher.group(1), matcher.group(2))
                } else {
                    throw new RuntimeException("Failed to resolve developer information from last commit with " + lastCommitAuthor)
                }
            }
            if (ext.licenses == null || ext.licenses.isEmpty()) {
                def licenseFile = rootProject.file('LICENSE')
                String name = 'UNLICENSE'
                if (licenseFile.exists()) {
                    def firstLine = Files.readAllLines(licenseFile.toPath())
                            .stream()
                            .map { it.trim() }
                            .filter { !it.isEmpty() }
                            .findFirst()
                    if (firstLine.present) {
                        name = firstLine.present
                    }
                }

                ext.license(name, ext.url)
            }


            rootProject.allprojects {
                if (ext.automaticSourcesAndJavadoc) {
                    apply plugin: PublishingStubsSetupPlugin.class
                }

                pluginManager.withPlugin('maven-publish') {
                    def currentProject = project
                    if (currentProject.description == null || currentProject.description.isEmpty()) {
                        currentProject.description = currentProject.name
                    }

                    def publishing = currentProject.extensions.findByName('publishing') as PublishingExtension
                    publishing.publications.withType(MavenPublication.class).configureEach {
                        pom {
                            name.set(artifactId)
                            description.set(currentProject.description)

                            url.set(ext.url)
                            licenses {
                                ext.licenses.forEach { lInfo ->
                                    license {
                                        name.set(lInfo.name)
                                        url.set(lInfo.url)
                                    }
                                }
                            }
                            developers {
                                ext.developers.forEach { dInfo ->
                                    developer {
                                        name.set(dInfo.name)
                                        email.set(dInfo.email)
                                        if (dInfo.organization != null) organization.set(dInfo.organization)
                                        if (dInfo.organizationUrl != null) organization.set(dInfo.organizationUrl)
                                    }
                                }
                            }
                            scm {
                                url.set(ext.scmUrl)
                                connection.set(ext.scmConnection)
                                developerConnection.set(ext.scmDeveloperConnection)
                            }
                        }
                    }


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
