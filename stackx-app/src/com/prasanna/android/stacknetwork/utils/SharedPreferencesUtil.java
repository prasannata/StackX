/*
    Copyright (C) 2013 Prasanna Thirumalai
    
    This file is part of StackX.

    StackX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    StackX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with StackX.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.prasanna.android.stacknetwork.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.prasanna.android.utils.LogWrapper;

public class SharedPreferencesUtil {
    private static final String TAG = SharedPreferencesUtil.class.getSimpleName();

    public static void setBoolean(Context context, String key, boolean on) {
        if (context != null && key != null) {
            Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefEditor.putBoolean(key, on);
            prefEditor.commit();
        }
    }

    public static boolean isSet(Context context, String name, boolean defaultValue) {
        if (context == null || name == null)
            return defaultValue;

        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaultValue);
    }

    public static void setInt(Context context, String key, int value) {
        if (context != null && key != null) {
            Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefEditor.putInt(key, value);
            prefEditor.commit();
        }
    }

    public static int getInt(Context context, String key, int defaultValue) {
        if (context != null && key != null)
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);

        return defaultValue;
    }

    public static void setLong(Context context, String key, long value) {
        if (context != null && key != null) {
            Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefEditor.putLong(key, value);
            prefEditor.commit();
        }
    }

    public static long getLong(Context context, String key, long defaultValue) {
        if (context != null && key != null)
            return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defaultValue);

        return defaultValue;
    }

    public static void setString(Context context, String key, String value) {
        if (context != null && key != null) {
            Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefEditor.putString(key, value);
            prefEditor.commit();
        }
    }

    public static String getString(Context context, String key, String defaultValue) {
        if (context != null && key != null)
            return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);

        return defaultValue;
    }

    public static void remove(Context context, String key) {
        if (context != null && key != null) {
            Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefEditor.remove(key);
            prefEditor.commit();
        }
    }

    /**
     * Writes the given object to specified filename under specified directory.
     * 
     * @param object
     *            Object to write.
     * @param directory
     *            Directory under which to create the file.
     * @param fileName
     *            File into which object is written.
     */
    public static void writeObject(Object object, File directory, String fileName) {
        if (object != null && directory != null && fileName != null) {
            if (directory.exists() == false)
                directory.mkdir();

            File cacheFile = new File(directory, fileName);

            if (cacheFile != null) {
                try {
                    ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(cacheFile));
                    oo.writeObject(object);
                    oo.close();
                }
                catch (FileNotFoundException e) {
                    LogWrapper.e(TAG, e.getMessage());
                }
                catch (IOException e) {
                    LogWrapper.e(TAG, e.getMessage());
                }
            }
        }
    }

    /**
     * Recursively reads the directory using {@link java.io.ObjectOutputStream
     * ObjectInputStream}
     * 
     * @param directory
     *            - Directory to read
     * @return
     */
    public static ArrayList<Object> readObjects(File directory) {
        return readObjects(directory, null);
    }

    /**
     * Recursively reads the directory using {@link java.io.ObjectOutputStream
     * ObjectInputStream}
     * 
     * @param directory
     *            - Directory to read
     * @param maxDepth
     *            - Maximum depth to recurse
     * @return
     */
    public static ArrayList<Object> readObjects(File directory, Integer maxDepth) {
        ArrayList<Object> objects = null;

        if (directory != null && directory.isDirectory() && directory.exists() == true) {
            objects = readObjectsInDir(directory, maxDepth);
        }

        return objects;
    }

    private static ArrayList<Object> readObjectsInDir(File directory, Integer depth) {
        ArrayList<Object> objects = null;

        if (depth == null || depth > 0) {
            objects = new ArrayList<Object>();

            String[] fileNames = directory.list();

            for (String fileName : fileNames) {
                File file = new File(directory, fileName);
                if (file.isDirectory()) {
                    ArrayList<Object> childObjects = readObjects(directory, depth != null ? --depth : depth);
                    if (childObjects != null) {
                        objects.addAll(childObjects);
                    }
                }
                else {
                    objects.add(readObject(file));
                }
            }
        }
        return objects;
    }

    public static Object readObject(File file) {
        Object object = null;

        if (file != null && file.exists() == true && file.isFile() == true) {
            try {
                ObjectInputStream oi = new ObjectInputStream(new FileInputStream(file));
                object = oi.readObject();
                oi.close();
            }
            catch (StreamCorruptedException e) {
                LogWrapper.e(TAG, e.getMessage());
            }
            catch (FileNotFoundException e) {
                LogWrapper.e(TAG, e.getMessage());
            }
            catch (IOException e) {
                LogWrapper.e(TAG, e.getMessage());
            }
            catch (ClassNotFoundException e) {
                LogWrapper.e(TAG, e.getMessage());
            }
        }

        return object;
    }

    /**
     * Clears application cache.
     * 
     * @param context
     */
    public static void clear(Context context) {
        if (context != null) {
            LogWrapper.d(TAG, "Clearing cache");

            File cacheDir = context.getCacheDir();

            if (cacheDir != null && cacheDir.isDirectory())
                deleteDir(cacheDir);

            clearSharedPreferences(context);
        }
    }

    public static void clearSharedPreferences(Context context) {
        Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEditor.clear();
        prefEditor.commit();
    }

    public static boolean deleteFile(File file) {
        boolean deleted = false;

        if (file != null && file.exists() == true && file.isFile())
            deleted = file.delete();

        return deleted;
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
                deleteDir(new File(dir, children[i]));
        }
        return dir.delete();
    }

    /**
     * Returns current cache size in bytes.
     * 
     * @param cacheDir
     * @return cache size in bytes.
     */
    public static long size(File cacheDir) {
        long size = 0;

        if (cacheDir != null && cacheDir.isDirectory()) {
            File[] files = cacheDir.listFiles();

            for (File file : files) {
                if (file.isFile())
                    size += file.length();
                else {
                    if (file.isDirectory())
                        size += size(file);
                }
            }
        }

        return size;
    }

    /**
     * Returns size of the specified cache directory in human readable form.
     * 
     * @param cacheDir
     * @return size of the directory
     */
    public static String getHumanReadableCacheSize(File cacheDir) {
        final int BYTE_UNIT = 1024;
        final String[] sizeUnit = { "K", "M" };
        long size = size(cacheDir);

        if (size < BYTE_UNIT)
            return size + " B";

        int exp = (int) (Math.log(size) / Math.log(BYTE_UNIT));

        return String.format(Locale.US, "%.1f %sB", size / Math.pow(BYTE_UNIT, exp), sizeUnit[exp - 1]);
    }
}
