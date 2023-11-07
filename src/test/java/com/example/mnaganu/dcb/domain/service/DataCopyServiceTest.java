package com.example.mnaganu.dcb.domain.service;

import com.example.mnaganu.dcb.domain.model.SampleModel;
import com.example.mnaganu.dcb.domain.model.SelectModel;
import com.example.mnaganu.dcb.domain.repository.CopySourceSampleRepository;
import com.example.mnaganu.dcb.domain.repository.CopyToSampleRepository;
import com.example.mnaganu.dcb.infrastructure.mapper.SampleRowMapper;
import com.example.mnaganu.dcb.infrastructure.repository.CopyToSampleRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class DataCopyServiceTest {
    private final DataCopyService dataCopyService;
    private final DataSource copyToDataSource;
    private final DataSource copySourceDataSource;

    @Autowired
    public DataCopyServiceTest (DataCopyService dataCopyService,
                                @Qualifier("copyToDataSource") DataSource copyToDataSource,
                                @Qualifier("copySourceDataSource") DataSource copySourceDataSource) {
        this.dataCopyService = dataCopyService;
        this.copyToDataSource = copyToDataSource;
        this.copySourceDataSource = copySourceDataSource;
    }

    @Test
    void copy_コピー元のデータ0件() {
        //コピー元のテーブルデータが0件であることの確認
        createTable(copySourceDataSource);
        List<SampleModel> copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList).isEmpty();

        //コピー先のテーブルデータが0件であることの確認
        createTable(copyToDataSource);
        List<SampleModel> copyToList = getTableData(copyToDataSource);
        assertThat(copyToList).isEmpty();

        //コピーメソッド実行
        dataCopyService.copy();

        //コピー元のテーブルデータが0件であることの確認
        copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList).isEmpty();

        //コピー先のテーブルデータが0件であることの確認
        copyToList = getTableData(copyToDataSource);
        assertThat(copyToList).isEmpty();
    }

    @Test
    void copy_コピー元のデータ1件コピー先データなし() {
        int index = 0;

        //コピー元のテーブルデータが1件であることの確認
        createTable(copySourceDataSource);
        createTestData(copySourceDataSource, "copySource", 1);
        List<SampleModel> copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルデータが0件であることの確認
        createTable(copyToDataSource);
        List<SampleModel> copyToList = getTableData(copyToDataSource);
        assertThat(copyToList).isEmpty();

        //コピーメソッド実行
        dataCopyService.copy();

        //コピー元のテーブルデータが1件であることの確認
        copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルにデータがコピーされること
        copyToList = getTableData(copyToDataSource);
        assertThat(copyToList.size()).isEqualTo(1);
        index = 0;
        for (SampleModel model : copyToList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }
    }

    @Test
    void copy_コピー元のデータ1件コピー先データあり() {
        int index = 0;

        //コピー元のテーブルデータが1件であることの確認
        createTable(copySourceDataSource);
        createTestData(copySourceDataSource, "copySource", 1);
        List<SampleModel> copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルデータが100件であることの確認
        createTable(copyToDataSource);
        createTestData(copyToDataSource, "copyTo", 100);
        List<SampleModel> copyToList = getTableData(copyToDataSource);
        assertThat(copyToList.size()).isEqualTo(100);
        index = 0;
        for (SampleModel model : copyToList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copyTo" + index);
            index++;
        }

        //コピーメソッド実行
        dataCopyService.copy();

        //コピー元のテーブルデータが1件であることの確認
        copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルにデータがコピーされること
        copyToList = getTableData(copyToDataSource);
        assertThat(copyToList.size()).isEqualTo(1);
        index = 0;
        for (SampleModel model : copyToList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }
    }

    @Test
    void copy_コピー元のデータ1000件コピー先データなし() {
        int index = 0;

        //コピー元のテーブルデータが1000件であることの確認
        createTable(copySourceDataSource);
        createTestData(copySourceDataSource, "copySource", 1000);
        List<SampleModel> copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1000);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルデータが0件であることの確認
        createTable(copyToDataSource);
        List<SampleModel> copyToList = getTableData(copyToDataSource);
        assertThat(copyToList).isEmpty();

        //コピーメソッド実行
        dataCopyService.copy();

        //コピー元のテーブルデータが1000件であることの確認
        copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1000);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルにデータがコピーされること
        copyToList = getTableData(copyToDataSource);
        assertThat(copyToList.size()).isEqualTo(1000);
        index = 0;
        for (SampleModel model : copyToList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }
    }

    @Test
    void copy_コピー元のデータ1000件コピー先データあり() {
        int index = 0;

        //コピー元のテーブルデータが1件であることの確認
        createTable(copySourceDataSource);
        createTestData(copySourceDataSource, "copySource", 1000);
        List<SampleModel> copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1000);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルデータが100件であることの確認
        createTable(copyToDataSource);
        createTestData(copyToDataSource, "copyTo", 100);
        List<SampleModel> copyToList = getTableData(copyToDataSource);
        assertThat(copyToList.size()).isEqualTo(100);
        index = 0;
        for (SampleModel model : copyToList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copyTo" + index);
            index++;
        }

        //コピーメソッド実行
        dataCopyService.copy();

        //コピー元のテーブルデータが1000件であることの確認
        copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1000);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルにデータがコピーされること
        copyToList = getTableData(copyToDataSource);
        assertThat(copyToList.size()).isEqualTo(1000);
        index = 0;
        for (SampleModel model : copyToList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }
    }

    @Test
    void copy_コピー元のデータ1001件コピー先データなし() {
        int index = 0;

        //コピー元のテーブルデータが1001件であることの確認
        createTable(copySourceDataSource);
        createTestData(copySourceDataSource, "copySource", 1001);
        List<SampleModel> copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1001);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルデータが0件であることの確認
        createTable(copyToDataSource);
        List<SampleModel> copyToList = getTableData(copyToDataSource);
        assertThat(copyToList).isEmpty();

        //コピーメソッド実行
        dataCopyService.copy();

        //コピー元のテーブルデータが1000件であることの確認
        copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1001);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルにデータがコピーされること
        copyToList = getTableData(copyToDataSource);
        assertThat(copyToList.size()).isEqualTo(1001);
        index = 0;
        for (SampleModel model : copyToList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }
    }

    @Test
    void copy_コピー元のデータ1001件コピー先データあり() {
        int index = 0;

        //コピー元のテーブルデータが1件であることの確認
        createTable(copySourceDataSource);
        createTestData(copySourceDataSource, "copySource", 1001);
        List<SampleModel> copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1001);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルデータが100件であることの確認
        createTable(copyToDataSource);
        createTestData(copyToDataSource, "copyTo", 100);
        List<SampleModel> copyToList = getTableData(copyToDataSource);
        assertThat(copyToList.size()).isEqualTo(100);
        index = 0;
        for (SampleModel model : copyToList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copyTo" + index);
            index++;
        }

        //コピーメソッド実行
        dataCopyService.copy();

        //コピー元のテーブルデータが1001件であることの確認
        copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList.size()).isEqualTo(1001);
        index = 0;
        for (SampleModel model : copySourceList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }

        //コピー先のテーブルにデータがコピーされること
        copyToList = getTableData(copyToDataSource);
        assertThat(copyToList.size()).isEqualTo(1001);
        index = 0;
        for (SampleModel model : copyToList) {
            assertThat(model.getId()).isEqualTo(index);
            assertThat(model.getName().get()).isEqualTo("copySource" + index);
            index++;
        }
    }

    @Test
    void copy_コピー元のテーブルなし() {
        //コピー元のテーブルを削除
        dropTable(copySourceDataSource);

        //コピー先のテーブルデータが0件であることの確認
        createTable(copyToDataSource);
        List<SampleModel> copyToList = getTableData(copyToDataSource);
        assertThat(copyToList).isEmpty();

        try {
            //コピーメソッド実行
            dataCopyService.copy();
            //Exception が発生しなかった場合エラー
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertThat(e.getMessage()).isEqualTo("コピー元の sample テーブルのデータを取得に失敗しました。");
        }

        //コピー先のテーブルデータが0件であることの確認
        copyToList = getTableData(copyToDataSource);
        assertThat(copyToList).isEmpty();

    }

    @Test
    void copy_コピー先のテーブルなし() {
        //コピー元のテーブルデータが0件であることの確認
        createTable(copySourceDataSource);
        List<SampleModel> copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList).isEmpty();

        //コピー先のテーブルを消す
        dropTable(copyToDataSource);

        try {
            //コピーメソッド実行
            dataCopyService.copy();
            //Exception が発生しなかった場合エラー
            fail();
        } catch(Exception e) {
            e.printStackTrace();
            assertThat(e.getMessage()).isEqualTo("コピー先の sample テーブルの truncate に失敗しました。");
        }

        //コピー元のテーブルデータが0件であることの確認
        copySourceList = getTableData(copySourceDataSource);
        assertThat(copySourceList).isEmpty();
    }

    @Test
    void copy_コピー先のテーブル更新に失敗() {
        //CopySourceSampleRepository のモックを作成
        CopySourceSampleRepository mockCopySourceSampleRepository = mock(CopySourceSampleRepository.class);

        //id が重複したリストを作成する
        List<SampleModel> copySourceList = new ArrayList<>();
        copySourceList.add(
                SampleModel.builder()
                        .id(1)
                        .name("name1")
                        .build());
        copySourceList.add(
                SampleModel.builder()
                        .id(1)
                        .name("name2")
                        .build());

        //id が重複したリストを持つ SelectModel を作成
        SelectModel<SampleModel> selectModel =
                SelectModel.<SampleModel>builder()
                        .list(copySourceList)
                        .offset(0)
                        .limit(1000)
                        .total(2)
                        .build();

        //select メソッドが呼ばれたら、キーが重複したデータを返すようにする
        Mockito.when(mockCopySourceSampleRepository.select(anyInt(), anyInt())).thenReturn(selectModel);

        //CopyToSampleRepositoryのインスタンス作成
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(copyToDataSource);
        CopyToSampleRepository copyToSampleRepository = new CopyToSampleRepositoryImpl(namedParameterJdbcTemplate, 1000);

        //モックを利用するようにサービスのインスタンスを作成
        DataCopyService service = new DataCopyService(mockCopySourceSampleRepository, copyToSampleRepository);

        //コピー先のテーブルデータが0件であることの確認
        createTable(copyToDataSource);
        List<SampleModel> copyToList = getTableData(copyToDataSource);
        assertThat(copyToList).isEmpty();

        try {
            //コピーメソッド実行
            service.copy();
            //Exception が発生しなかった場合エラー
            fail();
        } catch(Exception e) {
            e.printStackTrace();
            assertThat(e.getMessage()).isEqualTo("コピー先の sample テーブルの insert に失敗しました。");
        }

    }

    private void dropTable(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "DROP TABLE IF EXISTS `sample`";
        jdbcTemplate.execute(sql);
    }

    private void createTable(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "DROP TABLE IF EXISTS `sample`";
        jdbcTemplate.execute(sql);

        sql = "CREATE TABLE `sample` (" +
                "`id` int UNIQUE NOT NULL," +
                "`name` text" +
                ");";
        jdbcTemplate.execute(sql);
    }

    private void createTestData(DataSource dataSource, String name, int count) {
        if (count < 1) {
            return;
        }
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        for (int i=0; i< count; i++) {
            String sql = String.format("INSERT INTO sample VALUES( %d, '%s%d');", i, name, i);
            jdbcTemplate.execute(sql);
        }
    }

    private List<SampleModel> getTableData(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "SELECT * FROM sample";
        SampleRowMapper sampleRowMapper = new SampleRowMapper();
        return jdbcTemplate.query(sql, sampleRowMapper);
    }

}
