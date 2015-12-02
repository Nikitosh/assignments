package ru.spbau.mit;

import java.util.*;
import java.lang.reflect.Constructor;

public class Injector {

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */

    private static HashMap <String, Object> createdClasses = new HashMap<>();
    private static HashSet <String> creatingInProgressClasses = new HashSet<>();

    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        creatingInProgressClasses.add(rootClassName);
        Constructor <?> constructor = Class.forName(rootClassName).getConstructors()[0];
        Class [] parameterTypes = constructor.getParameterTypes();
        Object [] parameterObjects = new Object[parameterTypes.length];

        ArrayList <String> extendedImplementationClassNames = new ArrayList<String>();
        extendedImplementationClassNames.addAll(implementationClassNames);
        if (!implementationClassNames.contains(rootClassName))
            extendedImplementationClassNames.add(rootClassName);

        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterClass = parameterTypes[i];
            String className = null;
            ArrayList <String> implementationClasses = new ArrayList<>();
            for (String newImplementationClassName : extendedImplementationClassNames) {
                if (parameterClass.isAssignableFrom(Class.forName(newImplementationClassName))) {
                    implementationClasses.add(newImplementationClassName);
                }
            }
            if (implementationClasses.size() == 0) {
                throw new ImplementationNotFoundException();
            }
            if (implementationClasses.size() > 1) {
                throw new AmbiguousImplementationException();
            }
            className = implementationClasses.get(0);
            if (createdClasses.containsKey(className)) {
                parameterObjects[i] = createdClasses.get(className);
            }
            else {
                if (creatingInProgressClasses.contains(className)) {
                    throw new InjectionCycleException();
                }
                parameterObjects[i] = initialize(className, extendedImplementationClassNames);
            }
        }

        Object resultClass = constructor.newInstance(parameterObjects);
        creatingInProgressClasses.remove(rootClassName);
        createdClasses.put(rootClassName, resultClass);
        return resultClass;
    }
}