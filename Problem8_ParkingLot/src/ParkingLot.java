import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

class ParkingSpot {
    int spotNumber;
    String licensePlate;
    long entryTime;
    boolean isOccupied;

    public ParkingSpot(int spotNumber) {
        this.spotNumber = spotNumber;
        this.isOccupied = false;
    }
}

public class ParkingLot {
    private ParkingSpot[] spots;
    private HashMap<String, Integer> vehicleToSpot;
    private int totalSpots;
    private int totalProbes;
    private int parkedCount;

    public ParkingLot(int totalSpots) {
        this.totalSpots = totalSpots;
        this.spots = new ParkingSpot[totalSpots];
        this.vehicleToSpot = new HashMap<>();

        for (int i = 0; i < totalSpots; i++) {
            spots[i] = new ParkingSpot(i);
        }
    }

    public int parkVehicle(String licensePlate) {
        if (vehicleToSpot.containsKey(licensePlate)) {
            System.out.println("Vehicle " + licensePlate + " is already parked at spot #" +
                    vehicleToSpot.get(licensePlate));
            return -1;
        }

        // Hash function: use license plate hash to find preferred spot
        int preferredSpot = Math.abs(licensePlate.hashCode()) % totalSpots;
        int probes = 0;

        // Linear probing
        for (int i = 0; i < totalSpots; i++) {
            int spotIndex = (preferredSpot + i) % totalSpots;
            probes++;

            if (!spots[spotIndex].isOccupied) {
                // Park the vehicle
                spots[spotIndex].isOccupied = true;
                spots[spotIndex].licensePlate = licensePlate;
                spots[spotIndex].entryTime = System.currentTimeMillis();
                vehicleToSpot.put(licensePlate, spotIndex);
                parkedCount++;
                totalProbes += probes;

                System.out.println("parkVehicle(\"" + licensePlate + "\") → Assigned spot #" +
                        spotIndex + " (" + (probes - 1) + " probes)");
                return spotIndex;
            }
        }

        System.out.println("Parking lot is full! Cannot park " + licensePlate);
        return -1;
    }

    public void exitVehicle(String licensePlate) {
        Integer spotIndex = vehicleToSpot.get(licensePlate);
        if (spotIndex == null) {
            System.out.println("Vehicle " + licensePlate + " not found in parking lot");
            return;
        }

        ParkingSpot spot = spots[spotIndex];
        long duration = System.currentTimeMillis() - spot.entryTime;
        double hours = duration / (1000.0 * 60 * 60);
        double fee = hours * 5.0; // $5 per hour

        // Free the spot
        spot.isOccupied = false;
        spot.licensePlate = null;
        vehicleToSpot.remove(licensePlate);
        parkedCount--;

        System.out.println("exitVehicle(\"" + licensePlate + "\") → Spot #" + spotIndex +
                " freed, Duration: " + String.format("%.2f", hours) +
                "h, Fee: $" + String.format("%.2f", fee));
    }

    public void getStatistics() {
        double occupancy = (parkedCount * 100.0) / totalSpots;
        double avgProbes = parkedCount > 0 ? (totalProbes * 1.0 / parkedCount) : 0;

        System.out.println("\n=== Parking Lot Statistics ===");
        System.out.println("Occupancy: " + String.format("%.1f", occupancy) + "%");
        System.out.println("Average Probes: " + String.format("%.2f", avgProbes));
        System.out.println("Total Spots: " + totalSpots);
        System.out.println("Parked Vehicles: " + parkedCount);
        System.out.println("Available Spots: " + (totalSpots - parkedCount));
        System.out.println("================================\n");
    }

    public static void main(String[] args) {
        ParkingLot parkingLot = new ParkingLot(500);

        // Park some vehicles
        parkingLot.parkVehicle("ABC-1234");
        parkingLot.parkVehicle("ABC-1235");
        parkingLot.parkVehicle("XYZ-9999");
        parkingLot.parkVehicle("DEF-5678");
        parkingLot.parkVehicle("GHI-9012");

        // Exit a vehicle
        parkingLot.exitVehicle("ABC-1234");

        // Park another vehicle (should use the freed spot)
        parkingLot.parkVehicle("JKL-3456");

        // Get statistics
        parkingLot.getStatistics();
    }
}