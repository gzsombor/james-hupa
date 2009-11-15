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
package org.apache.hupa.client.mvp.place;

import org.apache.hupa.client.mvp.MainPresenter;
import org.eclipse.swt.widgets.Display;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.inject.Inject;
import com.google.inject.Provider;

import net.customware.gwt.presenter.client.gin.ProvidedPresenterPlace;
import net.customware.gwt.presenter.client.place.PlaceRequest;

public class MainPresenterPlace extends ProvidedPresenterPlace<MainPresenter>{

    private static String FOLDER = "folder";
    private static String SEARCH = "search";
    @Inject
    public MainPresenterPlace(Provider<MainPresenter> presenter) {
        super(presenter);
    }

    @Override
    public String getName() {
        return "Main";
    }

    @Override
    protected void preparePresenter(PlaceRequest request, MainPresenter presenter) {
        String searchValue = request.getParameter(SEARCH, "");
        presenter.getDisplay().getSearchValue().setValue(searchValue);
        GWT.log("Pres=" + request.toString(),null);
        super.preparePresenter(request, presenter);
    }

    @Override
    protected PlaceRequest prepareRequest(PlaceRequest request, MainPresenter presenter) {
        String searchValue = presenter.getDisplay().getSearchValue().getValue();
        if (searchValue != null && searchValue.length() > 0) {
            request = request.with(SEARCH, searchValue);

        }
        GWT.log("Req=" + request.toString(),null);

        return super.prepareRequest(request, presenter);
    }
    
    

}
