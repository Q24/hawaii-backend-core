/**
 * Copyright 2014-2018 Q24
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kahu.hawaii.util.logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * <p>
 * Encapsulate context information for logging actions.
 * </p>
 *
 * <p>
 * This provides a nested structure of key-value mappings that can be pushed and
 * popped as new logging contexts are encountered/finished.
 * </p>
 *
 * <p>
 * The ordering of keys is stable: when iterating, the keys are discovered in
 * the order they were added to the context.
 * </p>
 *
 * @author ErnstJan.Plugge
 */
public class LoggingContext {
    /**
     * The {@link ThreadLocal} object for the context. This maintains one
     * instance of LoggingContext for each active thread.
     */
    public static ThreadLocal<LoggingContext> context = new ThreadLocal<LoggingContext>() {
        @Override
        protected LoggingContext initialValue() {
            return new LoggingContext();
        }
    };

    /**
     * The context stack. The last element is the top of the stack. Pushing adds
     * a new map to the end that is a shallow copy of the previous top. The
     * stack is never empty.
     */
    private final List<LoggingContextMap> stack = new ArrayList<>();

    /**
     * A resource that, when closed, will pop the context.
     *
     * @author ErnstJan.Plugge
     */
    public class PopResource implements AutoCloseable {
        private boolean closed = false;

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            pop();
        }
    }

    private LoggingContext() {
        // Can only be instantiated by this class's statics.
        stack.add(new LoggingContextMap());
    }

    /**
     * Remove the complete context, including hidden pushed contexts, for this
     * thread. To prevent memory leaks, it's important that this method is
     * called whenever a thread begins "new" work (begins serving a servlet,
     * usually), and when it finishes that "new" work.
     */
    public static void remove() {
        context.remove();
    }

    /**
     * Get the LoggingContext instance for the current thread. This will always
     * be the same instance for the lifetime of the thread unless remove() is
     * called.
     *
     * @return the context
     */
    public static LoggingContext get() {
        return context.get();
    }

    /**
     * Shortcut to get the top of the stack
     *
     * @return the last element of the stack
     */
    private LoggingContextMap peek() {
        return stack.get(stack.size() - 1);
    }

    /**
     * <p>
     * Establish a new context. Any changes to the context will be lost when it
     * is popped. After a pop(), the state in the context is exactly the same as
     * before the push().
     * </p>
     *
     * <p>
     * After the push, the context is a copy of the context that existed before.
     * </p>
     *
     * @return a managed resource that may be used in a try-with-resource
     *         statement to ensure proper cleanup of a pushed context. If not
     *         used, the caller must ensure that pop() is called eventually in a
     *         properly nested manner.
     */
    public PopResource push() {
        stack.add(new LoggingContextMap(peek()));
        return new PopResource();
    }

    /**
     * <p>
     * Establish a new context. Any changes to the context will be lost when it
     * is popped. After a pop(), the state in the context is exactly the same as
     * before the push().
     * </p>
     *
     * <p>
     * After the push, the context is a copy of the supplied newContext.
     * </p>
     *
     * @param newContext
     *            the new context to establish
     * @return a managed resource that may be used in a try-with-resource
     *         statement to ensure proper cleanup of a pushed context. If not
     *         used, the caller must ensure that pop() is called eventually in a
     *         properly nested manner.
     */
    public PopResource push(LoggingContextMap newContext) {
        stack.add(new LoggingContextMap(newContext));
        return new PopResource();
    }

    /**
     * Remove the top element from the stack. If there is only one element, that
     * context map is only cleared, not removed, to ensure that there is always
     * at least one element on the stack. Note that this should never, ever
     * happen, as this indicates a pop() without a corresponding push() or a
     * double pop().
     */
    public void pop() {
        if (stack.size() == 1) {
            peek().clear();
        }
        stack.remove(stack.size() - 1);
    }

    /**
     * Like Map's method of the same name. Performs the same operation, but on
     * the current context on top of the stack.
     *
     * @param key
     *            the key to remove
     * @return the old value for the key or null
     */
    public Object remove(String key) {
        return peek().remove(key);
    }

    /**
     * Like Map's method of the same name. Performs the same operation, but on
     * the current context on top of the stack.
     *
     * @param key
     *            the key to check
     * @return true if the key is present in the context
     */
    public boolean containsKey(String key) {
        return peek().containsKey(key);
    }

    /**
     * Like Map's method of the same name. Performs the same operation, but on
     * the current context on top of the stack.
     *
     * @return true if the context contains no keys
     */
    public boolean isEmpty() {
        return peek().isEmpty();
    }

    /**
     * Like Map's method of the same name. Performs the same operation, but on
     * the current context on top of the stack.
     *
     * @param key
     *            the key to add/modify
     * @param value
     *            the value for the key
     * @return the old value for the key or null
     */
    public Object put(String key, Object value) {
        return peek().put(key, value);
    }

    /**
     * Like Map's method of the same name. Performs the same operation, but on
     * the current context on top of the stack.
     *
     * @param key
     *            the key to get
     * @return the value associated with the key or null
     */
    public Object get(String key) {
        return peek().get(key);
    }

    public Object getFromRootContext(String key) {
        return stack.get(0).get(key);
    }

    public Object putAtRootContext(String key, Object value) {
        return stack.get(0).put(key, value);
    }

    /**
     * <p>
     * Compute a measure of complexity for an object. This determines if a
     * JSONObject or JSONArray is pretty-printed over multiple lines or
     * compactly printed on a single line.
     * </p>
     *
     * <p>
     * Most objects have complexity 1. JSONObject instances have complexity 1
     * plus the sum of the complexities of the values in the object (keys are
     * not counted). JSONArray instances have complexity 1 plus the sum of the
     * complexities of the values in the array.
     * </p>
     *
     * @param value
     *            the object to calculate the complexity for
     * @return the complexity as a positive int
     * @throws JSONException
     *             "this can't happen"
     */
    public static int getObjectComplexity(Object value) {
        try {
            if (value instanceof JSONObject) {
                int n = 1;
                JSONObject o = (JSONObject) value;
                JSONArray a = o.names();
                if (a != null) {
                    for (int i = 0; i < a.length(); i++) {
                        n += getObjectComplexity(o.get(a.getString(i)));
                    }
                }
                return n;
            }
            if (value instanceof JSONArray) {
                int n = 1;
                JSONArray a = (JSONArray) value;
                for (int i = 0; i < a.length(); i++) {
                    n += getObjectComplexity(a.get(i));
                }
                return n;
            }
        } catch (JSONException cant_happen) {
            // Ignore
        }
        return 1;
    }

    /**
     * Format a string as a JSON value. This is used instead of
     * JSONObject.quote() because that method in Jettison will escape forward
     * slashes as "\/", which is unnecessary, ugly and poorly readable in log
     * files.
     *
     * @param value
     *            The string to quote
     * @return The quoted string
     */
    private static String formatString(String value) {
        if (value == null) {
            return "null";
        }
        if (value.length() == 0) {
            return "\"\"";
        }

        int len = value.length();
        StringBuilder sb = new StringBuilder(len + 4);

        sb.append('"');
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                sb.append('\\');
                sb.append(c);
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\r':
                sb.append("\\r");
                break;
            default:
                if (c < ' ') {
                    String t = "000" + Integer.toHexString(c);
                    sb.append("\\u" + t.substring(t.length() - 4));
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Format a single object as a JSON value: null, a number, a boolean, a
     * string, an object or an array.
     *
     * @param value
     *            the object to format
     * @return the JSON representation of the object
     */
    private static String formatValue(Object value, int complexityThreshold) {
        try {
            if (value == null || value == JSONObject.NULL || value == JSONObject.EXPLICIT_NULL) {
                return "null";
            }
            if (value instanceof Number) {
                return JSONObject.numberToString((Number) value);
            }
            if (value instanceof Boolean) {
                return value.toString();
            }
            if (value instanceof JSONObject) {
                if (getObjectComplexity(value) > complexityThreshold) {
                    return ((JSONObject) value).toString(2);
                } else {
                    return value.toString();
                }
            }
            if (value instanceof JSONArray) {
                if (getObjectComplexity(value) > complexityThreshold) {
                    return ((JSONArray) value).toString(2);
                } else {
                    return value.toString();
                }
            }
            return formatString(value.toString());
        } catch (JSONException cant_happen) {
            return "null";
        }
    }

    /**
     * <p>
     * Format all fields in the current context, taking into account the key
     * order and the predefined tracing fields.
     * </p>
     *
     * <p>
     * Each field is formatted as 'field=value', where field is the key and
     * value is the JSON representation of the value. Fields are separated by a
     * single space character.
     * </p>
     *
     * <p>
     * The literalField parameter, if non-null, specifies a single field that
     * should be treated specially. Instead of formatting it as a JSON value, it
     * is formatted as a literal '#' character plus the result of
     * value.toString(). Newlines are replaced by a newline followed by a single
     * space character, to avoid confusing the multiline log detector. This
     * literalField is always the last field in the output.
     * </p>
     *
     * @param literalField
     *            the name of the literal field, or null if literal should not
     *            be used.
     * @return the formatted string with all fields
     */
    public String formatFields(String literalField, String[] tracingFields, int complexityThreshold) {
        LoggingContextMap m = peek().clone();
        boolean first = true;
        StringBuilder sb = new StringBuilder();

        for (String field : tracingFields) {
            if (field.equals(literalField)) {
                continue;
            }
            Object value = m.get(field);
            if (value == null || (value instanceof String && ((String) value).length() == 0) || value == JSONObject.EXPLICIT_NULL || value == JSONObject.NULL) {
                continue;
            }
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append(field);
            sb.append('=');
            sb.append(formatValue(value, complexityThreshold));
            m.remove(field);
        }

        for (Map.Entry<String, Object> entry : m.entrySet()) {
            String field = entry.getKey();
            if (field.equals(literalField)) {
                continue;
            }
            Object value = entry.getValue();
            if (value == null || (value instanceof String && ((String) value).length() == 0) || value == JSONObject.EXPLICIT_NULL || value == JSONObject.NULL) {
                continue;
            }
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append(field);
            sb.append('=');
            sb.append(formatValue(value, complexityThreshold));
        }

        if (literalField != null) {
            Object value = m.get(literalField);
            if (!(value == null || (value instanceof String && ((String) value).length() == 0) || value == JSONObject.EXPLICIT_NULL || value == JSONObject.NULL)) {
                if (!first) {
                    sb.append(' ');
                }
                first = false;
                sb.append(literalField);
                sb.append("=#");
                value = value.toString().replace("\n", "\n ");
                sb.append(value);
            }
        }

        return sb.toString();
    }

    /**
     * The time elapsed between the time the top of the stack was created and
     * now, in millis.
     *
     * @return
     */
    public double getContextDuration() {
        return (System.nanoTime() - peek().getCreated()) / 1000000.0d;
    }

    /**
     * Return a copy of the current context. This can be saved so that a worker
     * thread can use it to recreate the context of the responsible caller.
     *
     * @return a shallow copy of the context
     */
    public LoggingContextMap getSnapshot() {
        return peek().clone();
    }
}
