package com.example.home_api.data_source.transaction.group;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.repositories.transaction.group.GroupCreateRepository;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group.GroupName;
import com.example.home_api.domain.value_objects.transaction.group.GroupPassword;
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
public class GroupCreateRepositoryImpl implements GroupCreateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.所属グループ;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GroupCreateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String create(GroupId groupId, GroupPassword password, GroupName groupName) {

        SqlAndParams sap = createSql(groupId, password, groupName);

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

            return groupId.getValue();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private SqlAndParams createSql(GroupId groupId, GroupPassword password, GroupName groupName) {

        // 不正値防御（念のため）
        if (groupId == null || groupId.getValue() == null) {
            log.error("createSql {} は必須項目です", "groupId");
            throw new IllegalArgumentException("");
        }
        if (password == null || password.getValue() == null) {
            log.error("createSql {} は必須項目です", "password");
            throw new IllegalArgumentException("");
        }
        if (groupName == null || groupName.getValue() == null) {
            log.error("createSql {} は必須項目です", "groupName");
            throw new IllegalArgumentException("");
        }

        // セットする値のリストを作成
        List<Object> valueItems = new ArrayList<>();
        valueItems.add(groupId.getValue());
        valueItems.add(password.getValue());
        valueItems.add(groupName.getValue());
        valueItems.add("CURRENT_TIMESTAMP");
        valueItems.add("CURRENT_TIMESTAMP");

        // SQL生成
        return SqlAndParams.toSqlAndParamsForInsert(
                "group",
                List.of("group_id", "group_password", "group_name", "create_date", "update_date"),
                valueItems
        );
    }
}
