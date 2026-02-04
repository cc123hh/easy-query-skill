package com.easy.query.test;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Navigate;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 博客实体示例
 * 演示 @EntityProxy、@Table、@Navigate 注解的使用
 */
@Data
@Table("t_blog")
@EntityProxy
public class BlogEntity implements ProxyEntityAvailable<BlogEntity, BlogEntityProxy> {

    /**
     * 主键 ID
     */
    @Column(primaryKey = true)
    private String id;

    /**
     * 所属主题 ID（多对一关系）
     */
    private String topicId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容（大字段，建议查询时忽略）
     */
    private String content;

    /**
     * 博客链接
     */
    private String url;

    /**
     * 点赞数
     */
    private Integer star;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 评分
     */
    private BigDecimal score;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序值
     */
    private BigDecimal order;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 是否删除（逻辑删除）
     */
    @LogicDelete(strategy = LogicDeleteStrategyEnum.BOOLEAN)
    private Boolean deleted;

    /**
     * 所属主题（多对一导航）
     */
    @Navigate(value = RelationTypeEnum.ManyToOne,
              selfProperty = "topicId",
              targetProperty = "id")
    private Topic topic;

    /**
     * 评论列表（一对多导航）
     */
    @Navigate(value = RelationTypeEnum.OneToMany,
              selfProperty = "id",
              targetProperty = "blogId",
              subQueryToGroupJoin = false)
    private List<Comment> comments;

    /**
     * 标签列表（多对多导航）
     */
    @Navigate(value = RelationTypeEnum.ManyToMany,
              selfProperty = "id",
              targetProperty = "blogId",
              mappingClass = BlogTag.class)
    private List<Tag> tags;

    /**
     * 分类（一对一导航）
     */
    @Navigate(value = RelationTypeEnum.OneToOne,
              selfProperty = "id",
              targetProperty = "blogId")
    private BlogCategory category;
}
