package com.easy.query.test;

import com.easy.query.core.api.EasyEntityQuery;
import com.easy.query.core.proxy.core.track.TrackManager;

/**
 * Easy-Query 差异化更新（Tracking Update）示例
 * 演示如何只更新修改过的字段，提升更新性能
 */
public class TrackingUpdateExample {

    private final EasyEntityQuery easyEntityQuery;

    public TrackingUpdateExample(EasyEntityQuery easyEntityQuery) {
        this.easyEntityQuery = easyEntityQuery;
    }

    /**
     * 1. 基础差异化更新
     * 只更新修改过的字段
     */
    public void basicTrackingUpdate() {
        TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
        try {
            // 开启跟踪
            trackManager.begin();

            // 查询实体（启用跟踪）
            BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
                    .asTracking()
                    .whereById("123")
                    .firstNotNull("博客不存在");

            // 添加到跟踪管理器
            easyEntityQuery.addTracking(blog);

            // 修改属性（只修改了 title）
            blog.setTitle("新标题");

            // 执行更新（只更新 title 字段）
            long rows = easyEntityQuery.updatable(blog).executeRows();

            System.out.println("更新行数: " + rows);

        } finally {
            // 释放跟踪
            trackManager.release();
        }
    }

    /**
     * 2. 多字段差异化更新
     * 只更新被修改的字段，未修改的字段不会出现在 UPDATE 语句中
     */
    public void multiFieldTrackingUpdate() {
        TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
        try {
            trackManager.begin();

            BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
                    .asTracking()
                    .whereById("123")
                    .firstNotNull();

            easyEntityQuery.addTracking(blog);

            // 修改多个字段
            blog.setTitle("更新的标题");
            blog.setScore(blog.getScore().add(new BigDecimal("0.1")));
            blog.setViewCount(blog.getViewCount() + 1);
            // status、star 等字段未修改

            // 只更新 title、score、viewCount 三个字段
            easyEntityQuery.updatable(blog).executeRows();

        } finally {
            trackManager.release();
        }
    }

    /**
     * 3. 批量差异化更新
     * 更新多个实体，每个实体只更新其修改过的字段
     */
    public void batchTrackingUpdate() {
        TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
        try {
            trackManager.begin();

            // 查询多个博客
            List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class)
                    .asTracking()
                    .where(b -> b.status().eq(1))
                    .toList();

            // 添加到跟踪
            easyEntityQuery.addTracking(blogs);

            // 修改不同的字段
            for (BlogEntity blog : blogs) {
                // 有的只修改 title
                if (blog.getScore().compareTo(new BigDecimal("3.0")) < 0) {
                    blog.setTitle("低分博客");
                }
                // 有的只修改 score
                else if (blog.getStar() < 10) {
                    blog.setScore(new BigDecimal("5.0"));
                }
                // 有的修改多个字段
                else {
                    blog.setViewCount(blog.getViewCount() + 1);
                    blog.setStar(blog.getStar() + 1);
                }
            }

            // 批量更新（每个实体只更新其修改的字段）
            for (BlogEntity blog : blogs) {
                easyEntityQuery.updatable(blog).executeRows();
            }

        } finally {
            trackManager.release();
        }
    }

    /**
     * 4. 差异化更新 + 事务
     * 在事务中进行差异化更新，确保数据一致性
     */
    public void trackingUpdateInTransaction() {
        TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
        try {
            trackManager.begin();

            // 开启事务
            easyEntityQuery.beginTransaction();

            try {
                BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
                        .asTracking()
                        .whereById("123")
                        .firstNotNull();

                easyEntityQuery.addTracking(blog);

                // 修改字段
                blog.setTitle("事务中的更新");
                blog.setScore(new BigDecimal("4.5"));

                // 差异化更新
                easyEntityQuery.updatable(blog).executeRows();

                // 提交事务
                easyEntityQuery.commit();

            } catch (Exception e) {
                // 回滚事务
                easyEntityQuery.rollback();
                throw e;
            }

        } finally {
            trackManager.release();
        }
    }

    /**
     * 5. 对比：普通更新 vs 差异化更新
     */
    public void compareUpdateMethods() {
        String blogId = "123";

        // === 普通更新：更新所有字段 ===
        BlogEntity blog1 = easyEntityQuery.queryable(BlogEntity.class)
                .whereById(blogId)
                .firstNotNull();
        blog1.setTitle("新标题");
        easyEntityQuery.updatable(blog1).executeRows();
        // SQL: UPDATE t_blog SET title=?, content=?, url=?, star=?, ... WHERE id=?
        // 所有字段都会更新（即使没有修改）

        // === 差异化更新：只更新修改的字段 ===
        TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
        try {
            trackManager.begin();

            BlogEntity blog2 = easyEntityQuery.queryable(BlogEntity.class)
                    .asTracking()
                    .whereById(blogId)
                    .firstNotNull();
            easyEntityQuery.addTracking(blog2);
            blog2.setTitle("新标题");
            easyEntityQuery.updatable(blog2).executeRows();
            // SQL: UPDATE t_blog SET title=? WHERE id=?
            // 只更新 title 字段

        } finally {
            trackManager.release();
        }
    }

    /**
     * 6. 条件差异化更新
     * 根据条件决定是否更新
     */
    public void conditionalTrackingUpdate() {
        TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
        try {
            trackManager.begin();

            BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
                    .asTracking()
                    .whereById("123")
                    .firstNotNull();

            easyEntityQuery.addTracking(blog);

            // 条件判断：只有满足条件才更新
            if (blog.getViewCount() != null && blog.getViewCount() > 100) {
                blog.setScore(blog.getScore().add(new BigDecimal("0.5")));
            }

            if (blog.getStar() != null && blog.getStar() < 10) {
                blog.setIsTop(true);
            }

            // 只更新满足条件的字段
            easyEntityQuery.updatable(blog).executeRows();

        } finally {
            trackManager.release();
        }
    }

    /**
     * 7. 差异化更新 + 关联对象
     * 更新实体及其关联对象
     */
    public void trackingUpdateWithRelations() {
        TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
        try {
            trackManager.begin();

            // 查询主题（启用跟踪）
            Topic topic = easyEntityQuery.queryable(Topic.class)
                    .asTracking()
                    .whereById("123")
                    .firstNotNull();

            easyEntityQuery.addTracking(topic);

            // 修改主题字段
            topic.setTitle("新主题标题");
            topic.setStars(topic.getStars() + 1);

            // 更新主题
            easyEntityQuery.updatable(topic).executeRows();

            // 查询并更新关联的博客
            List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class)
                    .asTracking()
                    .where(b -> b.topicId().eq(topic.getId()))
                    .toList();

            easyEntityQuery.addTracking(blogs);

            for (BlogEntity blog : blogs) {
                blog.setScore(blog.getScore().add(new BigDecimal("0.1")));
                easyEntityQuery.updatable(blog).executeRows();
            }

        } finally {
            trackManager.release();
        }
    }
}
