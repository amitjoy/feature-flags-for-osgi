package com.amitinside.featureflags.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;

public final class ManagerHelperTest {

    @Test(expected = InvocationTargetException.class)
    public void testHelperInstantiation()
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final Constructor<ManagerHelper> constructor = ManagerHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testAsList() {
        final List<String> list1 = ManagerHelper.asList(null);

        assertNotNull(list1);
        assertTrue(list1.isEmpty());

        final List<String> list2 = ManagerHelper.asList(new String[] { "a" });

        assertNotNull(list2);
        assertEquals("a", list2.get(0));
    }

}
