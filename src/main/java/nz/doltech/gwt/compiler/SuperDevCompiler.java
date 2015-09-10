package nz.doltech.gwt.compiler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.ScriptElement;

import java.util.logging.Logger;

public class SuperDevCompiler {
    private static final Logger logger = Logger.getLogger(SuperDevCompiler.class.getName());

    public static final String CODE_SERVER_URL = "http://127.0.0.1:9876";

    private static SuperDevCompiler superDevCompiler = null;

    public static SuperDevCompiler get() {
        return get(CODE_SERVER_URL);
    }

    public static SuperDevCompiler get(String serverUrl) {
        if(superDevCompiler == null) {
            superDevCompiler = new SuperDevCompiler(GWT.getModuleName(), serverUrl);
        }
        return superDevCompiler;
    }

    public interface InjectedCallback {
        void onInjected();
    }

    public interface StartedCallback {
        void onStarted(String moduleName, String requestUrl);
    }

    public interface PollCallback {
        void onPoll(float startTime);
    }

    public interface CompletedCallback {
        /**
         * Called when the CodeServer successfully compiles.
         * @return true to stop the automatic page refresh.
         */
        boolean onCompleted(JavaScriptObject json);
    }

    public interface FailedCallback {
        void onFailed(String reason, String logUrl);
    }

    private String moduleName;
    private String serverUrl;
    private boolean injected = false;

    private InjectedCallback injectedCallback;
    private StartedCallback startCallback;
    private PollCallback pollCallback;
    private CompletedCallback completeCallback;
    private FailedCallback failedCallback;

