package ru.spbau.mit;

import java.util.List;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

public class Injector {

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */

    private static HashMap <String, Object> createdClasses = new HashMap<>();
    private static HashSet <String> creatingInProgressClasses = new HashSet<>();

    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        creatingInProgressClasses.add(rootClassName);
        Class<?> rootClass = Class.forName(rootClassName);
        Constructor <?> constructor = rootClass.getConstructors()[0];
        Class [] parameterTypes = constructor.getParameterTypes();
        Object [] parameterObjects = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterClass = parameterTypes[i];
            String className = null;
            if (parameterClass.isInterface()) {
                ArrayList <String> implementationClasses = new ArrayList<>();
                for (String implementationClassName : implementationClassNames) {
                    Class <?> [] interfaces = Class.forName(implementationClassName).getInterfaces();
                    for (int j = 0; j < interfaces.length; j++) {
                        if (interfaces[j].equals(parameterClass)) {
                            implementationClasses.add(implementationClassName);
                        }
                    }
                }
                if (implementationClasses.size() == 0) {
                    throw new ImplementationNotFoundException();
                }
                if (implementationClasses.size() > 1) {
                    throw new AmbiguousImplementationException();
                }
                className = implementationClasses.get(0);
            }
            else {
                className = parameterClass.getCanonicalName();
                if (!implementationClassNames.contains(className)) {
                    throw new ImplementationNotFoundException();
                }
            }
            if (createdClasses.containsKey(className)) {
                parameterObjects[i] = createdClasses.get(className);
            }
            else {
                if (creatingInProgressClasses.contains(className)) {
                    throw new InjectionCycleException();
                }
                parameterObjects[i] = initialize(className, implementationClassNames);
            }
        }
        Object resultClass = constructor.newInstance(parameterObjects);
        creatingInProgressClasses.remove(rootClassName);
        createdClasses.put(rootClassName, resultClass);
        return resultClass;
    }
}