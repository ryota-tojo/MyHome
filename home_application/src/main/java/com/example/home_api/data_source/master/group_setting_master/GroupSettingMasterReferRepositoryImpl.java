package com.example.home_api.data_source.master.group_setting_master;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.entities.master.GroupSettingMasterEntity;
import com.example.home_api.domain.repositories.master.group_setting_master.GroupSettingMasterReferRepository;
import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterInitValue;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterKey;
import com.example.home_api.domain.value_objects.master.group_setting_master.GroupSettingMasterRemarks;
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
public class GroupSettingMasterReferRepositoryImpl implements GroupSettingMasterReferRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.所属グループ設定マスタ;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GroupSettingMasterReferRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<GroupSettingMasterEntity> find(GroupSettingMasterKey key) {

        SqlAndParams sap = createSql(key);
        List<GroupSettingMasterEntity> result = new ArrayList<>();

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
                            GroupSettingMasterEntity.of(
                                    GroupSettingMasterKey.of(rs.getString("key")),
                                    GroupSettingMasterInitValue.of(rs.getString("init_value")),
                                    GroupSettingMasterRemarks.of(rs.getString("remarks")),
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

    private SqlAndParams createSql(GroupSettingMasterKey key) {

        // WHERE句の条件リストを作成
        List<WhereItem> whereItems = new ArrayList<>();
        if (key != null && key.getValue() != null) {
            whereItems.add(WhereItem.from("key", key.getValue()));
        }

        // SQL生成
        return SqlAndParams.toSqlAndParamsForSelect(
                "group_setting_master",
                List.of("*"),
                whereItems,
                null,
                null,
                null
        );
    }
}
