package com.alibaba.com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.AbstractHessianDecoder;
import io.github.wuwen5.hessian.io.BaseDeserializer;
import io.github.wuwen5.hessian.io.FieldDeserializer2Factory;
import io.github.wuwen5.hessian.io.IOExceptionWrapper;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serializing an object for known object types.
 */
public class RecordDeserializer extends BaseDeserializer implements Deserializer {

    private RecordUtil.RecordComponent[] _components;
    private Map<String, RecordUtil.RecordComponent> _componentMap;
    private Constructor<?> _constructor;
    private Class<?> _cl;

    public RecordDeserializer(Class<?> cl, FieldDeserializer2Factory fieldFactory) {
        _cl = cl;
        _components = RecordUtil.getRecordComponents(cl);
        _constructor = RecordUtil.getCanonicalConstructor(cl);
        _componentMap = new ConcurrentHashMap<>();
        for (RecordUtil.RecordComponent component : _components) {
            _componentMap.put(component.name(), component);
        }
    }

    @Override
    public Class<?> getType() {
        return _cl;
    }

    @Override
    public Object readObject(AbstractHessianDecoder in, Object[] fields) throws IOException {
        try {
            return readObject(in, (String[]) fields);
        } catch (IOException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOExceptionWrapper(_cl.getName() + ":" + e, e);
        }
    }

    @Override
    public Object readObject(AbstractHessianDecoder in, String[] fieldNames) throws IOException {
        try {
            int ref = in.addRef(null);
            Object[] args = new Object[_components.length];
            boolean[] readedIndex = new boolean[_components.length];
            for (String fieldName : fieldNames) {
                RecordUtil.RecordComponent component = _componentMap.get(fieldName);
                if (component == null) {
                    // ignore this field
                    in.readObject();
                    continue;
                }
                Object target;
                target = in.readObject(component.type());
                args[component.index()] = target;
                readedIndex[component.index()] = true;
            }
            for (int i = 0; i < readedIndex.length; i++) {
                if (!readedIndex[i]) {
                    args[i] = FieldDeserializer2Factory.getParamArg(_components[i].type());
                }
            }
            Object obj = _constructor.newInstance(args);
            in.setRef(ref, obj);

            return obj;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOExceptionWrapper(_cl.getName() + ":" + e, e);
        }
    }
}
