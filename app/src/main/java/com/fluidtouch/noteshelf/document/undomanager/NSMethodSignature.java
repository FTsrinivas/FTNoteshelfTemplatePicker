package com.fluidtouch.noteshelf.document.undomanager;

import java.lang.reflect.Method;


public class NSMethodSignature {
    public Class<? extends Object> reflectClass;
    public String selector;
    public int numberOfParams = 0;
    Method method;
    boolean isValid = false;
    private int numberOfArguments;

    public int numberOfArguments() {
        return numberOfArguments;
    }

    public boolean reflectMethod(Class<? extends Object> reflectClass, String selector) {
        this.reflectClass = reflectClass;
        String methodName;
        methodName = selector;


        Method[] ms = reflectClass.getMethods();
        for (int i = 0; i < ms.length; i++) {
            Method method = ms[i];
            if (method.getName().equals(methodName)) {
                Object[] params = method.getParameterTypes();
                if (params.length == numberOfParams) {
                    this.method = method;
                    this.selector = selector;
                    numberOfArguments = params.length;
                    isValid = true;
                    break;
                }
            }
        }
        if (!isValid) {
            throw new IllegalArgumentException("No Such Method");
        }
        return isValid;
    }

}