    private SuperDevCompiler(String moduleName, String serverUrl) {
        this.moduleName = moduleName;
        this.serverUrl = serverUrl;

        // Setup __gwt_bookmarklet_globals
        setupGlobals();

        // Inject the dev_mode_on.js
        injectDevModeOn();
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    private native void setupGlobals() /*-{
        // Setup globals
        var globals = $wnd.__gwt_bookmarklet_globals;
        if (typeof globals == 'undefined') {
            globals = {
                callback_counter: 0,
                callbacks: {}
            };
            $wnd.__gwt_bookmarklet_globals = globals;
        }
    }-*/;

    /**
     * Invoke a SuperDevMode compilation with auto refresh.
     */
    public void compile() {
        compile(true);
    }

    /**
     * Invoke a SuperDevMode compilation.
     * @param refresh if true will auto refresh module.
     */
    public native void compile(boolean refresh) /*-{
        if (!!$wnd.__gwt_bookmarklet_globals.compiling) {
            // A module is already being compiled.
            return;
        }

        var moduleName = this.@nz.doltech.gwt.compiler.SuperDevCompiler::moduleName;
        var serverUrl = this.@nz.doltech.gwt.compiler.SuperDevCompiler::serverUrl;

        // Compile erorr checking
        var error = null;
        if(moduleName) {
            error = this.@nz.doltech.gwt.compiler.SuperDevCompiler::getCannotCompileError()();
        }
        var logUrl = serverUrl + '/log/' + moduleName;

        if (moduleName && !error) {
            // Probably a regular compile, so check in the page.
            var active_modules = $wnd.__gwt_activeModules;
            getPropMap = active_modules[moduleName].bindings;

            var params = this.@nz.doltech.gwt.compiler.SuperDevCompiler::getBindingParameters(Lcom/google/gwt/core/client/JavaScriptObject;)(getPropMap);
            var urlPrefix = serverUrl + "/recompile/" + moduleName + "?" + params;

            var _this = this;
            var lastPollStart;

            function onPollFinished(event) {
                if ($wnd.__gwt_bookmarklet_globals.compiling && event.status == "compiling") {
                    //dialog.updateProgress(event);
                    // Date.now() fails in IE8
                    var waitTime = 1000 - (new Date().getTime() - lastPollStart);
                    if (waitTime > 0) {
                        setTimeout(poll, waitTime);
                    } else {
                        poll();
                    }
                }
                // otherwise it's idle or an unknown event type, so stop
            }

            function poll() {
                if ($wnd.__gwt_bookmarklet_globals.compiling) {
                    // Date.now() fails in IE8
                    lastPollStart = new Date().getTime();
                    _this.@nz.doltech.gwt.compiler.SuperDevCompiler::onPoll(F)(lastPollStart);
                    _this.@nz.doltech.gwt.compiler.SuperDevCompiler::callJsonp(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(serverUrl + '/progress?', onPollFinished);
                }
            }

            function onCompileCompleted(json) {
                $wnd.__gwt_bookmarklet_globals.compiling = false;
                if (json.status != 'ok') {
                    _this.@nz.doltech.gwt.compiler.SuperDevCompiler::onCompileFailed(Ljava/lang/String;)(logUrl);
                    return;
                }
                if(!_this.@nz.doltech.gwt.compiler.SuperDevCompiler::onCompileCompleted(Lcom/google/gwt/core/client/JavaScriptObject;)(json)) {
                    _this.@nz.doltech.gwt.compiler.SuperDevCompiler::reloadInDevMode()();
                }
            }

            $wnd.__gwt_bookmarklet_globals.compiling = true;

            setTimeout(poll, 1000);
            this.@nz.doltech.gwt.compiler.SuperDevCompiler::onCompileStarted(Ljava/lang/String;Ljava/lang/String;)(moduleName, urlPrefix);
            this.@nz.doltech.gwt.compiler.SuperDevCompiler::callJsonp(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(urlPrefix, onCompileCompleted);
        }
        else {
            this.@nz.doltech.gwt.compiler.SuperDevCompiler::onCompileFailed(Ljava/lang/String;Ljava/lang/String;)(error, logUrl)
        }
    }-*/;

    private void onCompileStarted(String moduleName, String requestUrl) {
        if(startCallback != null) {
            startCallback.onStarted(moduleName, requestUrl);
        }
    }

    private void onPoll(float startTime) {
        if(pollCallback != null) {
            pollCallback.onPoll(startTime);
        }
    }

    private boolean onCompileCompleted(JavaScriptObject json) {
        return completeCallback != null && completeCallback.onCompleted(json);
    }

    private void onCompileFailed(String logUrl) {
        onCompileFailed("GWT compilation failed, check the logs for the reason why.", logUrl);
    }

    private void onCompileFailed(String reason, String log) {
        if(failedCallback != null) {
            failedCallback.onFailed(reason, log);
        }
    }

    private native String getBindingParameters(JavaScriptObject getPropMap) /*-{
        var session_key = '__gwtDevModeSession:' + this.@nz.doltech.gwt.compiler.SuperDevCompiler::moduleName;

        var prop_map = getPropMap();
        var props = [];
        for (var key in prop_map) {
            props.push(encodeURIComponent(key) + '=' + encodeURIComponent(prop_map[key]));
        }

        var params;
        if (!props.length) {
            // There is only one permutation, maybe because we're in dev mode already.
            // Use the cached value if present.
            var cached = window.sessionStorage[session_key];
            return cached || '';
        }

        var encoded = props.join('&') + '&';
        // Cache it for the next recompile.
        window.sessionStorage[session_key] = encoded;
        return encoded;
    }-*/;

    private native void callJsonp(String urlPrefix, JavaScriptObject callback) /*-{
        var callbackId = 'c' + $wnd.__gwt_bookmarklet_globals.callback_counter++;
        $wnd.__gwt_bookmarklet_globals.callbacks[callbackId] = function(json) {
            delete $wnd.__gwt_bookmarklet_globals.callbacks[callbackId];
            callback(json);
        };

        var url = urlPrefix + '_callback=__gwt_bookmarklet_globals.callbacks.' +
            callbackId;

        var script = $doc.createElement('script');
        script.src = url;
        $doc.getElementsByTagName('head')[0].appendChild(script);
    }-*/;

    /**
     * Determines if the code server is configured to run the given module.
     * @return {boolean} true if the code server supports the given module.
     */
    private native boolean isModuleOnCodeServer() /*-{
        try {
            var modules_on_codeserver = $wnd.__gwt_codeserver_config.moduleNames;
            // Support browsers without indexOf() (e.g. IE8).
            for (var i = 0; i < modules_on_codeserver.length; i++) {
                if (modules_on_codeserver[i] == this.@nz.doltech.gwt.compiler.SuperDevCompiler::moduleName) {
                    return true;
                }
            }
        } catch(e) {}
        return false;

    }-*/;

    /**
     * Determines whether we can recompile a module and see the results. If not,
     * explains why not.
     * @return {string} The error message, or null if there is no error and
     *     a recompile will succeed.
     */
    private native String getCannotCompileError() /*-{
        if (!this.@nz.doltech.gwt.compiler.SuperDevCompiler::isModuleOnCodeServer()()) {
            return 'The code server isn\'t configured to compile this module.';
        }
        var moduleName = this.@nz.doltech.gwt.compiler.SuperDevCompiler::moduleName;

        var modules_on_page = $wnd.__gwt_activeModules;
        if (!modules_on_page || !(moduleName in modules_on_page)) {
            return 'The current page doesn\'t have this module.';
        }

        var mod = modules_on_page[moduleName];

        var dev_mode_key = '__gwtDevModeHook:' + moduleName;
        var dev_mode_on = mod['superdevmode'] ||
            window.sessionStorage[dev_mode_key];

        if (!dev_mode_on && !mod.canRedirect) {
            return 'This module doesn\'t have Super Dev Mode enabled.';
        }

        // looks okay
        return null;
    }-*/;

    /**
     * Tells the GWT application to replace itself with a different version that
     * the code server is serving, then reloads the page. (This only works if
     * the GWT application was compiled with the new dev mode hook turned on in
     * the GWT linker.)
     */
    public native void reloadInDevMode() /*-{
        var moduleName = this.@nz.doltech.gwt.compiler.SuperDevCompiler::moduleName;
        var serverUrl = this.@nz.doltech.gwt.compiler.SuperDevCompiler::serverUrl;

        var key = '__gwtDevModeHook:' + moduleName;
        window.sessionStorage[key] = serverUrl + '/' + moduleName + '/' +
            moduleName + '.nocache.js';
        $wnd.location.reload();
    }-*/;

    /**
     * Turns dev mode off for the given module, then reloads the page.
     * @param moduleName The modules name
     */
    private native void  reloadWithoutDevMode(String moduleName) /*-{
        var key = '__gwtDevModeHook:' + this.@nz.doltech.gwt.compiler.SuperDevCompiler::moduleName;
        window.sessionStorage.removeItem(key);
        $wnd.location.reload();
    }-*/;

    public boolean isDevModeInjected() {
        return injected || isModuleOnCodeServer();
    }

    public Element getDevModeOnScriptElement() {
        HeadElement head = Document.get().getHead();
        NodeList<Node> childNodes = head.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.getItem(i);
            if(Element.is(childNode)) {
                Element child = childNode.cast();
                if (ScriptElement.is(child)) {
                    ScriptElement scriptElement = ScriptElement.as(child);
                    String scriptSrc = scriptElement.getSrc();
                    if (scriptSrc != null && scriptSrc.contains("dev_mode_on.js")) {
                        return child;
                    }
                }
            }
        }
        return null;
    }

