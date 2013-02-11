package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public abstract class AbstractStackxService extends Service
{
    private static final String TAG = AbstractStackxService.class.getSimpleName();

    private static boolean isRunning = false;
    private Looper serviceLooper;
    private Handler serviceHandler;

    protected abstract Handler getServiceHandler(Looper looper);

    private static List<Object> toNotifyObjects = Collections.synchronizedList(new ArrayList<Object>());

    public interface OnHandlerComplete
    {
        void onHandleMessageFinish(Message message, Object... args);
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    protected static synchronized void setRunning(boolean running)
    {
        isRunning = running;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = getServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand");

        setRunning(true);

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    public static void registerForCompleteNotification(Object object)
    {
        if (object != null)
        {
            if (isRunning)
            {
                Log.d(TAG, "Object registered for service complete notification");

                toNotifyObjects.add(object);
            }
            else
            {
                synchronized (object)
                {
                    object.notify();
                }
            }
        }
    }

    protected void notifyWaitingObjectsOnComplete()
    {
        for (Object object : toNotifyObjects)
        {
            synchronized (object)
            {
                Log.d(TAG, "Notifying object");

                object.notify();
            }
        }

        toNotifyObjects.clear();
    }
}
