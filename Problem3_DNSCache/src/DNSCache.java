import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

class DNSEntry {
    String domain;
    String ipAddress;
    long timestamp;
    long ttl; // Time to live in seconds

    public DNSEntry(String domain, String ipAddress, long ttl) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.timestamp = System.currentTimeMillis();
        this.ttl = ttl * 1000; // Convert to milliseconds
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > ttl;
    }
}

public class DNSCache {
    private ConcurrentHashMap<String, DNSEntry> cache;
    private int cacheHits;
    private int cacheMisses;
    private long totalLookupTime;
    private int maxCacheSize;

    public DNSCache(int maxCacheSize) {
        this.cache = new ConcurrentHashMap<>();
        this.cacheHits = 0;
        this.cacheMisses = 0;
        this.totalLookupTime = 0;
        this.maxCacheSize = maxCacheSize;
    }

    public String resolve(String domain) {
        long startTime = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            cacheHits++;
            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            totalLookupTime += elapsed;
            System.out.println("Cache HIT → " + entry.ipAddress + " (retrieved in " + elapsed + "ms)");
            return entry.ipAddress;
        } else {
            cacheMisses++;
            // Simulate upstream DNS query
            String ipAddress = queryUpstreamDNS(domain);
            long ttl = 300; // Default TTL

            if (cache.size() >= maxCacheSize) {
                cleanExpiredEntries();
            }

            if (cache.size() < maxCacheSize) {
                cache.put(domain, new DNSEntry(domain, ipAddress, ttl));
            }

            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            totalLookupTime += elapsed;
            System.out.println("Cache MISS → Query upstream → " + ipAddress + " (TTL: " + ttl + "s)");
            return ipAddress;
        }
    }

    private String queryUpstreamDNS(String domain) {
        // Simulate DNS lookup - in real scenario, this would query actual DNS server
        try {
            Thread.sleep(100); // Simulate network delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Return mock IP addresses
        HashMap<String, String> dnsRecords = new HashMap<>();
        dnsRecords.put("google.com", "172.217.14.206");
        dnsRecords.put("facebook.com", "157.240.1.35");
        dnsRecords.put("amazon.com", "54.239.28.85");

        return dnsRecords.getOrDefault(domain, "93.184.216.34");
    }

    private void cleanExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public void getCacheStats() {
        int total = cacheHits + cacheMisses;
        double hitRate = total > 0 ? (cacheHits * 100.0 / total) : 0;
        double avgLookupTime = total > 0 ? (totalLookupTime * 1.0 / total) : 0;

        System.out.println("Cache Statistics:");
        System.out.println("Hit Rate: " + String.format("%.2f", hitRate) + "%");
        System.out.println("Average Lookup Time: " + String.format("%.2f", avgLookupTime) + "ms");
        System.out.println("Cache Size: " + cache.size() + "/" + maxCacheSize);
        System.out.println("Total Hits: " + cacheHits);
        System.out.println("Total Misses: " + cacheMisses);
    }

    public static void main(String[] args) {
        DNSCache dnsCache = new DNSCache(1000);

        System.out.println("First request to google.com:");
        dnsCache.resolve("google.com");

        System.out.println("\nSecond request to google.com:");
        dnsCache.resolve("google.com");

        System.out.println("\nRequest to facebook.com:");
        dnsCache.resolve("facebook.com");

        System.out.println("\nRequest to amazon.com:");
        dnsCache.resolve("amazon.com");

        System.out.println("\nThird request to google.com:");
        dnsCache.resolve("google.com");

        System.out.println();
        dnsCache.getCacheStats();
    }
}