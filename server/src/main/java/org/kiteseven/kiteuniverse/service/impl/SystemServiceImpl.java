package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.constant.CommonConstants;
import org.kiteseven.kiteuniverse.pojo.vo.SystemInfoVO;
import org.kiteseven.kiteuniverse.service.SystemService;
import org.springframework.stereotype.Service;

@Service
public class SystemServiceImpl implements SystemService {

    @Override
    public SystemInfoVO getSystemInfo() {
        SystemInfoVO systemInfo = new SystemInfoVO();
        systemInfo.setSystemName(CommonConstants.SYSTEM_NAME);
        systemInfo.setModuleName("server");
        systemInfo.setVersion("0.0.1-SNAPSHOT");
        systemInfo.setDescription("Kite Universe server module");
        return systemInfo;
    }
}
