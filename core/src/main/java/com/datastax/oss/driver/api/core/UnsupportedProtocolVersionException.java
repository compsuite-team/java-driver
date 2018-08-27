/*
 * Copyright DataStax, Inc.
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
package com.datastax.oss.driver.api.core;

import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.driver.shaded.guava.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * Indicates that we've attempted to connect to a Cassandra node with a protocol version that it
 * cannot handle (e.g., connecting to a C* 2.1 node with protocol version 4).
 *
 * <p>The only time when this is returned directly to the client (wrapped in a {@link
 * AllNodesFailedException}) is at initialization. If it happens later when the driver is already
 * connected, it is just logged an the corresponding node is forced down.
 */
public class UnsupportedProtocolVersionException extends DriverException {
  private static final long serialVersionUID = 0;

  private final SocketAddress address;
  private final List<ProtocolVersion> attemptedVersions;

  @NonNull
  public static UnsupportedProtocolVersionException forSingleAttempt(
      @NonNull SocketAddress address, @NonNull ProtocolVersion attemptedVersion) {
    String message =
        String.format("[%s] Host does not support protocol version %s", address, attemptedVersion);
    return new UnsupportedProtocolVersionException(
        address, message, Collections.singletonList(attemptedVersion), null);
  }

  @NonNull
  public static UnsupportedProtocolVersionException forNegotiation(
      @NonNull SocketAddress address, @NonNull List<ProtocolVersion> attemptedVersions) {
    String message =
        String.format(
            "[%s] Protocol negotiation failed: could not find a common version (attempted: %s). "
                + "Note that the driver does not support Cassandra 2.0 or lower.",
            address, attemptedVersions);
    return new UnsupportedProtocolVersionException(
        address, message, ImmutableList.copyOf(attemptedVersions), null);
  }

  public UnsupportedProtocolVersionException(
      @Nullable SocketAddress address, // technically nullable, but should never be in real life
      @NonNull String message,
      @NonNull List<ProtocolVersion> attemptedVersions) {
    this(address, message, attemptedVersions, null);
  }

  private UnsupportedProtocolVersionException(
      SocketAddress address,
      String message,
      List<ProtocolVersion> attemptedVersions,
      ExecutionInfo executionInfo) {
    super(message, executionInfo, null, true);
    this.address = address;
    this.attemptedVersions = attemptedVersions;
  }

  /** The address of the node that threw the error. */
  @Nullable
  public SocketAddress getAddress() {
    return address;
  }

  /** The versions that were attempted. */
  @NonNull
  public List<ProtocolVersion> getAttemptedVersions() {
    return attemptedVersions;
  }

  @NonNull
  @Override
  public DriverException copy() {
    return new UnsupportedProtocolVersionException(
        address, getMessage(), attemptedVersions, getExecutionInfo());
  }
}