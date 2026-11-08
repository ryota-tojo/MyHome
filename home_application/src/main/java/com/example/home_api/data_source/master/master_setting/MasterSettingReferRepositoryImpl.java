package com.example.home_api.data_source.master.master_setting;

import com.example.home_api.domain.common.*;
import com.example.home_api.domain.entities.master.MasterSettingEntity;
import com.example.home_api.domain.repositories.master.master_setting.MasterSettingReferRepository;
import com.example.home_api.domain.value_objects.*;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingRemarks;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MasterSettingReferRepositoryImpl implements MasterSettingReferRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.マスタ設定;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public MasterSettingReferRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<MasterSettingEntity> find(MasterSettingKey key, Offset offset, Limit limit) {

        SqlAndParams sap = createSql(key, offset, limit);
        List<MasterSettingEntity> result = new ArrayList<>();

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sap.getSql());
        ) {
            for (int i = 0; i < sap.getParams().size(); i++) {
                ps.setObject(i + 1, sap.getParams().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(
                            MasterSettingEntity.of(
                                    MasterSettingKey.of(rs.getString("key")),
                                    MasterSettingValue.of(rs.getString("value")),
                                    MasterSettingRemarks.of(rs.getString("remarks")),
                                    CreateDatetime.of(
                                            rs.getTimestamp("create_date").toLocalDateTime()
                                    ),
                                    UpdateDatetime.of(
                                            rs.getTimestamp("update_date").toLocalDateTime()
                                    )
                            )
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private SqlAndParams createSql(MasterSettingKey key, Offset offset, Limit limit) {

        // WHERE句の条件リストを作成
        List<WhereItem> whereItems = new ArrayList<>();
        if (key != null && key.getValue() != null) {
            whereItems.add(WhereItem.from("key", key.getValue()));
        }

        // SQL生成
        return SqlAndParams.toSqlAndParamsForSelect(
                "master_setting",
                List.of("*"),
                whereItems,
                null,
                offset,
                limit
        );
    }
}
