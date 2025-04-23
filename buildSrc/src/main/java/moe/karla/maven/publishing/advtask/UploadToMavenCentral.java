package moe.karla.maven.publishing.advtask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class UploadToMavenCentral {
    private static byte[] getUserPassword() {
        String mavenPublishPassword = System.getenv("MAVEN_PUBLISH_PASSWORD");
        if (mavenPublishPassword == null || mavenPublishPassword.isEmpty()) {
            throw new RuntimeException("Missing MAVEN_PUBLISH_PASSWORD");
        }
        if (mavenPublishPassword.contains(":")) {
            return mavenPublishPassword.getBytes(StandardCharsets.UTF_8);
        }

        String mavenPublishUser = System.getenv("MAVEN_PUBLISH_USER");
        if (mavenPublishUser == null || mavenPublishUser.isEmpty()) {
            throw new RuntimeException("Missing MAVEN_PUBLISH_USER and MAVEN_PUBLISH_PASSWORD not defined with user part");
        }
        return (mavenPublishUser + ":" + mavenPublishPassword).getBytes(StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Throwable {

        HttpClient httpclient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(
                "https://central.sonatype.com/api/v1/publisher/upload?publishingType=" + System.getenv("MAVEN_PUBLISH_PUBLISHING_TYPE")
                        + "&name=" + URLEncoder.encode(System.getenv("MAVEN_PUBLISH_PUBLISHING_NAME"), "UTF-8")
        );
        httpPost.addHeader("Authorization", "Bearer " + Base64.getEncoder().encodeToString(
                getUserPassword()
        ));

        httpPost.setEntity(
                MultipartEntityBuilder.create()
                        .addBinaryBody("bundle", new File(args[0]))
                        .build()
        );

        HttpResponse response = httpclient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() / 100 != 2) {
            System.err.println("HTTP Post Exited with " + response.getStatusLine().getStatusCode());
            if (response.getEntity() != null) {
                InputStream content = response.getEntity().getContent();
                if (content != null) {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int len = content.read(buffer);
                        if (len == -1) break;

                        System.err.write(buffer, 0, len);
                    }
                }
            }
            System.exit(540);
        }
    }
}
