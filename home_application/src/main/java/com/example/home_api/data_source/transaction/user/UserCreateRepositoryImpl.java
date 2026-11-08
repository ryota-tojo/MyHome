package com.example.home_api.data_source.transaction.user;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.repositories.transaction.user.UserCreateRepository;
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
public class UserCreateRepositoryImpl implements UserCreateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.ユーザー;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public UserCreateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String create(UserId userId, UserPassword userPassword, UserName userName, UserPermissionFlag userPermissionFlag, UserApproveFlag userApproveFlag, UserDeleteFlag userDeleteFlag) {

        SqlAndParams sap = createSql(userId, userPassword, userName, userPermissionFlag, userApproveFlag, userDeleteFlag);

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sap.getSql());
        ) {
            for (int i = 0; i < sap.getParams().size(); i++) {
                ps.setObject(i + 1, sap.getParams().get(i));
            }

            int affected = ps.executeUpdate();
            if (affected == 0) {
                log.error("{} の作成に失敗しました：影響を受けた行なし",itemName);
                throw new RuntimeException();
            }

            return userId.getValue();

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
        if (userPassword == null || userPassword.getValue() == null || userPassword.getValue().isEmpty()) {
            log.error("createSql {} は必須項目です","userPassword");
            throw new IllegalArgumentException("");
        }
        if (userName == null || userName.getValue() == null || userName.getValue().isEmpty()) {
            log.error("createSql {} は必須項目です","userName");
            throw new IllegalArgumentException("");
        }
        if (userPermissionFlag == null) {
            log.error("createSql {} は必須項目です","userPermissionFlag");
            throw new IllegalArgumentException("");
        }
        if (userApproveFlag == null) {
            log.error("createSql {} は必須項目です","userApproveFlag");
            throw new IllegalArgumentException("");
        }
        if (userDeleteFlag == null) {
            log.error("createSql {} は必須項目です","userDeleteFlag");
            throw new IllegalArgumentException("");
        }

        // セットする値のリストを作成
        List<Object> valueItems = new ArrayList<>();
        valueItems.add(userId.getValue());
        valueItems.add(userPassword.getValue());
        valueItems.add(userName.getValue());
        valueItems.add(userPermissionFlag.getValue());
        valueItems.add(userApproveFlag.getValue());
        valueItems.add(userDeleteFlag.getValue());
        if(userApproveFlag.getValue() == 1){
            valueItems.add("CURRENT_TIMESTAMP");
        }else{
            valueItems.add("NULL");
        }
        if(userDeleteFlag.getValue() == 1){
            valueItems.add("CURRENT_TIMESTAMP");
        }else{
            valueItems.add("NULL");
        }
        valueItems.add("CURRENT_TIMESTAMP");
        valueItems.add("CURRENT_TIMESTAMP");

        // SQL生成
        return SqlAndParams.toSqlAndParamsForInsert(
                "\"user\"",
                List.of("user_id","user_password","user_name","permission_flag","approve_flag","delete_flag","approve_date","delete_date","create_date","update_date"),
                valueItems
        );
    }
}
