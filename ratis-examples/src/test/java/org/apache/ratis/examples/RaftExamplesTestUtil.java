/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ratis.examples;

import org.apache.ratis.MiniRaftCluster;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.MiniRaftClusterWithGRpc;
import org.apache.ratis.hadooprpc.MiniRaftClusterWithHadoopRpc;
import org.apache.ratis.netty.MiniRaftClusterWithNetty;
import org.apache.ratis.server.simulation.MiniRaftClusterWithSimulatedRpc;
import org.apache.ratis.statemachine.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RaftExamplesTestUtil {
  public static final Logger LOG = LoggerFactory.getLogger(RaftExamplesTestUtil.class);

  private static void add(
      Collection<Object[]> clusters, MiniRaftCluster.Factory factory,
      String[] ids, RaftProperties properties)
      throws IOException {
    clusters.add(new Object[]{factory.newCluster(ids, properties, true)});
  }

  public static Collection<Object[]> getMiniRaftClusters(
      RaftProperties prop, int clusterSize, Class<?>... clusterClasses)
      throws IOException {
    final List<Class<?>> classes = Arrays.asList(clusterClasses);
    final boolean isAll = classes.isEmpty(); //empty means all

    final Iterator<String[]> ids = new Iterator<String[]>() {
      private int i = 0;
      @Override
      public boolean hasNext() {
        return true;
      }
      @Override
      public String[] next() {
        return MiniRaftCluster.generateIds(clusterSize, i++*clusterSize);
      }
    };

    final List<Object[]> clusters = new ArrayList<>();

    if (isAll || classes.contains(MiniRaftClusterWithSimulatedRpc.class)) {
      add(clusters, MiniRaftClusterWithSimulatedRpc.FACTORY, ids.next(), prop);
    }
    if (isAll || classes.contains(MiniRaftClusterWithHadoopRpc.class)) {
      add(clusters, MiniRaftClusterWithHadoopRpc.FACTORY, ids.next(), prop);
    }
    if (isAll || classes.contains(MiniRaftClusterWithNetty.class)) {
      add(clusters, MiniRaftClusterWithNetty.FACTORY, ids.next(), prop);
    }
    if (isAll || classes.contains(MiniRaftClusterWithGRpc.class)) {
      add(clusters, MiniRaftClusterWithGRpc.FACTORY, ids.next(), prop);
    }
    for(int i = 0; i < clusters.size(); i++) {
      LOG.info(i + ": " + clusters.get(i)[0].getClass().getSimpleName());
    }
    LOG.info("#clusters = " + clusters.size());
    return clusters;
  }

  public static <S extends StateMachine> Collection<Object[]> getMiniRaftClusters(
      Class<S> stateMachineClass, Class<?>... clusterClasses) throws IOException {
    final RaftProperties prop = new RaftProperties();
    prop.setClass(MiniRaftCluster.STATEMACHINE_CLASS_KEY,
        stateMachineClass, StateMachine.class);
    return getMiniRaftClusters(prop, 3, clusterClasses);
  }
}
