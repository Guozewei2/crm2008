package com.bjpowernode.crm.workbench.service;

import com.bjpowernode.crm.workbench.domain.Clue;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface ClueService {
    int saveCreateClue(Clue clue);
    public Clue queryClueForDetailById(String id);
    void saveConvertClue(Map<String,Object> map);
}
