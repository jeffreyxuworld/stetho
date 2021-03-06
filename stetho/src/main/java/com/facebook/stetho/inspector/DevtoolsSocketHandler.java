/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.stetho.inspector;

import android.content.Context;
import android.net.LocalSocket;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.server.SecureSocketHandler;
import com.facebook.stetho.server.SocketLike;
import com.facebook.stetho.server.SocketLikeHandler;
import com.facebook.stetho.server.http.ExactPathMatcher;
import com.facebook.stetho.server.http.HandlerRegistry;
import com.facebook.stetho.server.http.LightHttpServer;
import com.facebook.stetho.websocket.WebSocketHandler;

import java.io.IOException;

public class DevtoolsSocketHandler implements SocketLikeHandler {
  private final Context mContext;
  private final Iterable<ChromeDevtoolsDomain> mModules;
  private final LightHttpServer mServer;

  public DevtoolsSocketHandler(Context context, Iterable<ChromeDevtoolsDomain> modules) {
    mContext = context;
    mModules = modules;
    mServer = createServer();
  }

  private LightHttpServer createServer() {
    HandlerRegistry registry = new HandlerRegistry();
    ChromeDiscoveryHandler discoveryHandler =
        new ChromeDiscoveryHandler(
            mContext,
            ChromeDevtoolsServer.PATH);
    discoveryHandler.register(registry);
    registry.register(
        new ExactPathMatcher(ChromeDevtoolsServer.PATH),
        new WebSocketHandler(new ChromeDevtoolsServer(mModules)));

    return new LightHttpServer(registry);
  }

  @Override
  public void onAccepted(SocketLike socket) throws IOException {
    mServer.serve(socket);
  }
}
