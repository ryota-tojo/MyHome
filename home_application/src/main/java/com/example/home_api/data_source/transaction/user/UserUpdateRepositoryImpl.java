package com.example.home_api.data_source.transaction.user;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SetItem;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.repositories.transaction.user.UserUpdateRepository;
import com.example.home_api.domain.value_objects.transaction.user.*;
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
public class UserUpdateRepositoryImpl implements UserUpdateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.ユーザー;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public UserUpdateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Integer update(UserId userId, UserPassword userPassword, UserName userName, UserPermissionFlag userPermissionFlag, UserApproveFlag userApproveFlag, UserDeleteFlag userDeleteFlag) {

        SqlAndParams sap = createSql(userId, userPassword, userName, userPermissionFlag, userApproveFlag, userDeleteFlag);

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sap.getSql());
        ) {
            for (int i = 0; i < sap.getParams().size(); i++) {
                ps.setObject(i + 1, sap.getParams().get(i));
            }
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private SqlAndParams createSql(UserId userId, UserPassword userPassword, UserName userName, UserPermissionFlag userPermissionFlag, UserApproveFlag userApproveFlag, UserDeleteFlag userDeleteFlag) {

        // 不正値防御（念のため）
        if (userId == null || userId.getValue() == null || userId.getValue().isEmpty()) {
            log.error("createSql {} は必須項目です","userId");
            throw new IllegalArgumentException("");
        }

        // SET句の条件リストを作成(オブジェクトNULL：除外、値NULL：許容)
        List<SetItem> setItems = new ArrayList<>();
        if (userPassword != null) {
            setItems.add(SetItem.from("user_password", (userPassword.getValue() == null) ? "NULL" : userPassword.getValue()));
        }
        if (userName != null) {
            setItems.add(SetItem.from("user_name", (userName.getValue() == null) ? "NULL" : userName.getValue()));
        }
        if (userPermissionFlag != null) {
            setItems.add(SetItem.from("permission_flag", userPermissionFlag.getValue()));
        }
        if (userApproveFlag != null) {
            setItems.add(SetItem.from("approve_flag", userApproveFlag.getValue()));
        }
        if (userDeleteFlag != null) {
            setItems.add(SetItem.from("delete_flag", userDeleteFlag.getValue()));
        }
        if (userApproveFlag != null && userApproveFlag.getValue() == 1) {
            setItems.add(SetItem.from("approve_date", "CURRENT_TIMESTAMP"));
        }
        if (userDeleteFlag != null && userDeleteFlag.getValue() == 1) {
            setItems.add(SetItem.from("delete_date", "CURRENT_TIMESTAMP"));
        }
        setItems.add(SetItem.from("update_date", "CURRENT_TIMESTAMP"));

        // WHERE句のAND条件リストを作成
        List<WhereItem> whereItemsForAnd = new ArrayList<>();
        whereItemsForAnd.add(WhereItem.from("user_id", userId.getValue()));

        // WHERE句のOR条件リストを作成
        List<WhereItem> whereItemsForOr = null;

        // SQL生成
        return SqlAndParams.toSqlAndParamsForUpdate(
                "\"user\"",
                setItems,
                whereItemsForAnd,
                whereItemsForOr
        );
    }
}
