/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.badlogic.gdx.beans;

import com.badlogic.gdx.beans.Encoder;
import com.badlogic.gdx.beans.Expression;
import com.badlogic.gdx.beans.Statement;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.harmony.beans.BeansUtils;
import org.apache.harmony.beans.internal.nls.Messages;

/**
 * <code>XMLEncoder</code> extends <code>Encoder</code> to write out the encoded
 * statements and expressions in XML format. The XML can be read by
 * <code>XMLDecoder</code> later to restore objects and their states.
 * <p>
 * The API is similar to <code>ObjectOutputStream</code>.
 * </p>
 * 
 */
public class XMLEncoder extends Encoder {

    private static final String DEFAULT_ENCODING = "UTF-8"; //$NON-NLS-1$

    private static int DEADLOCK_THRESHOLD = 7;

    /*
     * Every object written by the encoder has a record.
     */
    private static class Record {
        // The expression by which the object is created or obtained.
        Expression exp = null;

        // Id of the object, if it is referenced more than once.
        String id = null;

        // Count of the references of the object.
        int refCount = 0;

        // A list of statements that execute on the object.
        ArrayList<Statement> stats = new ArrayList<Statement>();
    }

    private static final int INDENT_UNIT = 1;

    private static final boolean isStaticConstantsSupported = true;

    // the main record of all root objects
    private ArrayList<Object> flushPending = new ArrayList<Object>();

    // the record of root objects with a void tag
    private ArrayList<Object> flushPendingStat = new ArrayList<Object>();

    // keep the pre-required objects for each root object
    private ArrayList<Object> flushPrePending = new ArrayList<Object>();

    private boolean hasXmlHeader = false;

    /*
     * if any expression or statement references owner, it is set true in method
     * recordStatement() or recordExpressions(), and, at the first time
     * flushObject() meets an owner object, it calls the flushOwner() method and
     * then set needOwner to false, so that all succeeding flushing of owner
     * will call flushExpression() or flushStatement() normally, which will get
     * a reference of the owner property.
     */
    private boolean needOwner = false;

    private PrintWriter out;

    private Object owner = null;

    private IdentityHashMap<Object, Record> objRecordMap = new IdentityHashMap<Object, Record>();

    private IdentityHashMap<Class<?>, Integer> clazzCounterMap = new IdentityHashMap<Class<?>, Integer>();

    private IdentityHashMap<Object, ArrayList<Object>> objPrePendingCache = new IdentityHashMap<Object, ArrayList<Object>>();

    private boolean writingObject = false;

    /**
     * Construct a <code>XMLEncoder</code>.
     * 
     * @param out
     *            the output stream where XML is written to
     */
    public XMLEncoder(OutputStream out) {
        if (null != out) {
            try {
                this.out = new PrintWriter(new OutputStreamWriter(out,
                        DEFAULT_ENCODING), true);
            } catch (UnsupportedEncodingException e) {
                // should never occur
            }
        }
    }

    /**
     * Call <code>flush()</code> first, then write out XML footer and close the
     * underlying output stream.
     */
    public void close() {
        flush();
        out.println("</java>"); //$NON-NLS-1$
        out.close();
    }

    private StringBuffer decapitalize(String s) {
        StringBuffer buf = new StringBuffer(s);
        buf.setCharAt(0, Character.toLowerCase(buf.charAt(0)));
        return buf;
    }

    private String idSerialNoOfObject(Object obj) {
        Class<?> clazz = obj.getClass();
        Integer serialNo = (Integer) clazzCounterMap.get(clazz);
        serialNo = serialNo == null ? 0 : serialNo;
        String id = BeansUtils.idOfClass(obj.getClass()) + serialNo;
        clazzCounterMap.put(clazz, ++serialNo);
        return id;
    }

