import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

class PageView {
    String url;
    String userId;
    String source;
    long timestamp;

    public PageView(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
        this.timestamp = System.currentTimeMillis();
    }
}

public class AnalyticsDashboard {
    private HashMap<String, Integer> pageViews;
    private HashMap<String, HashSet<String>> uniqueVisitors;
    private HashMap<String, Integer> trafficSources;
    private int totalVisits;

    public AnalyticsDashboard() {
        this.pageViews = new HashMap<>();
        this.uniqueVisitors = new HashMap<>();
        this.trafficSources = new HashMap<>();
        this.totalVisits = 0;
    }

    public void processEvent(PageView event) {
        // Count page views
        pageViews.put(event.url, pageViews.getOrDefault(event.url, 0) + 1);

        // Track unique visitors
        uniqueVisitors.computeIfAbsent(event.url, k -> new HashSet<>()).add(event.userId);

        // Track traffic sources
        trafficSources.put(event.source, trafficSources.getOrDefault(event.source, 0) + 1);

        totalVisits++;
    }

    public void getDashboard() {
        System.out.println("=== REAL-TIME ANALYTICS DASHBOARD ===\n");

        // Top Pages
        System.out.println("Top Pages:");
        List<Map.Entry<String, Integer>> sortedPages = new ArrayList<>(pageViews.entrySet());
        sortedPages.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortedPages) {
            if (rank > 10) break;
            int uniqueCount = uniqueVisitors.getOrDefault(entry.getKey(), new HashSet<>()).size();
            System.out.println(rank + ". " + entry.getKey() + " - " + entry.getValue() +
                    " views (" + uniqueCount + " unique)");
            rank++;
        }

        System.out.println();

        // Traffic Sources
        System.out.println("Traffic Sources:");
        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / totalVisits;
            System.out.println(entry.getKey() + ": " + String.format("%.1f", percentage) + "%");
        }

        System.out.println();
        System.out.println("Total Visits: " + totalVisits);
        System.out.println("=====================================\n");
    }

    public static void main(String[] args) {
        AnalyticsDashboard dashboard = new AnalyticsDashboard();

        // Simulate page views
        dashboard.processEvent(new PageView("/article/breaking-news", "user_123", "google"));
        dashboard.processEvent(new PageView("/article/breaking-news", "user_456", "facebook"));
        dashboard.processEvent(new PageView("/article/breaking-news", "user_789", "google"));
        dashboard.processEvent(new PageView("/sports/championship", "user_111", "direct"));
        dashboard.processEvent(new PageView("/sports/championship", "user_222", "google"));
        dashboard.processEvent(new PageView("/sports/championship", "user_333", "facebook"));
        dashboard.processEvent(new PageView("/home", "user_444", "direct"));
        dashboard.processEvent(new PageView("/home", "user_555", "google"));
        dashboard.processEvent(new PageView("/article/tech-news", "user_666", "twitter"));
        dashboard.processEvent(new PageView("/article/breaking-news", "user_777", "direct"));

        // Display dashboard
        dashboard.getDashboard();
    }
}