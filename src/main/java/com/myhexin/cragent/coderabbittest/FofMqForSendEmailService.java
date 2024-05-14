package com.myhexin.cragent.coderabbittest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class FofMqForSendEmailService {

    private TTradeTradereq dealSuper(PdfDto pdfDto, TTradeTradereq tradeReq) {
        // 判断当前交易单号是否属于超级转换
        SuperConvertSplitRef splitRef = superConvertSplitRefManager.querySuperConvertSplitRef(pdfDto.getTTradeTradereq().getVcAppsheetserialno());
        if (null != splitRef) {
            // 如果是超级转换，增加备注
            // 如果是第一笔赎回+申购，查询申购信息
            if (SystemConstant.ONE.equals(splitRef.getOriginalFlag())) {
                String value = fofProductPropertyService.findValue(tradeReq.getVcCustid(), BizParamDefine.SUPER_CONVERT_DOCUMENT_MODE_PROPERTY);
                SuperConvertDocumentModeEnum mode = SuperConvertDocumentModeEnum.of(value);
                if (mode == SuperConvertDocumentModeEnum.MODE_136_142) {
                    // 这种模式下，只有超转024会进这里
                    if (!TradeTypeEnum.SHU_HUI.getCode().equals(splitRef.getBusinessCode())) {
                        throw new IllegalStateException("#setTradeReq error. unexpected 136+142"
                                + Parameters.writeAsString("tradeReq", tradeReq));
                    }
                    superConvert024To036(tradeReq, splitRef, pdfDto);
                } else {
                    if (TradeTypeEnum.SHU_HUI.getCode().equals(splitRef.getBusinessCode())) {
                        superConvert024To036(tradeReq, splitRef, pdfDto);
                    } else if (TradeTypeEnum.SHEN_GOU.getCode().equals(splitRef.getBusinessCode())) {
                        tradeReq.setRemark(String.format(CommonDefine.SUPER_REF_REMARK, tradeReq.getVcAppsheetserialno()));
                    } else {
                        throw new IllegalStateException("#setTradeReq error. unexpected 124+122"
                                + Parameters.writeAsString("tradeReq", tradeReq));
                    }
                }
            } else {
                tradeReq.setRemark(String.format(CommonDefine.SUPER_REF_REMARK, tradeReq.getVcAppsheetserialno()));
            }
        }
        return tradeReq;
    }

    public void createPdfDto(Map<String, String> map) {
        String appDay = tradeDateService.getAppDay().getRespBean();
        String[] array = map.get("appSheetSerialNo").split(CommonDefine.COMMA);
        PdfDto pdfDto = new PdfDto();
        log.info("createPdfDto:map={}", map);
        // 必要参数校验
        if (CollectionUtils.isEmpty(map) || StringUtils.isEmpty(map.get("fofId"))
                || StringUtils.isEmpty(map.get("tradeType")) || StringUtils.isEmpty(map.get("appSheetSerialNo"))) {
            return;
        }
        pdfDto.setDate(DateUtils.getCurrentDay());
        pdfDto.setCnDate(DateUtils.parseDate(pdfDto.getDate(), DateUtils.DEFAULT_DATE, CommonUtils.CHINESE_DATE));
        pdfDto.setFofId(map.get("fofId"));
        pdfDto.setTradeType(map.get("tradeType"));
        pdfDto.setAppsheetserialnos(map.get("appSheetSerialNo"));
        TTradeTradereq tradeTradereq = new TTradeTradereq();
        pdfDto.setTTradeTradereq(tradeTradereq);
        FofProduct fofProduct = fofProductManager.selectProductByFofId(pdfDto.getFofId());
        if (null != fofProduct) {
            pdfDto.setInvestManagerCode(fofProduct.getInvestManagerCode());
            pdfDto.setOrgId(fofProduct.getOrgId());
        }
        // 根据交易码查询对应那些枚举类
        List<PdfTypeEnum> enums = PdfTypeEnum.getTypeByTradeType(pdfDto.getTradeType());
        log.info("createPdfDto={}", enums);

        if (CollectionUtils.isEmpty(enums)) {
            return;
        }
        for (PdfTypeEnum pdf : enums) {
            if (!emailConfigManager.hasSingleSendConfig(pdfDto.getFofId(), pdf.getCode())) {
                continue;
            }
            String tradeCode = map.get("tradeCode");
            BigDecimal fee = calcFee(tradeCode);
            switch (pdf.getCode()) {
                // 交易申请单
                case SystemConstant.ONE:
                    schedulePositionMetricCache.storeAndIncreaseApplicationTotal(array.length, appDay);
                    log.info("=====createPdfDto:交易申请单开始：fofId={}, appno={}", pdfDto.getFofId(), JSON.toJSONString(pdfDto.getVcAppsheetserialnos()));
                    pdfDto.setPdfType(SystemConstant.ONE);
                    this.createTradeApply(pdfDto, appDay, fee);
                    log.info("=====createPdfDto:交易申请单完成：fofId={}, appno={}", pdfDto.getFofId(), JSON.toJSONString(pdfDto.getVcAppsheetserialnos()));
                    break;
                case SystemConstant.TWO:
                    schedulePositionMetricCache.storeAndIncreaseDrawTotal(1, appDay);
                    log.info("=====createPdfDto:资金划扣单开始：fofId={}, appno={}", pdfDto.getFofId(), JSON.toJSONString(pdfDto.getVcAppsheetserialnos()));
                    pdfDto.setPdfType(SystemConstant.ONE);
                    this.createFundsTransfer(pdfDto, appDay, fee);
                    log.info("=====createPdfDto:资金划扣单完成：fofId={}, appno={}", pdfDto.getFofId(), JSON.toJSONString(pdfDto.getVcAppsheetserialnos()));
                    break;
                default:
                    break;
            }
        }
    }
}
