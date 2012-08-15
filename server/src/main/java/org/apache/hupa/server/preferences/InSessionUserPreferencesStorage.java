/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.hupa.server.preferences;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.hupa.server.IMAPStoreCache;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A preferences storage which uses session as repository data
 */
public class InSessionUserPreferencesStorage extends UserPreferencesStorage {

    private final Provider<HttpSession> sessionProvider;
    
    @Inject
    public InSessionUserPreferencesStorage(IMAPStoreCache cache, Log logger, Provider<HttpSession> sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    @Override
    public UserPreferences getPreferences() {
        HttpSession session = sessionProvider.get();
        Object prefs = session.getAttribute(CONTACTS_ATTR);
        if (prefs instanceof UserPreferences) {
            return (UserPreferences) prefs;
        } else {
            UserPreferences up = new UserPreferences();
            session.setAttribute(CONTACTS_ATTR, up);
            return up;
        }
    }
    
    @Override
    public void storePreferences() {
        UserPreferences preferences = getPreferences();
        sessionProvider.get().setAttribute(CONTACTS_ATTR, preferences);
        preferences.clearChanged();
    }
    
}
