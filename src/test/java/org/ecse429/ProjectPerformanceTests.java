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
import org.json.JSONObject;

public class ProjectPerformanceTests {
    private static HttpClient client;

    @BeforeAll
    public static void setup() {
        client = HttpClient.newHttpClient();
    }

    /******************************
     * TESTS for /projects ENDPOINTS *
     * ****************************/

    private HttpResponse<String> createProject(String title, Boolean isActive, Boolean isCompleted, String description) throws IOException, InterruptedException {
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
        return client.send(request, BodyHandlers.ofString());
    }

    private HttpResponse<String> editProjectById(int projectId, String title, Boolean isActive, Boolean isCompleted, String description) throws IOException, InterruptedException {
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
        return client.send(request, BodyHandlers.ofString());
    }

    private HttpResponse<String> deleteProjectById(int projectId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects/" + projectId))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
        return client.send(request, BodyHandlers.ofString());
    }

    @Test
    public void testCreateProjectPerformance() throws IOException, InterruptedException {
        double[] transactionTime = new double[PerformanceTestUtils.numberOfObjects.length];
        double[] memoryUsage = new double[PerformanceTestUtils.numberOfObjects.length];
        double[] cpuUsage = new double[PerformanceTestUtils.numberOfObjects.length];

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        osBean.getProcessCpuLoad(); // Call to initialize

        for(int numberOfObjectsIndex = 0; numberOfObjectsIndex < PerformanceTestUtils.numberOfObjects.length; numberOfObjectsIndex++){
            long startTime = System.currentTimeMillis();
            for(int i = 0; i < PerformanceTestUtils.numberOfObjects[numberOfObjectsIndex]; i++){
                createProject("test_title", true, false, "create project performance test");
            }
            long endTime = System.currentTimeMillis();

            transactionTime[numberOfObjectsIndex] = (endTime - startTime);
            memoryUsage[numberOfObjectsIndex] = (double) osBean.getFreeMemorySize() / 1000000; // convert to MB
            cpuUsage[numberOfObjectsIndex] = osBean.getProcessCpuLoad() * 100; // convert to percentage
        }

        PerformanceTestUtils.writeDataToNewCSVFile("CreateProjectResults.csv", transactionTime, memoryUsage, cpuUsage);
    }

    @Test
    public void testEditProjectPerformance() throws IOException, InterruptedException {
        double[] transactionTime = new double[PerformanceTestUtils.numberOfObjects.length];
        double[] memoryUsage = new double[PerformanceTestUtils.numberOfObjects.length];
        double[] cpuUsage = new double[PerformanceTestUtils.numberOfObjects.length];

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        String firstProjectId;
        HttpResponse<String> response;

        // create projects we need to test editing project
        response = createProject("test_title", true, false, "edit project performance test");
        JSONObject jsonObject = new JSONObject(response.body());
        firstProjectId = jsonObject.getString("id");

        // last value in numberOfObjects[] is the most objects
        for(int i = 1; i < PerformanceTestUtils.numberOfObjects[PerformanceTestUtils.numberOfObjects.length - 1]; i++){
            createProject("test_title", true, false, "edit project performance test");
        }

        for(int numberOfObjectsIndex = 0; numberOfObjectsIndex < PerformanceTestUtils.numberOfObjects.length; numberOfObjectsIndex++){
            int currentProjectId = Integer.parseInt(firstProjectId);

            long startTime = System.nanoTime();
            for(int i = 0; i < PerformanceTestUtils.numberOfObjects[numberOfObjectsIndex]; i++){
                editProjectById(currentProjectId, "test_title", true, false, "edit project performance test " + PerformanceTestUtils.numberOfObjects[numberOfObjectsIndex]);
                currentProjectId++;
            }

            // wrong values
            transactionTime[numberOfObjectsIndex] = (System.nanoTime() - startTime) / 1000000.0; // convert to milliseconds
            memoryUsage[numberOfObjectsIndex] = (double) osBean.getFreeMemorySize() / 1000000; // convert to MB
            cpuUsage[numberOfObjectsIndex] = osBean.getProcessCpuLoad() * 100; // convert to percentage
        }

        PerformanceTestUtils.writeDataToNewCSVFile("EditProjectResults.csv", transactionTime, memoryUsage, cpuUsage);
    }

    @Test
    public void testDeleteProjectPerformance() throws IOException, InterruptedException {
        double[] transactionTime = new double[PerformanceTestUtils.numberOfObjects.length];
        double[] memoryUsage = new double[PerformanceTestUtils.numberOfObjects.length];
        double[] cpuUsage = new double[PerformanceTestUtils.numberOfObjects.length];

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        int currentProjectId;
        HttpResponse<String> response;

        // create projects we need to test deleting project
        response = createProject("test_title", true, false, "edit project performance test");
        JSONObject jsonObject = new JSONObject(response.body());
        currentProjectId = Integer.parseInt(jsonObject.getString("id"));

        // calculate number of objects and create objects to delete
        int totalNumberOfObjects = 0;
        for(int numberOfObjects: PerformanceTestUtils.numberOfObjects){
            totalNumberOfObjects += numberOfObjects;
        }

        for(int i = 1; i < totalNumberOfObjects; i++){
            createProject("test_title", true, false, "delete project performance test");
        }

        for(int numberOfObjectsIndex = 0; numberOfObjectsIndex < PerformanceTestUtils.numberOfObjects.length; numberOfObjectsIndex++){

            long startTime = System.nanoTime();
            for(int i = 0; i < PerformanceTestUtils.numberOfObjects[numberOfObjectsIndex]; i++){
                deleteProjectById(currentProjectId);
                currentProjectId++;
            }

            // wrong values
            transactionTime[numberOfObjectsIndex] = (System.nanoTime() - startTime) / 1000000.0; // convert to milliseconds
            memoryUsage[numberOfObjectsIndex] = (double) osBean.getFreeMemorySize() / 1000000; // convert to MB
            cpuUsage[numberOfObjectsIndex] = osBean.getProcessCpuLoad() * 100; // convert to percentage
        }

        PerformanceTestUtils.writeDataToNewCSVFile("DeleteProjectResults.csv", transactionTime, memoryUsage, cpuUsage);
    }
}