    /**
     * Writes out all objects since last flush to the output stream.
     * <p>
     * The implementation write the XML header first if it has not been written.
     * Then all pending objects since last flush are written.
     * </p>
     */
    @SuppressWarnings("nls")
    public void flush() {
        synchronized (this) {
            // write xml header
            if (!hasXmlHeader) {
                out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                out.println("<java version=\""
                        + System.getProperty("java.version")
                        + "\" class=\"com.esotericsoftware.android.beans.XMLDecoder\">");
                hasXmlHeader = true;
            }

            // preprocess pending objects
            for (Iterator<Object> iter = flushPending.iterator(); iter
                    .hasNext();) {
                Object o = iter.next();
                Record rec = objRecordMap.get(o);
                if (rec != null) {
                    preprocess(o, rec);
                }
            }

            // flush pending objects
            for (Iterator<Object> iter = flushPending.iterator(); iter
                    .hasNext();) {
                Object o = iter.next();
                flushObject(o, INDENT_UNIT);
                // remove flushed obj
                iter.remove();
            }

            // clear statement records
            objRecordMap.clear();
            flushPendingStat.clear();
            objPrePendingCache.clear();
            clazzCounterMap.clear();

            // remove all old->new mappings
            super.clear();
        }
    }

    @SuppressWarnings("nls")
    private void flushBasicObject(Object obj, int indent) {
        if (obj instanceof Proxy) {
            return;
        }
        flushIndent(indent);
        if (obj == null) {
            out.println("<null />");
        } else if (obj instanceof String) {
            Record rec = objRecordMap.get(obj);
            if (null != rec) {
                flushExpression(obj, rec, indent - 3,
                        flushPendingStat.contains(obj));
                return;
            }
            out.print("<string>");
            flushString((String) obj);
            out.println("</string>");
        } else if (obj instanceof Class<?>) {
            out.println("<class>" + ((Class<?>) obj).getName() + "</class>");
        } else if (obj instanceof Boolean) {
            out.println("<boolean>" + obj + "</boolean>");
        } else if (obj instanceof Byte) {
            out.println("<byte>" + obj + "</byte>");
        } else if (obj instanceof Character) {
            out.println("<char>" + obj + "</char>");
        } else if (obj instanceof Double) {
            out.println("<double>" + obj + "</double>");
        } else if (obj instanceof Float) {
            out.println("<float>" + obj + "</float>");
        } else if (obj instanceof Integer) {
            out.println("<int>" + obj + "</int>");
        } else if (obj instanceof Long) {
            out.println("<long>" + obj + "</long>");
        } else if (obj instanceof Short) {
            out.println("<short>" + obj + "</short>");
        } else {
            getExceptionListener().exceptionThrown(
                    new Exception(Messages.getString("beans.73", obj)));
        }
    }

    @SuppressWarnings("nls")
    private void flushExpression(Object obj, Record rec, int indent,
            boolean asStatement) {
        // flush
        Statement stat = asStatement ? new Statement(rec.exp.getTarget(),
                rec.exp.getMethodName(), rec.exp.getArguments()) : rec.exp;
        if (isStaticConstantsSupported
                && "getField".equals(stat.getMethodName())) {
            flushStatField(stat, indent);
            return;
        }

        // not first time, use idref
        if (rec.id != null) {
            flushIndent(indent);
            out.print("<object idref=\"");
            out.print(rec.id);
            out.println("\" />");
            return;
        }

        // generate id, if necessary
        if (rec.refCount > 1 && rec.id == null) {
            rec.id = idSerialNoOfObject(obj);
        }

        // flush
        flushStatement(stat, rec.id, rec.stats, indent);
    }

    private void flushIndent(int indent) {
        for (int i = 0; i < indent; i++) {
            out.print(' ');
        }
    }

    private void flushObject(Object obj, int indent) {
        Record rec = objRecordMap.get(obj);
        if (rec == null && !isBasicType(obj)) {
            return;
        }

        if (obj == owner && this.needOwner) {
            flushOwner(obj, rec, indent);
            this.needOwner = false;
            return;
        }

        if (isBasicType(obj)) {
            flushBasicObject(obj, indent);
        } else {
            flushExpression(obj, rec, indent, flushPendingStat.contains(obj));
        }
    }

