/**
 * Copyright 2012 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.corundumstudio.socketio.namespace;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NamespacesHub {

    private final ConcurrentMap<String, Namespace> namespaces = new ConcurrentHashMap<String, Namespace>();

    public Namespace create(String name) {
        Namespace namespace = namespaces.get(name);
        if (namespace == null) {
            namespace = new Namespace(name);
            Namespace oldNamespace = namespaces.putIfAbsent(name, namespace);
            if (oldNamespace != null) {
                namespace = oldNamespace;
            }
        }
        return namespace;
    }

    public Namespace get(String name) {
        return namespaces.get(name);
    }

    public void remove(String name) {
        Namespace namespace = namespaces.remove(name);
        namespace.getBroadcastOperations().disconnect();
    }

}
