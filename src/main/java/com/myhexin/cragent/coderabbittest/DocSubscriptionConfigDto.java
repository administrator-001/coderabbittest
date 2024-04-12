package com.myhexin.cragent.coderabbittest;

import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单据订阅
 * t_fof_doc_config a inner join t_fof_doc_subscription_config b on a.id = b.nd_doc_config_id
 */
@Data
public class DocSubscriptionConfigDto {

    private Long id;

    private String fofId;

    private DocTypeEnum docType;

    private Period period;

    private DocSubscriptionConfig.SubscriptionType subscriptionType;

    private String[] emails;

    private DocSubscriptionConfig.PushDateOption pushDateOption;

    private LocalTime pushTime;

    private DocSubscriptionConfig.Switch onOff;

    /**
     * 按目前的UI逻辑，校验某种单据配置的数据是否有异常
     * 1. 单据类型 docType 必须相同
     * 2. 开关状态 onOff 必须相同
     * 3. 邮箱 emails 必须相同
     * 4. 单笔配置最多一个
     * 5. 汇总配置存在多条，那么每条的推送时间 pushTime 必须不同
     *
     * @param dtoList 必须是同一种单据的一批订阅配置
     */
    public static void valid(List<DocSubscriptionConfigDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return;
        }
        // dtoList必须是同类单据
        if (dtoList.stream().map(DocSubscriptionConfigDto::getDocType).distinct().count() > 1) {
            throw new IllegalStateException("代码有bug" + dtoList);
        }
        if (dtoList.stream().map(DocSubscriptionConfigDto::getOnOff).distinct().count() > 1) {
            throw new IllegalStateException("开关有脏数据" + dtoList);
        }
        if (dtoList.stream().map(c -> StringUtils.join(c.getEmails(), ",")).distinct().count() > 1) {
            throw new IllegalStateException("邮箱有脏数据" + dtoList);
        }
        if (dtoList.stream().filter(d -> d.getSubscriptionType() == DocSubscriptionConfig.SubscriptionType.SINGLE).count() > 1) {
            throw new IllegalStateException("单笔配置有脏数据" + dtoList);
        }

        List<DocSubscriptionConfigDto> summaryConfigList =
                dtoList.stream().filter(d -> d.getSubscriptionType() == DocSubscriptionConfig.SubscriptionType.SUMMARY)
                        .collect(Collectors.toList());
        if (summaryConfigList.size() != summaryConfigList.stream().map(DocSubscriptionConfigDto::getPushTime).distinct().count()) {
            throw new IllegalStateException("汇总推送时间有脏数据" + dtoList);
        }
    }
}
