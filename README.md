# SuperDevModeUI
GWT wrapper for the super dev mode compilation invoker, with some of my own sugar! Since I use Tomcat the SuperDevMode 
doesn't recompile when I refresh the page, so I made it attach a key down handler to the RootPanel to auto invoke a 
compile when pressing F5. I also added a ghost button that will sit in the top left corner of the page that can invoke
a recompile.

## How to use
Add the `nz.co.doltech.gwt:sdm-compiler` dependency to your project.

With maven:
```xml
<dependency>
    <groupId>nz.co.doltech.gwt</groupId>
    <artifactId>sdm-compiler</artifactId>
    <version>1.0</version>
</dependency>
```

Next you need to add the GWT module like so:
```xml
<inherits name="nz.co.doltech.gwt.SuperDevMode"/>
```

Now you can implement the `SuperDevModeUI` or simply manipulate the `SuperDevCompiler`.
```java
SuperDevCompiler.get().setInjectedCallback(new InjectedCallback() {
    @Override
    public void onInjected() {
        SuperDevModeUI superDevModeUI = GWT.create(SuperDevModeUI.class);
        RootPanel.get().add(superDevModeUI);
    }
});
```
This will wait to ensure dev mode is available before adding the `SuperDevModeUI`.

You can also hide the UI components if you simply want the key binding, like so:
```java
SuperDevModeUI superDevModeUI = GWT.create(SuperDevModeUI.class);
superDevModeUI.disableUI();
```

![Example One](https://dl.dropboxusercontent.com/u/49948294/wiki/sdm_compiler/sdm-ui-1.png)

![Example Two](https://dl.dropboxusercontent.com/u/49948294/wiki/sdm_compiler/sdm-ui-2.png)

Report any issues and happy hacking!