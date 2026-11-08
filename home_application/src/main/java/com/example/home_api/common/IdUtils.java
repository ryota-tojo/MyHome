package com.example.home_api.common;

import com.example.home_api.domain.common.TableNames;
import com.example.home_api.domain.value_objects.transaction.group.GroupId;

public class IdUtils {

    public static String createId(TableNames tableName, String identifierId) {
        return tableName.getItemName() + "_" + identifierId + "_" + DateUtils.getTimeStamp();
    }
}
