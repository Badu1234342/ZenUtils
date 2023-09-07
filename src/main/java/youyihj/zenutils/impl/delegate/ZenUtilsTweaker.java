package youyihj.zenutils.impl.delegate;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.network.NetworkSide;
import crafttweaker.preprocessor.PreprocessorManager;
import crafttweaker.runtime.IScriptProvider;
import crafttweaker.runtime.ITweaker;
import crafttweaker.runtime.ScriptLoader;
import crafttweaker.runtime.events.CrTLoaderLoadingEvent;
import crafttweaker.runtime.events.CrTScriptLoadingEvent;
import crafttweaker.util.IEventHandler;
import youyihj.zenutils.api.reload.ActionReloadCallback;
import youyihj.zenutils.api.reload.IActionReloadCallbackFactory;
import youyihj.zenutils.api.reload.Reloadable;
import youyihj.zenutils.impl.reload.AnnotatedActionReloadCallback;

import java.util.*;

public class ZenUtilsTweaker implements ITweaker {
    private final ITweaker tweaker;
    private boolean freeze = false;
    private final Queue<ActionReloadCallback<?>> reloadableActions = new LinkedList<>();
    private final Map<Class<?>, IActionReloadCallbackFactory<?>> reloadCallbacks = new HashMap<>();

    public ZenUtilsTweaker(ITweaker tweaker) {
        this.tweaker = tweaker;
    }

    @Override
    public void apply(IAction action) {
        ActionReloadCallback<IAction> reloadCallback = getReloadCallback(action);
        boolean reloadable = reloadCallback != null;
        if (!freeze) {
            if (validateAction(action)) return;
            if (reloadable) reloadCallback.beforeApply(false);
            action.apply();
            if (reloadable) reloadCallback.afterApply(false);
        } else if (reloadable) {
            if (validateAction(action)) return;
            reloadCallback.beforeApply(true);
            reloadCallback.applyReload();
            reloadCallback.afterApply(true);
        } else {
            String describe = action.describe();
            if (describe != null && !describe.isEmpty()) {
                CraftTweakerAPI.logInfo("Tried to reload action: " + describe + ", which is not reloadable.");
            }
        }
        if (reloadable && reloadCallback.hasUndoMethod()) {
            reloadableActions.add(reloadCallback);
        }
    }

    @Override
    public void setScriptProvider(IScriptProvider provider) {
        tweaker.setScriptProvider(provider);
    }

    @Override
    public void load() {
        tweaker.load();
    }

    @Override
    public boolean loadScript(boolean isSyntaxCommand, String loaderName) {
        return tweaker.loadScript(isSyntaxCommand, loaderName);
    }

    @Override
    public void loadScript(boolean isSyntaxCommand, ScriptLoader loader) {
        tweaker.loadScript(isSyntaxCommand, loader);
    }

    @Override
    public List<IAction> getActions() {
        return tweaker.getActions();
    }

    @Override
    public void enableDebug() {
        tweaker.enableDebug();
    }

    @Override
    public PreprocessorManager getPreprocessorManager() {
        return tweaker.getPreprocessorManager();
    }

    @Override
    public NetworkSide getNetworkSide() {
        return tweaker.getNetworkSide();
    }

    @Override
    public void setNetworkSide(NetworkSide networkSide) {
        tweaker.setNetworkSide(networkSide);
    }

    @Override
    public void registerLoadStartedEvent(IEventHandler<CrTLoaderLoadingEvent.Started> eventHandler) {
        tweaker.registerLoadStartedEvent(eventHandler);
    }

    @Override
    public void registerLoadFinishedEvent(IEventHandler<CrTLoaderLoadingEvent.Finished> eventHandler) {
        tweaker.registerLoadFinishedEvent(eventHandler);
    }

    @Override
    public void registerLoadAbortedEvent(IEventHandler<CrTLoaderLoadingEvent.Aborted> eventHandler) {
        tweaker.registerLoadAbortedEvent(eventHandler);
    }

    @Override
    public void registerScriptLoadPreEvent(IEventHandler<CrTScriptLoadingEvent.Pre> eventHandler) {
        tweaker.registerScriptLoadPreEvent(eventHandler);
    }

    @Override
    public void registerScriptLoadPostEvent(IEventHandler<CrTScriptLoadingEvent.Post> eventHandler) {
        tweaker.registerScriptLoadPostEvent(eventHandler);
    }

    @Override
    public List<ScriptLoader> getLoaders() {
        return tweaker.getLoaders();
    }

    @Override
    public ScriptLoader getOrCreateLoader(String... nameAndAliases) {
        return tweaker.getOrCreateLoader(nameAndAliases);
    }

    public void freezeActionApplying() {
        this.freeze = true;
    }

    public ITweaker getITweaker() {
        return tweaker;
    }

    public void onReload() {
        while (!reloadableActions.isEmpty()) {
            reloadableActions.poll().undo();
        }
    }

    public Queue<ActionReloadCallback<?>> getReloadableActions() {
        return reloadableActions;
    }

    public <T extends IAction> void addReloadCallback(Class<T> clazz, IActionReloadCallbackFactory<T> callback) {
        reloadCallbacks.put(clazz, callback);
    }

    private boolean validateAction(IAction action) {
        if (!action.validate()) {
            CraftTweakerAPI.logError("Action could not be applied", new UnsupportedOperationException(action.describeInvalid()));
            return true;
        }
        String describe = action.describe();
        if (describe != null && !describe.isEmpty()) {
            CraftTweakerAPI.logInfo(describe);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private ActionReloadCallback<IAction> getReloadCallback(IAction action) {
        for (Class<?> clazz = action.getClass(); IAction.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
            if (reloadCallbacks.containsKey(clazz)) {
                return ((IActionReloadCallbackFactory<IAction>) reloadCallbacks.get(clazz)).create(action);
            } else if (clazz.isAnnotationPresent(Reloadable.class)) {
                reloadCallbacks.put(clazz, AnnotatedActionReloadCallback::new);
                return new AnnotatedActionReloadCallback(action);
            }
        }
        return null;
    }
}
