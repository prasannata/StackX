/*
    Copyright (C) 2012 Prasanna Thirumalai
    
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
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.prasanna.android.stacknetwork.fragment.SettingsFragment;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.model.User.UserType;

public class SharedPreferencesUtil
{
    private static final String TAG = SharedPreferencesUtil.class.getSimpleName();
    private static final int BYTE_UNIT = 1024;
    private static String userAccessToken;
    private static final String[] sizeUnit =
    { "K", "M" };

    public static class CacheFileName
    {
        public static final String SITE_CACHE_FILE_NAME = "sites";
        public static final String REGD_SITE_CACHE_FILE_NAME = "registeredSites";
    }

    public static boolean isFirstRun(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                StringConstants.IS_FIRST_RUN, true);
    }

    public static void setFirstRunComplete(Context context)
    {
        if (context != null)
        {
            Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefEditor.putBoolean(StringConstants.IS_FIRST_RUN, false);
            prefEditor.commit();
        }
    }

    public static void clearDefaultSite(Context context)
    {
        SharedPreferencesUtil.setDefaultSiteName(context, null);
        SharedPreferencesUtil.removeDefaultSite(context);
    }

    public static void setDefaultSiteName(Context context, String name)
    {
        Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEditor.putString(SettingsFragment.KEY_PREF_DEFAULT_SITE, name);
        prefEditor.commit();
    }

    public static String getDefaultSiteName(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                SettingsFragment.KEY_PREF_DEFAULT_SITE, null);
    }

    public static void setDefaultSite(Context context, Site site)
    {
        if (site != null && context != null)
        {
            File dir = new File(context.getCacheDir(), StringConstants.DEFAULTS);
            writeObject(site, dir, StringConstants.SITE);
        }
    }

    public static Site getDefaultSite(Context context)
    {
        if (context != null)
        {
            File dir = new File(context.getCacheDir(), StringConstants.DEFAULTS);
            if (dir.exists() && dir.isDirectory())
            {
                File file = new File(dir, StringConstants.SITE);
                if (file.exists() && file.isFile())
                {
                    return (Site) readObject(file);
                }

            }
        }

        return null;
    }

    public static void removeDefaultSite(Context context)
    {
        if (context != null)
        {
            File dir = new File(context.getCacheDir(), StringConstants.DEFAULTS);
            if (dir.exists() && dir.isDirectory())
            {
                File file = new File(dir, StringConstants.SITE);
                if (file.exists() && file.isFile())
                {
                    deleteFile(file);
                }
            }
        }
    }

    public static boolean hasSiteListCache(File cacheDir)
    {
        boolean present = false;
        File file = new File(cacheDir, CacheFileName.SITE_CACHE_FILE_NAME);

        if (file != null)
        {
            present = file.exists();
        }

        Log.d(TAG, "Sites cached: " + present);
        return present;
    }

    public static void cacheSiteList(File cacheDir, ArrayList<Site> sites)
    {
        if (cacheDir != null && sites != null && sites.isEmpty() == false)
        {
            Log.d(TAG, "Caching sites");

            writeObject(sites, cacheDir, CacheFileName.SITE_CACHE_FILE_NAME);

            ArrayList<Site> registeredSites = new ArrayList<Site>();

            for (Site site : sites)
            {
                if (site.userType.equals(UserType.REGISTERED))
                {
                    registeredSites.add(site);
                }
            }

            SoftReference<ArrayList<Site>> registeredSitesSoftReference = new SoftReference<ArrayList<Site>>(
                    registeredSites);

            if (!registeredSites.isEmpty())
                cacheRegisteredSites(cacheDir, registeredSitesSoftReference.get());

        }
    }

    public static void clearSiteListCache(File cacheDir)
    {
        if (cacheDir != null)
        {
            deleteDir(new File(cacheDir, CacheFileName.SITE_CACHE_FILE_NAME));
            deleteDir(new File(cacheDir, CacheFileName.REGD_SITE_CACHE_FILE_NAME));
        }
    }

    public static void cacheQuestion(File cacheDir, Question question)
    {
        Log.d(TAG, "Caching question");

        if (question != null && question.id > 0)
        {
            File directory = new File(cacheDir, StringConstants.QUESTIONS);

            writeObject(question, directory, String.valueOf(question.id));
        }
    }

    public static void cacheRegisteredSites(File cacheDir, ArrayList<Site> sites)
    {
        Log.d(TAG, "Caching registered sites");

        HashMap<String, Site> sitesMap = new HashMap<String, Site>();

        for (Site site : sites)
        {
            sitesMap.put(site.name, site);
        }

        writeObject(sitesMap, cacheDir, CacheFileName.REGD_SITE_CACHE_FILE_NAME);
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Site> getSiteListFromCache(File cacheDir)
    {
        Log.d(TAG, cacheDir.toString());
        return (ArrayList<Site>) readObject(new File(cacheDir, CacheFileName.SITE_CACHE_FILE_NAME));
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Site> getRegisteredSitesForUser(File cacheDir)
    {
        Log.d(TAG, cacheDir.toString());
        return (HashMap<String, Site>) readObject(new File(cacheDir,
                CacheFileName.REGD_SITE_CACHE_FILE_NAME));
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
    public static void writeObject(Object object, File directory, String fileName)
    {
        if (object != null && directory != null && fileName != null)
        {
            if (directory.exists() == false)
            {
                directory.mkdir();
            }

            File cacheFile = new File(directory, fileName);

            if (cacheFile != null)
            {
                try
                {
                    ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(cacheFile));
                    oo.writeObject(object);
                    oo.close();
                }
                catch (FileNotFoundException e)
                {
                    Log.e(TAG, e.getMessage());
                }
                catch (IOException e)
                {
                    Log.e(TAG, e.getMessage());
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
    public static ArrayList<Object> readObjects(File directory)
    {
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
    public static ArrayList<Object> readObjects(File directory, Integer maxDepth)
    {
        ArrayList<Object> objects = null;

        if (directory != null && directory.isDirectory() && directory.exists() == true)
        {
            objects = readObjectsInDir(directory, maxDepth);
        }

        return objects;
    }

    private static ArrayList<Object> readObjectsInDir(File directory, Integer depth)
    {
        ArrayList<Object> objects = null;

        if (depth == null || depth > 0)
        {
            objects = new ArrayList<Object>();

            String[] fileNames = directory.list();

            for (String fileName : fileNames)
            {
                File file = new File(directory, fileName);
                if (file.isDirectory())
                {
                    ArrayList<Object> childObjects = readObjects(directory, depth != null ? --depth
                            : depth);
                    if (childObjects != null)
                    {
                        objects.addAll(childObjects);
                    }
                }
                else
                {
                    objects.add(readObject(file));
                }
            }
        }
        return objects;
    }

    public static Object readObject(File file)
    {
        Object object = null;

        if (file != null && file.exists() == true && file.isFile() == true)
        {
            Log.d(TAG, "Fetch cached objects");

            try
            {
                ObjectInputStream oi = new ObjectInputStream(new FileInputStream(file));
                object = oi.readObject();
                oi.close();
            }
            catch (StreamCorruptedException e)
            {
                Log.e(TAG, e.getMessage());
            }
            catch (FileNotFoundException e)
            {
                Log.e(TAG, e.getMessage());
            }
            catch (IOException e)
            {
                Log.e(TAG, e.getMessage());
            }
            catch (ClassNotFoundException e)
            {
                Log.e(TAG, e.getMessage());
            }
        }

        return object;
    }

    /**
     * Clears application cache.
     * 
     * @param context
     */
    public static void clear(Context context)
    {
        if (context != null)
        {
            Log.d(TAG, "Clearing cache");

            File cacheDir = context.getCacheDir();

            if (cacheDir != null && cacheDir.isDirectory())
            {
                deleteDir(cacheDir);
            }

            clearSharedPreferences(context);
        }
    }

    public static void clearSharedPreferences(Context context)
    {
        Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEditor.clear();
        prefEditor.commit();
        userAccessToken = null;
    }

    public static boolean deleteFile(File file)
    {
        boolean deleted = false;

        if (file != null && file.exists() == true && file.isFile())
        {
            deleted = file.delete();
        }

        return deleted;
    }

    public static boolean deleteDir(File dir)
    {
        if (dir != null && dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                deleteDir(new File(dir, children[i]));
            }
        }
        return dir.delete();
    }

    public static void cacheAccessToken(Context context, String accessToken)
    {
        if (accessToken != null)
        {
            Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefEditor.putString(StringConstants.ACCESS_TOKEN, accessToken);
            prefEditor.commit();
            userAccessToken = accessToken;
        }
    }

    /**
     * Returns the access token for current logged in user.
     * 
     * @param context
     * @return access token
     */
    public static String getAccessToken(Context context)
    {
        if (userAccessToken == null && context != null)
        {
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
            userAccessToken = sharedPreferences.getString(StringConstants.ACCESS_TOKEN, null);
        }

        return userAccessToken;
    }

    public static void loadAccessToken(Context context)
    {
        getAccessToken(context);
    }

    /**
     * Returns current cache size in bytes.
     * 
     * @param cacheDir
     * @return cache size in bytes.
     */
    public static long size(File cacheDir)
    {
        long size = 0;

        if (cacheDir != null && cacheDir.isDirectory())
        {
            File[] files = cacheDir.listFiles();

            for (File file : files)
            {
                if (file.isFile())
                {
                    size += file.length();
                }
                else
                {
                    if (file.isDirectory())
                    {
                        size += size(file);
                    }
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
    public static String getHumanReadableCacheSize(File cacheDir)
    {
        long size = size(cacheDir);

        if (size < BYTE_UNIT)
        {
            return size + " B";
        }

        int exp = (int) (Math.log(size) / Math.log(BYTE_UNIT));

        return String.format(Locale.US, "%.1f %sB", size / Math.pow(BYTE_UNIT, exp),
                sizeUnit[exp - 1]);
    }

    public static String getQuestionDirSize(File cacheDir)
    {
        return getHumanReadableCacheSize(new File(cacheDir, StringConstants.QUESTIONS));
    }

    public static void deleteQuestion(File cacheDir, long questionId)
    {
        File directory = new File(cacheDir, StringConstants.QUESTIONS);
        deleteFile(new File(directory, String.valueOf(questionId)));
    }

    public static void deleteAllQuestions(File cacheDir)
    {
        File directory = new File(cacheDir, StringConstants.QUESTIONS);
        deleteDir(directory);
    }

    public static void cacheMe(File cacheDir, User user)
    {
        if (cacheDir != null && user != null)
        {
            File dir = new File(cacheDir, StringConstants.ME);
            writeObject(user, dir, OperatingSite.getSite().name + "." + StringConstants.ME);
        }
    }

    public static User getMe(File cacheDir)
    {
        if (cacheDir != null)
        {
            File dir = new File(cacheDir, StringConstants.ME);
            File file = new File(dir, OperatingSite.getSite().name + "." + StringConstants.ME);
            if (file.exists()) return (User) readObject(file);
        }

        return null;
    }

    public static void cacheMeAccounts(File cacheDir, ArrayList<Account> accounts)
    {
        if (cacheDir != null && accounts != null)
        {
            File dir = new File(cacheDir, StringConstants.ME);
            writeObject(accounts, dir, StringConstants.ACCOUNTS);
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Account> getMeAccounts(File cacheDir)
    {
        if (cacheDir != null)
        {
            File dir = new File(cacheDir, StringConstants.ME);
            File file = new File(dir, StringConstants.ACCOUNTS);
            if (file.exists()) return (ArrayList<Account>) readObject(file);
        }

        return null;
    }
}
