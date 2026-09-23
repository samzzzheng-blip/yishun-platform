package com.techtron.onebook.module.app.service.collection;
import org.junit.jupiter.api.Test;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import static org.junit.jupiter.api.Assertions.*;
class RegistrationDeletePolicyTest {
    @Test void onlyOwnUncommittedRegistrationsAreEligible() {
        var c = new CollectionDO().setUserId(674L).setStatus(0).setTradeStatus(0).setGetbackStatus(0);
        assertTrue(RegistrationDeletePolicy.allowed(c, 674L));
        assertFalse(RegistrationDeletePolicy.allowed(c, 305L));
        assertFalse(RegistrationDeletePolicy.allowed(c, null));
        assertFalse(RegistrationDeletePolicy.allowed(null, 674L));
        c.setStatus(2); assertTrue(RegistrationDeletePolicy.allowed(c, 674L));
        c.setStatus(1); assertFalse(RegistrationDeletePolicy.allowed(c, 674L));
        c.setStatus(0).setTradeStatus(2); assertFalse(RegistrationDeletePolicy.allowed(c, 674L));
        c.setTradeStatus(0).setGetbackStatus(1); assertFalse(RegistrationDeletePolicy.allowed(c, 674L));
    }
}