    private native void setCompiling(boolean compiling) /*-{
        $wnd.__gwt_bookmarklet_globals.compiling = compiling;
    }-*/;

    private native void injectDevModeOn() /*-{
        $wnd.__gwt_bookmarklet_globals.compiling = true;

        var serverUrl = this.@nz.doltech.gwt.compiler.SuperDevCompiler::serverUrl;
        var moduleName = this.@nz.doltech.gwt.compiler.SuperDevCompiler::moduleName;
        $wnd.__gwt_bookmarklet_params = {'server_url': serverUrl + '/', 'module_name':moduleName};
        var s = $wnd.document.createElement('script');
        s.src = serverUrl + '/dev_mode_on.js';
        void($wnd.document.getElementsByTagName('head')[0].appendChild(s));

        this.@nz.doltech.gwt.compiler.SuperDevCompiler::onInjected()();
    }-*/;

    private void onInjected() {
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
            int maxTries = 0;
            @Override
            public boolean execute() {
                Element devModeOn = getDevModeOnScriptElement();
                if (devModeOn != null && isModuleOnCodeServer()) {
                    logger.fine("dev_mode_on.js is now injected! (took " + maxTries + " tries)");
                    devModeOn.removeFromParent();

                    injected = true;
                    if (injectedCallback != null) {
                        injectedCallback.onInjected();
                    }

                    setCompiling(false);
                    return false;
                } else {
                    logger.fine("dev_mode_on.js is not injected yet! (" + maxTries + " tries)");
                    boolean maxed = maxTries++ < 20;
                    setCompiling(!maxed);
                    return maxed;
                }
            }
        }, 200);
    }

    public void setInjectedCallback(InjectedCallback injectedCallback) {
        this.injectedCallback = injectedCallback;
        if(isDevModeInjected()) {
            injectedCallback.onInjected();
        }
    }

    public void setCompileStartCallback(StartedCallback startCallback) {
        this.startCallback = startCallback;
    }

    public void setPollCallback(PollCallback pollCallback) {
        this.pollCallback = pollCallback;
    }

    public void setCompileCompleteCallback(CompletedCallback completeCallback) {
        this.completeCallback = completeCallback;
    }

    public void setCompileFailedCallback(FailedCallback failedCallback) {
        this.failedCallback = failedCallback;
    }
}
