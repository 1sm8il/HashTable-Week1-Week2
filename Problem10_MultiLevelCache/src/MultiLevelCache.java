import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

class Video {
    String id;
    String title;
    String content;
    int accessCount;
    long lastAccessTime;

    public Video(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.accessCount = 0;
        this.lastAccessTime = System.currentTimeMillis();
    }
}

class CacheStats {
    int hits;
    int misses;
    long totalTime;

    public void recordHit(long time) {
        hits++;
        totalTime += time;
    }

    public void recordMiss(long time) {
        misses++;
        totalTime += time;
    }

    public double getHitRate() {
        int total = hits + misses;
        return total > 0 ? (hits * 100.0 / total) : 0;
    }

    public double getAvgTime() {
        int total = hits + misses;
        return total > 0 ? (totalTime * 1.0 / total) : 0;
    }
}

public class MultiLevelCache {
    private LinkedHashMap<String, Video> l1Cache;
    private HashMap<String, Video> l2Cache;
    private HashMap<String, Video> l3Database;
    private CacheStats l1Stats;
    private CacheStats l2Stats;
    private CacheStats l3Stats;
    private final int L1_MAX_SIZE = 10;
    private final int L2_MAX_SIZE = 50;
    private final int PROMOTION_THRESHOLD = 3;

    public MultiLevelCache() {
        // L1: Access-order LinkedHashMap for LRU eviction
        this.l1Cache = new LinkedHashMap<String, Video>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Video> eldest) {
                return size() > L1_MAX_SIZE;
            }
        };

        this.l2Cache = new HashMap<>();
        this.l3Database = new HashMap<>();
        this.l1Stats = new CacheStats();
        this.l2Stats = new CacheStats();
        this.l3Stats = new CacheStats();
    }

    public Video getVideo(String videoId) {
        long startTime = System.nanoTime();

        // Check L1 Cache
        Video video = l1Cache.get(videoId);
        if (video != null) {
            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            l1Stats.recordHit(elapsed);
            video.accessCount++;
            video.lastAccessTime = System.currentTimeMillis();
            System.out.println("L1 Cache HIT → " + video.title + " (" + elapsed + "ms)");
            return video;
        }

        // Check L2 Cache
        long l1Time = (System.nanoTime() - startTime) / 1_000_000;
        video = l2Cache.get(videoId);
        if (video != null) {
            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            l2Stats.recordHit(elapsed);
            video.accessCount++;
            video.lastAccessTime = System.currentTimeMillis();

            // Promote to L1
            l2Cache.remove(videoId);
            l1Cache.put(videoId, video);

            System.out.println("L1 Cache MISS (" + l1Time + "ms) → L2 Cache HIT (" + elapsed + "ms) → Promoted to L1");
            return video;
        }

        // Check L3 Database
        long l2Time = (System.nanoTime() - startTime) / 1_000_000;
        video = l3Database.get(videoId);
        if (video != null) {
            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            l3Stats.recordHit(elapsed);
            video.accessCount++;
            video.lastAccessTime = System.currentTimeMillis();

            // Add to L2
            if (l2Cache.size() >= L2_MAX_SIZE) {
                // Simple eviction: remove random entry
                l2Cache.keySet().remove(l2Cache.keySet().iterator().next());
            }
            l2Cache.put(videoId, video);

            System.out.println("L1 Cache MISS (" + l1Time + "ms) → L2 Cache MISS (" + l2Time + "ms) → " +
                    "L3 Database HIT (" + elapsed + "ms) → Added to L2");
            return video;
        }

        // Cache miss at all levels
        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        l3Stats.recordMiss(elapsed);
        System.out.println("Cache MISS at all levels (" + elapsed + "ms) → Video not found");
        return null;
    }

    public void addVideoToDatabase(Video video) {
        l3Database.put(video.id, video);
    }

    public void getStatistics() {
        System.out.println("\n=== Multi-Level Cache Statistics ===");
        System.out.println("L1 Cache:");
        System.out.println("  Hit Rate: " + String.format("%.1f", l1Stats.getHitRate()) + "%");
        System.out.println("  Avg Time: " + String.format("%.2f", l1Stats.getAvgTime()) + "ms");
        System.out.println("  Size: " + l1Cache.size() + "/" + L1_MAX_SIZE);

        System.out.println("\nL2 Cache:");
        System.out.println("  Hit Rate: " + String.format("%.1f", l2Stats.getHitRate()) + "%");
        System.out.println("  Avg Time: " + String.format("%.2f", l2Stats.getAvgTime()) + "ms");
        System.out.println("  Size: " + l2Cache.size() + "/" + L2_MAX_SIZE);

        System.out.println("\nL3 Database:");
        System.out.println("  Hit Rate: " + String.format("%.1f", l3Stats.getHitRate()) + "%");
        System.out.println("  Avg Time: " + String.format("%.2f", l3Stats.getAvgTime()) + "ms");
        System.out.println("  Size: " + l3Database.size());

        // Overall stats
        int totalHits = l1Stats.hits + l2Stats.hits + l3Stats.hits;
        int totalRequests = l1Stats.hits + l1Stats.misses + l2Stats.hits + l2Stats.misses + l3Stats.hits + l3Stats.misses;
        double overallHitRate = totalRequests > 0 ? (totalHits * 100.0 / totalRequests) : 0;

        long totalTime = l1Stats.totalTime + l2Stats.totalTime + l3Stats.totalTime;
        double avgTime = totalRequests > 0 ? (totalTime * 1.0 / totalRequests) : 0;

        System.out.println("\nOverall:");
        System.out.println("  Hit Rate: " + String.format("%.1f", overallHitRate) + "%");
        System.out.println("  Avg Time: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("=======================================\n");
    }

    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache();

        // Add videos to database
        cache.addVideoToDatabase(new Video("video_123", "Introduction to Java", "Content 123"));
        cache.addVideoToDatabase(new Video("video_456", "Advanced Python", "Content 456"));
        cache.addVideoToDatabase(new Video("video_789", "Web Development", "Content 789"));
        cache.addVideoToDatabase(new Video("video_999", "Machine Learning", "Content 999"));

        System.out.println("=== Testing Multi-Level Cache ===\n");

        // First access - should go to L3
        System.out.println("First access to video_123:");
        cache.getVideo("video_123");

        // Second access - should be in L1
        System.out.println("\nSecond access to video_123:");
        cache.getVideo("video_123");

        // Access another video
        System.out.println("\nFirst access to video_456:");
        cache.getVideo("video_456");

        // Access video_789
        System.out.println("\nFirst access to video_789:");
        cache.getVideo("video_789");

        // Access non-existent video
        System.out.println("\nAccess non-existent video_000:");
        cache.getVideo("video_000");

        // Show statistics
        cache.getStatistics();
    }
}