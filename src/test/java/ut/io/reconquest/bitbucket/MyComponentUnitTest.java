package ut.io.reconquest.bitbucket;

import org.junit.Test;
import io.reconquest.bitbucket.api.MyPluginComponent;
import io.reconquest.bitbucket.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}