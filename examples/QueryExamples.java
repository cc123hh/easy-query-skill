package com.easy.query.test;

import com.easy.query.core.api.EasyEntityQuery;
import com.easy.query.core.enums.propagation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Easy-Query 查询操作示例
 * 演示各种常见的查询场景
 */
public class QueryExamples {

    private final EasyEntityQuery easyEntityQuery;

    public QueryExamples(EasyEntityQuery easyEntityQuery) {
        this.easyEntityQuery = easyEntityQuery;
    }

    /**
     * 1. 基础查询 - 查询单条记录
     */
    public BlogEntity querySingle() {
        return easyEntityQuery.queryable(BlogEntity.class)
                .where(b -> b.id().eq("123"))
                .firstOrNull();
    }

    /**
     * 2. 多条件查询
     */
    public List<BlogEntity> queryMultipleConditions() {
        return easyEntityQuery.queryable(BlogEntity.class)
                .where(b -> {
                    b.title().like("Spring%");
                    b.score().gt(new BigDecimal("3.0"));
                    b.status().eq(1);
                    b.publishTime().ge(LocalDateTime.now().minusDays(30));
                })
                .orderBy(b -> b.publishTime().desc())
                .toList();
    }

    /**
     * 3. IN 查询
     */
    public List<BlogEntity> queryIn() {
        List<String> ids = Arrays.asList("1", "2", "3");
        return easyEntityQuery.queryable(BlogEntity.class)
                .where(b -> b.id().in(ids))
                .toList();
    }

    /**
     * 4. BETWEEN 查询
     */
    public List<BlogEntity> queryBetween() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        
        return easyEntityQuery.queryable(BlogEntity.class)
                .where(b -> b.publishTime().rangeClosed(startTime, endTime))
                .toList();
    }

    /**
     * 5. 分组查询
     */
    public List<BlogStatistics> queryGroupBy() {
        return easyEntityQuery.queryable(BlogEntity.class)
                .where(b -> b.status().eq(1))
                .groupBy(b -> GroupKeys.of(b.topicId()))
                .select(group -> Select.DRAFT.of(
                        group.key1(),
                        group.groupTable().id().count(),
                        group.groupTable().score().avg(),
                        group.groupTable().star().sum()
                ))
                .toList();
    }

    /**
     * 6. 分页查询
     */
    public EasyPageResult<BlogEntity> queryPage(int pageIndex, int pageSize) {
        return easyEntityQuery.queryable(BlogEntity.class)
                .where(b -> b.status().eq(1))
                .orderBy(b -> b.publishTime().desc())
                .toPageResult(pageIndex, pageSize);
    }

    /**
     * 7. 只查询指定字段（性能优化）
     */
    public List<BlogEntity> querySelectFields() {
        return easyEntityQuery.queryable(BlogEntity.class)
                .select(b -> new BlogEntityProxy()
                        .id().set(b.id())
                        .title().set(b.title())
                        .score().set(b.score())
                        // 不查询 content 等大字段
                )
                .toList();
    }

    /**
     * 8. 统计查询
     */
    public long queryCount() {
        return easyEntityQuery.queryable(BlogEntity.class)
                .where(b -> b.status().eq(1))
                .count();
    }

    /**
     * 9. EXISTS 子查询
     */
    public List<Topic> queryExists() {
        return easyEntityQuery.queryable(Topic.class)
                .where(t -> {
                    t.exists(() -> {
                        return easyEntityQuery.queryable(BlogEntity.class)
                                .where(b -> b.topicId().eq(t.id())
                                           .and(b.status().eq(1)))
                                .select(b -> b.id());
                    });
                })
                .toList();
    }

    /**
     * 10. UNION 查询
     */
    public List<BlogEntity> queryUnion() {
        EntityQueryable<BlogEntityProxy, BlogEntity> q1 = easyEntityQuery
                .queryable(BlogEntity.class)
                .where(b -> b.status().eq(1));

        EntityQueryable<BlogEntityProxy, BlogEntity> q2 = easyEntityQuery
                .queryable(BlogEntity.class)
                .where(b -> b.status().eq(2));

        return q1.union(q2).toList();
    }

    /**
     * 11. 动态表名查询
     */
    public List<BlogEntity> queryDynamicTable(String tableName) {
        return easyEntityQuery.queryable(BlogEntity.class)
                .asTable(t -> tableName)
                .where(b -> b.status().eq(1))
                .toList();
    }

    /**
     * 12. 隐式 CASE WHEN 查询
     */
    public List<StatisticsVO> queryCaseWhen() {
        return easyEntityQuery.queryable(BlogEntity.class)
                .select(b -> Select.DRAFT.of(
                        b.id().count().filter(() -> b.status().eq(1)),
                        b.id().count().filter(() -> b.status().eq(2)),
                        b.score().avg().filter(() -> b.status().eq(1))
                ))
                .toList();
    }
}
