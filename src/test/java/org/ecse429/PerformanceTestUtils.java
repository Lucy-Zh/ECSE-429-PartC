package org.ecse429;

import java.io.FileWriter;
import java.io.IOException;

public class PerformanceTestUtils {
  public static int[] numberOfObjects = {1, 10, 50, 100, 500, 1000};

  public static void writeDataToNewCSVFile(
      String fileName, double[] transactionTime, double[] memoryUsage, double[] cpuUsage) {
    try (FileWriter writer =
        new FileWriter("src/test/resources/performance-test-results/" + fileName)) {
      writer.append("Number of Objects,Transaction Time (ms),Memory Usage (MB),CPU Usage (%)\n");

      for (int i = 0; i < numberOfObjects.length; i++) {
        writer
            .append(String.valueOf(numberOfObjects[i]))
            .append(",")
            .append(String.valueOf(transactionTime[i]))
            .append(",")
            .append(String.valueOf(memoryUsage[i]))
            .append(",")
            .append(String.valueOf(cpuUsage[i]))
            .append("\n");
      }
    } catch (IOException e) {
      System.err.println("Error writing to CSV file: " + e.getMessage());
    }
  }
}
