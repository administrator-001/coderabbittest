package com.myhexin.cragent.coderabbittest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FofMqForSendEmailService2 {

    /**
     * 处理超级转换
     * 
     * @param pdfDto pdf生成对象填值
     * @param tradeReq 交易申请单
     * @return 交易申请单
     */
    private TTradeTradereq dealSuper(PdfDto pdfDto, TTradeTradereq tradeReq) {
        // 判断当前交易单号是否属于超级转换
        SuperConvertSplitRef splitRef = superConvertSplitRefManager.querySuperConvertSplitRef(pdfDto.getTTradeTradereq().getVcAppsheetserialno());
        if (splitRef == null) {
            return tradeReq;
        }
        // 如果是超级转换，增加备注
        // 如果是第一笔赎回+申购，查询申购信息
        if (!SystemConstant.ONE.equals(splitRef.getOriginalFlag())) {
            tradeReq.setRemark(String.format(CommonDefine.SUPER_REF_REMARK, tradeReq.getVcAppsheetserialno()));
            return tradeReq;
        }
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
        return tradeReq;
    }
}
