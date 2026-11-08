package com.example.home_api.data_source.transaction.group;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SetItem;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.repositories.transaction.group.GroupUpdateRepository;
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
public class GroupUpdateRepositoryImpl implements GroupUpdateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.所属グループ;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public GroupUpdateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Integer update(GroupId groupId, GroupPassword password, GroupName groupName) {

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
                log.error("{} の更新に失敗しました：影響を受けた行なし",itemName);
                throw new RuntimeException();
            }

            return affected;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private SqlAndParams createSql(GroupId groupId, GroupPassword password, GroupName groupName) {

        // 不正値防御（念のため）
        if (groupId == null || groupId.getValue() != null) {
            log.error("createSql {} は必須項目です","groupId");
            throw new IllegalArgumentException("");
        }

        // SET句の条件リストを作成(オブジェクトNULL：除外、値NULL：許容)
        List<SetItem> setItems = new ArrayList<>();
        if (password != null) {
            setItems.add(SetItem.from("group_password", (password.getValue() == null) ? "NULL" : password.getValue()));
        }
        if (groupName != null) {
            setItems.add(SetItem.from("group_name", (groupName.getValue() == null) ? "NULL" : groupName.getValue()));
        }
        setItems.add(SetItem.from("update_date", "CURRENT_TIMESTAMP"));

        // WHERE句の条件リストを作成
        List<WhereItem> whereItemsForAnd = new ArrayList<>();
        whereItemsForAnd.add(WhereItem.from("group_id", groupId.getValue()));

        // WHERE句のOR条件リストを作成
        List<WhereItem> whereItemsForOr = null;

        // SQL生成
        return SqlAndParams.toSqlAndParamsForUpdate(
                "group",
                setItems,
                whereItemsForAnd,
                whereItemsForOr
        );
    }
}
