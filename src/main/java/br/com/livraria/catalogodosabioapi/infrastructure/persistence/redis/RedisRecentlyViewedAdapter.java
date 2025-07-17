package br.com.livraria.catalogodosabioapi.infrastructure.persistence.redis;

import br.com.livraria.catalogodosabioapi.core.usecase.boundary.out.RecentlyViewedPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class RedisRecentlyViewedAdapter implements RecentlyViewedPort {

    private static final String KEY_PREFIX = "recently_viewed:";
    private static final int MAX_ITEMS = 10;
    private static final Duration TTL = Duration.ofDays(5);

    private final RedisTemplate<String, String> redisTemplate;
    private final ListOperations<String, String> listOps;

    public RedisRecentlyViewedAdapter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.listOps = redisTemplate.opsForList();
        log.info("RedisRecentlyViewedAdapter inicializado. ListOperations obtido do RedisTemplate.");
    }

    @Override
    public void save(String clientId, String bookId) {
        String key = KEY_PREFIX + clientId;
        log.debug("A guardar visualização no Redis. Chave: {}, Livro ID: {}", key, bookId);

        listOps.remove(key, 0, bookId);

        listOps.leftPush(key, bookId);

        listOps.trim(key, 0, MAX_ITEMS - 1);

        redisTemplate.expire(key, TTL);
    }

    @Override
    public List<String> findByClientId(String clientId) {
        String key = KEY_PREFIX + clientId;
        log.debug("Buscando visualizados recentemente no Redis. Chave: {}", key);

        // Busca todos os elementos da lista (de 0 a -1 significa "todos")
        List<String> bookIds = listOps.range(key, 0, -1);

        log.debug("Busca de visualizados recentes retornou {} registros",bookIds != null ? bookIds.size() : 0);
        return bookIds != null ? bookIds : Collections.emptyList();
    }
}
