package org.apache.rocketmq.client.common;

public class FilterExpression {
        private String tag;

        private String filterExpressionType;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getFilterExpressionType() {
            return filterExpressionType;
        }

        public void setFilterExpressionType(String filterExpressionType) {
            this.filterExpressionType = filterExpressionType;
        }
    }