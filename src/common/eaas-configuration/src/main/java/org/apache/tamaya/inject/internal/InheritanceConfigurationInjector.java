package org.apache.tamaya.inject.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Priority;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.functions.Supplier;
import org.apache.tamaya.inject.ConfigurationInjector;
import org.apache.tamaya.inject.api.ConfigAutoInject;
import org.apache.tamaya.inject.spi.ConfiguredType;

@Priority(100)
public class InheritanceConfigurationInjector implements ConfigurationInjector {
    protected DefaultConfigurationInjector wrappedInjector;
    protected Field configuredTypesField;
    protected Method isConfigAnnotatedMethod;
    
    public InheritanceConfigurationInjector() {
        try {
            wrappedInjector = new DefaultConfigurationInjector();
            
            configuredTypesField = wrappedInjector.getClass().getDeclaredField("configuredTypes");
            configuredTypesField.setAccessible(true);
            
            isConfigAnnotatedMethod = wrappedInjector.getClass().getDeclaredMethod("isConfigAnnotated", Class.class);
            isConfigAnnotatedMethod.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    protected Map<Class<?>, ConfiguredType> getConfiguredTypes() {
        try {
            return (Map<Class<?>, ConfiguredType>)configuredTypesField.get(wrappedInjector);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    protected boolean isConfigAnnotated(Class<?> type) {
        try {
            return (Boolean)isConfigAnnotatedMethod.invoke(wrappedInjector, type);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public <T> T configure(T instance) {
        return configure(instance, ConfigurationProvider.getConfiguration());
    }

    @Override
    public <T> T configure(T instance, Configuration config) {
        Class<?> type = Objects.requireNonNull(instance).getClass();
        registerType(type);

        return wrappedInjector.configure(instance, config);
    }

    @Override
    public <T> T createTemplate(Class<T> templateType) {
        return wrappedInjector.createTemplate(templateType);
    }

    @Override
    public <T> T createTemplate(Class<T> templateType, Configuration config) {
        return wrappedInjector.createTemplate(templateType, config);
    }

    @Override
    public <T> Supplier<T> getConfiguredSupplier(Supplier<T> supplier) {
        return wrappedInjector.getConfiguredSupplier(supplier);
    }

    @Override
    public <T> Supplier<T> getConfiguredSupplier(Supplier<T> supplier, Configuration config) {
        return wrappedInjector.getConfiguredSupplier(supplier, config);
    }
    
    public ConfiguredType registerType(Class<?> type) {
        ConfiguredType confType = getConfiguredTypes().get(type);
        if (confType == null) {
            if(!isConfigAnnotated(type) && !wrappedInjector.isAutoConfigureEnabled()){
                return null;
            }
            confType = new InheritanceConfiguredTypeImpl(type);
            getConfiguredTypes().put(type, confType);
            InjectionHelper.sendConfigurationEvent(confType);
        }
        return confType;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static class InheritanceConfiguredTypeImpl extends ConfiguredTypeImpl {
        public InheritanceConfiguredTypeImpl(Class type) {
            // register actual runtime type
            super(type);
            
            // also add all parent classes
            try {
                Method initFieldsMethod = ConfiguredTypeImpl.class.getDeclaredMethod("initFields", Class.class, boolean.class);
                initFieldsMethod.setAccessible(true);
                Method initMethodsMethod = ConfiguredTypeImpl.class.getDeclaredMethod("initMethods", Class.class, boolean.class);
                initMethodsMethod.setAccessible(true);
                
                Class superclass = type.getSuperclass();
                while (superclass != null) {
                    if (!isConfigured(type)) {
                        initFieldsMethod.invoke(this, superclass, true);
                        initMethodsMethod.invoke(this, superclass, true);
                    } else {
                        ConfigAutoInject autoInject = (ConfigAutoInject) type.getAnnotation(ConfigAutoInject.class);
                        if (autoInject != null) {
                            initFieldsMethod.invoke(this, superclass, autoInject != null);
                            initMethodsMethod.invoke(this, superclass, autoInject != null);
                        } else {
                            initFieldsMethod.invoke(this, superclass, false);
                            initMethodsMethod.invoke(this, superclass, false);
                        }
                    }
                    superclass = superclass.getSuperclass();
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