    @SuppressWarnings("nls")
    private void flushOwner(Object obj, Record rec, int indent) {
        if (rec.refCount > 1 && rec.id == null) {
            rec.id = idSerialNoOfObject(obj);
        }

        flushIndent(indent);
        String tagName = "void";
        out.print("<");
        out.print(tagName);

        // id attribute
        if (rec.id != null) {
            out.print(" id=\"");
            out.print(rec.id);
            out.print("\"");
        }

        out.print(" property=\"owner\"");

        // open tag, end
        if (rec.exp.getArguments().length == 0 && rec.stats.isEmpty()) {
            out.println("/>");
            return;
        }
        out.println(">");

        // arguments
        for (int i = 0; i < rec.exp.getArguments().length; i++) {
            flushObject(rec.exp.getArguments()[i], indent + INDENT_UNIT);
        }

        // sub statements
        flushSubStatements(rec.stats, indent);

        // close tag
        flushIndent(indent);
        out.print("</");
        out.print(tagName);
        out.println(">");
    }

    @SuppressWarnings("nls")
    private void flushStatArray(Statement stat, String id, List<?> subStats,
            int indent) {
        // open tag, begin
        flushIndent(indent);
        out.print("<array");

        // id attribute
        if (id != null) {
            out.print(" id=\"");
            out.print(id);
            out.print("\"");
        }

        // class & length
        out.print(" class=\"");
        out.print(((Class<?>) stat.getArguments()[0]).getName());
        out.print("\" length=\"");
        out.print(stat.getArguments()[1]);
        out.print("\"");

        // open tag, end
        if (subStats.isEmpty()) {
            out.println("/>");
            return;
        }
        out.println(">");

        // sub statements
        flushSubStatements(subStats, indent);

        // close tag
        flushIndent(indent);
        out.println("</array>");
    }

    @SuppressWarnings("nls")
    private void flushStatCommon(Statement stat, String id, List<?> subStats,
            int indent) {
        // open tag, begin
        flushIndent(indent);
        String tagName = stat instanceof Expression ? "object" : "void";
        out.print("<");
        out.print(tagName);

        // id attribute
        if (id != null) {
            out.print(" id=\"");
            out.print(id);
            out.print("\"");
        }

        // special class attribute
        if (stat.getTarget() instanceof Class<?>) {
            out.print(" class=\"");
            out.print(((Class<?>) stat.getTarget()).getName());
            out.print("\"");
        }

        // method attribute
        if (!"new".equals(stat.getMethodName())) {
            out.print(" method=\"");
            out.print(stat.getMethodName());
            out.print("\"");
        }

        // open tag, end
        if (stat.getArguments().length == 0 && subStats.isEmpty()) {
            out.println("/>");
            return;
        }
        out.println(">");

        // arguments
        for (int i = 0; i < stat.getArguments().length; i++) {
            flushObject(stat.getArguments()[i], indent + INDENT_UNIT);
        }

        // sub statements
        flushSubStatements(subStats, indent);

        // close tag
        flushIndent(indent);
        out.print("</");
        out.print(tagName);
        out.println(">");
    }

    @SuppressWarnings("nls")
    private void flushStatement(Statement stat, String id, List<?> subStats,
            int indent) {
        Object target = stat.getTarget();
        String method = stat.getMethodName();
        Object args[] = stat.getArguments();

        // special case for array
        if (Array.class == target && BeansUtils.NEWINSTANCE.equals(method)) {
            flushStatArray(stat, id, subStats, indent);
            return;
        }
        // special case for get(int) and set(int, Object)
        if (isGetArrayStat(target, method, args)
                || isSetArrayStat(target, method, args)) {
            flushStatIndexed(stat, id, subStats, indent);
            return;
        }
        // special case for getProperty() and setProperty(Object)
        if (isGetPropertyStat(method, args) || isSetPropertyStat(method, args)) {
            flushStatGetterSetter(stat, id, subStats, indent);
            return;
        }

        if (isStaticConstantsSupported
                && "getField".equals(stat.getMethodName())) {
            flushStatField(stat, indent);
            return;
        }

        // common case
        flushStatCommon(stat, id, subStats, indent);
    }

