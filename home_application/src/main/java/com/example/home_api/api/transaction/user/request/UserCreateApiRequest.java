package com.example.home_api.api.transaction.user.request;

import com.example.home_api.common.ParamUtils;
import com.example.home_api.domain.common.ParamCheckResult;
import com.example.home_api.domain.value_objects.transaction.user.*;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingKey;
import com.example.home_api.domain.value_objects.transaction.user_setting.UserSettingValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class UserCreateApiRequest {
    UserId userId;
    UserPassword password;
    UserName name;
    UserPermissionFlag permissionflag;
    UserApproveFlag approveflag;
    UserDeleteFlag deleteflag;

    public static ParamCheckResult paramCheck(JsonNode request) {

        ParamCheckResult paramCheckResultForUserId = ParamUtils.isValidCheckForRequired(request, "user_id");
        if (!paramCheckResultForUserId.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForUserId.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForPassword = ParamUtils.isValidCheckForRequired(request, "user_password");
        if (!paramCheckResultForPassword.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForPassword.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForUserName = ParamUtils.isValidCheckForRequired(request, "user_name");
        if (!paramCheckResultForUserName.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForUserName.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForPermission = ParamUtils.isValidCheckForRequired(request, "permission_flag");
        if (!paramCheckResultForPermission.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForPermission.getMessages()
            );
        }
        ParamCheckResult validateForPermission = UserPermissionFlag.validate(request);
        if (!validateForPermission.isResult()) {
            return ParamCheckResult.of(
                    false,
                    validateForPermission.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForApproval = ParamUtils.isValidCheckForRequired(request, "approve_flag");
        if (!paramCheckResultForApproval.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForApproval.getMessages()
            );
        }
        ParamCheckResult validateForApprove = UserApproveFlag.validate(request);
        if (!validateForApprove.isResult()) {
            return ParamCheckResult.of(
                    false,
                    validateForApprove.getMessages()
            );
        }
        ParamCheckResult paramCheckResultForDelete = ParamUtils.isValidCheckForRequired(request, "delete_flag");
        if (!paramCheckResultForDelete.isResult()) {
            return ParamCheckResult.of(
                    false,
                    paramCheckResultForDelete.getMessages()
            );
        }
        ParamCheckResult validateForDelete = UserDeleteFlag.validate(request);
        if (!validateForDelete.isResult()) {
            return ParamCheckResult.of(
                    false,
                    validateForDelete.getMessages()
            );
        }
        return ParamCheckResult.of(
                true,
                null
        );
    }

    public static UserCreateApiRequest from(JsonNode request) {

        return new UserCreateApiRequest(
                getParamForUserId(request),
                getParamForForPassword(request),
                getParamForForName(request),
                getParamForForPermissionFlag(request),
                getParamForForApproveFlag(request),
                getParamForForDeleteFlag(request)
        );
    }

    private static UserId getParamForUserId(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "user_id")) {
            case キーなし:
            case 値なし:
                System.out.println("パラメータの取得に失敗しました<user_id>");
                throw new IllegalArgumentException();
            default:
                return UserId.of(request.get("user_id").asText());
        }
    }

    private static UserPassword getParamForForPassword(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "user_password")) {
            case キーなし:
            case 値なし:
                System.out.println("パラメータの取得に失敗しました<user_password>");
                throw new IllegalArgumentException();
            default:
                return UserPassword.of(request.get("user_password").asText());
        }
    }

    private static UserName getParamForForName(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "user_name")) {
            case キーなし:
            case 値なし:
                System.out.println("パラメータの取得に失敗しました<user_name>");
                throw new IllegalArgumentException();
            default:
                return UserName.of(request.get("user_name").asText());
        }
    }

    private static UserPermissionFlag getParamForForPermissionFlag(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "permission_flag")) {
            case キーなし:
            case 値なし:
                System.out.println("パラメータの取得に失敗しました<permission_flag>");
                throw new IllegalArgumentException();
            default:
                return UserPermissionFlag.of(request.get("permission_flag").asInt());
        }
    }

    private static UserApproveFlag getParamForForApproveFlag(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "approve_flag")) {
            case キーなし:
            case 値なし:
                System.out.println("パラメータの取得に失敗しました<approve_flag>");
                throw new IllegalArgumentException();
            default:
                return UserApproveFlag.of(request.get("approve_flag").asInt());
        }
    }

    private static UserDeleteFlag getParamForForDeleteFlag(JsonNode request) {
        switch (ParamUtils.getParamCondition(request, "delete_flag")) {
            case キーなし:
            case 値なし:
                System.out.println("パラメータの取得に失敗しました<delete_flag>");
                throw new IllegalArgumentException();
            default:
                return UserDeleteFlag.of(request.get("delete_flag").asInt());
        }
    }
}
