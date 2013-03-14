package com.prasanna.android.runners;

import java.lang.reflect.InvocationTargetException;

import org.junit.runner.notification.RunNotifier;
import org.mockito.runners.MockitoJUnitRunner;

public class StackXMockitoRunner extends MockitoJUnitRunner
{
    public StackXMockitoRunner(Class<?> klass) throws InvocationTargetException
    {
        super(klass);
    }

    @Override
    public void run(RunNotifier notifier)
    {
        super.run(notifier);
    }
    
    
}
