/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEWebService.
 *
 * JEWebService is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEWebService is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.rest;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class IconCache {

    private static IconCache _instance;
    private static LoadingCache<String, ClassIcon> _cache;

    /**
     * Default constructor TODO: make the expire date configurable
     */
    public IconCache() {
        _cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .removalListener(new RemovalListener<String, ClassIcon>() {

                    @Override
                    public void onRemoval(RemovalNotification<String, ClassIcon> notification) {
//                        System.out.println("Remove cached icon for: " + notification.getKey() + " because: " + notification.getCause());
                    }
                })
                .build(new CacheLoader<String, ClassIcon>() {

                    @Override
                    public ClassIcon load(String key) throws Exception {
                        return new ClassIcon(key, null);
                    }
                });
    }

    /**
     * Get the Singleton IconCache. This Singleton is thread save but maybe this
     * will cost to much performace in the future
     *
     * TODO: impleent a worker to check if an usr can be removed after some time
     *
     * @return singleton
     */
    public static synchronized IconCache getInstance() {
        if (IconCache._instance == null) {
            IconCache._instance = new IconCache();
        }
        return IconCache._instance;
    }

    /**
     * Add an new Icon to the Cache
     *
     * @param user idetifier
     * @param ds
     */
    public void addIcon(String user, ClassIcon ds) {
//        _connections.put(username, ds);
        _cache.put(user, ds);
    }

    /**
     * Get the cached icon for the given JEVisClasse. The ClassIcon will habe an
     * emty base54 if its emty
     *
     * @param className
     * @return
     * @throws ExecutionException
     */
    public ClassIcon getIcon(String className) throws ExecutionException {
//        try {
//        System.out.println("From Icon Cache: " + _cache.get(className));
        return _cache.get(className);
//            if (_cache.getIfPresent(className) != null) {
//                return _cache.get(className);
//            } else {
//                throw ErrorBuilder.ErrorBuilder(Response.Status.UNAUTHORIZED.getStatusCode(), 6001, "Missing icon access");
//            }
//        } catch (ExecutionException ex) {
//            Logger.getLogger(DSConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
//            throw ErrorBuilder.ErrorBuilder(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), 6002, "There was an error within the cache.");
//        }
    }
}
