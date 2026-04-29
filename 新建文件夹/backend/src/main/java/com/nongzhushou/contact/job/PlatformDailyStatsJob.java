package com.nongzhushou.contact.job;

import com.nongzhushou.contact.mapper.ContactRevealMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 公益平台每日运营数据统计 Job
 *
 * 每天凌晨 1:00 执行，将前一日的公益对接数据聚合写入
 * platform_daily_stats 快照表，供 Admin 看板和公益首页高效读取。
 *
 * 设计原则：
 * - 幂等执行（ON DUPLICATE KEY UPDATE），重跑不产生重复数据
 * - 失败仅记录日志，不影响主业务
 */
@Component
public class PlatformDailyStatsJob {

    private static final Logger log = LoggerFactory.getLogger(PlatformDailyStatsJob.class);

    @Autowired
    private ContactRevealMapper revealMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 每日凌晨 1:00 — 统计前一天的公益对接数据
     *
     * cron 格式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void computeDailyStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);
        log.info("[公益统计Job] 开始统计日期: {}", dateStr);

        try {
            // 1. 当日新增求助需求数
            int totalDemands = revealMapper.countTodayDemands(yesterday);

            // 2. 当日联系成功次数（contact_reveal_log 记录数）
            int totalConnected = revealMapper.countByZoneAndDate(null, yesterday, yesterday);

            // 3. 截至当日注册服务者总数
            int totalOperators = revealMapper.countTotalOperators();

            // 4. 当日有活动记录的片区数
            int coveredZones = revealMapper.countCoveredZones(yesterday);

            // 5. 写入快照表（幂等）
            jdbcTemplate.update("""
                    INSERT INTO platform_daily_stats
                        (stat_date, zone_id, total_demands, total_connected, total_operators, covered_zones)
                    VALUES (?, NULL, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        total_demands    = VALUES(total_demands),
                        total_connected  = VALUES(total_connected),
                        total_operators  = VALUES(total_operators),
                        covered_zones    = VALUES(covered_zones)
                    """,
                    yesterday, totalDemands, totalConnected, totalOperators, coveredZones
            );

            log.info("[公益统计Job] {} 统计完成 — 需求:{} 对接:{} 服务者:{} 覆盖片区:{}",
                    dateStr, totalDemands, totalConnected, totalOperators, coveredZones);

        } catch (Exception e) {
            log.error("[公益统计Job] {} 统计失败: {}", dateStr, e.getMessage(), e);
            // 失败不抛出，不影响主业务
        }
    }

    /**
     * 应用启动时补跑近7天缺失的统计数据（冷启动保护）
     *
     * 延迟 30 秒执行，避免与应用启动争抢资源
     */
    @Scheduled(initialDelay = 30_000, fixedDelay = Long.MAX_VALUE)
    public void backfillRecentStats() {
        log.info("[公益统计Job] 启动补跑，检查近7天数据...");
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= 7; i++) {
            LocalDate date = today.minusDays(i);
            try {
                Integer exists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM platform_daily_stats WHERE stat_date = ? AND zone_id IS NULL",
                        Integer.class, date
                );
                if (exists == null || exists == 0) {
                    log.info("[公益统计Job] 补跑缺失日期: {}", date);
                    computeForDate(date);
                }
            } catch (Exception e) {
                log.warn("[公益统计Job] 补跑 {} 失败: {}", date, e.getMessage());
            }
        }
    }

    // ─── 内部方法 ───

    private void computeForDate(LocalDate date) {
        int totalDemands   = revealMapper.countTodayDemands(date);
        int totalConnected = revealMapper.countByZoneAndDate(null, date, date);
        int totalOperators = revealMapper.countTotalOperators();
        int coveredZones   = revealMapper.countCoveredZones(date);

        jdbcTemplate.update("""
                INSERT INTO platform_daily_stats
                    (stat_date, zone_id, total_demands, total_connected, total_operators, covered_zones)
                VALUES (?, NULL, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    total_demands   = VALUES(total_demands),
                    total_connected = VALUES(total_connected),
                    total_operators = VALUES(total_operators),
                    covered_zones   = VALUES(covered_zones)
                """,
                date, totalDemands, totalConnected, totalOperators, coveredZones
        );
    }
}
