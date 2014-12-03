/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: GCInfoCommandImpl.java
 * Author:   13073050
 * Date:     2014年10月31日 下午4:37:42
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.app.spy.core.command.impl;

import java.util.Map;

import org.apache.log4j.Logger;

import com.suning.app.spy.core.command.AbstractCommandHandler;
import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.service.ServiceControl;
import com.suning.app.spy.core.service.impl.JmxOperateService;
import com.suning.shared.spy.model.AggregateGCInfoModel;
import com.suning.shared.spy.model.GCInfoModel;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class GCInfoCommandImpl extends AbstractCommandHandler<AggregateGCInfoModel> {

    private static final Logger LOGGER = Logger.getLogger(GCInfoCommandImpl.class); 
    private JmxOperateService jmxOperateService = 
            ServiceControl.getInstance().getBean(JmxOperateService.class);

	@Override
	public String getCommandName() {
		return Constants.METRICS_GC;
	}

	@Override
	public AggregateGCInfoModel doHandle(String[] cmdList) {
		LOGGER.info("Excute doHandle method , Get GC Information !");
		AggregateGCInfoModel gcInfoModel = new AggregateGCInfoModel();
		Map<String, GCInfoModel> listGC = this.jmxOperateService.getExtJmxGcInfo();
		gcInfoModel.setExtGcInfo(listGC);
		GCInfoModel targetGcInfo = this.jmxOperateService.getTargetGCInfo();
		gcInfoModel.setTargetGcInfo(targetGcInfo);
		GCInfoModel selfGCInfoModel = this.jmxOperateService.getSelfGcInfo();
		gcInfoModel.setSelfGcInfo(selfGCInfoModel);
        return gcInfoModel;
	}

}
