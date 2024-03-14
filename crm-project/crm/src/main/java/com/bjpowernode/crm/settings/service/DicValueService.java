package com.bjpowernode.crm.settings.service;

import com.bjpowernode.crm.settings.domain.DicValue;
import org.springframework.stereotype.Service;

import java.util.List;

@Service()
public interface DicValueService {
    List<DicValue> queryDicValueByTypeCode(String typeCode);
}
