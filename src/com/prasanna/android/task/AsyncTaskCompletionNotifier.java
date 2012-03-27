package com.prasanna.android.task;

public interface AsyncTaskCompletionNotifier<T>
{
    public void notifyOnCompletion(T result);
}
