package com.tencent.supersonic.chat.server.processor.parse;

import com.tencent.supersonic.chat.server.plugin.PluginQueryManager;
import com.tencent.supersonic.chat.server.pojo.ParseContext;
import com.tencent.supersonic.headless.api.pojo.SchemaElement;
import com.tencent.supersonic.headless.api.pojo.SemanticParseInfo;
import com.tencent.supersonic.headless.api.pojo.request.QueryFilter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ParseInfoFormatProcessor formats parse info to make it more readable to the users.
 **/
public class ParseInfoFormatProcessor implements ParseResultProcessor {
    @Override
    public void process(ParseContext parseContext) {
        parseContext.getResponse().getSelectedParses().forEach(p -> {
            if (PluginQueryManager.isPluginQuery(p.getQueryMode())
                    || "PLAIN_TEXT".equals(p.getQueryMode())) {
                return;
            }

            formatNL2SQLParseInfo(p);
        });
    }

    private static void formatNL2SQLParseInfo(SemanticParseInfo parseInfo) {
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("**数据集:** ").append(parseInfo.getDataSet().getName()).append(" ");
        List<String> metricNames = parseInfo.getMetrics().stream().map(SchemaElement::getName)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(metricNames)) {
            textBuilder.append(" **指标:** ").append(String.join(",", metricNames));
        }
        List<String> dimensionNames = parseInfo.getDimensions().stream().map(SchemaElement::getName)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dimensionNames)) {
            textBuilder.append(" **维度:** ").append(String.join(",", dimensionNames));
        }
        if (parseInfo.getDateInfo() != null) {
            textBuilder.append(" **数据时间:** ").append(parseInfo.getDateInfo().getStartDate())
                    .append("~").append(parseInfo.getDateInfo().getEndDate()).append(" ");
        }
        if (!CollectionUtils.isEmpty(parseInfo.getDimensionFilters())
                || CollectionUtils.isEmpty(parseInfo.getMetricFilters())) {
            Set<QueryFilter> queryFilters = parseInfo.getDimensionFilters();
            queryFilters.addAll(parseInfo.getMetricFilters());
            if (!queryFilters.isEmpty()) {
                textBuilder.append(" **筛选条件:** ");
            }
            for (QueryFilter queryFilter : queryFilters) {
                textBuilder.append(queryFilter.getName()).append(" ")
                        .append(queryFilter.getOperator().getValue()).append(" ")
                        .append(queryFilter.getValue()).append(" ");
            }
        }
        parseInfo.setTextInfo(textBuilder.toString());
    }
}
