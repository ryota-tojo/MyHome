package com.example.home_api.data_source.transaction.group_setting;

import com.example.home_api.common.IdUtils;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.TableNames;
import com.example.home_api.domain.repositories.transaction.group_setting.GroupSettingCreateRepository;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingKey;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GroupSettingCreateRepositoryImpl implements GroupSettingCreateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.所属グループ設定;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GroupSettingCreateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String create(GroupId groupId, GroupSettingKey key, GroupSettingValue value) {

        SqlAndParams sap = createSql(groupId, key, value);

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sap.getSql());
        ) {
            for (int i = 0; i < sap.getParams().size(); i++) {
                ps.setObject(i + 1, sap.getParams().get(i));
            }

            int affected = ps.executeUpdate();
            if (affected == 0) {
                log.error("{} の作成に失敗しました：影響を受けた行なし", itemName);
                throw new RuntimeException();
            }

            return key.getValue();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private SqlAndParams createSql(GroupId groupId, GroupSettingKey key, GroupSettingValue value) {

        // 不正値防御（念のため）
        if (groupId == null || groupId.getValue() == null) {
            log.error("createSql {} は必須項目です", "groupId");
            throw new IllegalArgumentException("");
        }
        if (key == null || key.getValue() == null) {
            log.error("createSql {} は必須項目です", "key");
            throw new IllegalArgumentException("");
        }

        // セットする値のリストを作成
        List<Object> valueItems = new ArrayList<>();
        valueItems.add(IdUtils.createId(TableNames.所属グループ設定, groupId.getValue() + "_" + key.getValue()));
        valueItems.add(groupId.getValue());
        valueItems.add(key.getValue());
        if (value == null || value.getValue() == null) {
            valueItems.add("NULL");
        } else {
            valueItems.add(value.getValue());
        }
        valueItems.add("CURRENT_TIMESTAMP");
        valueItems.add("CURRENT_TIMESTAMP");

        // SQL生成
        return SqlAndParams.toSqlAndParamsForInsert(
                "group_setting",
                List.of("id", "group_id", "key", "value", "create_date", "update_date"),
                valueItems
        );
    }
}
