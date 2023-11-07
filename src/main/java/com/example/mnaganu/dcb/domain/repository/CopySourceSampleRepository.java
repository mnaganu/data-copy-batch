package com.example.mnaganu.dcb.domain.repository;

import com.example.mnaganu.dcb.domain.model.SampleModel;
import com.example.mnaganu.dcb.domain.model.SelectModel;

import java.util.Optional;

public interface CopySourceSampleRepository {
    void truncate();
    Optional<SampleModel> selectById(int id);
    SelectModel<SampleModel> select(int offset, int limit);
    int insert(SampleModel model);
}
