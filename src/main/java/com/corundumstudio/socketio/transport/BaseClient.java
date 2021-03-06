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
package com.corundumstudio.socketio.transport;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.corundumstudio.socketio.AckManager;
import com.corundumstudio.socketio.Disconnectable;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.namespace.Namespace;
import com.corundumstudio.socketio.parser.Packet;
import com.corundumstudio.socketio.parser.PacketType;

public abstract class BaseClient {

    private final ConcurrentMap<Namespace, SocketIOClient> namespaceClients = new ConcurrentHashMap<Namespace, SocketIOClient>();

    private final Disconnectable disconnectable;
    private final AckManager ackManager;
    private final UUID sessionId;
    protected Channel channel;

    public BaseClient(UUID sessionId, AckManager ackManager, Disconnectable disconnectable) {
        this.sessionId = sessionId;
        this.ackManager = ackManager;
        this.disconnectable = disconnectable;
    }

    public abstract ChannelFuture send(Packet packet);

    public void removeClient(SocketIOClient client) {
        namespaceClients.remove((Namespace)client.getNamespace());
        if (namespaceClients.isEmpty()) {
            disconnectable.onDisconnect(this);
        }
    }

    public SocketIOClient getClient(Namespace namespace) {
        SocketIOClient client = namespaceClients.get(namespace);
        if (client == null) {
            client = new NamespaceClient(this, namespace);
            SocketIOClient oldClient = namespaceClients.putIfAbsent(namespace, client);
            if (oldClient != null) {
                client = oldClient;
            }
        }
        return client;
    }

    public Collection<SocketIOClient> getAllClients() {
        return namespaceClients.values();
    }

    public AckManager getAckManager() {
        return ackManager;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public SocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
    }

    public void disconnect() {
        ChannelFuture future = send(new Packet(PacketType.DISCONNECT));
        future.addListener(ChannelFutureListener.CLOSE);

        disconnectable.onDisconnect(this);
    }

}
