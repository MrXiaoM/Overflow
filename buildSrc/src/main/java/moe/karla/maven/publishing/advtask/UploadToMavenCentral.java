package moe.karla.maven.publishing.advtask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

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

    public static void execute(String publishingType, String publishingName, File bundle) throws Throwable {

        CloseableHttpClient httpclient = HttpClientBuilder.create().build();

        String url = "https://central.sonatype.com/api/v1/publisher/upload?publishingType=" + publishingType + "&name=" + URLEncoder.encode(publishingName, StandardCharsets.UTF_8);
        HttpPost httpPost = new HttpPost(url);
        System.out.println("POST " + url);
        httpPost.addHeader("Authorization", "Bearer " + Base64.getEncoder().encodeToString(
                getUserPassword()
        ));

        httpPost.setEntity(
                MultipartEntityBuilder.create()
                        .addBinaryBody("bundle", bundle)
                        .build()
        );

        HttpResponse response = httpclient.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode / 100 != 2) {
            System.err.println("HTTP Post Exited with " + statusCode);
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
            throw new Exception("HTTP " + statusCode);
        } else {
            String deploymentId = EntityUtils.toString(response.getEntity());
            System.out.println("Deployment ID: " + deploymentId);
        }
        httpclient.close();
    }
}
