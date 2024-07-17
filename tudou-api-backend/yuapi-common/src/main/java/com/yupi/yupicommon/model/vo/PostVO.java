package com.yupi.yupicommon.model.vo;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.io.Serializable;


/**
 * 帖子视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostVO extends Page {

    private Boolean hasThumb;

    private static final long serialVersionUID = 1L;
}
