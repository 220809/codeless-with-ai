package com.dingzk.codeless.common;

import lombok.Data;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用分页查询请求
 *
 * @author ding
 * @date 2025/12/3 20:03
 */
@Data
public class SearchRequest implements Serializable {
    /**
     * 页码
     */
    private Integer pageNum = 1;
    /**
     * 每页条数
     */
    private Integer pageSize = 10;
    /**
     * 排序字段
     */
    private String sortField = "id";
    /**
     * 顺序规则
     */
    private String sortOrder = SortOrder.ASC.getOrder();

    @Getter
    public enum SortOrder {
        ASC("asc"), DESC("desc");
        private final String order;

        SortOrder(String order) {
            this.order = order;
        }

        public static SortOrder fromString(String order) {
            if (order == null) {
                return null;
            }
            for (SortOrder sortOrder : SortOrder.values()) {
                if (sortOrder.order.equalsIgnoreCase(order)) {
                    return sortOrder;
                }
            }
            return null;
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;
}