    @SuppressWarnings("nls")
    private void flushStatField(Statement stat, int indent) {
        // open tag, begin
        flushIndent(indent);
        out.print("<object");

        // special class attribute
        Object target = stat.getTarget();
        if (target instanceof Class<?>) {
            out.print(" class=\"");
            out.print(((Class<?>) target).getName());
            out.print("\"");
        }

        Field field = null;
        if (target instanceof Class<?> && stat.getArguments().length == 1
                && stat.getArguments()[0] instanceof String) {
            try {
                field = ((Class<?>) target).getField((String) stat
                        .getArguments()[0]);
            } catch (Exception e) {
                // ignored
            }
        }

        if (field != null && Modifier.isStatic(field.getModifiers())) {
            out.print(" field=\"");
            out.print(stat.getArguments()[0]);
            out.print("\"");
            out.println("/>");
        } else {
            out.print(" method=\"");
            out.print(stat.getMethodName());
            out.print("\"");
            out.println(">");
            flushObject(stat.getArguments()[0], indent + INDENT_UNIT);
            flushIndent(indent);
            out.println("</object>");
        }
    }

    @SuppressWarnings("nls")
    private void flushStatGetterSetter(Statement stat, String id,
            List<?> subStats, int indent) {
        // open tag, begin
        flushIndent(indent);
        String tagName = stat instanceof Expression ? "object" : "void";
        out.print("<");
        out.print(tagName);

        // id attribute
        if (id != null) {
            out.print(" id=\"");
            out.print(id);
            out.print("\"");
        }

        // special class attribute
        if (stat.getTarget() instanceof Class<?>) {
            out.print(" class=\"");
            out.print(((Class<?>) stat.getTarget()).getName());
            out.print("\"");
        }

        // property attribute
        out.print(" property=\"");
        out.print(decapitalize(stat.getMethodName().substring(3)));
        out.print("\"");

        // open tag, end
        if (stat.getArguments().length == 0 && subStats.isEmpty()) {
            out.println("/>");
            return;
        }
        out.println(">");

        // arguments
        for (int i = 0; i < stat.getArguments().length; i++) {
            flushObject(stat.getArguments()[i], indent + INDENT_UNIT);
        }

        // sub statements
        flushSubStatements(subStats, indent);

        // close tag
        flushIndent(indent);
        out.print("</");
        out.print(tagName);
        out.println(">");
    }

    @SuppressWarnings("nls")
    private void flushStatIndexed(Statement stat, String id, List<?> subStats,
            int indent) {
        // open tag, begin
        flushIndent(indent);
        String tagName = stat instanceof Expression ? "object" : "void";
        out.print("<");
        out.print(tagName);

        // id attribute
        if (id != null) {
            out.print(" id=\"");
            out.print(id);
            out.print("\"");
        }

        // special class attribute
        if (stat.getTarget() instanceof Class<?>) {
            out.print(" class=\"");
            out.print(((Class<?>) stat.getTarget()).getName());
            out.print("\"");
        }

        // index attribute
        out.print(" index=\"");
        out.print(stat.getArguments()[0]);
        out.print("\"");

        // open tag, end
        if (stat.getArguments().length == 1 && subStats.isEmpty()) {
            out.println("/>");
            return;
        }
        out.println(">");

        // arguments
        for (int i = 1; i < stat.getArguments().length; i++) {
            flushObject(stat.getArguments()[i], indent + INDENT_UNIT);
        }

        // sub statements
        flushSubStatements(subStats, indent);

        // close tag
        flushIndent(indent);
        out.print("</");
        out.print(tagName);
        out.println(">");
    }

