import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class UsernameChecker {
    private HashMap<String, String> usernameMap;
    private HashMap<String, Integer> attemptFrequency;

    public UsernameChecker() {
        usernameMap = new HashMap<>();
        attemptFrequency = new HashMap<>();
    }

    public boolean checkAvailability(String username) {
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
        return !usernameMap.containsKey(username);
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add(username + "1");
        suggestions.add(username + "2");
        suggestions.add(username.replace("_", "."));
        return suggestions;
    }

    public String getMostAttempted() {
        String mostAttempted = "";
        int maxCount = 0;
        for (var entry : attemptFrequency.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }
        return mostAttempted;
    }

    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();
        System.out.println("checkAvailability(\"john_doe\"): " + checker.checkAvailability("john_doe"));
        System.out.println("suggestAlternatives(\"john_doe\"): " + checker.suggestAlternatives("john_doe"));
    }
}