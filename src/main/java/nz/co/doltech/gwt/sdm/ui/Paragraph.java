/**
 * Copyright 2015 Doltech Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package nz.co.doltech.gwt.sdm.ui;

import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.user.client.ui.HTMLPanel;

public class Paragraph extends HTMLPanel {

    public Paragraph() {
        super(ParagraphElement.TAG, "");
    }

    public Paragraph(final String html) {
        this();
        setHTML(html);
    }

    public void setHTML(final String html) {
        getElement().setInnerHTML(html);
    }

    public String getHTML() {
        return getElement().getInnerHTML();
    }
}
