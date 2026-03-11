import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryManager {
    private ConcurrentHashMap<String, Integer> inventory;
    private HashMap<String, Queue<String>> waitingList;

    public InventoryManager() {
        inventory = new ConcurrentHashMap<>();
        waitingList = new HashMap<>();
    }

    public void addProduct(String productId, int quantity) {
        inventory.put(productId, quantity);
        waitingList.put(productId, new LinkedList<>());
    }

    public synchronized boolean purchaseItem(String productId, String userId) {
        Integer stock = inventory.get(productId);
        if (stock != null && stock > 0) {
            inventory.put(productId, stock - 1);
            return true;
        } else {
            waitingList.get(productId).offer(userId);
            return false;
        }
    }

    public int checkStock(String productId) {
        return inventory.getOrDefault(productId, 0);
    }

    public int getWaitingListPosition(String productId, String userId) {
        Queue<String> queue = waitingList.get(productId);
        if (queue == null) return -1;

        int position = 1;
        for (String id : queue) {
            if (id.equals(userId)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public synchronized void restock(String productId, int quantity) {
        int currentStock = inventory.getOrDefault(productId, 0);
        inventory.put(productId, currentStock + quantity);

        // Process waiting list
        Queue<String> queue = waitingList.get(productId);
        while (queue != null && !queue.isEmpty() && quantity > 0) {
            queue.poll();
            quantity--;
        }
    }

    public static void main(String[] args) {
        InventoryManager manager = new InventoryManager();

        // Add product with 100 units
        manager.addProduct("IPHONE15_256GB", 100);

        // Check stock
        System.out.println("checkStock(\"IPHONE15_256GB\"): " + manager.checkStock("IPHONE15_256GB") + " units available");

        // Purchase items
        System.out.println("purchaseItem(\"IPHONE15_256GB\", user123): " + manager.purchaseItem("IPHONE15_256GB", "user123"));
        System.out.println("Stock after purchase: " + manager.checkStock("IPHONE15_256GB"));

        // Simulate multiple purchases
        for (int i = 0; i < 100; i++) {
            manager.purchaseItem("IPHONE15_256GB", "user" + i);
        }

        System.out.println("Stock after 100 purchases: " + manager.checkStock("IPHONE15_256GB"));
        System.out.println("Next purchase (should fail): " + manager.purchaseItem("IPHONE15_256GB", "user999"));
        System.out.println("Waiting list position: " + manager.getWaitingListPosition("IPHONE15_256GB", "user999"));
    }
}