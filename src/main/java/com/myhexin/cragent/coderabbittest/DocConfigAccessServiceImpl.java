package com.myhexin.cragent.coderabbittest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DocConfigAccessServiceImpl implements DocConfigAccessService {

    @FofExceptionHandler
    @Override
    public DocConfigQueryVo query(@NotNull String fofId) {
        List<DocSubscriptionConfigDto> configList = docSubscriptionConfigService.listConfig(fofId);

        DocConfigQueryVo docConfigQueryVo = new DocConfigQueryVo();
        // 如果找不到配置，展示空配置
        Map<DocTypeEnum, List<DocSubscriptionConfigDto>> collect = configList.stream()
                .filter(c -> c.getDocType() != DocTypeEnum.STATEMENT)
                .collect(Collectors.groupingBy(DocSubscriptionConfigDto::getDocType));
        // 获取除对账单外的其他单据配置，并按照组织类型进行排序
        fillEmptyConfigs(collect, fofId);
        List<DocConfigVo> docConfigList =
                collect.entrySet().stream()
                        .sorted(Comparator.comparing(e -> e.getKey().getSort()))
                        .map(Map.Entry::getValue).map(DocConfigVo::of).collect(Collectors.toList());
        docConfigQueryVo.setDocConfigList(docConfigList);

        // 获取用户信息
        List<DocSubscriptionConfigDto> statementConfigList =
                configList.stream().filter(c -> c.getDocType() == DocTypeEnum.STATEMENT).collect(Collectors.toList());
        List<StatementDocConfigVo> stateDocConfigList = statementConfigList.stream().map(StatementDocConfigVo::of).collect(Collectors.toList());
        docConfigQueryVo.setStatementDocConfigList(stateDocConfigList);

        return docConfigQueryVo;
    }
}
