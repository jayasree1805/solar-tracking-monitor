package solar_tracker;
import java.io.*;
public class SimulatedSolar {
    public static void main(String[] args) {
        File file = new File("C:\\Users\\JAYASREE G KALKURA\\JAVA\\sensor_data.txt");

        if (!file.exists()) {
            System.out.println("ERROR: File not found! Run the MATLAB script first.");
            return;
        }

        String previousLine = "";

        while (true) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String lastLine = null;
                String line;
                while ((line = br.readLine()) != null) {
                    lastLine = line;
                }

                if (lastLine != null && !lastLine.equals(previousLine)) {
                    processData(lastLine);
                    previousLine = lastLine;
                }
            } catch (Exception e) {
                System.out.println("Error reading file: " + e.getMessage());
            }

            try {
                Thread.sleep(2000); // 2-second delay between reads
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void processData(String data) {
        try {
            String[] parts = data.split(",");
            int azimuth = Integer.parseInt(parts[0].trim());
            int tilt = Integer.parseInt(parts[1].trim());
            double energyProduced = Double.parseDouble(parts[2].trim());
            double energyRemaining = Double.parseDouble(parts[3].trim());
            double dustLevel = Double.parseDouble(parts[4].trim());

            double efficiency = (energyProduced / (energyProduced + energyRemaining)) * 100;

            System.out.println("Azimuth: " + azimuth + "°");
            System.out.println("Tilt: " + tilt + "°");
            System.out.println("Energy Produced: " + energyProduced + " Wh");
            System.out.println("Energy Remaining: " + energyRemaining + " Wh");
            System.out.println("Efficiency: " + String.format("%.2f", efficiency) + "%");
            System.out.println("Dust Level: " + dustLevel + " µg/m³");

            if (dustLevel > 300) {
                System.out.println("⚠ ALERT: Dust accumulation detected! Consider cleaning the panel.");
            }
            System.out.println("---------------------------------");
        } catch (Exception e) {
            System.out.println("Error processing data: " + e.getMessage());
        }
    }
}
