package org.ecse429;

import com.google.gson.JsonObject;
import com.sun.management.OperatingSystemMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TodoPerformanceTests {
  private static HttpClient client;

  @BeforeAll
  public static void setup() {
    client = HttpClient.newHttpClient();
  }

  /******************************
   * TESTS for /todos ENDPOINTS *
   * ****************************/

  private HttpResponse<String> createTodo(String title, Boolean isDone, String description)
      throws IOException, InterruptedException {
    JsonObject requestJson = new JsonObject();
    requestJson.addProperty("title", title);
    requestJson.addProperty("doneStatus", isDone);
    requestJson.addProperty("description", description);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:4567/todos"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
            .build();
    return client.send(request, BodyHandlers.ofString());
  }

  private HttpResponse<String> editTodoById(
      int todoId, String title, Boolean isDone, String description)
      throws IOException, InterruptedException {
    JsonObject requestJson = new JsonObject();
    requestJson.addProperty("title", title);
    requestJson.addProperty("doneStatus", isDone);
    requestJson.addProperty("description", description);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:4567/todos/" + todoId))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
            .build();
    return client.send(request, BodyHandlers.ofString());
  }

  private HttpResponse<String> deleteTodoById(int todoId) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:4567/todos/" + todoId))
            .header("Content-Type", "application/json")
            .DELETE()
            .build();
    return client.send(request, BodyHandlers.ofString());
  }

  @Test
  public void testCreateTodoPerformance() throws IOException, InterruptedException {
    double[] transactionTime = new double[PerformanceTestUtils.numberOfObjects.length];
    double[] memoryUsage = new double[PerformanceTestUtils.numberOfObjects.length];
    double[] cpuUsage = new double[PerformanceTestUtils.numberOfObjects.length];

    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    osBean.getProcessCpuLoad(); // Call to initialize

    for (int numberOfObjectsIndex = 0;
        numberOfObjectsIndex < PerformanceTestUtils.numberOfObjects.length;
        numberOfObjectsIndex++) {
      long startTime = System.currentTimeMillis();
      for (int i = 0; i < PerformanceTestUtils.numberOfObjects[numberOfObjectsIndex]; i++) {
        createTodo("test_title", true, "create todo performance test");
      }
      long endTime = System.currentTimeMillis();

      transactionTime[numberOfObjectsIndex] = (endTime - startTime);
      memoryUsage[numberOfObjectsIndex] =
          (double) osBean.getFreeMemorySize() / 1000000; // convert to MB
      cpuUsage[numberOfObjectsIndex] = osBean.getProcessCpuLoad() * 100; // convert to percentage
    }

    PerformanceTestUtils.writeDataToNewCSVFile(
        "CreateTodoResults.csv", transactionTime, memoryUsage, cpuUsage);
  }

  @Test
  public void testEditTodoPerformance() throws IOException, InterruptedException {
    double[] transactionTime = new double[PerformanceTestUtils.numberOfObjects.length];
    double[] memoryUsage = new double[PerformanceTestUtils.numberOfObjects.length];
    double[] cpuUsage = new double[PerformanceTestUtils.numberOfObjects.length];

    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    osBean.getProcessCpuLoad(); // Call to initialize

    String firstTodoId;
    HttpResponse<String> response;

    // create todos we need to test editing todo
    response = createTodo("test_title", true, "edit project performance test");
    JSONObject jsonObject = new JSONObject(response.body());
    firstTodoId = jsonObject.getString("id");

    // last value in numberOfObjects[] is the most objects
    for (int i = 1;
        i < PerformanceTestUtils.numberOfObjects[PerformanceTestUtils.numberOfObjects.length - 1];
        i++) {
      createTodo("test_title", true, "edit project performance test");
    }

    for (int numberOfObjectsIndex = 0;
        numberOfObjectsIndex < PerformanceTestUtils.numberOfObjects.length;
        numberOfObjectsIndex++) {
      int currentTodoId = Integer.parseInt(firstTodoId);

      long startTime = System.currentTimeMillis();
      for (int i = 0; i < PerformanceTestUtils.numberOfObjects[numberOfObjectsIndex]; i++) {
        editTodoById(
            currentTodoId,
            "test_title",
            true,
            "edit project performance test "
                + PerformanceTestUtils.numberOfObjects[numberOfObjectsIndex]);
        currentTodoId++;
      }
      long endTime = System.currentTimeMillis();

      transactionTime[numberOfObjectsIndex] = (endTime - startTime);
      memoryUsage[numberOfObjectsIndex] =
          (double) osBean.getFreeMemorySize() / 1000000; // convert to MB
      cpuUsage[numberOfObjectsIndex] = osBean.getProcessCpuLoad() * 100; // convert to percentage
    }

    PerformanceTestUtils.writeDataToNewCSVFile(
        "EditTodoResults.csv", transactionTime, memoryUsage, cpuUsage);
  }

  @Test
  public void testDeleteTodoPerformance() throws IOException, InterruptedException {
    double[] transactionTime = new double[PerformanceTestUtils.numberOfObjects.length];
    double[] memoryUsage = new double[PerformanceTestUtils.numberOfObjects.length];
    double[] cpuUsage = new double[PerformanceTestUtils.numberOfObjects.length];

    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    osBean.getProcessCpuLoad(); // Call to initialize

    int currentTodoId;
    HttpResponse<String> response;

    // create todos we need to test deleting todo
    response = createTodo("test_title", true, "edit project performance test");
    JSONObject jsonObject = new JSONObject(response.body());
    currentTodoId = Integer.parseInt(jsonObject.getString("id"));

    // calculate number of objects and create objects to delete
    int totalNumberOfObjects = 0;
    for (int numberOfObjects : PerformanceTestUtils.numberOfObjects) {
      totalNumberOfObjects += numberOfObjects;
    }

    for (int i = 1; i < totalNumberOfObjects; i++) {
      createTodo("test_title", true, "delete project performance test");
    }

    for (int numberOfObjectsIndex = 0;
        numberOfObjectsIndex < PerformanceTestUtils.numberOfObjects.length;
        numberOfObjectsIndex++) {

      long startTime = System.currentTimeMillis();
      for (int i = 0; i < PerformanceTestUtils.numberOfObjects[numberOfObjectsIndex]; i++) {
        deleteTodoById(currentTodoId);
        currentTodoId++;
      }
      long endTime = System.currentTimeMillis();

      transactionTime[numberOfObjectsIndex] = (endTime - startTime);
      memoryUsage[numberOfObjectsIndex] =
          (double) osBean.getFreeMemorySize() / 1000000; // convert to MB
      cpuUsage[numberOfObjectsIndex] = osBean.getProcessCpuLoad() * 100; // convert to percentage
    }

    PerformanceTestUtils.writeDataToNewCSVFile(
        "DeleteTodoResults.csv", transactionTime, memoryUsage, cpuUsage);
  }
}
