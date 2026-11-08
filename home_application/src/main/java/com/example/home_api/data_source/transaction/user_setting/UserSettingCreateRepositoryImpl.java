package com.example.home_api.data_source.transaction.user_setting;

import com.example.home_api.common.IdUtils;
import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.TableNames;
import com.example.home_api.domain.repositories.transaction.user_setting.UserSettingCreateRepository;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserSettingCreateRepositoryImpl implements UserSettingCreateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.ユーザー設定;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public UserSettingCreateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String create(UserId userId, UserSettingKey key, UserSettingValue value) {

        SqlAndParams sap = createSql(userId, key, value);

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

            return key.getValue();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private SqlAndParams createSql(UserId userId, UserSettingKey key, UserSettingValue value) {

        // 不正値防御（念のため）
        if (userId == null || userId.getValue() == null || userId.getValue().isEmpty()) {
            log.error("createSql {} は必須項目です","userId");
            throw new IllegalArgumentException("");
        }
        if (key == null || key.getValue() == null || key.getValue().isEmpty()) {
            log.error("createSql {} は必須項目です","key");
            throw new IllegalArgumentException("");
        }

        // セットする値のリストを作成
        List<Object> valueItems = new ArrayList<>();
        valueItems.add(userId.getValue());
        valueItems.add(key.getValue());
        if (value == null || value.getValue() == null) {
            valueItems.add("NULL");
        } else {
            valueItems.add(value.getValue());
        }
        valueItems.add("CURRENT_TIMESTAMP");
        valueItems.add("CURRENT_TIMESTAMP");

        // SQL生成
        return SqlAndParams.toSqlAndParamsForInsert(
                "user_setting",
                List.of("user_id", "key", "value", "create_date", "update_date"),
                valueItems
        );
    }
}
