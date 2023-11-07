package com.example.mnaganu.dcb.domain.service;

import com.example.mnaganu.dcb.domain.model.SampleModel;
import com.example.mnaganu.dcb.domain.model.SelectModel;
import com.example.mnaganu.dcb.domain.repository.CopySourceSampleRepository;
import com.example.mnaganu.dcb.domain.repository.CopyToSampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataCopyService {
    private static final Logger logger = LoggerFactory.getLogger(DataCopyService.class);
    private CopySourceSampleRepository copySourceSampleRepository;
    private CopyToSampleRepository copyToSampleRepository;

    public DataCopyService(CopySourceSampleRepository copySourceSampleRepository,
                           CopyToSampleRepository copyToSampleRepository) {
        this.copySourceSampleRepository = copySourceSampleRepository;
        this.copyToSampleRepository = copyToSampleRepository;
    }

    public void copy() {
        logger.info("copy 開始");

        logger.info("コピー先の sample テーブルを truncate します。");
        try {
            copyToSampleRepository.truncate();
            logger.info("コピー先の sample テーブルを truncate しました。");
        } catch (Exception e) {
            logger.error("コピー先の sample テーブルの truncate に失敗しました。", e);
            throw new RuntimeException("コピー先の sample テーブルの truncate に失敗しました。", e);
        }

        int offset = 0;
        int limit = 1000;
        int total = 0;
        do {
            SelectModel<SampleModel> selectModel =
                    SelectModel.<SampleModel>builder()
                            .list(new ArrayList<SampleModel>())
                            .offset(0)
                            .limit(0)
                            .total(0)
                            .build();
            try {
                logger.info("コピー元の sample テーブルのデータを取得します。");
                selectModel = copySourceSampleRepository.select(offset, limit);
                logger.info("コピー元の sample テーブルのデータを取得しました。");
            } catch (Exception e) {
                logger.error("コピー元の sample テーブルのデータを取得に失敗しました。");
                throw new RuntimeException("コピー元の sample テーブルのデータを取得に失敗しました。", e);
            }

            List<SampleModel> list = selectModel.getList();

            try {
                logger.info("コピー先の sample テーブルに insert します。");
                list.stream().forEach(model -> copyToSampleRepository.insert(model));
                logger.info("コピー先の sample テーブルに insert しました。");
            } catch (Exception e) {
                logger.error("コピー先の sample テーブルの insert に失敗しました。");
                throw new RuntimeException("コピー先の sample テーブルの insert に失敗しました。", e);
            }

            total = selectModel.getTotal();
            offset = offset + limit;
        } while (offset < total);

        logger.info("copy 終了");
    }
}
