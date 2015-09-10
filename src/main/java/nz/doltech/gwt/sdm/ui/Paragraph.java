package nz.doltech.gwt.sdm.ui;

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
