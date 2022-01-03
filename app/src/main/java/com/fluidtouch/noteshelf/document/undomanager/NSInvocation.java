package com.fluidtouch.noteshelf.document.undomanager;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;

public class NSInvocation {
    private NSMethodSignature sig;
    private Object target = null;
    private Object[] arguments;

    private NSInvocation(NSMethodSignature sig) {
        this.sig = sig;
        arguments = new Object[sig.numberOfArguments() + 2];
        arguments[0] = target;
        arguments[1] = sig.selector;
    }

    public static NSInvocation invocationWithMethodSignature(NSMethodSignature sig) {
        return new NSInvocation(sig);
    }

    public Object target() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
        arguments[0] = target;
    }

    public void setArgument(Object argumentLocation, int idx) {
        if (idx < arguments.length) {
            arguments[idx] = argumentLocation;
        }

    }

    public Object getArgument(int idx) {
        if (idx < arguments.length) {
            return arguments[idx];
        } else {
            return null;
        }
    }

    public void invoke() {
        Object[] args = new Object[arguments.length - 2];
        for (int i = 0; i < arguments.length - 2; i++) {
            args[i] = arguments[i + 2];
        }
        try {
            sig.method.invoke(target, args);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.i(NSInvocation.class.getName(),"exception: "+ e.getMessage());
        }
    }

    public String selector() {
        return sig.selector;
    }

    public void invokeWithTarget(Object target) {
        this.setTarget(target);
        this.invoke();
    }

    public int getParamsCount() {
        return sig.numberOfArguments();
    }

}







