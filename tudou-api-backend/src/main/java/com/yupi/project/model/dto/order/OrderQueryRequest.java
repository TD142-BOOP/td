package com.yupi.project.model.dto.order;



import com.yupi.yupicommon.common.PageRequest;
import lombok.Data;

import java.io.Serializable;


@Data
public class OrderQueryRequest extends PageRequest implements Serializable {
    private String type;
}