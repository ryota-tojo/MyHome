package com.example.home_api.data_source.transaction.user_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.entities.transaction.user.UserSettingEntity;
import com.example.home_api.domain.repositories.transaction.user_setting.UserSettingReferRepository;
import com.example.home_api.domain.value_objects.*;
import com.example.home_api.domain.value_objects.transaction.user.UserId;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingValue;
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
public class UserSettingReferRepositoryImpl implements UserSettingReferRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.ユーザー設定;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public UserSettingReferRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<UserSettingEntity> find(UserId userId, UserSettingKey key) {

        SqlAndParams sap = createSql(userId, key);
        List<UserSettingEntity> result = new ArrayList<>();

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
                            UserSettingEntity.of(
                                    UserId.of(rs.getString("user_id")),
                                    UserSettingKey.of(rs.getString("key")),
                                    UserSettingValue.of(rs.getString("value")),
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

    private SqlAndParams createSql(UserId userId, UserSettingKey key) {

        // WHERE句の条件リストを作成
        List<WhereItem> whereItems = new ArrayList<>();
        if (userId != null && userId.getValue() != null) {
            whereItems.add(WhereItem.from("user_id", userId.getValue()));
        }
        if (key != null && key.getValue() != null) {
            whereItems.add(WhereItem.from("key", key.getValue()));
        }

        // SQL生成
        return SqlAndParams.toSqlAndParamsForSelect(
                "user_setting",
                List.of("*"),
                whereItems,
                null,
                null,
                null
        );
    }
}
