package com.example.home_api.data_source.transaction.group;

import com.example.home_api.domain.common.*;
import com.example.home_api.domain.entities.transaction.group.GroupEntity;
import com.example.home_api.domain.repositories.transaction.group.GroupReferRepository;
import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GroupReferRepositoryImpl implements GroupReferRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.所属グループ;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GroupReferRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<GroupEntity> find(GroupId groupId, GroupPassword groupPassword, GroupName name, Offset offset, Limit limit) {

        SqlAndParams sap = createSql(groupId, groupPassword, name, offset, limit);
        List<GroupEntity> result = new ArrayList<>();

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
                            GroupEntity.of(
                                    GroupId.of(rs.getString("group_id")),
                                    GroupPassword.of(rs.getString("group_password")),
                                    GroupName.of(rs.getString("group_name")),
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

    private SqlAndParams createSql(GroupId groupId, GroupPassword groupPassword, GroupName name, Offset offset, Limit limit) {

        // WHERE句の条件リストを作成
        List<WhereItem> whereItems = new ArrayList<>();
        if (groupId.getValue() != null) {
            whereItems.add(WhereItem.from("group_id", groupId.getValue()));
        }
        if (groupPassword.getValue() != null) {
            whereItems.add(WhereItem.from("group_password", groupPassword.getValue()));
        }
        if (name != null && name.getValue() != null) {
            whereItems.add(WhereItem.from("group_name", "LIKE,%" + name.getValue() + "%"));
        }

        // SQL生成
        return SqlAndParams.toSqlAndParamsForSelect(
                "group",
                List.of("*"),
                whereItems,
                null,
                offset,
                limit
        );
    }
}