    @SuppressWarnings("nls")
    private void flushString(String s) {
        char c;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if (c == '<') {
                out.print("&lt;");
            } else if (c == '>') {
                out.print("&gt;");
            } else if (c == '&') {
                out.print("&amp;");
            } else if (c == '\'') {
                out.print("&apos;");
            } else if (c == '"') {
                out.print("&quot;");
            } else {
                out.print(c);
            }
        }
    }

    private void flushSubStatements(List<?> subStats, int indent) {
        for (int i = 0; i < subStats.size(); i++) {
            Statement subStat = (Statement) subStats.get(i);
            try {
                if (subStat.getClass() == Expression.class) {
                    Expression subExp = (Expression) subStat;
                    Object obj = subExp.getValue();
                    Record rec = objRecordMap.get(obj);
                    flushExpression(obj, rec, indent + INDENT_UNIT, true);
                } else {
                    flushStatement(subStat, null, Collections.EMPTY_LIST,
                            indent + INDENT_UNIT);
                }
            } catch (Exception e) {
                // should not happen
                getExceptionListener().exceptionThrown(e);
            }
        }
    }

    /**
     * Returns the owner of this encoder.
     * 
     * @return the owner of this encoder
     */
    public Object getOwner() {
        return owner;
    }

    private boolean isBasicType(Object value) {
        return value == null || value instanceof Boolean
                || value instanceof Byte || value instanceof Character
                || value instanceof Class<?> || value instanceof Double
                || value instanceof Float || value instanceof Integer
                || value instanceof Long || value instanceof Short
                || value instanceof String || value instanceof Proxy;
    }

    private boolean isGetArrayStat(Object target, String method, Object[] args) {
        return (BeansUtils.GET.equals(method) && args.length == 1
                && args[0] instanceof Integer && target.getClass().isArray());
    }

    private boolean isGetPropertyStat(String method, Object[] args) {
        return (method.startsWith(BeansUtils.GET) && method.length() > 3 && args.length == 0);
    }

    private boolean isSetArrayStat(Object target, String method, Object[] args) {
        return (BeansUtils.SET.equals(method) && args.length == 2
                && args[0] instanceof Integer && target.getClass().isArray());
    }

    private boolean isSetPropertyStat(String method, Object[] args) {
        return (method.startsWith(BeansUtils.SET) && method.length() > 3 && args.length == 1);
    }

    /*
     * The preprocess removes unused statements and counts references of every
     * object
     */
    private void preprocess(Object obj, Record rec) {
        if (writingObject && isBasicType(obj)) {
            return;
        }

        if (obj instanceof Class<?>) {
            return;
        }

        // count reference
        rec.refCount++;

        // do things only one time for each record
        if (rec.refCount > 1) {
            return;
        }

        // do it recursively
        if (null != rec.exp) {
            // deal with 'field' property
            Record targetRec = objRecordMap.get(rec.exp.getTarget());
            if (targetRec != null && targetRec.exp != null
                    && "getField".equals(targetRec.exp.getMethodName())) {
                objRecordMap.remove(obj);
            }

            Object args[] = rec.exp.getArguments();
            for (int i = 0; i < args.length; i++) {
                Record argRec = objRecordMap.get(args[i]);
                if (argRec != null) {
                    preprocess(args[i], argRec);
                }
            }
        }

        for (Iterator<?> iter = rec.stats.iterator(); iter.hasNext();) {
            Statement subStat = (Statement) iter.next();
            if (subStat.getClass() == Expression.class) {
                try {
                    Expression subExp = (Expression) subStat;
                    Record subRec = objRecordMap.get(subExp.getValue());
                    if (subRec == null || subRec.exp == null
                            || subRec.exp != subExp) {
                        iter.remove();
                        continue;
                    }
                    preprocess(subExp.getValue(), subRec);
                    if (subRec.stats.isEmpty()) {
                        if (isGetArrayStat(subExp.getTarget(),
                                subExp.getMethodName(), subExp.getArguments())
                                || isGetPropertyStat(subExp.getMethodName(),
                                        subExp.getArguments())) {
                            iter.remove();
                            continue;
                        }
                    }
                } catch (Exception e) {
                    getExceptionListener().exceptionThrown(e);
                    iter.remove();
                }
                continue;
            }

            Object subStatArgs[] = subStat.getArguments();
            for (int i = 0; i < subStatArgs.length; i++) {
                Record argRec = objRecordMap.get(subStatArgs[i]);
                if (argRec != null) {
                    preprocess(subStatArgs[i], argRec);
                }
            }
        }
    }

    private void recordExpression(Object value, Expression exp) {
        // record how a new object is created or obtained
        Record rec = objRecordMap.get(value);
        if (rec == null) {
            rec = new Record();
            objRecordMap.put(value, rec);
        }

        if (rec.exp == null) {
            // it is generated by its sub statements
            for (Statement statement : rec.stats) {
                if (statement.getClass() == Expression.class) {
                    flushPrePending.add(value);
                }
            }
        }

        rec.exp = exp;

        // deal with 'owner' property
        if (value == owner && owner != null) {
            needOwner = true;
        }

        // also record as a statement
        recordStatement(exp);
    }

    private void recordStatement(Statement stat) {
        if (null == stat) {
            return;
        }
        // deal with 'owner' property
        if (stat.getTarget() == owner && owner != null) {
            needOwner = true;
        }

        // record how a statement affects the target object
        Record rec = objRecordMap.get(stat.getTarget());
        if (rec == null) {
            rec = new Record();
            objRecordMap.put(stat.getTarget(), rec);
        }
        rec.stats.add(stat);
    }

    /**
     * Imperfect attempt to detect a dead loop. This works with specific
     * patterns that can be found in our AWT implementation. See HARMONY-5707
     * for details.
     * 
     * @param value
     *            the object to check dupes for
     * @return true if a dead loop detected; false otherwise FIXME
     */
    private boolean checkDeadLoop(Object value) {
        int n = 0;
        Object obj = value;

        while (obj != null) {
            Record rec = objRecordMap.get(obj);

            if (rec != null && rec.exp != null) {
                obj = rec.exp.getTarget();
            } else {
                break;
            }

            if (obj != null
                    && (obj.getClass().isAssignableFrom(value.getClass()))
                    && obj.equals(value)) {
                n++;

                if (n >= DEADLOCK_THRESHOLD) {
                    // System.out.println("Dead loop hit!");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets the owner of this encoder.
     * 
     * @param owner
     *            the owner to set
     */
    public void setOwner(Object owner) {
        this.owner = owner;
    }

    /**
     * Records the expression so that it can be written out later, then calls
     * super implementation.
     */
    @Override
    public void writeExpression(Expression oldExp) {
        if (null == oldExp) {
            throw new NullPointerException();
        }
        boolean oldWritingObject = writingObject;
        writingObject = true;
        // get expression value
        Object oldValue = expressionValue(oldExp);
        // check existence
        if (oldValue == null || get(oldValue) != null
                && (oldWritingObject || oldValue.getClass() != String.class)) {
            return;
        }

        // record how the object is obtained
        if (!isBasicType(oldValue)
                || (!oldWritingObject && oldValue.getClass() == String.class)) {
            recordExpression(oldValue, oldExp);
        }

        // try to detect if we run into a dead loop
        if (checkDeadLoop(oldValue)) {
            return;
        }
        super.writeExpression(oldExp);
        writingObject = oldWritingObject;
    }

    /**
     * Records the object so that it can be written out later, then calls super
     * implementation.
     */
    @Override
    public void writeObject(Object o) {
        synchronized (this) {
            ArrayList<Object> prePending = objPrePendingCache.get(o);
            if (prePending == null) {
                boolean oldWritingObject = writingObject;
                writingObject = true;
                try {
                    super.writeObject(o);
                } finally {
                    writingObject = oldWritingObject;
                }
            } else {
                flushPrePending.clear();
                flushPrePending.addAll(prePending);
            }

            // root object
            if (!writingObject) {
                boolean isNotCached = prePending == null;
                // is not cached, add to cache
                if (isNotCached && o != null) {
                    prePending = new ArrayList<Object>();
                    prePending.addAll(flushPrePending);
                    objPrePendingCache.put(o, prePending);
                }

                // add to pending
                flushPending.addAll(flushPrePending);
                flushPendingStat.addAll(flushPrePending);
                flushPrePending.clear();

                if (isNotCached && flushPending.contains(o)) {
                    flushPendingStat.remove(o);
                } else {
                    flushPending.add(o);
                }
                if (needOwner) {
                    this.flushPending.remove(owner);
                    this.flushPending.add(0, owner);
                }
            }
        }
    }

    /**
     * Records the statement so that it can be written out later, then calls
     * super implementation.
     */
    @Override
    public void writeStatement(Statement oldStat) {
        if (null == oldStat) {
            System.err
                    .println("java.lang.Exception: XMLEncoder: discarding statement null");
            System.err.println("Continuing...");
            return;
        }

        // record how the object is changed
        recordStatement(oldStat);
        super.writeStatement(oldStat);
    }
}