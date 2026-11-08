package com.example.home_api.data_source.transaction.group_member;

import com.example.home_api.common.IdUtils;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.TableNames;
import com.example.home_api.domain.repositories.transaction.group.GroupCreateRepository;
import com.example.home_api.domain.repositories.transaction.group_member.GroupMemberCreateRepository;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
import com.example.home_api.domain.value_objects.transaction.group.GroupName;
import com.example.home_api.domain.value_objects.transaction.group.GroupPassword;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupApproveFlag;
import com.example.home_api.domain.value_objects.transaction.group_member.GroupLeaderFlag;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
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
public class GroupMemberCreateRepositoryImpl implements GroupMemberCreateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.所属グループ;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GroupMemberCreateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String create(GroupId groupId, UserId userId, GroupApproveFlag approveFlag, GroupLeaderFlag leaderFlag) {

        SqlAndParams sap = createSql(groupId, userId, approveFlag,leaderFlag);

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

    private SqlAndParams createSql(GroupId groupId, UserId userId, GroupApproveFlag approveFlag, GroupLeaderFlag leaderFlag) {

        // 不正値防御（念のため）
        if (groupId == null || groupId.getValue() == null) {
            log.error("createSql {} は必須項目です", "groupId");
            throw new IllegalArgumentException("");
        }
        if (userId == null || userId.getValue() == null) {
            log.error("createSql {} は必須項目です", "userId");
            throw new IllegalArgumentException("");
        }
        if (approveFlag == null) {
            log.error("createSql {} は必須項目です", "approveFlag");
            throw new IllegalArgumentException("");
        }
        if (leaderFlag == null) {
            log.error("createSql {} は必須項目です", "leaderFlag");
            throw new IllegalArgumentException("");
        }

        // セットする値のリストを作成
        List<Object> valueItems = new ArrayList<>();
        valueItems.add(IdUtils.createId(TableNames.所属グループメンバー, groupId.getValue() + "_" + userId.getValue()));
        valueItems.add(groupId.getValue());
        valueItems.add(userId.getValue());
        valueItems.add(approveFlag.getValue());
        valueItems.add(leaderFlag.getValue());
        if(1 == approveFlag.getValue()){
            valueItems.add("CURRENT_TIMESTAMP");
        }else{
            valueItems.add(null);
        }
        valueItems.add("CURRENT_TIMESTAMP");
        valueItems.add("CURRENT_TIMESTAMP");

        // SQL生成
        return SqlAndParams.toSqlAndParamsForInsert(
                "group_member",
                List.of("id, group_id", "user?id", "approve_flag", "leader_flag", "approve_date", "create_date", "update_date"),
                valueItems
        );
    }
}
