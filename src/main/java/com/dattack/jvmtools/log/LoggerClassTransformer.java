/*
 * Copyright (c) 2014, The Dattack team (http://www.dattack.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dattack.jvmtools.log;

import java.io.ByteArrayInputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.management.ManagementFactory;
import java.security.ProtectionDomain;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.dattack.jvmtools.JMXException;
import com.dattack.jvmtools.JVMToolsTransformer;

/**
 * @author cvarela
 * @since 0.1
 */
public class LoggerClassTransformer implements JVMToolsTransformer {

    private final Set<String> includes;
    private final Set<String> excludes;

    public LoggerClassTransformer(final Set<String> includes, final Set<String> excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    @Override
    public byte[] transform(final ClassLoader loader, final String fullyQualifiedClassName,
            final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
            throws IllegalClassFormatException {

        byte[] byteCode = classfileBuffer;

        String className = fullyQualifiedClassName.replace("/", ".");

        if (checkPackage(className)) {
            try {
                ClassPool classPool = ClassPool.getDefault();
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                CtMethod[] methods = ctClass.getDeclaredMethods();
                for (CtMethod method : methods) {
                    System.out.println("Method longName: " + method.getLongName());
                    // method.insertBefore("com.dattack.jvmtools.log.LogHelper.log();");
                    method.insertBefore("com.dattack.jvmtools.log.LogHelper.log(\"" + method.getLongName() + "\");");
                }
                byteCode = ctClass.toBytecode();
                ctClass.detach();
            } catch (Throwable ex) {
                // ignore
            }
        }
        return byteCode;
    }

    private boolean checkPackage(final String className) {

        if ((includes != null)) {
            if (contains(includes, className)) {
                return !contains(excludes, className);
            }
        }

        return false;
    }

    private boolean contains(final Set<String> set, final String className) {
        if (set != null) {
            for (String item : set) {
                if (className.startsWith(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void registerMBean() throws JMXException {

        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("com.dattack.jvmtools:type=LogTransformer");
            LoggerController mbean = new LoggerController();
            mbs.registerMBean(mbean, name);
        } catch (final Exception e) {
            throw new JMXException(e);
        }
    }
}
