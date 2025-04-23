package moe.karla.maven.publishing

class MavenPublishingExtension {

    ///////////////////////////
    // BASIC
    ///////////////////////////

    /**
     * The URL of this project.
     *
     * Leave blank or null for auto guest.
     */
    public String url

    static enum PublishingType {
        /**
         * (default) a deployment will go through validation and, if it passes, automatically proceed to publish to Maven Central
         */
        AUTOMATIC,
        /**
         * a deployment will go through validation and require the user to manually publish it via the Portal UI
         */
        USER_MANAGED,
    }

    public PublishingType publishingType = PublishingType.AUTOMATIC

    /**
     * Generate sources jar and stub javadoc jar automatic
     */
    public boolean automaticSourcesAndJavadoc = true


    ///////////////////////////
    // LICENSES
    ///////////////////////////

    static class LicenseInfo {
        public String name
        public String url

        LicenseInfo(String name, String url) {
            this.name = name
            this.url = url
        }
    }

    /**
     * Licenses.
     *
     * Leave blank or null for auto guest
     */
    public List<LicenseInfo> licenses

    void license(String name, String url) {
        if (licenses == null) {
            this.licenses = new ArrayList<>()
        }
        licenses.add(new LicenseInfo(name, url))
    }


    ///////////////////////////
    // SCM
    ///////////////////////////
    public String scmConnection
    public String scmDeveloperConnection
    public String scmUrl

    void scm(String connection, String developerConnection, String url) {
        this.scmConnection = connection
        this.scmDeveloperConnection = developerConnection
        this.scmUrl = url
    }


    ///////////////////////////
    // DEVELOPERS
    ///////////////////////////
    static class DeveloperInfo {
        public String name
        public String email
        public String organization
        public String organizationUrl

        public DeveloperInfo(String name, String email) {
            this.name = name
            this.email = email
        }

        public DeveloperInfo(String name, String email, String organization, String organizationUrl) {
            this.name = name
            this.email = email
            this.organization = organization
            this.organizationUrl = organizationUrl
        }
    }
    public List<DeveloperInfo> developers

    void developer(String name, String email) {
        if (developers == null) {
            this.developers = new ArrayList<>()
        }
        developers.add(new DeveloperInfo(name, email))
    }

    void developer(String name, String email, String organization, String organizationUrl) {
        if (developers == null) {
            this.developers = new ArrayList<>()
        }
        developers.add(new DeveloperInfo(name, email, organization, organizationUrl))
    }
}
