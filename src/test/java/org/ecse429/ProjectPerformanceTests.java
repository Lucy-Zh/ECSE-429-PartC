package org.ecse429;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import com.google.gson.JsonObject;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class ProjectPerformanceTests {
    private static HttpClient client;

    @BeforeAll
    public static void setup() {
        client = HttpClient.newHttpClient();
    }

    /******************************
     * TESTS for /projects ENDPOINTS *
     * ****************************/

    private void createProject(String title, Boolean isActive, Boolean isCompleted, String description) throws IOException, InterruptedException {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("title", title);
        requestJson.addProperty("active", isActive);
        requestJson.addProperty("completed", isCompleted);
        requestJson.addProperty("description", description);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
                .build();
        client.send(request, BodyHandlers.ofString());
    }

    private void editProjectById(int projectId, String title, Boolean isActive, Boolean isCompleted, String description) throws IOException, InterruptedException {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("title", title);
        requestJson.addProperty("active", isActive);
        requestJson.addProperty("completed", isCompleted);
        requestJson.addProperty("description", description);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects/" + projectId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
                .build();
        client.send(request, BodyHandlers.ofString());
    }

    private void deleteProjectById(int projectId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects/" + projectId))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
        client.send(request, BodyHandlers.ofString());
    }

    @Test
    public void testCreateProjectPerformance() throws IOException, InterruptedException {
        double[] transactionTime = new double[PerformanceTestUtils.numberOfObjects.length];
        double[] memoryUsage = new double[PerformanceTestUtils.numberOfObjects.length];
        double[] cpuUsage = new double[PerformanceTestUtils.numberOfObjects.length];

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        for(int numberOfObjectsIndex = 0; numberOfObjectsIndex < PerformanceTestUtils.numberOfObjects.length; numberOfObjectsIndex++){
            long startTime = System.nanoTime();

            for(int i = 0; i < numberOfObjectsIndex; i++){
                createProject("test_title", true, false, "create project performance test");
            }

            // wrong values
            transactionTime[numberOfObjectsIndex] = (System.nanoTime() - startTime) / 1000000.0; // convert to milliseconds
            memoryUsage[numberOfObjectsIndex] = (double) osBean.getFreeMemorySize() / 1000000; // convert to MB
            cpuUsage[numberOfObjectsIndex] = osBean.getProcessCpuLoad() * 100; // convert to percentage
        }

        PerformanceTestUtils.writeDataToNewCSVFile("CreateProjectResults.csv", transactionTime, memoryUsage, cpuUsage);
    }
}
