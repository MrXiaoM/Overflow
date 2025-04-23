package moe.karla.maven.publishing

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.Usage
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar

class PublishingStubsSetupPlugin implements Plugin<Project> {
    private static final String STUB_JAVADOC_CONFIGURATION_NAME = 'mavenPublishingDummyStubJavadoc'

    static NamedDomainObjectProvider<Configuration> createStubJavadocConfiguration(Project root) {
        def configurations = root.configurations
        try {
            return configurations.named(STUB_JAVADOC_CONFIGURATION_NAME)
        } catch (UnknownDomainObjectException ignored) {
        }

        root.apply plugin: 'java-base'
        def stubTask = root.tasks.register('createMavenPublishingDummyStubJavadoc', Jar.class) { Jar task ->
            task.group = 'publishing'
            task.archiveClassifier.set('javadoc')
            task.archiveBaseName.set('maven-publishing-stub')
            task.archiveVersion.set('')
        }

        return configurations.register(STUB_JAVADOC_CONFIGURATION_NAME) { Configuration configuration ->
            configuration.outgoing {
                artifact(stubTask)
            }
            configuration.attributes {
                attribute(Usage.USAGE_ATTRIBUTE, root.objects.named(Usage.class, "java-runtime"))
                attribute(Category.CATEGORY_ATTRIBUTE, root.objects.named(Category.class, "documentation"))
                attribute(Bundling.BUNDLING_ATTRIBUTE, root.objects.named(Bundling.class, "external"))
                attribute(DocsType.DOCS_TYPE_ATTRIBUTE, root.objects.named(DocsType.class, DocsType.JAVADOC))
            }
        }
    }

    @Override
    void apply(Project target) {
        def stubConfig = createStubJavadocConfiguration(target.rootProject)


        target.pluginManager.withPlugin('java') {
            // setup sourcesJar & javadocs
            def javaExt = target.extensions.findByName('java') as JavaPluginExtension
            javaExt.withSourcesJar()

            AdhocComponentWithVariants javaComponent = (AdhocComponentWithVariants) target.components.findByName("java")
            javaComponent.addVariantsFromConfiguration(stubConfig.get()) {
                // dependencies for this variant are considered runtime dependencies
                it.mapToMavenScope("runtime")
                // and also optional dependencies, because we don't want them to leak
                it.mapToOptional()
            }
        }
    }
}
