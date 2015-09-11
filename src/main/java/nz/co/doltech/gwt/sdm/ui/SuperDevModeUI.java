package nz.co.doltech.gwt.sdm.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import nz.co.doltech.gwt.sdm.SuperDevCompiler;
import nz.co.doltech.gwt.sdm.SuperDevCompiler.*;
import nz.co.doltech.gwt.sdm.util.StyleUtil;

public class SuperDevModeUI extends Composite {

    private static final UiBinder uibinder = GWT.create(UiBinder.class);

    public interface UiBinder extends com.google.gwt.uibinder.client.UiBinder<Widget, SuperDevModeUI> {}

    private static final Resources resources = GWT.create(Resources.class);

    interface Resources extends ClientBundle {
        interface SuperDevModeStyle extends CssResource {
            String sdm();

            @ClassName("compile-btn")
            String compileBtn();

            @ClassName("error-panel")
            String errorPanel();
        }

        @Source("superdevmode.css")
        SuperDevModeStyle style();
    }

    @UiField HTMLPanel sdmPanel;
    @UiField Button btnCompile;

    private boolean showErrorLog = true;

    private SuperDevCompiler superDevCompiler = SuperDevCompiler.get();

    private AbsolutePanel progressPanel;

    public SuperDevModeUI() {
        resources.style().ensureInjected();

        superDevCompiler.setPollCallback(new PollCallback() {
            @Override
            public void onPoll(float startTime) {
                if (progressPanel != null && progressPanel.getWidgetCount() > 0) {
                    Paragraph paragraph = (Paragraph) progressPanel.getWidget(0);
                    paragraph.setHTML(paragraph.getHTML() + ".");
                }
            }
        });

        superDevCompiler.setCompileStartCallback(new StartedCallback() {
            @Override
            public void onStarted(String moduleName, String requestUrl) {
                progressPanel = new AbsolutePanel();
                progressPanel.add(new Paragraph("Compiling " + moduleName));
                showMessagePanel(progressPanel);
            }
        });

        superDevCompiler.setCompileCompleteCallback(new CompletedCallback() {
            @Override
            public boolean onCompleted(JavaScriptObject json) {
                compilationStopped();
                return false;
            }
        });

        superDevCompiler.setCompileFailedCallback(new FailedCallback() {
            @Override
            public void onFailed(String reason, String logUrl) {
                btnCompile.setText("Try Again");

                AbsolutePanel content = new AbsolutePanel();
                content.getElement().getStyle().setColor("red");
                content.add(new Paragraph(reason));
                Widget error;
                if(!showErrorLog) {
                    error = new Anchor("View Error Log", logUrl);
                    ((Anchor)error).setTarget("_blank");
                } else {
                    error = new Frame(logUrl);
                    error.setWidth("730px");
                    error.setHeight("300px");
                }
                content.add(error);
                showMessagePanel(content);

                compilationStopped();
            }
        });

        RootPanel.get().addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_F5) {
                    event.preventDefault();
                    startCompile();
                }
            }
        }, KeyDownEvent.getType());

        initWidget(uibinder.createAndBindUi(this));
    }

    @UiHandler("btnCompile")
    protected void btnCompileClick(ClickEvent event) {
        startCompile();
    }

    public void showMessagePanel(AbsolutePanel content) {
        PopupPanel popupPanel = new PopupPanel(true);
        popupPanel.addStyleName(resources.style().errorPanel());
        popupPanel.add(content);
        popupPanel.setAnimationEnabled(true);
        popupPanel.setGlassEnabled(true);
        sdmPanel.add(popupPanel);
        popupPanel.center();

        Style popupStyle = popupPanel.getElement().getStyle();
        double top = StyleUtil.getMeasurementValue(popupStyle.getTop());
        Unit unit = StyleUtil.getMeasurementUnit(popupStyle.getTop());
        popupStyle.setTop(top - 75, unit);

        popupPanel.show();
    }

    private void compilationStopped() {
        if(progressPanel != null && progressPanel.isAttached()) {
            progressPanel.removeFromParent();
        }
        progressPanel = null;
    }

    public HTMLPanel getPanel() {
        return sdmPanel;
    }

    public void disableUI() {
        sdmPanel.clear();
    }

    public boolean isShowErrorLog() {
        return showErrorLog;
    }

    public void setShowErrorLog(boolean showErrorLog) {
        this.showErrorLog = showErrorLog;
    }

    public void startCompile() {
        btnCompile.setText("Compiling...");
        superDevCompiler.compile();
    }
}
