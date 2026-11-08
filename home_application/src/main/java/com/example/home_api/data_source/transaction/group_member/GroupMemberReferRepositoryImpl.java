package com.example.home_api.data_source.transaction.group_member;

import com.example.home_api.domain.common.*;
import com.example.home_api.domain.entities.transaction.group.GroupMemberEntity;
import com.example.home_api.domain.repositories.transaction.group_member.GroupMemberReferRepository;
import com.example.home_api.domain.value_objects.ApproveDatetime;
import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupApproveFlag;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupLeaderFlag;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupMemberId;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
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
public class GroupMemberReferRepositoryImpl implements GroupMemberReferRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.所属グループ;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GroupMemberReferRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<GroupMemberEntity> find(GroupId groupId, UserId userId, GroupApproveFlag approveFlag, GroupLeaderFlag leaderFlag, Offset offset, Limit limit) {

        SqlAndParams sap = createSql(groupId, userId, approveFlag, leaderFlag, offset, limit);
        List<GroupMemberEntity> result = new ArrayList<>();

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
                            GroupMemberEntity.of(
                                    GroupMemberId.of(rs.getString("id")),
                                    GroupId.of(rs.getString("group_id")),
                                    UserId.of(rs.getString("user_id")),
                                    GroupApproveFlag.of(rs.getInt("approve_flag")),
                                    GroupLeaderFlag.of(rs.getInt("leader_flag")),
                                    ApproveDatetime.of(
                                            rs.getTimestamp("approve_date").toLocalDateTime()
                                    ),
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

    private SqlAndParams createSql(GroupId groupId, UserId userId, GroupApproveFlag approveFlag, GroupLeaderFlag leaderFlag, Offset offset, Limit limit) {

        // WHERE句の条件リストを作成
        List<WhereItem> whereItems = new ArrayList<>();
        if (groupId != null) {
            whereItems.add(WhereItem.from("group_id", groupId.getValue()));
        }
        if (userId != null) {
            whereItems.add(WhereItem.from("user_id", userId.getValue()));
        }
        if (approveFlag != null) {
            whereItems.add(WhereItem.from("approve_flag", approveFlag.getValue()));
        }
        if (leaderFlag != null) {
            whereItems.add(WhereItem.from("leader_flag", leaderFlag.getValue()));
        }

        // SQL生成
        return SqlAndParams.toSqlAndParamsForSelect(
                "group_member",
                List.of("*"),
                whereItems,
                null,
                offset,
                limit
        );
    }
}
