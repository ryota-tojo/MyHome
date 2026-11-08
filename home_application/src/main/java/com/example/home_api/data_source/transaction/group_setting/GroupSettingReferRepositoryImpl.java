package com.example.home_api.data_source.transaction.group_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.entities.transaction.group.GroupSettingEntity;
import com.example.home_api.domain.repositories.transaction.group_setting.GroupSettingReferRepository;
import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingId;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingKey;
import com.example.home_api.domain.value_objects.transaction.group_setting.GroupSettingValue;
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
public class GroupSettingReferRepositoryImpl implements GroupSettingReferRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.所属グループ設定;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GroupSettingReferRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<GroupSettingEntity> find(GroupId userId, GroupSettingKey key) {

        SqlAndParams sap = createSql(userId, key);
        List<GroupSettingEntity> result = new ArrayList<>();

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
                            GroupSettingEntity.of(
                                    GroupSettingId.of(rs.getString("id")),
                                    GroupId.of(rs.getString("user_id")),
                                    GroupSettingKey.of(rs.getString("key")),
                                    GroupSettingValue.of(rs.getString("value")),
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

    private SqlAndParams createSql(GroupId groupId, GroupSettingKey key) {

        // 不正値防御（念のため）
        if (groupId == null) {
            log.error("createSql {} は必須項目です", "groupId");
            throw new IllegalArgumentException("");
        }

        // WHERE句の条件リストを作成
        List<WhereItem> whereItems = new ArrayList<>();
        if (groupId.getValue() != null) {
            whereItems.add(WhereItem.from("group_id", groupId.getValue()));
        }
        if (key != null && key.getValue() != null) {
            whereItems.add(WhereItem.from("key", key.getValue()));
        }

        // SQL生成
        return SqlAndParams.toSqlAndParamsForSelect(
                "group_setting",
                List.of("*"),
                whereItems,
                null,
                null,
                null
        );
    }
}
