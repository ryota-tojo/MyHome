package com.example.home_api.data_source.transaction.user;

import com.example.home_api.domain.common.*;
import com.example.home_api.domain.entities.transaction.user.UserEntity;
import com.example.home_api.domain.repositories.transaction.user.UserReferRepository;
import com.example.home_api.domain.value_objects.ApproveDatetime;
import com.example.home_api.domain.value_objects.CreateDatetime;
import com.example.home_api.domain.value_objects.DeleteDatetime;
import com.example.home_api.domain.value_objects.UpdateDatetime;
import com.example.home_api.domain.value_objects.transaction.user.*;
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
public class UserReferRepositoryImpl implements UserReferRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.ユーザー;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public UserReferRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<UserEntity> find(UserId userId, UserPassword userPassword,UserName userName, UserPermissionFlag permissionFlag, UserApproveFlag approveFlag, UserDeleteFlag deleteFlag, Offset offset, Limit limit) {

        SqlAndParams sap = createSql(userId, userPassword, userName, permissionFlag, approveFlag, deleteFlag, offset, limit);
        List<UserEntity> result = new ArrayList<>();

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
                            UserEntity.of(
                                    UserId.of(rs.getString("user_id")),
                                    UserPassword.of(rs.getString("user_password")),
                                    UserName.of(rs.getString("user_name")),
                                    UserPermissionFlag.of(rs.getInt("permission_flag")),
                                    UserApproveFlag.of(rs.getInt("approve_flag")),
                                    UserDeleteFlag.of(rs.getInt("delete_flag")),
                                    (rs.getTimestamp("approve_date")==null) ? null : ApproveDatetime.of(rs.getTimestamp("approve_date").toLocalDateTime()),
                                    (rs.getTimestamp("delete_date")==null) ? null : DeleteDatetime.of(rs.getTimestamp("delete_date").toLocalDateTime()),
                                    CreateDatetime.of(rs.getTimestamp("create_date").toLocalDateTime()),
                                    UpdateDatetime.of(rs.getTimestamp("update_date").toLocalDateTime())
                            )
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private SqlAndParams createSql(UserId userId, UserPassword userPassword, UserName userName, UserPermissionFlag permissionFlag, UserApproveFlag approveFlag, UserDeleteFlag deleteFlag, Offset offset, Limit limit) {

        // WHERE句の条件リストを作成
        List<WhereItem> whereItems = new ArrayList<>();
        if (userId != null && userId.getValue() != null) {
            whereItems.add(WhereItem.from("user_id", userId.getValue()));
        }
        if (userPassword != null && userPassword.getValue() != null) {
            whereItems.add(WhereItem.from("user_password", userName.getValue()));
        }
        if (userName != null && userName.getValue() != null) {
            whereItems.add(WhereItem.from("user_name", "LIKE,%" + userName.getValue() + "%"));
        }
        if (permissionFlag != null) {
            whereItems.add(WhereItem.from("permission_flag", permissionFlag.getValue()));
        }
        if (approveFlag != null) {
            whereItems.add(WhereItem.from("approve_flag", approveFlag.getValue()));
        }
        if (deleteFlag != null) {
            whereItems.add(WhereItem.from("delete_flag", deleteFlag.getValue()));
        }

        // SQL生成
        return SqlAndParams.toSqlAndParamsForSelect(
                "\"user\"",
                List.of("*"),
                whereItems,
                null,
                offset,
                limit
        );
    }
}
