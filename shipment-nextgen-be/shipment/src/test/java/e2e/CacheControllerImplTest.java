package e2e;

import com.quincus.shipment.impl.web.CacheControllerImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheControllerImplTest {

    @InjectMocks
    private CacheControllerImpl cacheController;

    @Mock
    private CacheManager cacheManager;


    @Test
    void testGetCacheInfo() {
        when(cacheManager.getCacheNames()).thenReturn(List.of("cache1", "cache2"));
        Cache cache1 = mock(Cache.class);
        Cache cache2 = mock(Cache.class);
        when(cacheManager.getCache("cache1")).thenReturn(cache1);
        when(cacheManager.getCache("cache2")).thenReturn(cache2);

        ResponseEntity<Map<String, Object>> response = cacheController.getCacheInfo();

        verify(cacheManager, times(1)).getCacheNames();
        verify(cacheManager, times(1)).getCache("cache1");
        verify(cacheManager, times(1)).getCache("cache2");

        assertThat(response.getBody()).hasSize(2)
                .containsEntry("cache1", cache1)
                .containsEntry("cache2", cache2);
    }

    @Test
    void testDeleteCacheByName() {
        String cacheName = "testCache";
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(cacheName)).thenReturn(cache);

        ResponseEntity<String> response = cacheController.deleteCacheByName(cacheName);

        verify(cacheManager, times(1)).getCache(cacheName);
        verify(cache, times(1)).clear();

        assertThat(response.getBody()).isEqualTo(String.format("Cache `%s` has been successfully removed!", cacheName));
    }

    @Test
    void testDeleteCacheByNameWhenCacheNotFound() {
        String cacheName = "unknownCache";
        when(cacheManager.getCache(cacheName)).thenReturn(null);

        ResponseEntity<String> response = cacheController.deleteCacheByName(cacheName);

        verify(cacheManager, times(1)).getCache(cacheName);

        assertThat(response.getBody()).isEqualTo(String.format("No cache `%s` found!", cacheName));
    }

}
