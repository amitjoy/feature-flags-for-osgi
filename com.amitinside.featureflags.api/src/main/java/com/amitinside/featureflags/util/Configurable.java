/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amitinside.featureflags.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.osgi.service.metatype.annotations.AttributeDefinition;

import aQute.bnd.annotation.metatype.Meta;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class Configurable<T> {

    public static final Pattern SPLITTER_P = Pattern.compile("(?<!\\\\)\\|");
    private static final String BND_ANNOTATION_CLASS_NAME = "aQute.bnd.osgi.Annotation";
    private static final String BND_ANNOTATION_METHOD_NAME = "getAnnotation";

    public static <T> T createConfigurable(final Class<T> c, final Map<?, ?> properties) {
        final Object o = Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[] { c },
                new ConfigurableHandler(properties, c.getClassLoader()));
        return c.cast(o);
    }

    public static <T> T createConfigurable(final Class<T> c, final Dictionary<?, ?> properties) {
        final Map<Object, Object> alt = new HashMap<>();
        for (final Enumeration<?> e = properties.keys(); e.hasMoreElements();) {
            final Object key = e.nextElement();
            alt.put(key, properties.get(key));
        }
        return createConfigurable(c, alt);
    }

    static class ConfigurableHandler implements InvocationHandler {
        final Map<?, ?> properties;
        final ClassLoader loader;

        ConfigurableHandler(final Map<?, ?> properties, final ClassLoader loader) {
            this.properties = properties;
            this.loader = loader;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final AttributeDefinition ad = method.getAnnotation(AttributeDefinition.class);
            String id = Configurable.mangleMethodName(method.getName());

            if (ad != null && !ad.name().equals("")) {
                id = ad.name();
            }

            Object o = properties.get(id);

            if (o == null) {
                if (ad != null) {
                    if (ad.required()) {
                        throw new IllegalStateException("Attribute is required but not set " + method.getName());
                    }

                    o = ad.defaultValue();
                    if (o.equals(Meta.NULL)) {
                        o = null;
                    }
                }
            }
            if (o == null) {
                final Class<?> rt = method.getReturnType();
                if (rt == boolean.class) {
                    return false;
                }

                if (method.getReturnType().isPrimitive()) {

                    o = "0";
                } else {
                    return null;
                }
            }

            return convert(method.getGenericReturnType(), o);
        }

        public Object convert(final Type type, Object o) throws Exception {
            if (type instanceof ParameterizedType) {
                final ParameterizedType pType = (ParameterizedType) type;
                return convert(pType, o);
            }

            if (type instanceof GenericArrayType) {
                final GenericArrayType gType = (GenericArrayType) type;
                return convertArray(gType.getGenericComponentType(), o);
            }

            Class<?> resultType = (Class<?>) type;

            if (resultType.isArray()) {
                return convertArray(resultType.getComponentType(), o);
            }

            final Class<?> actualType = o.getClass();
            if (actualType.isAssignableFrom(resultType)) {
                return o;
            }

            if (resultType == boolean.class || resultType == Boolean.class) {
                if (actualType == boolean.class || actualType == Boolean.class) {
                    return o;
                }

                if (Number.class.isAssignableFrom(actualType)) {
                    final double b = ((Number) o).doubleValue();
                    if (b == 0) {
                        return false;
                    }
                    return true;
                }
                if (o instanceof String) {
                    return Boolean.parseBoolean((String) o);
                }
                return true;

            } else if (resultType == byte.class || resultType == Byte.class) {
                if (Number.class.isAssignableFrom(actualType)) {
                    return ((Number) o).byteValue();
                }
                resultType = Byte.class;
            } else if (resultType == char.class) {
                resultType = Character.class;
            } else if (resultType == short.class) {
                if (Number.class.isAssignableFrom(actualType)) {
                    return ((Number) o).shortValue();
                }
                resultType = Short.class;
            } else if (resultType == int.class) {
                if (Number.class.isAssignableFrom(actualType)) {
                    return ((Number) o).intValue();
                }
                resultType = Integer.class;
            } else if (resultType == long.class) {
                if (Number.class.isAssignableFrom(actualType)) {
                    return ((Number) o).longValue();
                }
                resultType = Long.class;
            } else if (resultType == float.class) {
                if (Number.class.isAssignableFrom(actualType)) {
                    return ((Number) o).floatValue();
                }
                resultType = Float.class;
            } else if (resultType == double.class) {
                if (Number.class.isAssignableFrom(actualType)) {
                    return ((Number) o).doubleValue();
                }
                resultType = Double.class;
            }

            if (resultType.isPrimitive()) {
                throw new IllegalArgumentException("Unknown primitive: " + resultType);
            }

            if (Number.class.isAssignableFrom(resultType) && actualType == Boolean.class) {
                final Boolean b = (Boolean) o;
                o = b ? "1" : "0";
            } else if (actualType == String.class) {
                final String input = (String) o;
                if (Enum.class.isAssignableFrom(resultType)) {
                    return Enum.valueOf((Class<Enum>) resultType, input);
                }
                if (resultType == Class.class && loader != null) {
                    return loader.loadClass(input);
                }
                if (resultType == Pattern.class) {
                    return Pattern.compile(input);
                }
            } else if (resultType.isAnnotation() && actualType.getName().equals(BND_ANNOTATION_CLASS_NAME)) {
                final Method m = actualType.getMethod(BND_ANNOTATION_METHOD_NAME);
                final java.lang.annotation.Annotation a = (Annotation) m.invoke(o);
                if (resultType.isAssignableFrom(a.getClass())) {
                    return a;
                }
                throw new IllegalArgumentException("Annotation " + o + " is not of expected type " + resultType);
            }

            try {
                final Constructor<?> c = resultType.getConstructor(String.class);
                return c.newInstance(o.toString());
            } catch (final Throwable t) {
                // handled on next line
            }
            throw new IllegalArgumentException(
                    "No conversion to " + resultType + " from " + actualType + " value " + o);
        }

        private Object convert(final ParameterizedType pType, final Object o)
                throws InstantiationException, IllegalAccessException, Exception {
            Class<?> resultType = (Class<?>) pType.getRawType();
            if (Collection.class.isAssignableFrom(resultType)) {
                final Collection<?> input = toCollection(o);
                if (resultType.isInterface()) {
                    if (resultType == Collection.class || resultType == List.class) {
                        resultType = ArrayList.class;
                    } else if (resultType == Set.class || resultType == SortedSet.class) {
                        resultType = TreeSet.class;
                    } else if (resultType == Queue.class /*
                                                          * || resultType ==
                                                          * Deque.class
                                                          */) {
                        resultType = LinkedList.class;
                    } else if (resultType == Queue.class /*
                                                          * || resultType ==
                                                          * Deque.class
                                                          */) {
                        resultType = LinkedList.class;
                    } else {
                        throw new IllegalArgumentException(
                                "Unknown interface for a collection, no concrete class found: " + resultType);
                    }
                }

                final Collection<Object> result = (Collection<Object>) resultType.getConstructor().newInstance();
                final Type componentType = pType.getActualTypeArguments()[0];

                for (final Object i : input) {
                    result.add(convert(componentType, i));
                }
                return result;
            } else if (pType.getRawType() == Class.class) {
                return loader.loadClass(o.toString());
            }
            if (Map.class.isAssignableFrom(resultType)) {
                final Map<?, ?> input = toMap(o);
                if (resultType.isInterface()) {
                    if (resultType == SortedMap.class) {
                        resultType = TreeMap.class;
                    } else if (resultType == Map.class) {
                        resultType = LinkedHashMap.class;
                    } else {
                        throw new IllegalArgumentException(
                                "Unknown interface for a collection, no concrete class found: " + resultType);
                    }
                }
                final Map<Object, Object> result = (Map<Object, Object>) resultType.getConstructor().newInstance();
                final Type keyType = pType.getActualTypeArguments()[0];
                final Type valueType = pType.getActualTypeArguments()[1];

                for (final Map.Entry<?, ?> entry : input.entrySet()) {
                    result.put(convert(keyType, entry.getKey()), convert(valueType, entry.getValue()));
                }
                return result;
            }
            throw new IllegalArgumentException(
                    "cannot convert to " + pType + " because it uses generics and is not a Collection or a map");
        }

        Object convertArray(final Type componentType, final Object o) throws Exception {
            if (o instanceof String) {
                final String s = (String) o;
                if (componentType == Byte.class || componentType == byte.class) {
                    return s.getBytes("UTF-8");
                }
                if (componentType == Character.class || componentType == char.class) {
                    return s.toCharArray();
                }
            }
            final Collection<?> input = toCollection(o);
            final Class<?> componentClass = getRawClass(componentType);
            final Object array = Array.newInstance(componentClass, input.size());

            int i = 0;
            for (final Object next : input) {
                Array.set(array, i++, convert(componentType, next));
            }
            return array;
        }

        private Class<?> getRawClass(final Type type) {
            if (type instanceof Class) {
                return (Class<?>) type;
            }

            if (type instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) type).getRawType();
            }

            throw new IllegalArgumentException(
                    "For the raw type, type must be ParamaterizedType or Class but is " + type);
        }

        private Collection<?> toCollection(final Object o) {
            if (o instanceof Collection) {
                return (Collection<?>) o;
            }

            if (o.getClass().isArray()) {
                if (o.getClass().getComponentType().isPrimitive()) {
                    final int length = Array.getLength(o);
                    final List<Object> result = new ArrayList<>(length);
                    for (int i = 0; i < length; i++) {
                        result.add(Array.get(o, i));
                    }
                    return result;
                }
                return Arrays.asList((Object[]) o);
            }

            if (o instanceof String) {
                final String s = (String) o;
                if (SPLITTER_P.matcher(s).find()) {
                    return Arrays.asList(s.split("\\|"));
                } else {
                    return unescape(s);
                }

            }
            return Arrays.asList(o);
        }

        private Map<?, ?> toMap(final Object o) {
            if (o instanceof Map) {
                return (Map<?, ?>) o;
            }

            throw new IllegalArgumentException("Cannot convert " + o + " to a map as requested");
        }

    }

    public static String mangleMethodName(final String id) {
        final StringBuilder sb = new StringBuilder(id);
        for (int i = 0; i < sb.length(); i++) {
            final char c = sb.charAt(i);
            final boolean twice = i < sb.length() - 1 && sb.charAt(i + 1) == c;
            if (c == '$' || c == '_') {
                if (twice) {
                    sb.deleteCharAt(i + 1);
                } else if (c == '$') {
                    sb.deleteCharAt(i--); // Remove dollars
                } else {
                    sb.setCharAt(i, '.'); // Make _ into .
                }
            }
        }
        return sb.toString();
    }

    public static List<String> unescape(final String s) {
        // do it the OSGi way
        final List<String> tokens = new ArrayList<>();

        final String[] parts = s.split("(?<!\\\\),");

        for (String p : parts) {
            p = p.replaceAll("^\\s*", "");
            p = p.replaceAll("(?!<\\\\)\\s*$", "");
            p = p.replaceAll("\\\\([\\s,\\\\|])", "$1");
            tokens.add(p);
        }
        return tokens;
    }

}
