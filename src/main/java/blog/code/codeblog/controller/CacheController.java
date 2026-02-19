package blog.code.codeblog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * Limpa todos os caches da aplicação (FLUSHDB)
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        log.info("[clearAllCaches] Clearing all caches (FLUSHDB)");

        try {
            redisConnectionFactory.getConnection().serverCommands().flushDb();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "All caches cleared successfully (FLUSHDB)");
            response.put("timestamp", new Date());

            log.info("[clearAllCaches] All caches cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[clearAllCaches] Error clearing caches: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to clear caches: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Limpa um cache específico pelo nome (pattern: cacheName::*)
     */
    @DeleteMapping("/clear/{cacheName}")
    public ResponseEntity<Map<String, Object>> clearCache(@PathVariable String cacheName) {
        log.info("[clearCache] Clearing cache: {}", cacheName);

        try {
            String pattern = cacheName + "::*";
            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("[clearCache] Deleted {} keys from cache '{}'", keys.size(), cacheName);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache '" + cacheName + "' cleared successfully");
            response.put("keysDeleted", keys != null ? keys.size() : 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[clearCache] Error clearing cache '{}': {}", cacheName, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to clear cache: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Remove uma chave específica de um cache
     */
    @DeleteMapping("/clear/{cacheName}/{key}")
    public ResponseEntity<Map<String, String>> evictKey(
            @PathVariable String cacheName,
            @PathVariable String key) {
        log.info("[evictKey] Evicting key '{}' from cache '{}'", key, cacheName);

        try {
            String fullKey = cacheName + "::" + key;
            Boolean deleted = redisTemplate.delete(fullKey);

            Map<String, String> response = new HashMap<>();
            if (Boolean.TRUE.equals(deleted)) {
                response.put("message", "Key '" + key + "' evicted from cache '" + cacheName + "'");
            } else {
                response.put("message", "Key '" + key + "' not found in cache '" + cacheName + "'");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[evictKey] Error evicting key: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to evict key: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Lista todas as chaves no Redis (use com cuidado em produção)
     */
    @GetMapping("/keys")
    public ResponseEntity<Map<String, Object>> listKeys() {
        try {
            Set<String> keys = redisTemplate.keys("*");

            Map<String, Object> response = new HashMap<>();
            response.put("keys", keys);
            response.put("count", keys != null ? keys.size() : 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[listKeys] Error listing keys: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to list keys: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}


