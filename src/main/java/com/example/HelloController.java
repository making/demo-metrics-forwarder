package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
public class HelloController {
    private final JdbcTemplate jdbcTemplate;
    private final CounterService counterService;
    private final GaugeService gaugeService;

    public HelloController(JdbcTemplate jdbcTemplate, CounterService counterService, GaugeService gaugeService) {
        this.jdbcTemplate = jdbcTemplate;
        this.counterService = counterService;
        this.gaugeService = gaugeService;
    }

    @GetMapping
    @Transactional
    public List<Map<String, Object>> hello(@Value("#{request.remoteAddr}") String remoteAddr) {
        this.counterService.increment("hello");

        long start = System.currentTimeMillis();
        this.jdbcTemplate.update("INSERT INTO visits (timestamp, userip) VALUES (?, ?)", Timestamp.from(Instant.now()), remoteAddr);
        long elapsed = System.currentTimeMillis() - start;
        this.gaugeService.submit("insert.elapsed", elapsed);

        List<Map<String, Object>> result = this.jdbcTemplate.queryForList("SELECT timestamp, userip FROM visits ORDER BY timestamp DESC LIMIT ?", 10);
        return result;
    }
}
