package com.easy.query.test;

import com.easy.query.core.api.EasyEntityQuery;

import java.math.BigDecimal;
import java.util.List;

/**
 * Easy-Query 多表 Join 查询示例
 */
public class JoinExamples {

    private final EasyEntityQuery easyEntityQuery;

    public JoinExamples(EasyEntityQuery easyEntityQuery) {
        this.easyEntityQuery = easyEntityQuery;
    }

    /**
     * 1. Left Join 基础示例
     */
    public List<Topic> leftJoinBasic() {
        return easyEntityQuery.queryable(Topic.class)
                .leftJoin(BlogEntity.class, (t, b) -> t.id().eq(b.topicId()))
                .where((t, b) -> {
                    t.title().like("Java%");
                    b.status().eq(1);
                })
                .toList();
    }

    /**
     * 2. Inner Join 示例
     */
    public List<Topic> innerJoinBasic() {
        return easyEntityQuery.queryable(Topic.class)
                .innerJoin(BlogEntity.class, (t, b) -> t.id().eq(b.topicId()))
                .where((t, b) -> b.score().gt(new BigDecimal("4.0")))
                .toList();
    }

    /**
     * 3. Right Join 示例
     */
    public List<BlogEntity> rightJoinBasic() {
        return easyEntityQuery.queryable(BlogEntity.class)
                .rightJoin(Topic.class, (b, t) -> b.topicId().eq(t.id()))
                .where((b, t) -> t.stars().gt(100))
                .toList();
    }

    /**
     * 4. 多表 Join（3 个表）
     */
    public List<BlogEntity> multiTableJoin() {
        return easyEntityQuery.queryable(BlogEntity.class)
                .leftJoin(Topic.class, (b, t) -> b.topicId().eq(t.id()))
                .leftJoin(SysUser.class, (b, t, u) -> b.createBy().eq(u.id()))
                .where((b, t, u) -> {
                    t.title().like("Spring%");
                    u.username().eq("admin");
                    b.status().eq(1);
                })
                .toList();
    }

    /**
     * 5. Join + Group By
     */
    public List<TopicStatistics> joinWithGroupBy() {
        return easyEntityQuery.queryable(Topic.class)
                .leftJoin(BlogEntity.class, (t, b) -> t.id().eq(b.topicId()))
                .where((t, b) -> b.deleted().eq(false))
                .groupBy((t, b) -> GroupKeys.TABLE2.of(t.id(), t.title()))
                .select((t, b, g) -> new TopicStatisticsProxy()
                        .topicId().set(g.key1())
                        .topicTitle().set(g.key2())
                        .blogCount().set(g.group().t2().id().count())
                        .avgScore().set(g.group().t2().score().avg())
                        .totalStars().set(g.group().t2().star().sum())
                )
                .toList();
    }

    /**
     * 6. Join + 分页
     */
    public EasyPageResult<BlogEntity> joinWithPagination() {
        return easyEntityQuery.queryable(Topic.class)
                .leftJoin(BlogEntity.class, (t, b) -> t.id().eq(b.topicId()))
                .where((t, b) -> b.status().eq(1))
                .orderBy((t, b) -> b.publishTime().desc())
                .select((t, b) -> b)
                .toPageResult(1, 20);
    }

    /**
     * 7. 隐式 Join（自动生成 LEFT JOIN）
     */
    public List<BlogEntity> implicitJoin() {
        return easyEntityQuery.queryable(BlogEntity.class)
                .where(b -> {
                    // 自动生成 LEFT JOIN t_topic
                    b.topic().title().like("Java%");
                    // 自动生成 LEFT JOIN t_sys_user
                    b.author().username().eq("admin");
                })
                .toList();
    }

    /**
     * 8. 隐式 Join + 排序
     */
    public List<BlogEntity> implicitJoinWithOrderBy() {
        return easyEntityQuery.queryable(BlogEntity.class)
                .where(b -> b.topic().stars().gt(100))
                .orderBy(b -> {
                    b.topic().stars().desc();
                    b.publishTime().desc();
                })
                .toList();
    }

    /**
     * 9. Join 子查询
     */
    public List<BlogEntity> joinWithSubquery() {
        EntityQueryable<SysUserProxy, SysUser> activeUsers = easyEntityQuery
                .queryable(SysUser.class)
                .where(u -> u.status().eq(1));

        return easyEntityQuery.queryable(BlogEntity.class)
                .innerJoin(activeUsers, (b, u) -> b.createBy().eq(u.id()))
                .where((b, u) -> b.status().eq(1))
                .toList();
    }

    /**
     * 10. Join 聚合查询
     */
    public List<TopicWithBlogStats> joinWithAggregation() {
        return easyEntityQuery.queryable(Topic.class)
                .leftJoin(BlogEntity.class, (t, b) -> t.id().eq(b.topicId()))
                .where((t, b) -> b.deleted().eq(false))
                .groupBy((t, b) -> GroupKeys.TABLE1.of(t.id()))
                .select((t, b, g) -> new TopicWithBlogStatsProxy()
                        .topicId().set(g.key())
                        .topicTitle().set(g.group().t1().title())
                        .blogCount().set(g.group().t2().id().count())
                        .maxScore().set(g.group().t2().score().max())
                        .avgScore().set(g.group().t2().score().avg())
                )
                .toList();
    }
}
