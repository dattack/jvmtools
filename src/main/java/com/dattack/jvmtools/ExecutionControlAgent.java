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
package com.dattack.jvmtools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.dattack.jvmtools.log.LoggerClassTransformer;

/**
 * @author cvarela
 * @since 0.1
 */
public final class ExecutionControlAgent {

    public static void premain(final String agentArguments, final Instrumentation instrumentation)
            throws FileNotFoundException, IOException {

        System.out.println("Arguments: " + agentArguments);
        Set<String> includes = null;
        Set<String> excludes = null;
        if (agentArguments != null) {
            Properties properties = new Properties();
            properties.load(new FileInputStream(agentArguments));
            includes = getSet(properties.getProperty("includes"));
            excludes = getSet(properties.getProperty("excludes"));
        }

        // adds the transformer to log the trace execution
        addTransformer(instrumentation, new LoggerClassTransformer(includes, excludes));
    }

    private static void addTransformer(final Instrumentation instrumentation, final JVMToolsTransformer transformer) {

        instrumentation.addTransformer(transformer);

        try {
            transformer.registerMBean();
        } catch (JMXException e) {
            e.printStackTrace();
        }
    }

    private static Set<String> getSet(final String txt) {

        Set<String> set = null;
        if (txt != null) {
            String[] args = txt.split(",");
            if (args != null) {
                set = new HashSet<String>(args.length);
                for (String str : args) {
                    set.add(str.trim());
                }
            }
        }
        return set;
    }
}
