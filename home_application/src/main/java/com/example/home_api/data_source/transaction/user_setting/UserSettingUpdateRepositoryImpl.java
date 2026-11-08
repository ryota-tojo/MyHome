package com.example.home_api.data_source.transaction.user_setting;

import com.example.home_api.domain.common.ItemNames;
import com.example.home_api.domain.common.SetItem;
import com.example.home_api.domain.common.SqlAndParams;
import com.example.home_api.domain.common.WhereItem;
import com.example.home_api.domain.repositories.transaction.user_setting.UserSettingUpdateRepository;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingKey;
import com.example.home_api.domain.value_objects.master.master_setting.MasterSettingValue;
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
public class UserSettingUpdateRepositoryImpl implements UserSettingUpdateRepository {

    private final DataSource dataSource;
    public static final ItemNames itemName = ItemNames.ユーザー設定;
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public UserSettingUpdateRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Integer update(UserId userId, UserSettingKey key, UserSettingValue value) {

        SqlAndParams sap = createSql(userId, key, value);

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

        // SET句の条件リストを作成(オブジェクトNULL：除外、値NULL：許容)
        List<SetItem> setItems = new ArrayList<>();
        if (value != null) {
            setItems.add(SetItem.from("value", (value.getValue() == null) ? "NULL" : value.getValue()));
        }
        setItems.add(SetItem.from("update_date", "CURRENT_TIMESTAMP"));

        // WHERE句の条件リストを作成
        List<WhereItem> whereItemsForAnd = new ArrayList<>();
        whereItemsForAnd.add(WhereItem.from("user_id", userId.getValue()));
        whereItemsForAnd.add(WhereItem.from("key", key.getValue()));

        // WHERE句のOR条件リストを作成
        List<WhereItem> whereItemsForOr = null;

        // SQL生成
        return SqlAndParams.toSqlAndParamsForUpdate(
                "user_setting",
                setItems,
                whereItemsForAnd,
                whereItemsForOr
        );
    }
}
