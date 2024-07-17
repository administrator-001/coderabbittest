package com.myhexin.cragent.coderabbittest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * 日志测试代码
 */
@Slf4j
@Component
public class OpenFundAccountServiceImpl implements OpenFundAccountService {

    private final AccoTradeAccoProvider accoTradeAccoProvider;

    private final TaAccountConfigManager taAccountConfigManager;

    public OpenFundAccountServiceImpl(AccoTradeAccoProvider accoTradeAccoProvider,
                                      TaAccountConfigManager taAccountConfigManager) {
        this.accoTradeAccoProvider = accoTradeAccoProvider;
        this.taAccountConfigManager = taAccountConfigManager;
    }

    @Override
    public void openFundAccount(@NotNull OpenFundAccountRequest openFundAccountRequest) {
        log.info("#openFundAccount TA开户param {}", openFundAccountRequest.getAppSheetSerialNo());
        Result<OpenFundAccountRequest, Void> result = accoTradeAccoProvider
                .openFundAccountInstitution(openFundAccountRequest);
        log.info("#openFundAccount TA开户param: {}", openFundAccountRequest);
        log.info("#openFundAccount TA开户result: {}", result);
        log.info("#openFundAccount TA开户成功");
        try {
            result.success();
        } catch (ServiceException e) {
            log.error("TA开户 开户接口异常 param:{}", openFundAccountRequest.getAppSheetSerialNo(), e);
            throw new IllegalStateException("TA开户异常", e);
        }
        TaAccountConfig taAccountConfig = taAccountConfigManager
                .selectConfigByTaCode(openFundAccountRequest.getTaCode());
        openFundAccountExtService.saveTaConfig(taAccountConfig);
    }
}
