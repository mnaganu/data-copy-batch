package com.example.mnaganu.dcb.infrastructure.mapper;

import com.example.mnaganu.dcb.domain.model.SampleModel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SampleRowMapper implements RowMapper<SampleModel> {

    @Override
    public SampleModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        return SampleModel.builder()
                .id((Integer) rs.getObject("id"))
                .name(rs.getString("name"))
                .build();
    }
}
