package com.example.home_api.data_source.transaction.group_member;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SetItem;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.repositories.transaction.group_member.GroupMemberUpdateRepository;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;
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
public class GroupMemberUpdateRepositoryImpl implements GroupMemberUpdateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.所属グループ;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GroupMemberUpdateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Integer update(GroupId groupId, UserId userId, GroupApproveFlag approveFlag, GroupLeaderFlag leaderFlag) {

        SqlAndParams sap = createSql(groupId, userId, approveFlag, leaderFlag);

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sap.getSql());
        ) {
            for (int i = 0; i < sap.getParams().size(); i++) {
                ps.setObject(i + 1, sap.getParams().get(i));
            }

            int affected = ps.executeUpdate();
            if (affected == 0) {
                log.error("{} の更新に失敗しました：影響を受けた行なし", itemName);
                throw new RuntimeException();
            }

            return affected;

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

        // SET句の条件リストを作成(オブジェクトNULL：除外、値NULL：許容)
        List<SetItem> setItems = new ArrayList<>();
        if (approveFlag != null) {
            setItems.add(SetItem.from("approve_flag", approveFlag.getValue()));
        }
        if (approveFlag != null) {
            setItems.add(SetItem.from("leader_flag", approveFlag.getValue()));
        }
        if (approveFlag != null) {
            if (approveFlag.getValue() == 1) {
                setItems.add(SetItem.from("approve_date", "CURRENT_TIMESTAMP"));
            }
        }
        setItems.add(SetItem.from("update_date", "CURRENT_TIMESTAMP"));

        // WHERE句の条件リストを作成
        List<WhereItem> whereItemsForAnd = new ArrayList<>();
        whereItemsForAnd.add(WhereItem.from("group_id", groupId.getValue()));
        whereItemsForAnd.add(WhereItem.from("user_id", userId.getValue()));

        // WHERE句のOR条件リストを作成
        List<WhereItem> whereItemsForOr = null;

        // SQL生成
        return SqlAndParams.toSqlAndParamsForUpdate(
                "group_member",
                setItems,
                whereItemsForAnd,
                whereItemsForOr
        );
    }
}
