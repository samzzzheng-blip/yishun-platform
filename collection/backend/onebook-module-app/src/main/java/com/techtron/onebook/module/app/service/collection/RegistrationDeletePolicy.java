package com.techtron.onebook.module.app.service.collection;
import java.util.Objects;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
public final class RegistrationDeletePolicy {
    private RegistrationDeletePolicy() {}
    public static boolean allowed(CollectionDO c, Long userId) {
        return userId != null && userId > 0 && c != null && Objects.equals(c.getUserId(), userId)
            && (Objects.equals(c.getStatus(), 0) || Objects.equals(c.getStatus(), 2))
            && Objects.equals(c.getTradeStatus(), 0) && Objects.equals(c.getGetbackStatus(), 0);
    }
}
