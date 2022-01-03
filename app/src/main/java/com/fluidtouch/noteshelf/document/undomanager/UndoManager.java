package com.fluidtouch.noteshelf.document.undomanager;

import java.io.Serializable;
import java.util.Stack;

public class UndoManager implements Serializable {

    private Stack<NSInvocation> undoStack;
    private Stack<NSInvocation> redoStack;
    private boolean isUndoInProgress;
    private boolean isRedoInProgress;

    public UndoManager() {
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        isUndoInProgress = false;
        isRedoInProgress = false;
    }

    public void addUndo(Class<? extends Object> reflectClass, String selector, int numberOfParams, Object[] params, Object target) {
        NSMethodSignature nsMethodSignature = new NSMethodSignature();
        nsMethodSignature.selector = selector;
        nsMethodSignature.numberOfParams = numberOfParams;

        nsMethodSignature.reflectClass = reflectClass;
        NSInvocation nsInvocation = null;
        if (nsMethodSignature.reflectMethod(nsMethodSignature.reflectClass, nsMethodSignature.selector)) {
            nsInvocation = NSInvocation.invocationWithMethodSignature(nsMethodSignature);
            nsInvocation.setTarget(target);
            for (int i = 0; i < params.length; i++) {
                nsInvocation.setArgument(params[i], 2 + i);
            }
        }

        if (null != nsInvocation) {
            if (!isUndoInProgress || isRedoInProgress) {
                if (!isRedoInProgress) {
                    redoStack.clear();
                }
                undoStack.push(nsInvocation);
            } else {
                redoStack.push(nsInvocation);
            }
        }
    }


    public void undo() {
        if (!undoStack.empty()) {
            isUndoInProgress = true;
            NSInvocation nsInvocation = undoStack.pop();
            nsInvocation.invoke();
            isUndoInProgress = false;
        }
    }

    public void redo() {
        if (!redoStack.empty()) {
            isRedoInProgress = true;
            NSInvocation nsInvocation = redoStack.pop();
            nsInvocation.invoke();
            isRedoInProgress = false;
        }
    }

    public void restTarget(Object target) {
        for (int i = 0; i < undoStack.size(); i++) {
            undoStack.get(i).setTarget(target);
        }
    }

    public boolean canUndo() {
        return undoStack.size() > 0;
    }

    public boolean canRedo() {
        return redoStack.size() > 0;
    }
}
